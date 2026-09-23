package com.tickethub.infrastructure.partner.presenters;

import com.tickethub.application.partner.changeaddress.ChangePartnerAddressCommand;
import com.tickethub.application.partner.changename.ChangePartnerNameCommand;
import com.tickethub.application.partner.create.CreatePartnerCommand;
import com.tickethub.application.partner.retrieve.get.GetPartnerOutput;
import com.tickethub.application.partner.retrieve.list.ListPartnersOutput;
import com.tickethub.application.partner.update.UpdatePartnerCommand;
import com.tickethub.infrastructure.partner.models.ChangePartnerAddressRequest;
import com.tickethub.infrastructure.partner.models.ChangePartnerNameRequest;
import com.tickethub.infrastructure.partner.models.CreatePartnerRequest;
import com.tickethub.infrastructure.partner.models.PartnerListResponse;
import com.tickethub.infrastructure.partner.models.PartnerResponse;
import com.tickethub.infrastructure.partner.models.UpdatePartnerRequest;
import com.tickethub.infrastructure.shared.presenters.SharedMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = SharedMapper.class)
public interface PartnerMapper {

    PartnerResponse toResponse(GetPartnerOutput output);

    PartnerListResponse toListResponse(ListPartnersOutput output);

    CreatePartnerCommand toCommand(CreatePartnerRequest request);

    ChangePartnerNameCommand toCommand(String id, ChangePartnerNameRequest request);

    ChangePartnerAddressCommand toCommand(String id, ChangePartnerAddressRequest request);

    UpdatePartnerCommand toCommand(String id, UpdatePartnerRequest request);
}
