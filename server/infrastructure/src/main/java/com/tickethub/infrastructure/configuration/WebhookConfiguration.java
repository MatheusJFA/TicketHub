package com.tickethub.infrastructure.configuration;

import com.tickethub.domain.core.order.OrderGateway;
import com.tickethub.domain.core.partner.PartnerGateway;
import com.tickethub.domain.core.show.ShowGateway;
import com.tickethub.domain.core.spot.SpotGateway;
import com.tickethub.infrastructure.shared.http.BaseHttpClient;
import com.tickethub.infrastructure.webhooks.PartnerWebhookDispatcher;
import com.tickethub.infrastructure.webhooks.PartnerWebhookProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(PartnerWebhookProperties.class)
@ConditionalOnProperty(prefix = "tickethub.webhooks", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnBean({OrderGateway.class, SpotGateway.class, ShowGateway.class, PartnerGateway.class})
public class WebhookConfiguration {

    @Bean
    public PartnerWebhookDispatcher partnerWebhookDispatcher(
            final OrderGateway orders,
            final SpotGateway spots,
            final ShowGateway shows,
            final PartnerGateway partners,
            final ObjectMapper objectMapper,
            final PartnerWebhookProperties properties) {
        final var configured = RestClient.builder()
                .requestFactory(
                        BaseHttpClient.timedFactory(properties.getConnectTimeout(), properties.getReadTimeout()))
                .defaultHeader("Accept", "application/json")
                .defaultHeader("User-Agent", "tickethub-server")
                .build();
        return new PartnerWebhookDispatcher(orders, spots, shows, partners, configured, objectMapper, properties);
    }
}
