package com.tickethub.domain.core.section;

import static java.util.Objects.isNull;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.tickethub.domain.Entity;
import com.tickethub.domain.core.spot.Spot;
import com.tickethub.domain.exception.DomainException;
import com.tickethub.domain.shared.Location;
import com.tickethub.domain.shared.Money;
import com.tickethub.domain.shared.Name;
import com.tickethub.domain.shared.Text;
import com.tickethub.domain.validation.ValidationHandler;

public class Section extends Entity<SectionID> {
    private Name name;
    private Text description;
    private boolean isPublished;
    private long totalSpots;
    private long totalSpotsSold;
    private Money price;
    private final Set<Spot> spots;

    private Section(SectionID id, Name name, Text description, boolean isPublished, long totalSpots,
            long totalSpotsSold, Money price, Set<Spot> spots,
            Instant createdAt, Instant updatedAt, Instant deletedAt, String createdBy, String lastModifiedBy) {
        super(id, createdAt, updatedAt, deletedAt, createdBy, lastModifiedBy);
        this.name = name;
        this.description = description;
        this.isPublished = isPublished;
        this.totalSpots = totalSpots;
        this.totalSpotsSold = totalSpotsSold;
        this.price = price;
        this.spots = spots;
    }

    public static Section create(String name, String description, boolean isPublished, long totalSpots,
            long totalSpotsSold, Money price, Set<Spot> spots) {
        final SectionID id = SectionID.generate();
        final Set<Spot> spotList = isNull(spots) ? new HashSet<>() : new HashSet<>(spots);
        final var now = Instant.now();
        return new Section(id, Name.create(name), Text.create(description), isPublished, totalSpots, totalSpotsSold,
                price, spotList, now, now, null, null, null);
    }

    public static Section create(String name, String description, long totalSpots, Money price,
            String sectionCode, int seatNumberWidth) {
        if (totalSpots < 0) {
            throw new DomainException("'totalSpots' should not be negative");
        }
        final SectionID id = SectionID.generate();
        final Set<Spot> spots = generateSpots(totalSpots, sectionCode, seatNumberWidth);
        final var now = Instant.now();
        final Section section = new Section(id, Name.create(name), Text.create(description), false, totalSpots, 0,
                price, spots, now, now, null, null, null);

        return section;
    }

    /**
     * Creates a section shell without materialized spots, for asynchronous
     * spot generation (see {@link #generateMissingSpots(String)}).
     */
    public static Section createShell(String name, String description, long totalSpots, Money price) {
        if (totalSpots < 0) {
            throw new DomainException("'totalSpots' should not be negative");
        }
        final SectionID id = SectionID.generate();
        final var now = Instant.now();
        return new Section(id, Name.create(name), Text.create(description), false, totalSpots, 0,
                price, new HashSet<>(), now, now, null, null, null);
    }

    public static Section reconstitute(SectionID id, Name name, Text description, boolean isPublished, long totalSpots,
            long totalSpotsSold, Money price, Set<Spot> spots,
            Instant createdAt, Instant updatedAt, Instant deletedAt, String createdBy, String lastModifiedBy) {
        final Set<Spot> spotList = isNull(spots) ? new HashSet<>() : new HashSet<>(spots);
        return new Section(id, name, description, isPublished, totalSpots, totalSpotsSold,
                price, spotList, createdAt, updatedAt, deletedAt, createdBy, lastModifiedBy);
    }

    private static Set<Spot> generateSpots(long totalSpots, String sectionCode, int seatNumberWidth) {
        if (totalSpots < 0) {
            throw new DomainException("'totalSpots' should not be negative");
        }
        return Stream.iterate(0, i -> i + 1)
                .limit(totalSpots)
                .map(i -> Spot.create(Location.generateSeat(sectionCode, i + 1, seatNumberWidth)))
                .collect(Collectors.toCollection(HashSet::new));
    }

    /**
     * Materializes the spots still missing to reach {@code totalSpots},
     * continuing the seat numbering after the spots already present, and
     * returns the newly created spots. Idempotent: returns an empty set when
     * the section is already complete. The seat number width always comes
     * from the caller (see {@code tickethub.spots.seat-number-width}).
     */
    public Set<Spot> generateMissingSpots(final String sectionCode, final int seatNumberWidth) {
        final long existing = spots.size();
        if (existing >= totalSpots) {
            return Set.of();
        }
        final Set<Spot> generated = new HashSet<>();
        for (long seatNumber = existing + 1; seatNumber <= totalSpots; seatNumber++) {
            final Spot spot = Spot.create(Location.generateSeat(sectionCode, seatNumber, seatNumberWidth));
            spots.add(spot);
            generated.add(spot);
        }
        markAsUpdated();
        return Collections.unmodifiableSet(generated);
    }

    public void publishAll() {
        this.publish();
        this.spots.forEach(Spot::publish);
    }

    public void unpublishAll() {
        this.unpublish();
        this.spots.forEach(Spot::unpublish);
    }

    public void publish() {
        this.isPublished = true;
        this.markAsUpdated();
    }

    public void unpublish() {
        this.isPublished = false;
        this.markAsUpdated();
    }

    public Section changeName(final String name) {
        this.name = Name.create(name);
        markAsUpdated();
        return this;
    }

    public Section changeDescription(final String description) {
        this.description = Text.create(description);
        markAsUpdated();
        return this;
    }

    public Section changePrice(final Money price) {
        if (isNull(price)) {
            throw new DomainException("'price' should not be null");
        }
        this.price = price;
        markAsUpdated();
        return this;
    }

    public Name getName() {
        return name;
    }

    public Text getDescription() {
        return description;
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

    public Money getPrice() {
        return price;
    }

    public Set<Spot> getSpots() {
        return Collections.unmodifiableSet(spots);
    }

    @Override
    public void validate(final ValidationHandler handler) {
        final var validator = new SectionValidator(this, handler);
        validator.validate();
    }

}
