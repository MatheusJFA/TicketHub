package com.tickethub.infrastructure.api.controllers;

import com.tickethub.application.partner.approve.*;
import com.tickethub.application.partner.changeaddress.*;
import com.tickethub.application.partner.changename.*;
import com.tickethub.application.partner.changewebhook.*;
import com.tickethub.application.partner.create.*;
import com.tickethub.application.partner.delete.*;
import com.tickethub.application.partner.reject.*;
import com.tickethub.application.partner.retrieve.get.*;
import com.tickethub.application.partner.retrieve.list.*;
import com.tickethub.application.partner.update.*;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.infrastructure.api.HttpResults;
import com.tickethub.infrastructure.api.PartnerAPI;
import com.tickethub.infrastructure.api.models.*;
import com.tickethub.infrastructure.partner.models.*;
import com.tickethub.infrastructure.partner.presenters.PartnerMapper;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PartnerController implements PartnerAPI {
    private final ChangePartnerAddressUseCase changePartnerAddress;
    private final ChangePartnerNameUseCase changePartnerName;
    private final ChangePartnerWebhookUseCase changePartnerWebhook;
    private final CreatePartnerUseCase createPartner;
    private final DeletePartnerUseCase deletePartner;
    private final ApprovePartnerUseCase approvePartner;
    private final RejectPartnerUseCase rejectPartner;
    private final GetPartnerUseCase getPartner;
    private final ListPartnersUseCase listPartners;
    private final UpdatePartnerUseCase updatePartner;
    private final PartnerMapper mapper;

    public PartnerController(
            ChangePartnerAddressUseCase changePartnerAddress,
            ChangePartnerNameUseCase changePartnerName,
            ChangePartnerWebhookUseCase changePartnerWebhook,
            CreatePartnerUseCase createPartner,
            DeletePartnerUseCase deletePartner,
            ApprovePartnerUseCase approvePartner,
            RejectPartnerUseCase rejectPartner,
            GetPartnerUseCase getPartner,
            ListPartnersUseCase listPartners,
            UpdatePartnerUseCase updatePartner,
            PartnerMapper mapper) {
        this.changePartnerAddress = changePartnerAddress;
        this.changePartnerName = changePartnerName;
        this.changePartnerWebhook = changePartnerWebhook;
        this.createPartner = createPartner;
        this.deletePartner = deletePartner;
        this.approvePartner = approvePartner;
        this.rejectPartner = rejectPartner;
        this.getPartner = getPartner;
        this.listPartners = listPartners;
        this.updatePartner = updatePartner;
        this.mapper = mapper;
    }

    @Override
    public ResponseEntity<IdResponse> changePartnerAddress(String id, ChangePartnerAddressRequest input) {
        final var output = HttpResults.require(changePartnerAddress.execute(mapper.toCommand(id, input)));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }

    @Override
    public ResponseEntity<IdResponse> changePartnerName(String id, ChangePartnerNameRequest input) {
        final var output = HttpResults.require(changePartnerName.execute(mapper.toCommand(id, input)));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }

    @Override
    public ResponseEntity<IdResponse> changePartnerWebhook(String id, ChangePartnerWebhookRequest input) {
        final var output = HttpResults.require(changePartnerWebhook.execute(mapper.toCommand(id, input)));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }

    @Override
    public ResponseEntity<IdResponse> createPartner(CreatePartnerRequest input) {
        final var output = HttpResults.require(createPartner.execute(mapper.toCommand(input)));
        return ResponseEntity.created(URI.create("/partners/" + output.id())).body(new IdResponse(output.id()));
    }

    @Override
    public ResponseEntity<PartnerStatusResponse> approvePartner(String id) {
        final var output = HttpResults.require(approvePartner.execute(id));
        return ResponseEntity.ok(new PartnerStatusResponse(output.id(), output.status()));
    }

    @Override
    public ResponseEntity<PartnerStatusResponse> rejectPartner(String id) {
        final var output = HttpResults.require(rejectPartner.execute(id));
        return ResponseEntity.ok(new PartnerStatusResponse(output.id(), output.status()));
    }

    @Override
    public ResponseEntity<Void> deleteById(String id) {
        HttpResults.requireEmpty(deletePartner.execute(id));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<PartnerResponse> getById(String id) {
        return ResponseEntity.ok(mapper.toResponse(HttpResults.require(getPartner.execute(id))));
    }

    @Override
    public ResponseEntity<Pagination<PartnerListResponse>> list(
            String search, int page, int perPage, String sort, String direction) {
        final var result = HttpResults.require(
                        listPartners.execute(HttpResults.search(search, page, perPage, sort, direction)))
                .map(mapper::toListResponse);
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<IdResponse> updatePartner(String id, UpdatePartnerRequest input) {
        final var output = HttpResults.require(updatePartner.execute(mapper.toCommand(id, input)));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }
}
