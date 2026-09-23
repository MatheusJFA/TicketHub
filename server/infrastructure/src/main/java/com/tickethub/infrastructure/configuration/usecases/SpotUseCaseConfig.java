package com.tickethub.infrastructure.configuration.usecases;

import com.tickethub.application.spot.changelocation.ChangeSpotLocationUseCase;
import com.tickethub.application.spot.changelocation.DefaultChangeSpotLocationUseCase;
import com.tickethub.application.spot.create.CreateSpotUseCase;
import com.tickethub.application.spot.create.DefaultCreateSpotUseCase;
import com.tickethub.application.spot.delete.DefaultDeleteSpotUseCase;
import com.tickethub.application.spot.delete.DeleteSpotUseCase;
import com.tickethub.application.spot.publish.DefaultPublishSpotUseCase;
import com.tickethub.application.spot.publish.PublishSpotUseCase;
import com.tickethub.application.spot.retrieve.bysection.DefaultListSectionSpotsUseCase;
import com.tickethub.application.spot.retrieve.bysection.ListSectionSpotsUseCase;
import com.tickethub.application.spot.retrieve.get.DefaultGetSpotUseCase;
import com.tickethub.application.spot.retrieve.get.GetSpotUseCase;
import com.tickethub.application.spot.retrieve.list.DefaultListSpotsUseCase;
import com.tickethub.application.spot.retrieve.list.ListSpotsUseCase;
import com.tickethub.application.spot.unpublish.DefaultUnpublishSpotUseCase;
import com.tickethub.application.spot.unpublish.UnpublishSpotUseCase;
import com.tickethub.application.spot.update.DefaultUpdateSpotUseCase;
import com.tickethub.application.spot.update.UpdateSpotUseCase;
import com.tickethub.domain.core.section.SectionGateway;
import com.tickethub.domain.core.spot.SpotGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

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
    public CreateSpotUseCase createSpotUseCase(
            @Value("${tickethub.spots.seat-number-width:5}") final int seatNumberWidth) {
        Assert.isTrue(seatNumberWidth > 0, "tickethub.spots.seat-number-width must be positive");
        return new DefaultCreateSpotUseCase(spotGateway, sectionGateway, seatNumberWidth);
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
    public ListSectionSpotsUseCase listSectionSpotsUseCase() {
        return new DefaultListSectionSpotsUseCase(spotGateway);
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
