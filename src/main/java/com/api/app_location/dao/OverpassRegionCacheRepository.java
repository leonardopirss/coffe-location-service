package com.api.app_location.dao;

import com.api.app_location.entity.OverpassRegionCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OverpassRegionCacheRepository extends JpaRepository<OverpassRegionCache, Integer> {

    Optional<OverpassRegionCache> findByLatitudeBucketAndLongitudeBucketAndRadiusMeters(
            Double latitudeBucket,
            Double longitudeBucket,
            Integer radiusMeters
    );
}
