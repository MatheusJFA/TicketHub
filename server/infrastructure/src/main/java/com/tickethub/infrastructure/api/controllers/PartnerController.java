package com.tickethub.infrastructure.api.controllers;

import com.tickethub.infrastructure.api.models.*;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.domain.pagination.SearchQuery;
import com.tickethub.domain.validation.Notification;
import com.tickethub.application.partner.changeaddress.*;
import com.tickethub.application.partner.changename.*;
import com.tickethub.application.partner.create.*;
import com.tickethub.application.partner.delete.*;
import com.tickethub.application.partner.retrieve.get.*;
import com.tickethub.application.partner.retrieve.list.*;
import com.tickethub.infrastructure.partner.models.*;
import org.springframework.http.ResponseEntity;
import com.tickethub.infrastructure.api.PartnerAPI;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.function.Function;

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
        final Function<Notification, ResponseEntity<?>> onError = notification ->
                ResponseEntity.unprocessableEntity().body(notification);
        final Function<ChangePartnerAddressOutput, ResponseEntity<?>> onSuccess = output ->
                ResponseEntity.ok(new IdResponse(output.id()));
        final var command = new ChangePartnerAddressCommand(
                id,
                input.address() == null ? null : input.address().toDomain()
        );
        return changePartnerAddress.execute(command)
                .fold(onError, onSuccess);
    }

    @Override
    public ResponseEntity<?> changePartnerName(String id, ChangePartnerNameRequest input) {
        final Function<Notification, ResponseEntity<?>> onError = notification ->
                ResponseEntity.unprocessableEntity().body(notification);
        final Function<ChangePartnerNameOutput, ResponseEntity<?>> onSuccess = output ->
                ResponseEntity.ok(new IdResponse(output.id()));
        final var command = new ChangePartnerNameCommand(
                id,
                input.name()
        );
        return changePartnerName.execute(command).fold(onError, onSuccess);
    }

    @Override
    public ResponseEntity<?> createPartner(CreatePartnerRequest input) {
        final Function<Notification, ResponseEntity<?>> onError = notification ->
                ResponseEntity.unprocessableEntity().body(notification);
        final Function<CreatePartnerOutput, ResponseEntity<?>> onSuccess = output ->
                ResponseEntity.created(URI.create("/partners/" + output.id())).body(new IdResponse(output.id()));
        final var command = new CreatePartnerCommand(
                input.name(),
                input.cnpj(),
                input.address() == null ? null : input.address().toDomain()
        );
        return createPartner.execute(command)
                .fold(onError, onSuccess);
    }

    @Override
    public ResponseEntity<?> deleteById(String id) {
        return deletePartner.execute(id).<ResponseEntity<?>>fold(
                notification -> ResponseEntity.unprocessableEntity().body(notification),
                output -> ResponseEntity.noContent().build()
        );
    }

    @Override
    public ResponseEntity<?> getById(String id) {
        return getPartner.execute(id).<ResponseEntity<?>>fold(
                notification -> ResponseEntity.unprocessableEntity().body(notification),
                output -> ResponseEntity.ok(PartnerResponse.from(output))
        );
    }

    @Override
    public ResponseEntity<?> list(String search, int page, int perPage, String sort, String direction) {
        return listPartners.execute(new SearchQuery(page, perPage, search, sort, direction)).<ResponseEntity<?>>fold(
                notification -> ResponseEntity.unprocessableEntity().body(notification),
                output -> ResponseEntity.ok(output.map(PartnerListResponse::from))
        );
    }
}
