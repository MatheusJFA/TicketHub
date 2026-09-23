package com.tickethub.infrastructure.api.controllers;

import com.tickethub.infrastructure.api.models.IdResponse;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.application.show.addsection.AddSectionToShowUseCase;
import com.tickethub.application.show.changedescription.ChangeShowDescriptionUseCase;
import com.tickethub.application.show.changename.ChangeShowNameUseCase;
import com.tickethub.application.show.create.CreateShowUseCase;
import com.tickethub.application.show.delete.DeleteShowUseCase;
import com.tickethub.application.show.publish.PublishShowCommand;
import com.tickethub.application.show.publish.PublishShowUseCase;
import com.tickethub.application.show.publishall.PublishAllShowCommand;
import com.tickethub.application.show.publishall.PublishAllShowUseCase;
import com.tickethub.application.show.reschedule.RescheduleShowUseCase;
import com.tickethub.application.show.retrieve.get.GetShowUseCase;
import com.tickethub.application.show.retrieve.list.ListShowsUseCase;
import com.tickethub.application.section.retrieve.byshow.ListShowSectionsCommand;
import com.tickethub.application.section.retrieve.byshow.ListShowSectionsUseCase;
import com.tickethub.application.show.unpublish.UnpublishShowCommand;
import com.tickethub.application.show.unpublish.UnpublishShowUseCase;
import com.tickethub.application.show.unpublishall.UnpublishAllShowCommand;
import com.tickethub.application.show.unpublishall.UnpublishAllShowUseCase;
import com.tickethub.application.show.update.UpdateShowUseCase;
import com.tickethub.infrastructure.show.models.AddSectionToShowRequest;
import com.tickethub.infrastructure.show.models.ChangeShowDescriptionRequest;
import com.tickethub.infrastructure.show.models.ChangeShowNameRequest;
import com.tickethub.infrastructure.show.models.CreateShowRequest;
import com.tickethub.infrastructure.show.models.RescheduleShowRequest;
import com.tickethub.infrastructure.show.models.ShowListResponse;
import com.tickethub.infrastructure.show.models.ShowResponse;
import com.tickethub.infrastructure.show.models.UpdateShowRequest;
import com.tickethub.infrastructure.section.models.SectionListResponse;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import com.tickethub.infrastructure.api.ShowAPI;
import com.tickethub.infrastructure.api.HttpResults;
import com.tickethub.infrastructure.show.presenters.ShowMapper;
import com.tickethub.infrastructure.section.presenters.SectionMapper;
import com.tickethub.infrastructure.cache.TickethubCacheProperties;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
    private final ListShowSectionsUseCase listShowSections;
    private final UnpublishShowUseCase unpublishShow;
    private final UnpublishAllShowUseCase unpublishAllShow;
    private final UpdateShowUseCase updateShow;
    private final ShowMapper mapper;
    private final SectionMapper sectionMapper;

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
            ListShowSectionsUseCase listShowSections,
            UnpublishShowUseCase unpublishShow,
            UnpublishAllShowUseCase unpublishAllShow,
            UpdateShowUseCase updateShow,
            ShowMapper mapper,
            SectionMapper sectionMapper) {
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
        this.listShowSections = listShowSections;
        this.unpublishShow = unpublishShow;
        this.unpublishAllShow = unpublishAllShow;
        this.updateShow = updateShow;
        this.mapper = mapper;
        this.sectionMapper = sectionMapper;
    }

    @Override
    @CacheEvict(value = TickethubCacheProperties.SHOWS, allEntries = true)
    public ResponseEntity<IdResponse> addSectionToShow(String id, AddSectionToShowRequest input) {
        final var output = HttpResults.require(addSectionToShow.execute(mapper.toCommand(id, input)));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }

    @Override
    @CacheEvict(value = TickethubCacheProperties.SHOWS, allEntries = true)
    public ResponseEntity<IdResponse> changeShowDescription(String id, ChangeShowDescriptionRequest input) {
        final var output = HttpResults.require(changeShowDescription.execute(mapper.toCommand(id, input)));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }

    @Override
    @CacheEvict(value = TickethubCacheProperties.SHOWS, allEntries = true)
    public ResponseEntity<IdResponse> changeShowName(String id, ChangeShowNameRequest input) {
        final var output = HttpResults.require(changeShowName.execute(mapper.toCommand(id, input)));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }

    @Override
    @CacheEvict(value = TickethubCacheProperties.SHOWS, allEntries = true)
    public ResponseEntity<IdResponse> createShow(CreateShowRequest input) {
        final var output = HttpResults.require(createShow.execute(mapper.toCommand(input)));
        return ResponseEntity.created(URI.create("/shows/" + output.id())).body(new IdResponse(output.id()));
    }

    @Override
    @CacheEvict(value = TickethubCacheProperties.SHOWS, allEntries = true)
    public ResponseEntity<Void> deleteById(String id) {
        HttpResults.requireEmpty(deleteShow.execute(id));
        return ResponseEntity.noContent().build();
    }

    @Override
    @CacheEvict(value = TickethubCacheProperties.SHOWS, allEntries = true)
    public ResponseEntity<IdResponse> publishShow(String id) {
        final var output = HttpResults.require(publishShow.execute(new PublishShowCommand(id)));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }

    @Override
    @CacheEvict(value = TickethubCacheProperties.SHOWS, allEntries = true)
    public ResponseEntity<IdResponse> publishAllShow(String id) {
        final var output = HttpResults.require(publishAllShow.execute(new PublishAllShowCommand(id)));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }

    @Override
    @CacheEvict(value = TickethubCacheProperties.SHOWS, allEntries = true)
    public ResponseEntity<IdResponse> rescheduleShow(String id, RescheduleShowRequest input) {
        final var output = HttpResults.require(rescheduleShow.execute(mapper.toCommand(id, input)));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }

    @Override
    @Cacheable(TickethubCacheProperties.SHOWS)
    public ShowResponse getById(String id) {
        return mapper.toResponse(HttpResults.require(getShow.execute(id)));
    }

    @Override
    @Cacheable(TickethubCacheProperties.SHOWS)
    public Pagination<ShowListResponse> list(String search, int page, int perPage, String sort, String direction) {
        return HttpResults.require(listShows.execute(HttpResults.search(search, page, perPage, sort, direction))).map(mapper::toListResponse);
    }

    @Override
    @Cacheable(TickethubCacheProperties.SECTIONS)
    public Pagination<SectionListResponse> listSections(String id, String search, int page, int perPage,
            String sort, String direction) {
        return HttpResults.require(listShowSections.execute(ListShowSectionsCommand.with(id,
                HttpResults.search(search, page, perPage, sort, direction))))
                .map(sectionMapper::toListResponse);
    }

    @Override
    @CacheEvict(value = TickethubCacheProperties.SHOWS, allEntries = true)
    public ResponseEntity<IdResponse> unpublishShow(String id) {
        final var output = HttpResults.require(unpublishShow.execute(new UnpublishShowCommand(id)));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }

    @Override
    @CacheEvict(value = TickethubCacheProperties.SHOWS, allEntries = true)
    public ResponseEntity<IdResponse> unpublishAllShow(String id) {
        final var output = HttpResults.require(unpublishAllShow.execute(new UnpublishAllShowCommand(id)));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }

    @Override
    @CacheEvict(value = TickethubCacheProperties.SHOWS, allEntries = true)
    public ResponseEntity<IdResponse> updateShow(String id, UpdateShowRequest input) {
        final var output = HttpResults.require(updateShow.execute(mapper.toCommand(id, input)));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }
}
