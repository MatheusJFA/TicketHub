package com.tickethub.infrastructure.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import com.tickethub.infrastructure.cache.TickethubCacheProperties;

@DisplayName("Cache configuration")
class CacheConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(CacheConfiguration.class)
            .withBean(RedisConnectionFactory.class, () -> mock(RedisConnectionFactory.class));

    @Test
    @DisplayName("Registers caches with configured TTLs")
    void registersCachesWithConfiguredTtls() {
        runner.withPropertyValues(
                "tickethub.cache.shows-ttl=5m",
                "tickethub.cache.sections-ttl=5m",
                "tickethub.cache.spots-ttl=15s")
                .run(context -> {
                    assertThat(context).hasSingleBean(CacheManager.class);
                    final var manager = context.getBean(RedisCacheManager.class);
                    assertThat(manager.getCacheConfigurations())
                            .containsKeys(TickethubCacheProperties.SHOWS,
                                    TickethubCacheProperties.SECTIONS,
                                    TickethubCacheProperties.SPOTS);
                    assertThat(manager.getCacheConfigurations()
                            .get(TickethubCacheProperties.SHOWS).getTtlFunction()
                            .getTimeToLive("key", "value"))
                            .isEqualTo(Duration.ofMinutes(5));
                    assertThat(manager.getCacheConfigurations()
                            .get(TickethubCacheProperties.SPOTS).getTtlFunction()
                            .getTimeToLive("key", "value"))
                            .isEqualTo(Duration.ofSeconds(15));
                });
    }

    @Test
    @DisplayName("Skips caching when disabled")
    void skipsCachingWhenDisabled() {
        runner.withPropertyValues("tickethub.cache.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(CacheManager.class));
    }
}
