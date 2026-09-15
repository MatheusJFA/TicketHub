package com.tickethub.infrastructure.api.controllers;

import com.tickethub.infrastructure.api.models.*;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.application.show.addsection.*;
import com.tickethub.application.show.changedescription.*;
import com.tickethub.application.show.changename.*;
import com.tickethub.application.show.create.*;
import com.tickethub.application.show.delete.*;
import com.tickethub.application.show.publish.*;
import com.tickethub.application.show.publishall.*;
import com.tickethub.application.show.reschedule.*;
import com.tickethub.application.show.retrieve.get.*;
import com.tickethub.application.show.retrieve.list.*;
import com.tickethub.application.show.unpublish.*;
import com.tickethub.application.show.unpublishall.*;
import com.tickethub.infrastructure.show.models.*;
import org.springframework.http.ResponseEntity;
import com.tickethub.infrastructure.api.ShowAPI;
import com.tickethub.infrastructure.api.ApiSupport;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ShowController implements ShowAPI {
    private final AddSectionToShowUseCase addSectionToShow;
    private final ChangeShowDescriptionUseCase changeShowDescription;
    private final ChangeShowNameUseCase changeShowName;
    private final CreateShowUseCase createShow;
    private final DeleteShowUseCase deleteShow;
    private final PublishShowUseCase publishShow;
    private final PublishAllShowUseCase publishAllShow;
    private final RescheduleShowUseCase rescheduleShow;
    private final GetShowUseCase getShow;
    private final ListShowsUseCase listShows;
    private final UnpublishShowUseCase unpublishShow;
    private final UnpublishAllShowUseCase unpublishAllShow;

    public ShowController(AddSectionToShowUseCase addSectionToShow,
            ChangeShowDescriptionUseCase changeShowDescription,
            ChangeShowNameUseCase changeShowName,
            CreateShowUseCase createShow,
            DeleteShowUseCase deleteShow,
            PublishShowUseCase publishShow,
            PublishAllShowUseCase publishAllShow,
            RescheduleShowUseCase rescheduleShow,
            GetShowUseCase getShow,
            ListShowsUseCase listShows,
            UnpublishShowUseCase unpublishShow,
            UnpublishAllShowUseCase unpublishAllShow) {
        this.addSectionToShow = addSectionToShow;
        this.changeShowDescription = changeShowDescription;
        this.changeShowName = changeShowName;
        this.createShow = createShow;
        this.deleteShow = deleteShow;
        this.publishShow = publishShow;
        this.publishAllShow = publishAllShow;
        this.rescheduleShow = rescheduleShow;
        this.getShow = getShow;
        this.listShows = listShows;
        this.unpublishShow = unpublishShow;
        this.unpublishAllShow = unpublishAllShow;
    }

    @Override
    public ResponseEntity<IdResponse> addSectionToShow(String id, AddSectionToShowRequest input) {
        final var output = ApiSupport.execute(addSectionToShow, new AddSectionToShowCommand(id, input.name(), input.description(), input.totalSpots(), input.price() == null ? null : input.price().toDomain()));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }

    @Override
    public ResponseEntity<IdResponse> changeShowDescription(String id, ChangeShowDescriptionRequest input) {
        final var output = ApiSupport.execute(changeShowDescription, new ChangeShowDescriptionCommand(id, input.description()));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }

    @Override
    public ResponseEntity<IdResponse> changeShowName(String id, ChangeShowNameRequest input) {
        final var output = ApiSupport.execute(changeShowName, new ChangeShowNameCommand(id, input.name()));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }

    @Override
    public ResponseEntity<IdResponse> createShow(CreateShowRequest input) {
        final var output = ApiSupport.execute(createShow, new CreateShowCommand(input.partnerId(), input.name(), input.description(), input.date(), input.address() == null ? null : input.address().toDomain(), input.totalSpots()));
        return ResponseEntity.created(java.net.URI.create("/shows/" + output.id())).body(new IdResponse(output.id()));
    }

    @Override
    public ResponseEntity<Void> deleteById(String id) {
        ApiSupport.execute(deleteShow, id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<IdResponse> publishShow(String id) {
        final var output = ApiSupport.execute(publishShow, new PublishShowCommand(id));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }

    @Override
    public ResponseEntity<IdResponse> publishAllShow(String id) {
        final var output = ApiSupport.execute(publishAllShow, new PublishAllShowCommand(id));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }

    @Override
    public ResponseEntity<IdResponse> rescheduleShow(String id, RescheduleShowRequest input) {
        final var output = ApiSupport.execute(rescheduleShow, new RescheduleShowCommand(id, input.date()));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }

    @Override
    public ShowResponse getById(String id) {
        return ShowResponse.from(ApiSupport.execute(getShow, id));
    }

    @Override
    public Pagination<ShowListResponse> list(String search, int page, int perPage, String sort, String direction) {
        return ApiSupport.execute(listShows, ApiSupport.query(search, page, perPage, sort, direction)).map(ShowListResponse::from);
    }

    @Override
    public ResponseEntity<IdResponse> unpublishShow(String id) {
        final var output = ApiSupport.execute(unpublishShow, new UnpublishShowCommand(id));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }

    @Override
    public ResponseEntity<IdResponse> unpublishAllShow(String id) {
        final var output = ApiSupport.execute(unpublishAllShow, new UnpublishAllShowCommand(id));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }
}
