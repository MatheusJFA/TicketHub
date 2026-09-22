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
import com.tickethub.application.section.update.*;
import com.tickethub.infrastructure.section.models.*;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import com.tickethub.infrastructure.api.SectionAPI;
import com.tickethub.infrastructure.api.HttpResults;
import com.tickethub.infrastructure.section.presenters.SectionMapper;
import com.tickethub.infrastructure.cache.TickethubCacheProperties;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
    private final UpdateSectionUseCase updateSection;
    private final SectionMapper mapper;

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
            UnpublishAllSectionUseCase unpublishAllSection,
            UpdateSectionUseCase updateSection,
            SectionMapper mapper) {
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
        this.updateSection = updateSection;
        this.mapper = mapper;
    }

    @Override
    @CacheEvict(value = TickethubCacheProperties.SECTIONS, allEntries = true)
    public ResponseEntity<IdResponse> changeSectionDescription(String id, ChangeSectionDescriptionRequest input) {
        final var output = HttpResults.require(changeSectionDescription.execute(mapper.toCommand(id, input)));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }

    @Override
    @CacheEvict(value = TickethubCacheProperties.SECTIONS, allEntries = true)
    public ResponseEntity<IdResponse> changeSectionName(String id, ChangeSectionNameRequest input) {
        final var output = HttpResults.require(changeSectionName.execute(mapper.toCommand(id, input)));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }

    @Override
    @CacheEvict(value = TickethubCacheProperties.SECTIONS, allEntries = true)
    public ResponseEntity<IdResponse> changeSectionPrice(String id, ChangeSectionPriceRequest input) {
        final var output = HttpResults.require(changeSectionPrice.execute(mapper.toCommand(id, input)));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }

    @Override
    @CacheEvict(value = TickethubCacheProperties.SECTIONS, allEntries = true)
    public ResponseEntity<IdResponse> createSection(CreateSectionRequest input) {
        final var output = HttpResults.require(createSection.execute(mapper.toCommand(input)));
        return ResponseEntity.created(URI.create("/sections/" + output.id())).body(new IdResponse(output.id()));
    }

    @Override
    @CacheEvict(value = TickethubCacheProperties.SECTIONS, allEntries = true)
    public ResponseEntity<Void> deleteById(String id) {
        HttpResults.requireEmpty(deleteSection.execute(id));
        return ResponseEntity.noContent().build();
    }

    @Override
    @CacheEvict(value = TickethubCacheProperties.SECTIONS, allEntries = true)
    public ResponseEntity<IdResponse> publishSection(String id) {
        final var output = HttpResults.require(publishSection.execute(new PublishSectionCommand(id)));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }

    @Override
    @CacheEvict(value = TickethubCacheProperties.SECTIONS, allEntries = true)
    public ResponseEntity<IdResponse> publishAllSection(String id) {
        final var output = HttpResults.require(publishAllSection.execute(new PublishAllSectionCommand(id)));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }

    @Override
    @Cacheable(TickethubCacheProperties.SECTIONS)
    public SectionResponse getById(String id) {
        return mapper.toResponse(HttpResults.require(getSection.execute(id)));
    }

    @Override
    @Cacheable(TickethubCacheProperties.SECTIONS)
    public Pagination<SectionListResponse> list(String search, int page, int perPage, String sort, String direction) {
        return HttpResults.require(listSections.execute(HttpResults.search(search, page, perPage, sort, direction))).map(mapper::toListResponse);
    }

    @Override
    @CacheEvict(value = TickethubCacheProperties.SECTIONS, allEntries = true)
    public ResponseEntity<IdResponse> unpublishSection(String id) {
        final var output = HttpResults.require(unpublishSection.execute(new UnpublishSectionCommand(id)));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }

    @Override
    @CacheEvict(value = TickethubCacheProperties.SECTIONS, allEntries = true)
    public ResponseEntity<IdResponse> unpublishAllSection(String id) {
        final var output = HttpResults.require(unpublishAllSection.execute(new UnpublishAllSectionCommand(id)));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }

    @Override
    @CacheEvict(value = TickethubCacheProperties.SECTIONS, allEntries = true)
    public ResponseEntity<IdResponse> updateSection(String id, UpdateSectionRequest input) {
        final var output = HttpResults.require(updateSection.execute(mapper.toCommand(id, input)));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }
}
