package com.tickethub.infrastructure.configuration.usecases;

import com.tickethub.application.section.changedescription.ChangeSectionDescriptionUseCase;
import com.tickethub.application.section.changedescription.DefaultChangeSectionDescriptionUseCase;
import com.tickethub.application.section.changename.ChangeSectionNameUseCase;
import com.tickethub.application.section.changename.DefaultChangeSectionNameUseCase;
import com.tickethub.application.section.changeprice.ChangeSectionPriceUseCase;
import com.tickethub.application.section.changeprice.DefaultChangeSectionPriceUseCase;
import com.tickethub.application.section.create.CreateSectionUseCase;
import com.tickethub.application.section.create.DefaultCreateSectionUseCase;
import com.tickethub.application.section.delete.DeleteSectionUseCase;
import com.tickethub.application.section.delete.DefaultDeleteSectionUseCase;
import com.tickethub.application.section.publish.PublishSectionUseCase;
import com.tickethub.application.section.publish.DefaultPublishSectionUseCase;
import com.tickethub.application.section.publishall.PublishAllSectionUseCase;
import com.tickethub.application.section.publishall.DefaultPublishAllSectionUseCase;
import com.tickethub.application.section.retrieve.get.GetSectionUseCase;
import com.tickethub.application.section.retrieve.get.DefaultGetSectionUseCase;
import com.tickethub.application.section.retrieve.byshow.ListShowSectionsUseCase;
import com.tickethub.application.section.retrieve.byshow.DefaultListShowSectionsUseCase;
import com.tickethub.application.section.retrieve.list.ListSectionsUseCase;
import com.tickethub.application.section.retrieve.list.DefaultListSectionsUseCase;
import com.tickethub.application.section.unpublish.UnpublishSectionUseCase;
import com.tickethub.application.section.unpublish.DefaultUnpublishSectionUseCase;
import com.tickethub.application.section.unpublishall.UnpublishAllSectionUseCase;
import com.tickethub.application.section.unpublishall.DefaultUnpublishAllSectionUseCase;
import com.tickethub.application.section.update.UpdateSectionUseCase;
import com.tickethub.application.section.update.DefaultUpdateSectionUseCase;
import com.tickethub.domain.core.section.SectionGateway;
import com.tickethub.domain.core.show.ShowGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.util.Assert;

@Configuration(proxyBeanMethods = false)
@ConditionalOnBean({SectionGateway.class, ShowGateway.class})
public class SectionUseCaseConfig {
    private final SectionGateway sectionGateway;
    private final ShowGateway showGateway;

    public SectionUseCaseConfig(final SectionGateway sectionGateway, final ShowGateway showGateway) {
        this.sectionGateway = sectionGateway;
        this.showGateway = showGateway;
    }

    @Bean
    public ChangeSectionDescriptionUseCase changeSectionDescriptionUseCase() {
        return new DefaultChangeSectionDescriptionUseCase(sectionGateway);
    }

    @Bean
    public ChangeSectionNameUseCase changeSectionNameUseCase() {
        return new DefaultChangeSectionNameUseCase(sectionGateway);
    }

    @Bean
    public ChangeSectionPriceUseCase changeSectionPriceUseCase() {
        return new DefaultChangeSectionPriceUseCase(sectionGateway);
    }

    @Bean
    public CreateSectionUseCase createSectionUseCase(
            @Value("${tickethub.spots.seat-number-width:5}") final int seatNumberWidth) {
        Assert.isTrue(seatNumberWidth > 0, "tickethub.spots.seat-number-width must be positive");
        return new DefaultCreateSectionUseCase(sectionGateway, showGateway, seatNumberWidth);
    }

    @Bean
    public DeleteSectionUseCase deleteSectionUseCase() {
        return new DefaultDeleteSectionUseCase(sectionGateway);
    }

    @Bean
    public PublishSectionUseCase publishSectionUseCase() {
        return new DefaultPublishSectionUseCase(sectionGateway);
    }

    @Bean
    public PublishAllSectionUseCase publishAllSectionUseCase() {
        return new DefaultPublishAllSectionUseCase(sectionGateway);
    }

    @Bean
    public GetSectionUseCase getSectionUseCase() {
        return new DefaultGetSectionUseCase(sectionGateway);
    }

    @Bean
    public ListSectionsUseCase listSectionsUseCase() {
        return new DefaultListSectionsUseCase(sectionGateway);
    }

    @Bean
    public ListShowSectionsUseCase listShowSectionsUseCase() {
        return new DefaultListShowSectionsUseCase(sectionGateway);
    }

    @Bean
    public UnpublishSectionUseCase unpublishSectionUseCase() {
        return new DefaultUnpublishSectionUseCase(sectionGateway);
    }

    @Bean
    public UnpublishAllSectionUseCase unpublishAllSectionUseCase() {
        return new DefaultUnpublishAllSectionUseCase(sectionGateway);
    }

    @Bean
    public UpdateSectionUseCase updateSectionUseCase() {
        return new DefaultUpdateSectionUseCase(sectionGateway);
    }
}
