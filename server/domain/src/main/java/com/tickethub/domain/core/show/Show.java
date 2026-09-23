package com.tickethub.domain.core.show;

import static java.util.Objects.isNull;

import com.tickethub.domain.AggregateRoot;
import com.tickethub.domain.core.partner.PartnerID;
import com.tickethub.domain.core.section.Section;
import com.tickethub.domain.exception.DomainException;
import com.tickethub.domain.shared.Address;
import com.tickethub.domain.shared.Location;
import com.tickethub.domain.shared.Money;
import com.tickethub.domain.shared.Name;
import com.tickethub.domain.shared.Text;
import com.tickethub.domain.validation.ValidationHandler;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Show extends AggregateRoot<ShowID> {
    private Name name;
    private Text description;

    private OffsetDateTime date;
    private final Address address;

    private boolean isPublished;

    private long totalSpots;
    private long totalSpotsSold;

    private final PartnerID partnerId;

    private final Set<Section> sections;

    private Show(
            ShowID id,
            Name name,
            Text description,
            OffsetDateTime date,
            Address address,
            boolean isPublished,
            long totalSpots,
            long totalSpotsSold,
            PartnerID partnerId,
            Set<Section> sections,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt,
            String createdBy,
            String lastModifiedBy) {
        super(id, createdAt, updatedAt, deletedAt, createdBy, lastModifiedBy);
        this.name = name;
        this.description = description;
        this.date = date;
        this.address = address;
        this.isPublished = isPublished;
        this.totalSpots = totalSpots;
        this.totalSpotsSold = totalSpotsSold;
        this.partnerId = partnerId;
        this.sections = sections;
    }

    public static Show create(
            String name,
            String description,
            OffsetDateTime date,
            Address address,
            boolean isPublished,
            long totalSpots,
            long totalSpotsSold,
            PartnerID partnerId,
            Set<Section> sections) {
        final ShowID id = ShowID.generate();
        final Set<Section> sectionList = isNull(sections) ? new HashSet<>() : new HashSet<>(sections);
        final var now = Instant.now();
        return new Show(
                id,
                Name.create(name),
                Text.create(description),
                date,
                address,
                isPublished,
                totalSpots,
                totalSpotsSold,
                partnerId,
                sectionList,
                now,
                now,
                null,
                null,
                null);
    }

    public static Show create(
            String name,
            String description,
            OffsetDateTime date,
            Address address,
            long totalSpots,
            PartnerID partnerId,
            Set<Section> sections) {
        final ShowID id = ShowID.generate();
        final Set<Section> sectionList = isNull(sections) ? new HashSet<>() : new HashSet<>(sections);
        final var now = Instant.now();
        return new Show(
                id,
                Name.create(name),
                Text.create(description),
                date,
                address,
                false,
                totalSpots,
                0,
                partnerId,
                sectionList,
                now,
                now,
                null,
                null,
                null);
    }

    public static Show create(
            String name,
            String description,
            OffsetDateTime date,
            Address address,
            long totalSpots,
            PartnerID partnerId) {
        final ShowID id = ShowID.generate();
        final var now = Instant.now();
        return new Show(
                id,
                Name.create(name),
                Text.create(description),
                date,
                address,
                false,
                totalSpots,
                0,
                partnerId,
                new HashSet<>(),
                now,
                now,
                null,
                null,
                null);
    }

    public static Show reconstitute(
            ShowID id,
            Name name,
            Text description,
            OffsetDateTime date,
            Address address,
            boolean isPublished,
            long totalSpots,
            long totalSpotsSold,
            PartnerID partnerId,
            Set<Section> sections,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt,
            String createdBy,
            String lastModifiedBy) {
        final Set<Section> sectionList = isNull(sections) ? new HashSet<>() : new HashSet<>(sections);
        return new Show(
                id,
                name,
                description,
                date,
                address,
                isPublished,
                totalSpots,
                totalSpotsSold,
                partnerId,
                sectionList,
                createdAt,
                updatedAt,
                deletedAt,
                createdBy,
                lastModifiedBy);
    }

    public void addSection(String name, String description, long totalSpots, Money price, int seatNumberWidth) {
        if (totalSpots < 0) {
            throw new DomainException("'totalSpots' should not be negative");
        }
        final Section section = Section.create(
                name, description, totalSpots, price, Location.sectionCode(sections.size()), seatNumberWidth);
        this.sections.add(section);
        this.totalSpots += totalSpots;
        this.markAsUpdated();
    }

    /**
     * Adds a section shell without materialized spots, for asynchronous spot
     * generation. The returned section id lets callers reference it (e.g. in
     * a generation event) before the spots exist.
     */
    public Section addSectionShell(String name, String description, long totalSpots, Money price) {
        if (totalSpots < 0) {
            throw new DomainException("'totalSpots' should not be negative");
        }
        final Section section = Section.createShell(name, description, totalSpots, price);
        this.sections.add(section);
        this.totalSpots += totalSpots;
        this.markAsUpdated();
        return section;
    }

    public void publishAll() {
        this.publish();
        this.sections.forEach(Section::publishAll);
    }

    public void unpublishAll() {
        this.unpublish();
        this.sections.forEach(Section::unpublishAll);
    }

    public void publish() {
        this.isPublished = true;
        this.markAsUpdated();
    }

    public void unpublish() {
        this.isPublished = false;
        this.markAsUpdated();
    }

    /**
     * Counts one sold seat (ticket issuance). See
     * {@code Section.registerRefund} for why refunds clamp at zero.
     */
    public void registerSale() {
        this.totalSpotsSold++;
        this.markAsUpdated();
    }

    public void registerRefund() {
        this.totalSpotsSold = Math.max(0, totalSpotsSold - 1);
        this.markAsUpdated();
    }

    public Show changeName(final String name) {
        this.name = Name.create(name);
        markAsUpdated();
        return this;
    }

    public Show changeDescription(final String description) {
        this.description = Text.create(description);
        markAsUpdated();
        return this;
    }

    public Show reschedule(final OffsetDateTime date) {
        if (isNull(date)) {
            throw new DomainException("'date' should not be null");
        }
        this.date = date;
        markAsUpdated();
        return this;
    }

    public Name getName() {
        return name;
    }

    public Text getDescription() {
        return description;
    }

    public Address getAddress() {
        return address;
    }

    public OffsetDateTime getDate() {
        return date;
    }

    public boolean isPublished() {
        return isPublished;
    }

    public long getTotalSpots() {
        return totalSpots;
    }

    public long getTotalSpotsSold() {
        return totalSpotsSold;
    }

    public PartnerID getPartnerId() {
        return partnerId;
    }

    public Set<Section> getSections() {
        return Collections.unmodifiableSet(sections);
    }

    @Override
    public void validate(final ValidationHandler handler) {
        final var validator = new ShowValidator(this, handler);
        validator.validate();
    }
}
