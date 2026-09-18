package com.tickethub.infrastructure.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.tickethub.application.show.addsection.AddSectionToShowCommand;
import com.tickethub.application.show.changedescription.ChangeShowDescriptionCommand;
import com.tickethub.application.show.changename.ChangeShowNameCommand;
import com.tickethub.application.show.create.CreateShowCommand;
import com.tickethub.application.show.reschedule.RescheduleShowCommand;
import com.tickethub.application.show.retrieve.get.GetShowOutput;
import com.tickethub.application.show.retrieve.list.ListShowsOutput;
import com.tickethub.infrastructure.show.models.AddSectionToShowRequest;
import com.tickethub.infrastructure.show.models.ChangeShowDescriptionRequest;
import com.tickethub.infrastructure.show.models.ChangeShowNameRequest;
import com.tickethub.infrastructure.show.models.CreateShowRequest;
import com.tickethub.infrastructure.show.models.RescheduleShowRequest;
import com.tickethub.infrastructure.show.models.ShowListResponse;
import com.tickethub.infrastructure.show.models.ShowResponse;

@Mapper(componentModel = "spring", uses = SharedMapper.class)
public interface ShowMapper {

    ShowResponse toResponse(GetShowOutput output);

    ShowListResponse toListResponse(ListShowsOutput output);

    CreateShowCommand toCommand(CreateShowRequest request);

    @Mapping(target = "showId", source = "id")
    AddSectionToShowCommand toCommand(String id, AddSectionToShowRequest request);

    ChangeShowNameCommand toCommand(String id, ChangeShowNameRequest request);

    ChangeShowDescriptionCommand toCommand(String id, ChangeShowDescriptionRequest request);

    RescheduleShowCommand toCommand(String id, RescheduleShowRequest request);
}
