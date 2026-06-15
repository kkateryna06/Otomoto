package com.example.carmarketanalyzer;

import com.example.carmarketanalyzer.data.Car;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * REST controller for reading and manually managing stored car listings.
 */
@RestController
@RequestMapping("/api/cars")
@Slf4j
public class CarController {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id",
            "brand",
            "model",
            "year",
            "mileage",
            "currentPrice",
            "engineCapacity",
            "enginePower",
            "postedAt",
            "lastSeenAt",
            "lastCheckedAt"
    );

    private final CarRepository carRepository;

    public CarController(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    @GetMapping("/brands")
    public List<String> getBrands() {
        return carRepository.findDistinctBrands();
    }

    @GetMapping("/models")
    public List<String> getModels(@RequestParam String brand) {
        if (!hasText(brand)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "brand is required");
        }

        return carRepository.findDistinctModelsByBrand(brand.trim());
    }

    @GetMapping("/fuel-types")
    public List<String> getFuelTypes() {
        return carRepository.findDistinctFuelTypes();
    }

    @GetMapping("/body-types")
    public List<String> getBodyTypes() {
        return carRepository.findDistinctBodyTypes();
    }

    @GetMapping("/gearboxes")
    public List<String> getGearboxes() {
        return carRepository.findDistinctGearboxes();
    }

    @GetMapping("/transmissions")
    public List<String> getTransmissions() {
        return carRepository.findDistinctTransmissions();
    }

    /**
     * Returns all stored listings or a paged response when page/size are provided.
     */
    @GetMapping
    public Object getCars(
            HttpServletRequest request,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) List<String> brand,
            @RequestParam(required = false) List<String> model,
            @RequestParam(required = false) List<String> fuelType,
            @RequestParam(required = false) List<String> bodyType,
            @RequestParam(required = false) List<String> gearbox,
            @RequestParam(required = false) List<String> transmission,
            @RequestParam(required = false) List<String> sellerType,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer minYear,
            @RequestParam(required = false) Integer maxYear,
            @RequestParam(required = false) Integer minMileage,
            @RequestParam(required = false) Integer maxMileage,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) Integer minEngineCapacity,
            @RequestParam(required = false) Integer maxEngineCapacity,
            @RequestParam(required = false) Integer minEnginePower,
            @RequestParam(required = false) Integer maxEnginePower,
            @RequestParam(required = false) Boolean actual,
            @RequestParam(required = false) Instant postedFrom,
            @RequestParam(required = false) Instant postedTo,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        log.info("Cars request URL: {}", requestUrl(request));

        boolean hasFilters = hasValues(brand)
                || hasValues(model)
                || hasValues(fuelType)
                || hasValues(bodyType)
                || hasValues(gearbox)
                || hasValues(transmission)
                || hasValues(sellerType)
                || hasText(q)
                || minYear != null
                || maxYear != null
                || minMileage != null
                || maxMileage != null
                || minPrice != null
                || maxPrice != null
                || minEngineCapacity != null
                || maxEngineCapacity != null
                || minEnginePower != null
                || maxEnginePower != null
                || actual != null
                || postedFrom != null
                || postedTo != null;

        if (page == null && size == null && !hasFilters) {
            return carRepository.findAll();
        }

        Pageable pageable = buildPageRequest(page, size, sortBy, sortDirection);
        Specification<Car> specification = buildSpecification(
                brand,
                model,
                fuelType,
                bodyType,
                gearbox,
                transmission,
                sellerType,
                q,
                minYear,
                maxYear,
                minMileage,
                maxMileage,
                minPrice,
                maxPrice,
                minEngineCapacity,
                maxEngineCapacity,
                minEnginePower,
                maxEnginePower,
                actual,
                postedFrom,
                postedTo
        );

        Page<Car> cars = carRepository.findAll(specification, pageable);
        return cars;
    }

    private Pageable buildPageRequest(Integer page, Integer size, String sortBy, String sortDirection) {
        int pageNumber = page == null ? 0 : page;
        int pageSize = size == null ? DEFAULT_PAGE_SIZE : size;

        if (pageNumber < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page must be greater than or equal to 0");
        }
        if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "size must be between 1 and " + MAX_PAGE_SIZE);
        }
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unsupported sortBy field: " + sortBy);
        }

        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(sortDirection);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sortDirection must be asc or desc", e);
        }

        return PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortBy));
    }

    private Specification<Car> buildSpecification(
            List<String> brand,
            List<String> model,
            List<String> fuelType,
            List<String> bodyType,
            List<String> gearbox,
            List<String> transmission,
            List<String> sellerType,
            String q,
            Integer minYear,
            Integer maxYear,
            Integer minMileage,
            Integer maxMileage,
            Integer minPrice,
            Integer maxPrice,
            Integer minEngineCapacity,
            Integer maxEngineCapacity,
            Integer minEnginePower,
            Integer maxEnginePower,
            Boolean actual,
            Instant postedFrom,
            Instant postedTo
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            addTextInPredicate(predicates, criteriaBuilder, root.get("brand"), brand);
            addTextInPredicate(predicates, criteriaBuilder, root.get("model"), model);
            addTextInPredicate(predicates, criteriaBuilder, root.get("fuelType"), fuelType);
            addTextInPredicate(predicates, criteriaBuilder, root.get("bodyType"), bodyType);
            addTextInPredicate(predicates, criteriaBuilder, root.get("gearbox"), gearbox);
            addTextInPredicate(predicates, criteriaBuilder, root.get("transmission"), transmission);
            addTextInPredicate(predicates, criteriaBuilder, root.get("sellerType"), sellerType);

            addIntegerRange(predicates, criteriaBuilder, root.get("year"), minYear, maxYear);
            addIntegerRange(predicates, criteriaBuilder, root.get("mileage"), minMileage, maxMileage);
            addIntegerRange(predicates, criteriaBuilder, root.get("currentPrice"), minPrice, maxPrice);
            addIntegerRange(predicates, criteriaBuilder, root.get("engineCapacity"), minEngineCapacity, maxEngineCapacity);
            addIntegerRange(predicates, criteriaBuilder, root.get("enginePower"), minEnginePower, maxEnginePower);

            if (actual != null) {
                predicates.add(criteriaBuilder.equal(root.get("actual"), actual));
            }
            if (postedFrom != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("postedAt"), postedFrom));
            }
            if (postedTo != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("postedAt"), postedTo));
            }
            if (hasText(q)) {
                String pattern = likePattern(q);
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("brand")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("model")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("version")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("generation")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern)
                ));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private void addTextInPredicate(
            List<Predicate> predicates,
            jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder,
            jakarta.persistence.criteria.Path<String> path,
            List<String> values
    ) {
        List<String> normalizedValues = normalizeValues(values);
        if (normalizedValues.isEmpty()) {
            return;
        }

        predicates.add(criteriaBuilder.lower(path).in(normalizedValues));
    }

    private void addIntegerRange(
            List<Predicate> predicates,
            jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder,
            jakarta.persistence.criteria.Path<Integer> path,
            Integer min,
            Integer max
    ) {
        if (min != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(path, min));
        }
        if (max != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(path, max));
        }
    }

    private List<String> normalizeValues(List<String> values) {
        if (values == null) {
            return List.of();
        }

        return values.stream()
                .filter(this::hasText)
                .map(value -> value.trim().toLowerCase(Locale.ROOT))
                .toList();
    }

    private String likePattern(String value) {
        return "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
    }

    private boolean hasValues(List<String> values) {
        return !normalizeValues(values).isEmpty();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String requestUrl(HttpServletRequest request) {
        String queryString = request.getQueryString();
        if (queryString == null || queryString.isBlank()) {
            return request.getRequestURL().toString();
        }

        return request.getRequestURL() + "?" + queryString;
    }

    /**
     * Returns a single listing by database ID.
     */
    @GetMapping("/{id}")
    public Car getCarById(@PathVariable Long id) {
        return carRepository.findById(id).orElse(null);
    }

    /**
     * Returns the total number of stored listings.
     */
    @GetMapping("/count")
    public long getCarCount() {
        return carRepository.count();
    }

    /**
     * Saves a listing manually. Scraper-managed fields can still be updated later by URL rechecks.
     */
    @PostMapping
    public Car saveCar(@RequestBody Car car) {
        return carRepository.save(car);
    }

    /**
     * Deletes a listing by database ID.
     */
    @DeleteMapping("/{id}")
    public void deleteCar(@PathVariable Long id) {
        carRepository.deleteById(id);
    }
}
