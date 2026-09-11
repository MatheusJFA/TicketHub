package com.tickethub.domain.core.section;

import java.util.HashSet;
import static java.util.Objects.isNull;

import com.tickethub.domain.Entity;
import com.tickethub.domain.core.spot.Spot;
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
    private HashSet<Spot> spots;

    private Section(SectionID id, Name name, Text description, boolean isPublished, long totalSpots, long totalSpotsSold, Money price, HashSet<Spot> spots) {
        super(id);
        this.name = name;
        this.description = description;
        this.isPublished = isPublished;
        this.totalSpots = totalSpots;
        this.totalSpotsSold = totalSpotsSold;
        this.price = price;
        this.spots = spots;
    }

    public static Section create(String name, String description, boolean isPublished, long totalSpots, long totalSpotsSold, Money price, HashSet<Spot> spots) {
        final SectionID id = SectionID.generate();
        final HashSet<Spot> spotList = isNull(spots) ? new HashSet<>() : new HashSet<>(spots);
        return new Section(id, Name.create(name), Text.create(description), isPublished, totalSpots, totalSpotsSold, price, spotList);
    }

    public static Section create(String name, String description, long totalSpots, Money price, HashSet<Spot> spots) {
        final SectionID id = SectionID.generate();
        final HashSet<Spot> spotList = isNull(spots) ? new HashSet<>() : new HashSet<>(spots);
        return new Section(id, Name.create(name), Text.create(description), false, totalSpots, 0, price, spotList);
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

    public HashSet<Spot> getSpots() {
        return spots;
    }

    @Override
    public void validate(final ValidationHandler handler) {
        final var validator = new SectionValidator(this, handler);
        validator.validate();
    }

    @Override
    public Section clone() throws CloneNotSupportedException {
        return (Section) super.clone();
    }

}
