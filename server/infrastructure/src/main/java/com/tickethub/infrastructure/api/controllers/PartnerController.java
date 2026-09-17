package com.tickethub.infrastructure.api.controllers;

import com.tickethub.infrastructure.api.models.*;
import com.tickethub.application.partner.changeaddress.*;
import com.tickethub.application.partner.changename.*;
import com.tickethub.application.partner.create.*;
import com.tickethub.application.partner.delete.*;
import com.tickethub.application.partner.retrieve.get.*;
import com.tickethub.application.partner.retrieve.list.*;
import com.tickethub.infrastructure.partner.models.*;
import org.springframework.http.ResponseEntity;
import com.tickethub.infrastructure.api.ApiSupport;
import com.tickethub.infrastructure.api.PartnerAPI;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class PartnerController implements PartnerAPI {
    private final ChangePartnerAddressUseCase changePartnerAddress;
    private final ChangePartnerNameUseCase changePartnerName;
    private final CreatePartnerUseCase createPartner;
    private final DeletePartnerUseCase deletePartner;
    private final GetPartnerUseCase getPartner;
    private final ListPartnersUseCase listPartners;

    public PartnerController(ChangePartnerAddressUseCase changePartnerAddress,
            ChangePartnerNameUseCase changePartnerName,
            CreatePartnerUseCase createPartner,
            DeletePartnerUseCase deletePartner,
            GetPartnerUseCase getPartner,
            ListPartnersUseCase listPartners) {
        this.changePartnerAddress = changePartnerAddress;
        this.changePartnerName = changePartnerName;
        this.createPartner = createPartner;
        this.deletePartner = deletePartner;
        this.getPartner = getPartner;
        this.listPartners = listPartners;
    }

    @Override
    public ResponseEntity<?> changePartnerAddress(String id, ChangePartnerAddressRequest input) {
        final var output = ApiSupport.execute(changePartnerAddress, new ChangePartnerAddressCommand(
                id,
                input.address() == null ? null : input.address().toDomain()));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }

    @Override
    public ResponseEntity<?> changePartnerName(String id, ChangePartnerNameRequest input) {
        final var output = ApiSupport.execute(changePartnerName, new ChangePartnerNameCommand(
                id,
                input.name()));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }

    @Override
    public ResponseEntity<?> createPartner(CreatePartnerRequest input) {
        final var output = ApiSupport.execute(createPartner, new CreatePartnerCommand(
                input.name(),
                input.cnpj(),
                input.address() == null ? null : input.address().toDomain()));
        return ResponseEntity.created(URI.create("/partners/" + output.id())).body(new IdResponse(output.id()));
    }

    @Override
    public ResponseEntity<?> deleteById(String id) {
        ApiSupport.execute(deletePartner, id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<?> getById(String id) {
        return ResponseEntity.ok(PartnerResponse.from(ApiSupport.execute(getPartner, id)));
    }

    @Override
    public ResponseEntity<?> list(String search, int page, int perPage, String sort, String direction) {
        final var result = ApiSupport.execute(listPartners, ApiSupport.query(search, page, perPage, sort, direction))
                .map(PartnerListResponse::from);
        return ResponseEntity.ok(result);
    }
}
