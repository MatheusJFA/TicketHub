package com.tickethub.infrastructure.section.presenters;

import org.mapstruct.Mapper;

import com.tickethub.infrastructure.shared.presenters.SharedMapper;

import com.tickethub.application.section.changedescription.ChangeSectionDescriptionCommand;
import com.tickethub.application.section.changename.ChangeSectionNameCommand;
import com.tickethub.application.section.changeprice.ChangeSectionPriceCommand;
import com.tickethub.application.section.create.CreateSectionCommand;
import com.tickethub.application.section.retrieve.get.GetSectionOutput;
import com.tickethub.application.section.retrieve.list.ListSectionsOutput;
import com.tickethub.application.section.update.UpdateSectionCommand;
import com.tickethub.infrastructure.section.models.ChangeSectionDescriptionRequest;
import com.tickethub.infrastructure.section.models.ChangeSectionNameRequest;
import com.tickethub.infrastructure.section.models.ChangeSectionPriceRequest;
import com.tickethub.infrastructure.section.models.CreateSectionRequest;
import com.tickethub.infrastructure.section.models.SectionListResponse;
import com.tickethub.infrastructure.section.models.SectionResponse;
import com.tickethub.infrastructure.section.models.UpdateSectionRequest;

@Mapper(componentModel = "spring", uses = SharedMapper.class)
public interface SectionMapper {

    SectionResponse toResponse(GetSectionOutput output);

    SectionListResponse toListResponse(ListSectionsOutput output);

    CreateSectionCommand toCommand(CreateSectionRequest request);

    ChangeSectionNameCommand toCommand(String id, ChangeSectionNameRequest request);

    ChangeSectionDescriptionCommand toCommand(String id, ChangeSectionDescriptionRequest request);

    ChangeSectionPriceCommand toCommand(String id, ChangeSectionPriceRequest request);

    UpdateSectionCommand toCommand(String id, UpdateSectionRequest request);
}
