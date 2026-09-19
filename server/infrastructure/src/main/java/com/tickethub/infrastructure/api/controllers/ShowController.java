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
import com.tickethub.application.show.update.*;
import com.tickethub.infrastructure.show.models.*;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import com.tickethub.infrastructure.api.ShowAPI;
import com.tickethub.infrastructure.api.HttpResults;
import com.tickethub.infrastructure.show.presenters.ShowMapper;
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
    private final UpdateShowUseCase updateShow;
    private final ShowMapper mapper;

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
            UnpublishAllShowUseCase unpublishAllShow,
            UpdateShowUseCase updateShow,
            ShowMapper mapper) {
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
        this.updateShow = updateShow;
        this.mapper = mapper;
    }

    @Override
    public ResponseEntity<IdResponse> addSectionToShow(String id, AddSectionToShowRequest input) {
        final var output = HttpResults.require(addSectionToShow.execute(mapper.toCommand(id, input)));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }

    @Override
    public ResponseEntity<IdResponse> changeShowDescription(String id, ChangeShowDescriptionRequest input) {
        final var output = HttpResults.require(changeShowDescription.execute(mapper.toCommand(id, input)));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }

    @Override
    public ResponseEntity<IdResponse> changeShowName(String id, ChangeShowNameRequest input) {
        final var output = HttpResults.require(changeShowName.execute(mapper.toCommand(id, input)));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }

    @Override
    public ResponseEntity<IdResponse> createShow(CreateShowRequest input) {
        final var output = HttpResults.require(createShow.execute(mapper.toCommand(input)));
        return ResponseEntity.created(URI.create("/shows/" + output.id())).body(new IdResponse(output.id()));
    }

    @Override
    public ResponseEntity<Void> deleteById(String id) {
        HttpResults.requireEmpty(deleteShow.execute(id));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<IdResponse> publishShow(String id) {
        final var output = HttpResults.require(publishShow.execute(new PublishShowCommand(id)));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }

    @Override
    public ResponseEntity<IdResponse> publishAllShow(String id) {
        final var output = HttpResults.require(publishAllShow.execute(new PublishAllShowCommand(id)));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }

    @Override
    public ResponseEntity<IdResponse> rescheduleShow(String id, RescheduleShowRequest input) {
        final var output = HttpResults.require(rescheduleShow.execute(mapper.toCommand(id, input)));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }

    @Override
    public ShowResponse getById(String id) {
        return mapper.toResponse(HttpResults.require(getShow.execute(id)));
    }

    @Override
    public Pagination<ShowListResponse> list(String search, int page, int perPage, String sort, String direction) {
        return HttpResults.require(listShows.execute(HttpResults.search(search, page, perPage, sort, direction))).map(mapper::toListResponse);
    }

    @Override
    public ResponseEntity<IdResponse> unpublishShow(String id) {
        final var output = HttpResults.require(unpublishShow.execute(new UnpublishShowCommand(id)));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }

    @Override
    public ResponseEntity<IdResponse> unpublishAllShow(String id) {
        final var output = HttpResults.require(unpublishAllShow.execute(new UnpublishAllShowCommand(id)));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }

    @Override
    public ResponseEntity<IdResponse> updateShow(String id, UpdateShowRequest input) {
        final var output = HttpResults.require(updateShow.execute(mapper.toCommand(id, input)));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }
}
