package com.api.app_location.dao;

import com.api.app_location.entity.CoffeWork;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CoffeWorkRepository extends JpaRepository<CoffeWork, Integer> {
    List<CoffeWork> findByNameContainingIgnoreCase(String name);

    Optional<CoffeWork> findByOsmTypeAndOsmId(String osmType, Long osmId);

    boolean existsByOsmTypeAndOsmId(String osmType, Long osmId);

    @Query("SELECT c FROM CoffeWork c WHERE c.assessment >= 4")
    List<CoffeWork> bestCoffes(Pageable pageable);

    @Query(
            value = """
            SELECT c.*
            FROM coffework.establishment c
            WHERE c.lat IS NOT NULL
              AND c.lng IS NOT NULL
              AND (
                  6371 * acos(
                      least(1, greatest(-1,
                          cos(radians(:userLat)) * cos(radians(c.lat)) *
                          cos(radians(c.lng) - radians(:userLng)) +
                          sin(radians(:userLat)) * sin(radians(c.lat))
                      ))
                  )
              ) <= (:radiusMeters / 1000.0)
            ORDER BY (
                6371 * acos(
                    least(1, greatest(-1,
                        cos(radians(:userLat)) * cos(radians(c.lat)) *
                        cos(radians(c.lng) - radians(:userLng)) +
                        sin(radians(:userLat)) * sin(radians(c.lat))
                    ))
                )
            ) ASC
            """,
            countQuery = """
            SELECT count(*)
            FROM coffework.establishment c
            WHERE c.lat IS NOT NULL
              AND c.lng IS NOT NULL
              AND (
                  6371 * acos(
                      least(1, greatest(-1,
                          cos(radians(:userLat)) * cos(radians(c.lat)) *
                          cos(radians(c.lng) - radians(:userLng)) +
                          sin(radians(:userLat)) * sin(radians(c.lat))
                      ))
                  )
              ) <= (:radiusMeters / 1000.0)
            """,
            nativeQuery = true
    )
    Page<CoffeWork> nearestCoffeeShops(
            @Param("userLat") double lat,
            @Param("userLng") double longitude,
            @Param("radiusMeters") int radiusMeters,
            Pageable pageable
    );
}
