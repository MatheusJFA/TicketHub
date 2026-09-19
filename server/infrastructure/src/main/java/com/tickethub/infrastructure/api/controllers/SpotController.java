package com.tickethub.infrastructure.api.controllers;

import com.tickethub.infrastructure.api.models.*;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.application.spot.changelocation.*;
import com.tickethub.application.spot.create.*;
import com.tickethub.application.spot.delete.*;
import com.tickethub.application.spot.publish.*;
import com.tickethub.application.spot.retrieve.get.*;
import com.tickethub.application.spot.retrieve.list.*;
import com.tickethub.application.spot.unpublish.*;
import com.tickethub.application.spot.update.*;
import com.tickethub.infrastructure.spot.models.*;
import com.tickethub.infrastructure.spot.presenters.SpotMapper;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import com.tickethub.infrastructure.api.SpotAPI;
import com.tickethub.infrastructure.api.HttpResults;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SpotController implements SpotAPI {
    private final ChangeSpotLocationUseCase changeSpotLocation;
    private final CreateSpotUseCase createSpot;
    private final DeleteSpotUseCase deleteSpot;
    private final PublishSpotUseCase publishSpot;
    private final GetSpotUseCase getSpot;
    private final ListSpotsUseCase listSpots;
    private final UnpublishSpotUseCase unpublishSpot;
    private final UpdateSpotUseCase updateSpot;
    private final SpotMapper mapper;

    public SpotController(ChangeSpotLocationUseCase changeSpotLocation,
            CreateSpotUseCase createSpot,
            DeleteSpotUseCase deleteSpot,
            PublishSpotUseCase publishSpot,
            GetSpotUseCase getSpot,
            ListSpotsUseCase listSpots,
            UnpublishSpotUseCase unpublishSpot,
            UpdateSpotUseCase updateSpot,
            SpotMapper mapper) {
        this.changeSpotLocation = changeSpotLocation;
        this.createSpot = createSpot;
        this.deleteSpot = deleteSpot;
        this.publishSpot = publishSpot;
        this.getSpot = getSpot;
        this.listSpots = listSpots;
        this.unpublishSpot = unpublishSpot;
        this.updateSpot = updateSpot;
        this.mapper = mapper;
    }

    @Override
    public ResponseEntity<IdResponse> changeSpotLocation(String id, ChangeSpotLocationRequest input) {
        final var output = HttpResults.require(changeSpotLocation.execute(mapper.toCommand(id, input)));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }

    @Override
    public ResponseEntity<IdResponse> createSpot(CreateSpotRequest input) {
        final var output = HttpResults.require(createSpot.execute(mapper.toCommand(input)));
        return ResponseEntity.created(URI.create("/spots/" + output.id())).body(new IdResponse(output.id()));
    }

    @Override
    public ResponseEntity<Void> deleteById(String id) {
        HttpResults.requireEmpty(deleteSpot.execute(id));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<IdResponse> publishSpot(String id) {
        final var output = HttpResults.require(publishSpot.execute(new PublishSpotCommand(id)));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }

    @Override
    public SpotResponse getById(String id) {
        return mapper.toResponse(HttpResults.require(getSpot.execute(id)));
    }

    @Override
    public Pagination<SpotListResponse> list(String search, int page, int perPage, String sort, String direction) {
        return HttpResults.require(listSpots.execute(HttpResults.search(search, page, perPage, sort, direction))).map(mapper::toListResponse);
    }

    @Override
    public ResponseEntity<IdResponse> unpublishSpot(String id) {
        final var output = HttpResults.require(unpublishSpot.execute(new UnpublishSpotCommand(id)));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }

    @Override
    public ResponseEntity<IdResponse> updateSpot(String id, UpdateSpotRequest input) {
        final var output = HttpResults.require(updateSpot.execute(mapper.toCommand(id, input)));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }
}
