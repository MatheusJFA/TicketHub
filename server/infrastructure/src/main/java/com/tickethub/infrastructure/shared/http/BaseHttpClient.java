package com.tickethub.infrastructure.shared.http;

import static java.util.Objects.requireNonNull;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

public abstract class BaseHttpClient {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final RestClient restClient;
    protected final String provider;

    protected BaseHttpClient(final RestClient restClient, final String provider) {
        this.restClient = requireNonNull(restClient, "'restClient' should not be null");
        this.provider = requireNonNull(provider, "'provider' should not be null");
    }

    /**
     * Applies base URL and default headers to a builder. The request factory
     * (timeouts, mocks) stays with the caller so tests can bind
     * {@code MockRestServiceServer} before {@code build()}.
     */
    public static RestClient.Builder preparedBuilder(final RestClient.Builder builder, final String baseUrl) {
        return requireNonNull(builder, "'builder' should not be null")
                .baseUrl(requireNonNull(baseUrl, "'baseUrl' should not be null"))
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.USER_AGENT, "tickethub-server");
    }

    public static ClientHttpRequestFactory timedFactory(final Duration connectTimeout,
            final Duration readTimeout) {
        final var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);
        return requestFactory;
    }

    /**
     * GET returning empty on any upstream failure (fail-open). 4xx, 5xx,
     * timeouts and connection errors are logged and swallowed; use
     * {@link #getRequired(String, Class)} when the caller must fail-closed.
     */
    protected <T> Optional<T> getOptional(final String uri, final Class<T> responseType) {
        return getOptional(uri, Map.of(), responseType);
    }

    protected <T> Optional<T> getOptional(final String uri, final Map<String, ?> uriVariables,
            final Class<T> responseType) {
        try {
            return Optional.ofNullable(restClient.get()
                    .uri(uri, uriVariables)
                    .retrieve()
                    .body(responseType));
        } catch (final RestClientException e) {
            log.warn("Upstream call failed provider={} uri={} error={}", provider, uri, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * GET throwing {@link HttpUpstreamException} on any upstream failure,
     * for callers that must fail-closed.
     */
    protected <T> T getRequired(final String uri, final Class<T> responseType) {
        return getRequired(uri, Map.of(), responseType);
    }

    protected <T> T getRequired(final String uri, final Map<String, ?> uriVariables,
            final Class<T> responseType) {
        try {
            final var response = restClient.get()
                    .uri(uri, uriVariables)
                    .retrieve()
                    .toEntity(responseType);
            return Optional.ofNullable(response.getBody())
                    .orElseThrow(() -> new HttpUpstreamException(provider, 200, "Empty response body"));
        } catch (final HttpUpstreamException e) {
            throw e;
        } catch (final RestClientException e) {
            throw new HttpUpstreamException(provider, e);
        }
    }
}
