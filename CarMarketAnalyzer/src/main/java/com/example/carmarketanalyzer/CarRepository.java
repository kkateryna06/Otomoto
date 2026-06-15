package com.example.carmarketanalyzer;

import com.example.carmarketanalyzer.data.Car;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface CarRepository extends JpaRepository<Car, Long>, JpaSpecificationExecutor<Car> {
    Optional<Car> findByUrl(String url);

    @Query("""
            select distinct car.brand
            from Car car
            where car.brand is not null
              and trim(car.brand) <> ''
            order by car.brand
            """)
    List<String> findDistinctBrands();

    @Query("""
            select distinct car.model
            from Car car
            where car.model is not null
              and trim(car.model) <> ''
              and lower(car.brand) = lower(:brand)
            order by car.model
            """)
    List<String> findDistinctModelsByBrand(@Param("brand") String brand);

    @Query("""
            select distinct car.fuelType
            from Car car
            where car.fuelType is not null
              and trim(car.fuelType) <> ''
            order by car.fuelType
            """)
    List<String> findDistinctFuelTypes();

    @Query("""
            select distinct car.bodyType
            from Car car
            where car.bodyType is not null
              and trim(car.bodyType) <> ''
            order by car.bodyType
            """)
    List<String> findDistinctBodyTypes();

    @Query("""
            select distinct car.gearbox
            from Car car
            where car.gearbox is not null
              and trim(car.gearbox) <> ''
            order by car.gearbox
            """)
    List<String> findDistinctGearboxes();

    @Query("""
            select distinct car.transmission
            from Car car
            where car.transmission is not null
              and trim(car.transmission) <> ''
            order by car.transmission
            """)
    List<String> findDistinctTransmissions();

    @Query("""
            select car
            from Car car
            where car.actual = true
              and (
                  car.lastCheckedAt is null
                  or coalesce(car.postedAt, car.lastSeenAt) is null
                  or (
                      coalesce(car.postedAt, car.lastSeenAt) >= :freshCutoff
                      and car.lastCheckedAt <= :freshCheckBefore
                  )
                  or (
                      coalesce(car.postedAt, car.lastSeenAt) < :freshCutoff
                      and coalesce(car.postedAt, car.lastSeenAt) >= :middleCutoff
                      and car.lastCheckedAt <= :middleCheckBefore
                  )
                  or (
                      coalesce(car.postedAt, car.lastSeenAt) < :middleCutoff
                      and car.lastCheckedAt <= :oldCheckBefore
                  )
              )
            order by
                case when car.lastCheckedAt is null then 0 else 1 end,
                car.lastCheckedAt asc,
                car.id asc
            """)
    List<Car> findNextActualListingsForRecheck(
            @Param("freshCutoff") Instant freshCutoff,
            @Param("middleCutoff") Instant middleCutoff,
            @Param("freshCheckBefore") Instant freshCheckBefore,
            @Param("middleCheckBefore") Instant middleCheckBefore,
            @Param("oldCheckBefore") Instant oldCheckBefore,
            Pageable pageable
    );
}
