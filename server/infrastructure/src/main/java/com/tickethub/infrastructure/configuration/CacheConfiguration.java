package com.tickethub.infrastructure.configuration;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tickethub.infrastructure.cache.TickethubCacheProperties;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Cache compartilhado de leitura em Redis. A camada de aplicação segue
 * framework-free (ver {@code ArchitectureTest}): as anotações de cache vivem
 * nos controllers, que cacheiam DTOs de resposta (records). Decisões de
 * reserva nunca passam pelo cache — o fluxo de compra lê do Mongo.
 */
@Configuration(proxyBeanMethods = false)
@EnableCaching
@EnableConfigurationProperties(TickethubCacheProperties.class)
@ConditionalOnProperty(prefix = "tickethub.cache", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CacheConfiguration {

    @Bean
    public CacheManager cacheManager(
            final RedisConnectionFactory connectionFactory, final TickethubCacheProperties properties) {
        final var defaults = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer()));
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaults)
                .withInitialCacheConfigurations(Map.of(
                        TickethubCacheProperties.SHOWS,
                        defaults.entryTtl(properties.getShowsTtl()),
                        TickethubCacheProperties.SECTIONS,
                        defaults.entryTtl(properties.getSectionsTtl()),
                        TickethubCacheProperties.SPOTS,
                        defaults.entryTtl(properties.getSpotsTtl())))
                .build();
    }

    /**
     * JSON with type info: cached values are heterogeneous
     * ({@code Pagination<SomeListResponse>}, response records) and generics
     * erase item types, so every value carries its {@code @class}. Dates use
     * ISO-8601, like the HTTP API.
     */
    public static GenericJackson2JsonRedisSerializer valueSerializer() {
        final var mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                        .allowIfBaseType(Object.class)
                        .build(),
                ObjectMapper.DefaultTyping.EVERYTHING,
                JsonTypeInfo.As.PROPERTY);
        return new GenericJackson2JsonRedisSerializer(mapper);
    }
}
