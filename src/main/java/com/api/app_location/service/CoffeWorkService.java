package com.api.app_location.service;

import com.api.app_location.dao.CoffeWorkRepository;
import com.api.app_location.dao.OverpassRegionCacheRepository;
import com.api.app_location.dto.CoffeWorkDTO;
import com.api.app_location.entity.CoffeWork;
import com.api.app_location.entity.OverpassRegionCache;
import com.api.app_location.exception.FailedSaveException;
import com.api.app_location.integration.overpass.OverpassClient;
import com.api.app_location.integration.overpass.OverpassProperties;
import com.api.app_location.integration.overpass.dto.OverpassElement;
import com.api.app_location.integration.overpass.dto.OverpassResponse;
import com.api.app_location.mapper.CoffeWorkMapper;
import com.api.app_location.mapper.OverpassElementMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@Slf4j
public class CoffeWorkService {

    private static final double LOCATION_BUCKET_SCALE = 100.0;

    @Autowired
    private CoffeWorkRepository coffeWorkRepository;

    @Autowired
    private OverpassRegionCacheRepository overpassRegionCacheRepository;

    @Autowired
    private CoffeWorkMapper mapper;

    @Autowired
    private OverpassClient overpassClient;

    @Autowired
    private OverpassProperties overpassProperties;

    @Autowired
    private OverpassElementMapper overpassElementMapper;

    public List<CoffeWorkDTO> listAll(int page, int size) throws Exception {
        if (size >= 100) {
            throw new FailedSaveException("Limite máximo de items retornados atingido");
        }
        Pageable pageable = PageRequest.of(page, size);
        return coffeWorkRepository.findAll(pageable).map(mapper::toDTO).getContent();
    }

    public List<CoffeWorkDTO> listName(String name) {
        List<CoffeWork> entities = coffeWorkRepository.findByNameContainingIgnoreCase(name);
        return mapper.toDTOList(entities);
    }

    public List<CoffeWorkDTO> bestCoffes(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return coffeWorkRepository.bestCoffes(pageable).stream().map(mapper::toDTO).toList();
    }

    public CoffeWork save(CoffeWorkDTO dto) {
        try {
            CoffeWork mapperEntity = mapper.toEntity(dto);
            return coffeWorkRepository.save(mapperEntity);
        } catch (Exception e) {
            throw new FailedSaveException("Erro ao salvar café: " + e.getMessage());
        }
    }

    public String buildHomeOfficeQuery(double latitude, double longitude, int radius) {
        return String.format(Locale.US, """
        [out:json][timeout:25];
        (
          nwr["amenity"="cafe"]
            (around:%1$d,%2$.6f,%3$.6f);

          nwr["office"="coworking"]
            (around:%1$d,%2$.6f,%3$.6f);

          nwr["amenity"="coworking_space"]
            (around:%1$d,%2$.6f,%3$.6f);

          nwr["amenity"="library"]
            (around:%1$d,%2$.6f,%3$.6f);

          nwr["amenity"="internet_cafe"]
            (around:%1$d,%2$.6f,%3$.6f);
        );

        out center tags;
        """,
                radius,
                latitude,
                longitude
        );
    }

    public Page<CoffeWorkDTO> nearestCoffeeShops(double latitude, double longitude, int radius, int page, int size) {
        try {
            if (size >= 100) {
                throw new FailedSaveException("Limite máximo de items retornados atingido");
            }

            Pageable pageable = PageRequest.of(page, size);
            Double latitudeBucket = bucket(latitude);
            Double longitudeBucket = bucket(longitude);

            if (isRegionCacheValid(latitudeBucket, longitudeBucket, radius)) {
                Page<CoffeWorkDTO> cachedResult = findNearestFromDatabase(latitude, longitude, radius, pageable);

                if (!cachedResult.isEmpty()) {
                    return cachedResult;
                }

                log.info("Cache Overpass valido encontrado, mas sem dados no banco. Sincronizando novamente.");
            }

            String data = buildHomeOfficeQuery(latitude, longitude, radius);

            OverpassResponse response = overpassClient.execute(data);
            int savedElements = saveOrUpdateOverpassElements(response.elements());
            updateRegionCache(latitudeBucket, longitudeBucket, radius);
            log.info("Sincronizacao Overpass concluida: {} estabelecimentos salvos/atualizados", savedElements);

            return findNearestFromDatabase(latitude, longitude, radius, pageable);
        } catch (Exception e) {
            log.error("Erro ao localizar e sincronizar dados da Overpass", e);
            throw new FailedSaveException("Erro ao localizar: " + e.getMessage());
        }
    }

    private Page<CoffeWorkDTO> findNearestFromDatabase(double latitude, double longitude, int radius, Pageable pageable) {
        return coffeWorkRepository.nearestCoffeeShops(latitude, longitude, radius, pageable)
                .map(mapper::toDTO);
    }

    private boolean isRegionCacheValid(Double latitudeBucket, Double longitudeBucket, int radius) {
        LocalDateTime validAfter = LocalDateTime.now().minus(overpassProperties.cacheTtl());

        return overpassRegionCacheRepository
                .findByLatitudeBucketAndLongitudeBucketAndRadiusMeters(latitudeBucket, longitudeBucket, radius)
                .map(cache -> cache.getLastFetchedAt().isAfter(validAfter))
                .orElse(false);
    }

    private int saveOrUpdateOverpassElements(List<OverpassElement> elements) {
        if (elements == null || elements.isEmpty()) {
            return 0;
        }

        List<CoffeWork> coffeWorks = elements.stream()
                .map(overpassElementMapper::toEntity)
                .filter(this::hasRequiredOverpassData)
                .map(this::createOrUpdateOverpassElement)
                .filter(Objects::nonNull)
                .toList();

        coffeWorkRepository.saveAllAndFlush(coffeWorks);
        return coffeWorks.size();
    }

    private boolean hasRequiredOverpassData(CoffeWork coffeWork) {
        return coffeWork.getOsmType() != null
                && coffeWork.getOsmId() != null
                && coffeWork.getLatitude() != null
                && coffeWork.getLongitude() != null;
    }

    private CoffeWork createOrUpdateOverpassElement(CoffeWork source) {
        return coffeWorkRepository.findByOsmTypeAndOsmId(source.getOsmType(), source.getOsmId())
                .map(existing -> {
                    copyOverpassFields(source, existing);
                    return existing;
                })
                .orElse(source);
    }

    private void copyOverpassFields(CoffeWork source, CoffeWork target) {
        target.setName(source.getName());
        target.setAdress(source.getAdress());
        target.setStreet(source.getStreet());
        target.setAddressNumber(source.getAddressNumber());
        target.setNeighborhood(source.getNeighborhood());
        target.setPostalCode(source.getPostalCode());
        target.setMunicipality(source.getMunicipality());
        target.setUf(source.getUf());
        target.setLatitude(source.getLatitude());
        target.setLongitude(source.getLongitude());
        target.setInternetAccess(source.getInternetAccess());
        target.setOpeningHours(source.getOpeningHours());
    }

    private void updateRegionCache(Double latitudeBucket, Double longitudeBucket, int radius) {
        OverpassRegionCache cache = overpassRegionCacheRepository
                .findByLatitudeBucketAndLongitudeBucketAndRadiusMeters(latitudeBucket, longitudeBucket, radius)
                .orElseGet(() -> OverpassRegionCache.builder()
                        .latitudeBucket(latitudeBucket)
                        .longitudeBucket(longitudeBucket)
                        .radiusMeters(radius)
                        .build());

        cache.setLastFetchedAt(LocalDateTime.now());
        overpassRegionCacheRepository.save(cache);
    }

    private Double bucket(double coordinate) {
        return Math.round(coordinate * LOCATION_BUCKET_SCALE) / LOCATION_BUCKET_SCALE;
    }

    public CoffeWorkDTO delete(Integer id) {
        try {
            if (!coffeWorkRepository.existsById(id)) {
                log.info("id não encontrado: " + id);
                return null;
            }

            CoffeWorkDTO dto = mapper.toDTO(coffeWorkRepository.findById(id).get());
            coffeWorkRepository.deleteById(id);
            return dto;
        } catch (Exception e) {
            throw new FailedSaveException("Erro ao deletar café: " + e.getMessage());
        }
    }
}
