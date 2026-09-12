package com.tickethub.domain.core.section;

import static java.util.Objects.isNull;

import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private Section(SectionID id, Name name, Text description, boolean isPublished, long totalSpots,
            long totalSpotsSold, Money price, HashSet<Spot> spots) {
        super(id);
        this.name = name;
        this.description = description;
        this.isPublished = isPublished;
        this.totalSpots = totalSpots;
        this.totalSpotsSold = totalSpotsSold;
        this.price = price;
        this.spots = spots;
    }

    public static Section create(String name, String description, boolean isPublished, long totalSpots,
            long totalSpotsSold, Money price, HashSet<Spot> spots) {
        final SectionID id = SectionID.generate();
        final HashSet<Spot> spotList = isNull(spots) ? new HashSet<>() : new HashSet<>(spots);
        return new Section(id, Name.create(name), Text.create(description), isPublished, totalSpots, totalSpotsSold,
                price, spotList);
    }

    public static Section create(String name, String description, long totalSpots, Money price) {
        final SectionID id = SectionID.generate();
        final HashSet<Spot> spots = generateSpots(totalSpots);
        final Section section = new Section(id, Name.create(name), Text.create(description), false, totalSpots, 0,
                price, spots);

        return section;
    }

    private static HashSet<Spot> generateSpots(long totalSpots) {
        return Stream.iterate(0, i -> i + 1)
                .limit(totalSpots)
                .map(i -> Spot.create())
                .collect(Collectors.toCollection(HashSet::new));
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
        if (price == null) {
            throw new com.tickethub.domain.exception.DomainException("'price' should not be null");
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
