package com.example.carmarketanalyzer;

import com.example.carmarketanalyzer.data.Car;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST контроллер для работы с машинами из БД
 * Позволяет получать сохраненные данные о машинах
 */
@RestController
@RequestMapping("/api/cars")
public class CarController {

    private final CarRepository carRepository;

    public CarController(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    /**
     * Получить все машины
     */
    @GetMapping
    public List<Car> getCars() {
        return carRepository.findAll();
    }

    /**
     * Получить машину по ID
     */
    @GetMapping("/{id}")
    public Car getCarById(@PathVariable Long id) {
        return carRepository.findById(id).orElse(null);
    }

    /**
     * Получить количество машин в БД
     */
    @GetMapping("/count")
    public long getCarCount() {
        return carRepository.count();
    }

    /**
     * Сохранить машину вручную
     */
    @PostMapping
    public Car saveCar(@RequestBody Car car) {
        return carRepository.save(car);
    }

    /**
     * Удалить машину
     */
    @DeleteMapping("/{id}")
    public void deleteCar(@PathVariable Long id) {
        carRepository.deleteById(id);
    }
}
