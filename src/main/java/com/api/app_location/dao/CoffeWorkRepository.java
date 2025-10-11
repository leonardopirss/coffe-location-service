package com.api.app_location.dao;

import com.api.app_location.entity.CoffeWork;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.RepositoryDefinition;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoffeWorkRepository extends JpaRepository<CoffeWork, Integer> {
    List<CoffeWork> findByNameContainingIgnoreCase(String name);

    @Query("SELECT c FROM CoffeWork c WHERE c.assessment >= 4")
    List<CoffeWork> bestCoffes();

    @Query(value = """
            SELECT
                c.*,
                (
                    6371 * acos(
                        cos(radians(:userLat)) * cos(radians(c.latitude)) *
                        cos(radians(c.longitude) - radians(:userLng)) +
                        sin(radians(:userLat)) * sin(radians(c.latitude))
                    )
                ) AS distanciaKm,
                c
            FROM coffework.establishment c
            ORDER BY distanciaKm ASC
            LIMIT 10
            """, nativeQuery = true)
    List<CoffeWork> nearestCoffeeShops(@Param("userLat") double lat, @Param("userLng") double longitude);
}
