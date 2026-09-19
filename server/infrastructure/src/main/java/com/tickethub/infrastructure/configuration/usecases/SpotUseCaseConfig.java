package com.tickethub.infrastructure.configuration.usecases;

import com.tickethub.application.spot.changelocation.ChangeSpotLocationUseCase;
import com.tickethub.application.spot.changelocation.DefaultChangeSpotLocationUseCase;
import com.tickethub.application.spot.create.CreateSpotUseCase;
import com.tickethub.application.spot.create.DefaultCreateSpotUseCase;
import com.tickethub.application.spot.delete.DeleteSpotUseCase;
import com.tickethub.application.spot.delete.DefaultDeleteSpotUseCase;
import com.tickethub.application.spot.publish.PublishSpotUseCase;
import com.tickethub.application.spot.publish.DefaultPublishSpotUseCase;
import com.tickethub.application.spot.retrieve.get.GetSpotUseCase;
import com.tickethub.application.spot.retrieve.get.DefaultGetSpotUseCase;
import com.tickethub.application.spot.retrieve.list.ListSpotsUseCase;
import com.tickethub.application.spot.retrieve.list.DefaultListSpotsUseCase;
import com.tickethub.application.spot.unpublish.UnpublishSpotUseCase;
import com.tickethub.application.spot.unpublish.DefaultUnpublishSpotUseCase;
import com.tickethub.application.spot.update.UpdateSpotUseCase;
import com.tickethub.application.spot.update.DefaultUpdateSpotUseCase;
import com.tickethub.domain.core.spot.SpotGateway;
import com.tickethub.domain.core.section.SectionGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;

@Configuration(proxyBeanMethods = false)
@ConditionalOnBean({SpotGateway.class, SectionGateway.class})
public class SpotUseCaseConfig {
    private final SpotGateway spotGateway;
    private final SectionGateway sectionGateway;

    public SpotUseCaseConfig(final SpotGateway spotGateway, final SectionGateway sectionGateway) {
        this.spotGateway = spotGateway;
        this.sectionGateway = sectionGateway;
    }

    @Bean
    public ChangeSpotLocationUseCase changeSpotLocationUseCase() {
        return new DefaultChangeSpotLocationUseCase(spotGateway);
    }

    @Bean
    public CreateSpotUseCase createSpotUseCase() {
        return new DefaultCreateSpotUseCase(spotGateway, sectionGateway);
    }

    @Bean
    public DeleteSpotUseCase deleteSpotUseCase() {
        return new DefaultDeleteSpotUseCase(spotGateway);
    }

    @Bean
    public PublishSpotUseCase publishSpotUseCase() {
        return new DefaultPublishSpotUseCase(spotGateway);
    }

    @Bean
    public GetSpotUseCase getSpotUseCase() {
        return new DefaultGetSpotUseCase(spotGateway);
    }

    @Bean
    public ListSpotsUseCase listSpotsUseCase() {
        return new DefaultListSpotsUseCase(spotGateway);
    }

    @Bean
    public UnpublishSpotUseCase unpublishSpotUseCase() {
        return new DefaultUnpublishSpotUseCase(spotGateway);
    }

    @Bean
    public UpdateSpotUseCase updateSpotUseCase() {
        return new DefaultUpdateSpotUseCase(spotGateway);
    }
}
