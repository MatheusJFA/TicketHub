package com.tickethub.infrastructure.configuration.usecases;

import com.tickethub.application.show.addsection.AddSectionToShowUseCase;
import com.tickethub.application.show.addsection.DefaultAddSectionToShowUseCase;
import com.tickethub.application.show.changedescription.ChangeShowDescriptionUseCase;
import com.tickethub.application.show.changedescription.DefaultChangeShowDescriptionUseCase;
import com.tickethub.application.show.changename.ChangeShowNameUseCase;
import com.tickethub.application.show.changename.DefaultChangeShowNameUseCase;
import com.tickethub.application.show.create.CreateShowUseCase;
import com.tickethub.application.show.create.DefaultCreateShowUseCase;
import com.tickethub.application.show.delete.DeleteShowUseCase;
import com.tickethub.application.show.delete.DefaultDeleteShowUseCase;
import com.tickethub.application.show.publish.PublishShowUseCase;
import com.tickethub.application.show.publish.DefaultPublishShowUseCase;
import com.tickethub.application.show.publishall.PublishAllShowUseCase;
import com.tickethub.application.show.publishall.DefaultPublishAllShowUseCase;
import com.tickethub.application.show.reschedule.RescheduleShowUseCase;
import com.tickethub.application.show.reschedule.DefaultRescheduleShowUseCase;
import com.tickethub.application.show.retrieve.get.GetShowUseCase;
import com.tickethub.application.show.retrieve.get.DefaultGetShowUseCase;
import com.tickethub.application.show.retrieve.list.ListShowsUseCase;
import com.tickethub.application.show.retrieve.list.DefaultListShowsUseCase;
import com.tickethub.application.show.unpublish.UnpublishShowUseCase;
import com.tickethub.application.show.unpublish.DefaultUnpublishShowUseCase;
import com.tickethub.application.show.unpublishall.UnpublishAllShowUseCase;
import com.tickethub.application.show.unpublishall.DefaultUnpublishAllShowUseCase;
import com.tickethub.domain.core.show.ShowGateway;
import com.tickethub.domain.core.partner.PartnerGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;

@Configuration(proxyBeanMethods = false)
@ConditionalOnBean({ShowGateway.class, PartnerGateway.class})
public class ShowUseCaseConfig {
    private final ShowGateway showGateway;
    private final PartnerGateway partnerGateway;

    public ShowUseCaseConfig(final ShowGateway showGateway, final PartnerGateway partnerGateway) {
        this.showGateway = showGateway;
        this.partnerGateway = partnerGateway;
    }

    @Bean
    public AddSectionToShowUseCase addSectionToShowUseCase() {
        return new DefaultAddSectionToShowUseCase(showGateway);
    }

    @Bean
    public ChangeShowDescriptionUseCase changeShowDescriptionUseCase() {
        return new DefaultChangeShowDescriptionUseCase(showGateway);
    }

    @Bean
    public ChangeShowNameUseCase changeShowNameUseCase() {
        return new DefaultChangeShowNameUseCase(showGateway);
    }

    @Bean
    public CreateShowUseCase createShowUseCase() {
        return new DefaultCreateShowUseCase(showGateway, partnerGateway);
    }

    @Bean
    public DeleteShowUseCase deleteShowUseCase() {
        return new DefaultDeleteShowUseCase(showGateway);
    }

    @Bean
    public PublishShowUseCase publishShowUseCase() {
        return new DefaultPublishShowUseCase(showGateway);
    }

    @Bean
    public PublishAllShowUseCase publishAllShowUseCase() {
        return new DefaultPublishAllShowUseCase(showGateway);
    }

    @Bean
    public RescheduleShowUseCase rescheduleShowUseCase() {
        return new DefaultRescheduleShowUseCase(showGateway);
    }

    @Bean
    public GetShowUseCase getShowUseCase() {
        return new DefaultGetShowUseCase(showGateway);
    }

    @Bean
    public ListShowsUseCase listShowsUseCase() {
        return new DefaultListShowsUseCase(showGateway);
    }

    @Bean
    public UnpublishShowUseCase unpublishShowUseCase() {
        return new DefaultUnpublishShowUseCase(showGateway);
    }

    @Bean
    public UnpublishAllShowUseCase unpublishAllShowUseCase() {
        return new DefaultUnpublishAllShowUseCase(showGateway);
    }
}
