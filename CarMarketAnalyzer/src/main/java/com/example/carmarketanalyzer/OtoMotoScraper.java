package com.example.carmarketanalyzer;

import com.example.carmarketanalyzer.data.Car;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtoMotoScraper {

    private final CarRepository carRepository;
    private static final String BASE_URL = "https://www.otomoto.pl";
    private static final String SEARCH_URL = "https://www.otomoto.pl/osobowe";
    private static final long DELAY_MS = 1000;

    public void scrapeAndSave() {
        try {
            scrapeListings(SEARCH_URL);
            log.info("Scraping completed successfully");
        } catch (IOException e) {
            log.error("Error during scraping", e);
        }
    }

    private void scrapeListings(String url) throws IOException {
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
                    carRepository.save(car);
                    count++;
                    log.info("Saved car: {} {} from {}", car.getBrand(), car.getModel(), car.getUrl());
                }

                Thread.sleep(DELAY_MS);

            } catch (Exception e) {
                log.warn("Error parsing listing: {}", listingUrl, e);
            }
        }

        log.info("Successfully saved {} cars to database", count);
    }

    private Car parseCarFromListing(String url) {
        try {
            Car car = new Car();
            car.setUrl(url);

            parseCarDetails(car, url);

            return car;

        } catch (Exception e) {
            log.warn("Error parsing car from listing: {}", url, e);
            return null;
        }
    }

    private void parseCarDetails(Car car, String url) throws IOException {
        Document detailDoc = fetchDocument(url);

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


        // brand
        car.setBrand(getParam(advert, "make", false));

        // model
        car.setModel(getParam(advert, "model", false));

        // version
        car.setVersion(getParam(advert, "version", false));

        // generation
        car.setGeneration(getParam(advert, "generation", false));

        // price
        int price = Integer.parseInt(advert.path("price")
                .path("value").asString());

        HashMap<String, Integer> priceHistory = new HashMap<>();

        priceHistory.put(
                LocalDate.now().toString(),
                price
        );

        car.setPriceHistory(priceHistory);

        // gearbox
        car.setGearbox(getParam(advert, "gearbox", true));

        // transmission
        car.setTransmission(getParam(advert, "transmission", true));

        // fuel type
        car.setFuelType(getParam(advert, "fuel_type", true));

        // engine power
        car.setEnginePower(parseInteger(getParam(advert, "engine_power", true)));

        // engine capacity
        car.setEngineCapacity(parseInteger(getParam(advert, "engine_capacity", true)));

        // mileage
        car.setMileage(parseInteger(getParam(advert, "mileage", true)));

        // year
        car.setYear(parseInteger(getParam(advert, "year", true)));

        // color
        car.setColor(getParam(advert, "color", true));

        // body type
        car.setBodyType(getParam(advert, "body_type", true));

        // door count
        car.setDoorCount(parseInteger(getParam(advert, "door_count", true)));

        // seats
        car.setSeats(parseInteger(getParam(advert, "nr_seats", true)));

        // urban consumption
        car.setUrbanConsumption(parseDouble(getParam(advert, "urban_consumption", true)));

        // extra urban consumption
        car.setExtraUrbanConsumption(parseDouble(getParam(advert, "extra_urban_consumption", true)));

        // seller type
        car.setSellerType(advert.path("seller").path("type").toString().replace("\"", ""));

        // description
        car.setDescription(advert.path("description").toString());
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
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(10000)
                .get();
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
}
