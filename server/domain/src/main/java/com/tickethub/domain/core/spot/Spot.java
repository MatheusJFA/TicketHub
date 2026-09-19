package com.tickethub.domain.core.spot;

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

    public static Spot create() {
        final SpotID id = SpotID.generate();
        final var now = Instant.now();
        return new Spot(id, Location.generateRandom(), true, false, now, now, null, null, null);
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

    public Spot changeLocation(final Location location) {
        if (location == null) {
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
