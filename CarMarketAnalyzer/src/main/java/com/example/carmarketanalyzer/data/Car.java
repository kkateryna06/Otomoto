package com.example.carmarketanalyzer.data;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashMap;

@Data
@Entity
@Table(name = "cars_info")
public class Car {
    @Id
    @GeneratedValue
    @Column(name = "car_id")
    private Long id;
    private String brand;
    private String model;
    private String version;
    private String generation;
    private int year;
    private int mileage;
    @Column(name = "fuel_type")
    private String fuelType;
    private Integer engineCapacity;
    private Integer enginePower;
    @JdbcTypeCode(SqlTypes.JSON)
    private HashMap<String, Integer> priceHistory;
    private String bodyType;
    private String gearbox;
    private String transmission;
    private Double urbanConsumption;
    private Double extraUrbanConsumption;
    private String color;
    private Integer doorCount;
    private Integer seats;
    private String sellerType;
    @Column(columnDefinition = "TEXT")
    private String url;
    @JdbcTypeCode(SqlTypes.JSON)
    private HashMap<String, Integer> location;
    @Column(columnDefinition = "TEXT")
    private String photoPath;
    @Column(columnDefinition = "TEXT")
    private String htmlPath;
    @Column(columnDefinition = "TEXT")
    private String description;
}
