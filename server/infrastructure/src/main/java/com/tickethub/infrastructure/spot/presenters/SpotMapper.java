package com.tickethub.infrastructure.spot.presenters;

import org.mapstruct.Mapper;

import com.tickethub.infrastructure.shared.presenters.SharedMapper;

import com.tickethub.application.spot.changelocation.ChangeSpotLocationCommand;
import com.tickethub.application.spot.create.CreateSpotCommand;
import com.tickethub.application.spot.retrieve.get.GetSpotOutput;
import com.tickethub.application.spot.retrieve.list.ListSpotsOutput;
import com.tickethub.application.spot.update.UpdateSpotCommand;
import com.tickethub.infrastructure.spot.models.ChangeSpotLocationRequest;
import com.tickethub.infrastructure.spot.models.CreateSpotRequest;
import com.tickethub.infrastructure.spot.models.SpotListResponse;
import com.tickethub.infrastructure.spot.models.SpotResponse;
import com.tickethub.infrastructure.spot.models.UpdateSpotRequest;

@Mapper(componentModel = "spring", uses = SharedMapper.class)
public interface SpotMapper {

    SpotResponse toResponse(GetSpotOutput output);

    SpotListResponse toListResponse(ListSpotsOutput output);

    CreateSpotCommand toCommand(CreateSpotRequest request);

    ChangeSpotLocationCommand toCommand(String id, ChangeSpotLocationRequest request);

    UpdateSpotCommand toCommand(String id, UpdateSpotRequest request);
}
