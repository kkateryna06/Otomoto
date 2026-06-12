package com.example.carmarketanalyzer;

import com.example.carmarketanalyzer.data.Car;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for reading and manually managing stored car listings.
 */
@RestController
@RequestMapping("/api/cars")
public class CarController {

    private final CarRepository carRepository;

    public CarController(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    /**
     * Returns all stored listings.
     */
    @GetMapping
    public List<Car> getCars() {
        return carRepository.findAll();
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
