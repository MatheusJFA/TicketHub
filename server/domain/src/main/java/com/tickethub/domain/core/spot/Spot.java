package com.tickethub.domain.core.spot;

import com.tickethub.domain.Entity;
import com.tickethub.domain.shared.Location;

public class Spot extends Entity<SpotID> implements Cloneable {
    private Location location;
    private boolean isAvailable;
    private boolean isPublished;

    private Spot(SpotID id, Location location, boolean isAvailable, boolean isPublished) {
        super(id);
        this.location = location;
        this.isAvailable = isAvailable;
        this.isPublished = isPublished;
    }

    public static Spot create(Location location, boolean isAvailable, boolean isPublished) {
        final SpotID id = SpotID.generate();
        return new Spot(id, location, isAvailable, isPublished);
    }

    public static Spot create(Location location) {
        final SpotID id = SpotID.generate();
        return new Spot(id, location, true, false);
    }

    public static Spot create() {
        final SpotID id = SpotID.generate();
        return new Spot(id, null, true, false);
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
            throw new com.tickethub.domain.exception.DomainException("'location' should not be null");
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

    @Override
    public Spot clone() throws CloneNotSupportedException {
        return (Spot) super.clone();
    }

}
