package com.example.carmarketanalyzer;

import com.example.carmarketanalyzer.data.Car;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface CarRepository extends JpaRepository<Car, Long> {
    Optional<Car> findByUrl(String url);

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
