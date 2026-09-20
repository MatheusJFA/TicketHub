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
import com.tickethub.application.partner.update.UpdatePartnerUseCase;
import com.tickethub.application.partner.update.DefaultUpdatePartnerUseCase;
import com.tickethub.domain.core.partner.PartnerGateway;
import com.tickethub.domain.auth.PasswordHasher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;

@Configuration(proxyBeanMethods = false)
@ConditionalOnBean({PartnerGateway.class})
public class PartnerUseCaseConfig {
    private final PartnerGateway partnerGateway;
    private final PasswordHasher passwordHasher;

    public PartnerUseCaseConfig(final PartnerGateway partnerGateway, final PasswordHasher passwordHasher) {
        this.partnerGateway = partnerGateway;
        this.passwordHasher = passwordHasher;
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
        return new DefaultCreatePartnerUseCase(partnerGateway, passwordHasher);
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

    @Bean
    public UpdatePartnerUseCase updatePartnerUseCase() {
        return new DefaultUpdatePartnerUseCase(partnerGateway);
    }
}
