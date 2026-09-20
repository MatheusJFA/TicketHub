package com.tickethub.domain.core.spot;

import static java.util.Objects.isNull;

import java.time.Instant;

import com.tickethub.domain.Entity;
import com.tickethub.domain.exception.DomainException;
import com.tickethub.domain.shared.Location;

public class Spot extends Entity<SpotID> {
    private Location location;
    private boolean isAvailable;
    private boolean isPublished;

    private Spot(SpotID id, Location location, boolean isAvailable, boolean isPublished,
            Instant createdAt, Instant updatedAt, Instant deletedAt, String createdBy, String lastModifiedBy) {
        super(id, createdAt, updatedAt, deletedAt, createdBy, lastModifiedBy);
        this.location = location;
        this.isAvailable = isAvailable;
        this.isPublished = isPublished;
    }

    public static Spot create(Location location, boolean isAvailable, boolean isPublished) {
        final SpotID id = SpotID.generate();
        final var now = Instant.now();
        return new Spot(id, location, isAvailable, isPublished, now, now, null, null, null);
    }

    public static Spot create(Location location) {
        final SpotID id = SpotID.generate();
        final var now = Instant.now();
        return new Spot(id, location, true, false, now, now, null, null, null);
    }

    /**
     * Creates a spot with a random human-readable code. The seat number
     * width always comes from the caller (see
     * {@code tickethub.spots.seat-number-width}); it is never assumed here.
     */
    public static Spot create(int seatNumberWidth) {
        final SpotID id = SpotID.generate();
        final var now = Instant.now();
        return new Spot(id, Location.generateRandom(seatNumberWidth), true, false, now, now, null, null, null);
    }

    public static Spot reconstitute(SpotID id, Location location, boolean isAvailable, boolean isPublished,
            Instant createdAt, Instant updatedAt, Instant deletedAt, String createdBy, String lastModifiedBy) {
        return new Spot(id, location, isAvailable, isPublished,
                createdAt, updatedAt, deletedAt, createdBy, lastModifiedBy);
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
     * Marks the spot as used at check-in (door validation). A spot can only
     * be checked in once; a second scan fails so a ticket cannot be reused.
     */
    public void checkIn() {
        if (!isAvailable) {
            throw new DomainException("Spot is already used");
        }
        this.isAvailable = false;
        this.markAsUpdated();
    }

    public Spot changeLocation(final Location location) {
        if (isNull(location)) {
            throw new DomainException("'location' should not be null");
        }
        this.location = location;
        markAsUpdated();
        return this;
    }

    public Location getLocation() {
        return location;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public boolean isPublished() {
        return isPublished;
    }

}
