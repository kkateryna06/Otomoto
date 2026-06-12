package com.example.carmarketanalyzer.data;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
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
    private Integer year;
    private Integer mileage;
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
    @Column(name = "posted_at")
    private Instant postedAt;
    @Column(name = "is_actual", nullable = false)
    private boolean actual = true;
    @Column(name = "last_seen_at")
    private Instant lastSeenAt;
    @Column(name = "last_checked_at")
    private Instant lastCheckedAt;
    @Column(name = "disappeared_at")
    private Instant disappearedAt;
    @Column(name = "unavailable_checks_count", nullable = false)
    private int unavailableChecksCount = 0;
}
