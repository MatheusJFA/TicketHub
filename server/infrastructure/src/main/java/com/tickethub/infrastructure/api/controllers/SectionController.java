package com.tickethub.infrastructure.api.controllers;

import com.tickethub.infrastructure.api.models.*;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.application.section.changedescription.*;
import com.tickethub.application.section.changename.*;
import com.tickethub.application.section.changeprice.*;
import com.tickethub.application.section.create.*;
import com.tickethub.application.section.delete.*;
import com.tickethub.application.section.publish.*;
import com.tickethub.application.section.publishall.*;
import com.tickethub.application.section.retrieve.get.*;
import com.tickethub.application.section.retrieve.list.*;
import com.tickethub.application.section.unpublish.*;
import com.tickethub.application.section.unpublishall.*;
import com.tickethub.infrastructure.section.models.*;
import org.springframework.http.ResponseEntity;
import com.tickethub.infrastructure.api.SectionAPI;
import com.tickethub.infrastructure.api.ApiSupport;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SectionController implements SectionAPI {
    private final ChangeSectionDescriptionUseCase changeSectionDescription;
    private final ChangeSectionNameUseCase changeSectionName;
    private final ChangeSectionPriceUseCase changeSectionPrice;
    private final CreateSectionUseCase createSection;
    private final DeleteSectionUseCase deleteSection;
    private final PublishSectionUseCase publishSection;
    private final PublishAllSectionUseCase publishAllSection;
    private final GetSectionUseCase getSection;
    private final ListSectionsUseCase listSections;
    private final UnpublishSectionUseCase unpublishSection;
    private final UnpublishAllSectionUseCase unpublishAllSection;

    public SectionController(ChangeSectionDescriptionUseCase changeSectionDescription,
            ChangeSectionNameUseCase changeSectionName,
            ChangeSectionPriceUseCase changeSectionPrice,
            CreateSectionUseCase createSection,
            DeleteSectionUseCase deleteSection,
            PublishSectionUseCase publishSection,
            PublishAllSectionUseCase publishAllSection,
            GetSectionUseCase getSection,
            ListSectionsUseCase listSections,
            UnpublishSectionUseCase unpublishSection,
            UnpublishAllSectionUseCase unpublishAllSection) {
        this.changeSectionDescription = changeSectionDescription;
        this.changeSectionName = changeSectionName;
        this.changeSectionPrice = changeSectionPrice;
        this.createSection = createSection;
        this.deleteSection = deleteSection;
        this.publishSection = publishSection;
        this.publishAllSection = publishAllSection;
        this.getSection = getSection;
        this.listSections = listSections;
        this.unpublishSection = unpublishSection;
        this.unpublishAllSection = unpublishAllSection;
    }

    @Override
    public ResponseEntity<IdResponse> changeSectionDescription(String id, ChangeSectionDescriptionRequest input) {
        final var output = ApiSupport.execute(changeSectionDescription, new ChangeSectionDescriptionCommand(id, input.description()));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }

    @Override
    public ResponseEntity<IdResponse> changeSectionName(String id, ChangeSectionNameRequest input) {
        final var output = ApiSupport.execute(changeSectionName, new ChangeSectionNameCommand(id, input.name()));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }

    @Override
    public ResponseEntity<IdResponse> changeSectionPrice(String id, ChangeSectionPriceRequest input) {
        final var output = ApiSupport.execute(changeSectionPrice, new ChangeSectionPriceCommand(id, input.price() == null ? null : input.price().toDomain()));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }

    @Override
    public ResponseEntity<IdResponse> createSection(CreateSectionRequest input) {
        final var output = ApiSupport.execute(createSection, new CreateSectionCommand(input.name(), input.description(), input.totalSpots(), input.price() == null ? null : input.price().toDomain()));
        return ResponseEntity.created(java.net.URI.create("/sections/" + output.id())).body(new IdResponse(output.id()));
    }

    @Override
    public ResponseEntity<Void> deleteById(String id) {
        ApiSupport.execute(deleteSection, id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<IdResponse> publishSection(String id) {
        final var output = ApiSupport.execute(publishSection, new PublishSectionCommand(id));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }

    @Override
    public ResponseEntity<IdResponse> publishAllSection(String id) {
        final var output = ApiSupport.execute(publishAllSection, new PublishAllSectionCommand(id));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }

    @Override
    public SectionResponse getById(String id) {
        return SectionResponse.from(ApiSupport.execute(getSection, id));
    }

    @Override
    public Pagination<SectionListResponse> list(String search, int page, int perPage, String sort, String direction) {
        return ApiSupport.execute(listSections, ApiSupport.query(search, page, perPage, sort, direction)).map(SectionListResponse::from);
    }

    @Override
    public ResponseEntity<IdResponse> unpublishSection(String id) {
        final var output = ApiSupport.execute(unpublishSection, new UnpublishSectionCommand(id));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }

    @Override
    public ResponseEntity<IdResponse> unpublishAllSection(String id) {
        final var output = ApiSupport.execute(unpublishAllSection, new UnpublishAllSectionCommand(id));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }
}
