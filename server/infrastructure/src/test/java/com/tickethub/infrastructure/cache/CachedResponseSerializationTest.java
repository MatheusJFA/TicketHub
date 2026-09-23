package com.tickethub.infrastructure.cache;

import static org.assertj.core.api.Assertions.assertThat;

import com.tickethub.domain.pagination.Pagination;
import com.tickethub.infrastructure.configuration.CacheConfiguration;
import com.tickethub.infrastructure.spot.models.SpotListResponse;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

@DisplayName("Cached response serialization")
class CachedResponseSerializationTest {

    private final GenericJackson2JsonRedisSerializer serializer = CacheConfiguration.valueSerializer();

    @Test
    @DisplayName("Round-trips paginated spot responses with instants")
    void roundTripsPaginatedSpots() {
        final var page = new Pagination<>(
                0,
                10,
                1,
                List.of(new SpotListResponse(
                        "spot-1",
                        "A1",
                        true,
                        true,
                        Instant.parse("2026-09-22T10:00:00Z"),
                        Instant.parse("2026-09-22T10:00:00Z"),
                        null)));

        final var restored = serializer.deserialize(serializer.serialize(page), Pagination.class);

        assertThat(restored).isEqualTo(page);
    }
}
