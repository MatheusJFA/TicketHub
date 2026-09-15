package com.tickethub.infrastructure.configuration.usecases;

import com.tickethub.application.partner.changeaddress.ChangePartnerAddressUseCase;
import com.tickethub.application.partner.changeaddress.DefaultChangePartnerAddressUseCase;
import com.tickethub.application.partner.changename.ChangePartnerNameUseCase;
import com.tickethub.application.partner.changename.DefaultChangePartnerNameUseCase;
import com.tickethub.application.partner.create.CreatePartnerUseCase;
import com.tickethub.application.partner.create.DefaultCreatePartnerUseCase;
import com.tickethub.application.partner.delete.DeletePartnerUseCase;
import com.tickethub.application.partner.delete.DefaultDeletePartnerUseCase;
import com.tickethub.application.partner.retrieve.get.GetPartnerUseCase;
import com.tickethub.application.partner.retrieve.get.DefaultGetPartnerUseCase;
import com.tickethub.application.partner.retrieve.list.ListPartnersUseCase;
import com.tickethub.application.partner.retrieve.list.DefaultListPartnersUseCase;
import com.tickethub.domain.core.partner.PartnerGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;

@Configuration(proxyBeanMethods = false)
@ConditionalOnBean({PartnerGateway.class})
public class PartnerUseCaseConfig {
    private final PartnerGateway partnerGateway;

    public PartnerUseCaseConfig(final PartnerGateway partnerGateway) {
        this.partnerGateway = partnerGateway;
    }

    @Bean
    public ChangePartnerAddressUseCase changePartnerAddressUseCase() {
        return new DefaultChangePartnerAddressUseCase(partnerGateway);
    }

    @Bean
    public ChangePartnerNameUseCase changePartnerNameUseCase() {
        return new DefaultChangePartnerNameUseCase(partnerGateway);
    }

    @Bean
    public CreatePartnerUseCase createPartnerUseCase() {
        return new DefaultCreatePartnerUseCase(partnerGateway);
    }

    @Bean
    public DeletePartnerUseCase deletePartnerUseCase() {
        return new DefaultDeletePartnerUseCase(partnerGateway);
    }

    @Bean
    public GetPartnerUseCase getPartnerUseCase() {
        return new DefaultGetPartnerUseCase(partnerGateway);
    }

    @Bean
    public ListPartnersUseCase listPartnersUseCase() {
        return new DefaultListPartnersUseCase(partnerGateway);
    }
}
