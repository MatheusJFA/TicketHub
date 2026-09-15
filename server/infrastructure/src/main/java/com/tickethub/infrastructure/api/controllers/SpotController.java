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
import com.tickethub.infrastructure.spot.models.*;
import org.springframework.http.ResponseEntity;
import com.tickethub.infrastructure.api.SpotAPI;
import com.tickethub.infrastructure.api.ApiSupport;
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

    public SpotController(ChangeSpotLocationUseCase changeSpotLocation,
            CreateSpotUseCase createSpot,
            DeleteSpotUseCase deleteSpot,
            PublishSpotUseCase publishSpot,
            GetSpotUseCase getSpot,
            ListSpotsUseCase listSpots,
            UnpublishSpotUseCase unpublishSpot) {
        this.changeSpotLocation = changeSpotLocation;
        this.createSpot = createSpot;
        this.deleteSpot = deleteSpot;
        this.publishSpot = publishSpot;
        this.getSpot = getSpot;
        this.listSpots = listSpots;
        this.unpublishSpot = unpublishSpot;
    }

    @Override
    public ResponseEntity<IdResponse> changeSpotLocation(String id, ChangeSpotLocationRequest input) {
        final var output = ApiSupport.execute(changeSpotLocation, new ChangeSpotLocationCommand(id, input.location() == null ? null : com.tickethub.domain.shared.Location.create(input.location())));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }

    @Override
    public ResponseEntity<IdResponse> createSpot(CreateSpotRequest input) {
        final var output = ApiSupport.execute(createSpot, new CreateSpotCommand(input.location() == null ? null : com.tickethub.domain.shared.Location.create(input.location())));
        return ResponseEntity.created(java.net.URI.create("/spots/" + output.id())).body(new IdResponse(output.id()));
    }

    @Override
    public ResponseEntity<Void> deleteById(String id) {
        ApiSupport.execute(deleteSpot, id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<IdResponse> publishSpot(String id) {
        final var output = ApiSupport.execute(publishSpot, new PublishSpotCommand(id));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }

    @Override
    public SpotResponse getById(String id) {
        return SpotResponse.from(ApiSupport.execute(getSpot, id));
    }

    @Override
    public Pagination<SpotListResponse> list(String search, int page, int perPage, String sort, String direction) {
        return ApiSupport.execute(listSpots, ApiSupport.query(search, page, perPage, sort, direction)).map(SpotListResponse::from);
    }

    @Override
    public ResponseEntity<IdResponse> unpublishSpot(String id) {
        final var output = ApiSupport.execute(unpublishSpot, new UnpublishSpotCommand(id));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }
}
