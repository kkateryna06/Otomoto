package com.example.carmarketanalyzer;

import com.example.carmarketanalyzer.data.Car;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.data.domain.PageRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtoMotoScraper {

    private final CarRepository carRepository;
    @Value("${app.photo-storage-dir:data/photos}")
    private String photoStorageDir;
    @Value("${app.max-photos-per-listing:3}")
    private int maxPhotosPerListing;

    private static final String BASE_URL = "https://www.otomoto.pl";
    private static final long DELAY_MS = 1000;
    private static final int INACTIVE_CONFIRMATION_THRESHOLD = 2;
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    private static final Duration FRESH_LISTING_AGE = Duration.ofDays(3);
    private static final Duration MIDDLE_LISTING_AGE = Duration.ofDays(14);
    private static final Duration FRESH_RECHECK_INTERVAL = Duration.ofHours(12);
    private static final Duration MIDDLE_RECHECK_INTERVAL = Duration.ofHours(24);
    private static final Duration OLD_RECHECK_INTERVAL = Duration.ofHours(72);

    public void scrapeAndSave(String searchUrl, int maxRecheckListingsPerRun) {
        try {
            Set<String> scrapedUrls = scrapeListings(searchUrl);
            recheckKnownListings(scrapedUrls, maxRecheckListingsPerRun);
            log.info("Scraping completed successfully");
        } catch (IOException e) {
            log.error("Error during scraping", e);
        }
    }

    public void recheckExistingListings(int maxRecheckListingsPerRun) {
        recheckKnownListings(Set.of(), maxRecheckListingsPerRun);
    }

    private Set<String> scrapeListings(String url) throws IOException {
        Document doc = fetchDocument(url);

        Elements links = doc.select("a[href*=/oferta/]");

        Set<String> listingLinks = links.stream()
                .map(link -> link.attr("abs:href"))
                .collect(Collectors.toSet());

        log.info("Found {} listings", listingLinks.size());

        int count = 0;

        for (String listingUrl : listingLinks) {
            try {
                Car car = parseCarFromListing(listingUrl);

                if (car != null && isValidCar(car)) {
                    saveOrUpdateCar(car);
                    count++;
                    log.info("Saved or updated car: {} {} from {}", car.getBrand(), car.getModel(), car.getUrl());
                }

                Thread.sleep(DELAY_MS);

            } catch (Exception e) {
                log.warn("Error parsing listing: {}", listingUrl, e);
            }
        }

        log.info("Successfully saved or updated {} cars in database", count);
        return listingLinks;
    }

    private Car parseCarFromListing(String url) {
        try {
            ListingFetchResult fetchResult = fetchListingDocument(url);
            if (fetchResult.status() != ListingStatus.ACTIVE || fetchResult.document() == null) {
                log.warn("Listing is not available during parsing: {} ({})", url, fetchResult.status());
                return null;
            }

            Car car = new Car();
            car.setUrl(url);

            parseCarDetails(car, fetchResult.document(), url);
            markListingSeen(car, Instant.now());

            return car;

        } catch (Exception e) {
            log.warn("Error parsing car from listing: {}", url, e);
            return null;
        }
    }

    private void parseCarDetails(Car car, Document detailDoc, String url) throws IOException {
        Element script = detailDoc.selectFirst("script#__NEXT_DATA__");

        if (script == null) {
            log.warn("No __NEXT_DATA__ found: {}", url);
            return;
        }

        String json = script.html();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);

        JsonNode advert = root.path("props")
                .path("pageProps")
                .path("advert");

        // Otomoto keeps the full listing payload in __NEXT_DATA__; parametersDict is the stable source for car specs.
        car.setBrand(getParam(advert, "make", false));
        car.setModel(getParam(advert, "model", false));
        car.setVersion(getParam(advert, "version", false));
        car.setGeneration(getParam(advert, "generation", false));
        car.setGearbox(getParam(advert, "gearbox", true));
        car.setTransmission(getParam(advert, "transmission", true));
        car.setFuelType(getParam(advert, "fuel_type", true));
        car.setEnginePower(parseInteger(getParam(advert, "engine_power", true)));
        car.setEngineCapacity(parseInteger(getParam(advert, "engine_capacity", true)));
        car.setMileage(parseInteger(getParam(advert, "mileage", true)));
        car.setYear(parseInteger(getParam(advert, "year", true)));
        car.setColor(getParam(advert, "color", true));
        car.setBodyType(getParam(advert, "body_type", true));
        car.setDoorCount(parseInteger(getParam(advert, "door_count", true)));
        car.setSeats(parseInteger(getParam(advert, "nr_seats", true)));
        car.setUrbanConsumption(parseDouble(getParam(advert, "urban_consumption", true)));
        car.setExtraUrbanConsumption(parseDouble(getParam(advert, "extra_urban_consumption", true)));
        car.setSellerType(advert.path("seller").path("type").toString().replace("\"", ""));
        car.setDescription(advert.path("description").toString());
        car.setPostedAt(Instant.parse(advert.path("createdAt").asString()));
        storeListingPhotos(car, advert);

        int price = Integer.parseInt(advert.path("price")
                .path("value").asString());

        HashMap<String, Integer> priceHistory = new HashMap<>();

        priceHistory.put(priceHistoryKey(), price);

        car.setCurrentPrice(price);
        car.setPriceHistory(priceHistory);
    }

    private void saveOrUpdateCar(Car parsedCar) {
        Optional<Car> existingCar = carRepository.findByUrl(parsedCar.getUrl());

        if (existingCar.isEmpty()) {
            carRepository.save(parsedCar);
            return;
        }

        Car car = existingCar.get();
        Integer currentPrice = latestPrice(parsedCar.getPriceHistory());

        copyParsedFields(parsedCar, car);
        appendPriceIfChanged(car, currentPrice);
        markListingSeen(car, Instant.now());

        carRepository.save(car);
    }

    private void recheckKnownListings(Set<String> alreadyScrapedUrls, int maxRecheckListingsPerRun) {
        int queryLimit = Math.max(maxRecheckListingsPerRun + alreadyScrapedUrls.size(), maxRecheckListingsPerRun);
        Instant now = Instant.now();
        List<Car> activeCars = carRepository.findNextActualListingsForRecheck(
                now.minus(FRESH_LISTING_AGE),
                now.minus(MIDDLE_LISTING_AGE),
                now.minus(FRESH_RECHECK_INTERVAL),
                now.minus(MIDDLE_RECHECK_INTERVAL),
                now.minus(OLD_RECHECK_INTERVAL),
                PageRequest.of(0, queryLimit)
        );
        log.info("Rechecking up to {} known active listings from {} candidates", maxRecheckListingsPerRun, activeCars.size());

        int checkedCount = 0;
        for (Car car : activeCars) {
            if (alreadyScrapedUrls.contains(car.getUrl())) {
                continue;
            }

            if (checkedCount >= maxRecheckListingsPerRun) {
                break;
            }

            try {
                ListingFetchResult fetchResult = fetchListingDocument(car.getUrl());
                Instant checkedAt = Instant.now();

                if (fetchResult.status() == ListingStatus.ACTIVE && fetchResult.document() != null) {
                    Car parsedCar = new Car();
                    parsedCar.setUrl(car.getUrl());
                    parseCarDetails(parsedCar, fetchResult.document(), car.getUrl());

                    copyParsedFields(parsedCar, car);
                    appendPriceIfChanged(car, latestPrice(parsedCar.getPriceHistory()));
                    markListingSeen(car, checkedAt);
                    carRepository.save(car);
                } else if (fetchResult.status() == ListingStatus.GONE) {
                    markListingUnavailable(car, checkedAt);
                    carRepository.save(car);
                } else {
                    car.setLastCheckedAt(checkedAt);
                    carRepository.save(car);
                    log.warn("Listing check was inconclusive: {}", car.getUrl());
                }

                Thread.sleep(DELAY_MS);
                checkedCount++;
            } catch (Exception e) {
                car.setLastCheckedAt(Instant.now());
                carRepository.save(car);
                log.warn("Error rechecking listing: {}", car.getUrl(), e);
                checkedCount++;
            }
        }

        log.info("Finished rechecking {} known active listings", checkedCount);
    }

    private void copyParsedFields(Car source, Car target) {
        target.setBrand(source.getBrand());
        target.setModel(source.getModel());
        target.setVersion(source.getVersion());
        target.setGeneration(source.getGeneration());
        target.setYear(source.getYear());
        target.setMileage(source.getMileage());
        target.setFuelType(source.getFuelType());
        target.setEngineCapacity(source.getEngineCapacity());
        target.setEnginePower(source.getEnginePower());
        target.setCurrentPrice(source.getCurrentPrice());
        target.setBodyType(source.getBodyType());
        target.setGearbox(source.getGearbox());
        target.setTransmission(source.getTransmission());
        target.setUrbanConsumption(source.getUrbanConsumption());
        target.setExtraUrbanConsumption(source.getExtraUrbanConsumption());
        target.setColor(source.getColor());
        target.setDoorCount(source.getDoorCount());
        target.setSeats(source.getSeats());
        target.setSellerType(source.getSellerType());
        target.setLocation(source.getLocation());
        target.setPhotoPath(source.getPhotoPath());
        target.setPhotoUrls(source.getPhotoUrls());
        target.setLocalPhotoPaths(source.getLocalPhotoPaths());
        target.setHtmlPath(source.getHtmlPath());
        target.setDescription(source.getDescription());
        target.setPostedAt(source.getPostedAt());
    }

    private void storeListingPhotos(Car car, JsonNode advert) {
        List<String> photoUrls = extractPhotoUrls(advert);
        car.setPhotoUrls(photoUrls);

        if (photoUrls.isEmpty()) {
            car.setLocalPhotoPaths(List.of());
            car.setPhotoPath(null);
            return;
        }

        List<String> localPhotoPaths = downloadPhotos(car, photoUrls);
        car.setLocalPhotoPaths(localPhotoPaths);
        car.setPhotoPath(localPhotoPaths.isEmpty() ? null : localPhotoPaths.get(0));
    }

    private List<String> extractPhotoUrls(JsonNode advert) {
        JsonNode photos = advert.path("images").path("photos");
        if (photos.isMissingNode() || photos.isNull()) {
            return List.of();
        }

        List<String> photoUrls = new ArrayList<>();
        for (JsonNode photo : photos) {
            String photoUrl = photo.path("url").asString();
            if (photoUrl == null || photoUrl.isBlank()) {
                photoUrl = photo.path("id").asString();
            }
            if (photoUrl != null && !photoUrl.isBlank() && photoUrls.size() < maxPhotosPerListing) {
                photoUrls.add(photoUrl);
            }
        }

        return photoUrls;
    }

    private List<String> downloadPhotos(Car car, List<String> photoUrls) {
        List<String> localPhotoPaths = new ArrayList<>();
        String listingFolder = listingPhotoFolder(car);
        Path listingDirectory = Path.of(photoStorageDir).toAbsolutePath().normalize().resolve(listingFolder);

        try {
            Files.createDirectories(listingDirectory);
        } catch (IOException e) {
            log.warn("Could not create photo directory for listing: {}", car.getUrl(), e);
            return localPhotoPaths;
        }

        for (int index = 0; index < photoUrls.size(); index++) {
            String photoUrl = photoUrls.get(index);
            String fileName = photoFileName(index);
            Path targetPath = listingDirectory.resolve(fileName);
            String publicPath = "/photos/" + listingFolder + "/" + fileName;

            try {
                if (!Files.exists(targetPath)) {
                    byte[] imageBytes = Jsoup.connect(photoUrl)
                            .userAgent(USER_AGENT)
                            .timeout(15000)
                            .ignoreContentType(true)
                            .maxBodySize(0)
                            .execute()
                            .bodyAsBytes();
                    Files.write(targetPath, imageBytes);
                }
                localPhotoPaths.add(publicPath);
            } catch (Exception e) {
                log.warn("Could not download listing photo: {}", photoUrl, e);
            }
        }

        return localPhotoPaths;
    }

    private String listingPhotoFolder(Car car) {
        List<String> parts = new ArrayList<>();
        parts.add(car.getBrand());
        parts.add(car.getModel());
        if (car.getYear() != null) {
            parts.add(car.getYear().toString());
        }

        String label = slugify(String.join("-", parts));
        if (label.isBlank()) {
            label = "listing";
        }

        return label + "-" + shortHash(car.getUrl());
    }

    private String photoFileName(int index) {
        if (index == 0) {
            return "cover.jpg";
        }

        return "photo-%02d.jpg".formatted(index + 1);
    }

    private String slugify(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");

        if (normalized.length() <= 80) {
            return normalized;
        }

        return normalized.substring(0, 80).replaceAll("-+$", "");
    }

    private String shortHash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash).substring(0, 16);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private void appendPriceIfChanged(Car car, Integer currentPrice) {
        if (currentPrice == null) {
            return;
        }

        car.setCurrentPrice(currentPrice);

        HashMap<String, Integer> priceHistory = car.getPriceHistory();
        if (priceHistory == null) {
            priceHistory = new HashMap<>();
            car.setPriceHistory(priceHistory);
        }

        Integer latestPrice = latestPrice(priceHistory);
        if (!currentPrice.equals(latestPrice)) {
            priceHistory.put(priceHistoryKey(), currentPrice);
        }
    }

    private Integer latestPrice(Map<String, Integer> priceHistory) {
        if (priceHistory == null || priceHistory.isEmpty()) {
            return null;
        }

        return priceHistory.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .max(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .orElse(null);
    }

    private void markListingSeen(Car car, Instant checkedAt) {
        car.setActual(true);
        car.setLastSeenAt(checkedAt);
        car.setLastCheckedAt(checkedAt);
        car.setDisappearedAt(null);
        car.setUnavailableChecksCount(0);
    }

    private void markListingUnavailable(Car car, Instant checkedAt) {
        car.setLastCheckedAt(checkedAt);
        car.setUnavailableChecksCount(car.getUnavailableChecksCount() + 1);

        // Require repeated 404/410 responses to avoid marking active listings as gone after transient site errors.
        if (car.getUnavailableChecksCount() >= INACTIVE_CONFIRMATION_THRESHOLD) {
            car.setActual(false);
            if (car.getDisappearedAt() == null) {
                car.setDisappearedAt(checkedAt);
            }
        }
    }

    private String priceHistoryKey() {
        return Instant.now().truncatedTo(ChronoUnit.SECONDS).toString();
    }

    private String getParam(JsonNode advert, String key, boolean isValue) {
        JsonNode param = advert
                .path("parametersDict")
                .path(key)
                .path("values")
                .get(0);

        if (param == null || param.isMissingNode() || param.isNull()) {
            return null;
        }

        if (isValue) {
            if (!param.isMissingNode()) {
                param = param.path("value");
                return param.asString().substring(0, 1).toUpperCase() + param.asString().substring(1);
            }
        } else {
            if (!param.isMissingNode()) {
                param = param.path("label");
                return param.asString();
            }
        }

        return null;
    }

    private Document fetchDocument(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(10000)
                .get();
    }

    private ListingFetchResult fetchListingDocument(String url) throws IOException {
        Connection.Response response = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(10000)
                .ignoreHttpErrors(true)
                .execute();

        int statusCode = response.statusCode();
        if (statusCode == 404 || statusCode == 410) {
            return new ListingFetchResult(ListingStatus.GONE, null);
        }

        if (statusCode >= 400) {
            return new ListingFetchResult(ListingStatus.UNKNOWN, null);
        }

        Document document = response.parse();
        if (document.selectFirst("script#__NEXT_DATA__") == null) {
            return new ListingFetchResult(ListingStatus.UNKNOWN, document);
        }

        return new ListingFetchResult(ListingStatus.ACTIVE, document);
    }

    private boolean isValidCar(Car car) {
        return car.getBrand() != null && !car.getBrand().isEmpty() &&
               car.getUrl() != null && !car.getUrl().isEmpty();
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double parseDouble(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private enum ListingStatus {
        ACTIVE,
        GONE,
        UNKNOWN
    }

    private record ListingFetchResult(ListingStatus status, Document document) {
    }
}
