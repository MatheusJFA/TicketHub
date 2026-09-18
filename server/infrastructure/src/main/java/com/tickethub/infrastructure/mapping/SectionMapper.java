package com.tickethub.infrastructure.mapping;

import org.mapstruct.Mapper;

import com.tickethub.application.section.changedescription.ChangeSectionDescriptionCommand;
import com.tickethub.application.section.changename.ChangeSectionNameCommand;
import com.tickethub.application.section.changeprice.ChangeSectionPriceCommand;
import com.tickethub.application.section.create.CreateSectionCommand;
import com.tickethub.application.section.retrieve.get.GetSectionOutput;
import com.tickethub.application.section.retrieve.list.ListSectionsOutput;
import com.tickethub.infrastructure.section.models.ChangeSectionDescriptionRequest;
import com.tickethub.infrastructure.section.models.ChangeSectionNameRequest;
import com.tickethub.infrastructure.section.models.ChangeSectionPriceRequest;
import com.tickethub.infrastructure.section.models.CreateSectionRequest;
import com.tickethub.infrastructure.section.models.SectionListResponse;
import com.tickethub.infrastructure.section.models.SectionResponse;

@Mapper(componentModel = "spring", uses = SharedMapper.class)
public interface SectionMapper {

    SectionResponse toResponse(GetSectionOutput output);

    SectionListResponse toListResponse(ListSectionsOutput output);

    CreateSectionCommand toCommand(CreateSectionRequest request);

    ChangeSectionNameCommand toCommand(String id, ChangeSectionNameRequest request);

    ChangeSectionDescriptionCommand toCommand(String id, ChangeSectionDescriptionRequest request);

    ChangeSectionPriceCommand toCommand(String id, ChangeSectionPriceRequest request);
}
