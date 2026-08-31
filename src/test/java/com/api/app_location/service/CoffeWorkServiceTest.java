package com.api.app_location.service;

import com.api.app_location.dao.CoffeWorkRepository;
import com.api.app_location.dao.OverpassRegionCacheRepository;
import com.api.app_location.dto.CoffeWorkDTO;
import com.api.app_location.entity.CoffeWork;
import com.api.app_location.entity.OverpassRegionCache;
import com.api.app_location.integration.overpass.OverpassClient;
import com.api.app_location.integration.overpass.OverpassProperties;
import com.api.app_location.integration.overpass.dto.OverpassElement;
import com.api.app_location.integration.overpass.dto.OverpassResponse;
import com.api.app_location.mapper.CoffeWorkMapper;
import com.api.app_location.mapper.OverpassElementMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoffeWorkServiceTest {

    @Mock
    private CoffeWorkRepository coffeWorkRepository;

    @Mock
    private OverpassRegionCacheRepository overpassRegionCacheRepository;

    @Mock
    private OverpassClient overpassClient;

    private final CoffeWorkService service = new CoffeWorkService();
    private final CoffeWorkMapper coffeWorkMapper = Mappers.getMapper(CoffeWorkMapper.class);
    private final OverpassElementMapper overpassElementMapper = Mappers.getMapper(OverpassElementMapper.class);

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "coffeWorkRepository", coffeWorkRepository);
        ReflectionTestUtils.setField(service, "overpassRegionCacheRepository", overpassRegionCacheRepository);
        ReflectionTestUtils.setField(service, "mapper", coffeWorkMapper);
        ReflectionTestUtils.setField(service, "overpassClient", overpassClient);
        ReflectionTestUtils.setField(service, "overpassProperties", new OverpassProperties(
                URI.create("https://overpass-api.de"),
                Duration.ofSeconds(5),
                Duration.ofSeconds(30),
                Duration.ofDays(7)
        ));
        ReflectionTestUtils.setField(service, "overpassElementMapper", overpassElementMapper);
    }

    @Test
    void shouldReturnDatabasePageWhenRegionCacheIsValid() {
        OverpassRegionCache cache = OverpassRegionCache.builder()
                .latitudeBucket(-30.03)
                .longitudeBucket(-51.22)
                .radiusMeters(3000)
                .lastFetchedAt(LocalDateTime.now())
                .build();
        CoffeWork stored = CoffeWork.builder()
                .id(1)
                .name("Cafe Banco")
                .latitude(-30.0346)
                .longitude(-51.2177)
                .build();

        when(overpassRegionCacheRepository.findByLatitudeBucketAndLongitudeBucketAndRadiusMeters(-30.03, -51.22, 3000))
                .thenReturn(Optional.of(cache));
        when(coffeWorkRepository.nearestCoffeeShops(-30.0346, -51.2177, 3000, PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(stored), PageRequest.of(0, 10), 1));

        Page<CoffeWorkDTO> result = service.nearestCoffeeShops(-30.0346, -51.2177, 3000, 0, 10);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getName()).isEqualTo("Cafe Banco");
        verify(overpassClient, never()).execute(any());
        verify(coffeWorkRepository, never()).saveAllAndFlush(any());
    }

    @Test
    void shouldCallOverpassAndSaveWhenCacheIsMissing() {
        OverpassElement element = new OverpassElement(
                "node",
                123L,
                -30.0346,
                -51.2177,
                null,
                Map.of(
                        "name", "Cafe Overpass",
                        "addr:street", "Rua Exemplo",
                        "addr:housenumber", "10"
                )
        );

        when(overpassRegionCacheRepository.findByLatitudeBucketAndLongitudeBucketAndRadiusMeters(-30.03, -51.22, 3000))
                .thenReturn(Optional.empty());
        when(overpassClient.execute(any()))
                .thenReturn(new OverpassResponse(0.6, "Overpass API", null, List.of(element)));
        when(coffeWorkRepository.findByOsmTypeAndOsmId("node", 123L))
                .thenReturn(Optional.empty());
        when(coffeWorkRepository.saveAllAndFlush(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(overpassRegionCacheRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(coffeWorkRepository.nearestCoffeeShops(-30.0346, -51.2177, 3000, PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(List.of(overpassElementMapper.toEntity(element)), PageRequest.of(0, 20), 1));

        Page<CoffeWorkDTO> result = service.nearestCoffeeShops(-30.0346, -51.2177, 3000, 0, 20);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getName()).isEqualTo("Cafe Overpass");

        ArgumentCaptor<List<CoffeWork>> captor = ArgumentCaptor.forClass(List.class);
        verify(coffeWorkRepository).saveAllAndFlush(captor.capture());
        assertThat(captor.getValue()).hasSize(1);
        assertThat(captor.getValue().getFirst().getOsmType()).isEqualTo("node");
        assertThat(captor.getValue().getFirst().getOsmId()).isEqualTo(123L);
        assertThat(captor.getValue().getFirst().getAdress()).isEqualTo("Rua Exemplo, nº 10");
        verify(overpassRegionCacheRepository).save(any(OverpassRegionCache.class));
    }

    @Test
    void shouldCallOverpassAgainWhenCacheIsValidButDatabasePageIsEmpty() {
        OverpassRegionCache cache = OverpassRegionCache.builder()
                .latitudeBucket(-30.03)
                .longitudeBucket(-51.22)
                .radiusMeters(3000)
                .lastFetchedAt(LocalDateTime.now())
                .build();
        OverpassElement element = new OverpassElement(
                "way",
                456L,
                null,
                null,
                new com.api.app_location.integration.overpass.dto.OverpassCenter(-30.035, -51.218),
                Map.of("name", "Coworking Overpass")
        );

        when(overpassRegionCacheRepository.findByLatitudeBucketAndLongitudeBucketAndRadiusMeters(-30.03, -51.22, 3000))
                .thenReturn(Optional.of(cache));
        when(coffeWorkRepository.nearestCoffeeShops(-30.0346, -51.2177, 3000, PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0))
                .thenReturn(new PageImpl<>(List.of(overpassElementMapper.toEntity(element)), PageRequest.of(0, 10), 1));
        when(overpassClient.execute(any()))
                .thenReturn(new OverpassResponse(0.6, "Overpass API", null, List.of(element)));
        when(coffeWorkRepository.findByOsmTypeAndOsmId("way", 456L))
                .thenReturn(Optional.empty());
        when(coffeWorkRepository.saveAllAndFlush(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(overpassRegionCacheRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Page<CoffeWorkDTO> result = service.nearestCoffeeShops(-30.0346, -51.2177, 3000, 0, 10);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getName()).isEqualTo("Coworking Overpass");
        verify(overpassClient).execute(any());
        verify(coffeWorkRepository).saveAllAndFlush(any());
    }

    @Test
    void shouldUpdateExistingOsmPlaceInsteadOfCreatingDuplicate() {
        OverpassElement element = new OverpassElement(
                "node",
                123L,
                -30.0346,
                -51.2177,
                null,
                Map.of("name", "Nome Atualizado", "internet_access", "wlan")
        );
        CoffeWork existing = CoffeWork.builder()
                .id(99)
                .name("Nome Antigo")
                .osmType("node")
                .osmId(123L)
                .latitude(-30.0)
                .longitude(-51.0)
                .build();

        when(overpassRegionCacheRepository.findByLatitudeBucketAndLongitudeBucketAndRadiusMeters(-30.03, -51.22, 3000))
                .thenReturn(Optional.empty());
        when(overpassClient.execute(any()))
                .thenReturn(new OverpassResponse(0.6, "Overpass API", null, List.of(element)));
        when(coffeWorkRepository.findByOsmTypeAndOsmId("node", 123L))
                .thenReturn(Optional.of(existing));
        when(coffeWorkRepository.saveAllAndFlush(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(overpassRegionCacheRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(coffeWorkRepository.nearestCoffeeShops(anyDouble(), anyDouble(), anyInt(), eq(PageRequest.of(0, 10))))
                .thenReturn(new PageImpl<>(List.of(existing), PageRequest.of(0, 10), 1));

        service.nearestCoffeeShops(-30.0346, -51.2177, 3000, 0, 10);

        ArgumentCaptor<List<CoffeWork>> captor = ArgumentCaptor.forClass(List.class);
        verify(coffeWorkRepository).saveAllAndFlush(captor.capture());
        assertThat(captor.getValue()).hasSize(1);
        assertThat(captor.getValue().getFirst().getId()).isEqualTo(99);
        assertThat(captor.getValue().getFirst().getName()).isEqualTo("Nome Atualizado");
        assertThat(captor.getValue().getFirst().getInternetAccess()).isEqualTo("wlan");
    }
}
