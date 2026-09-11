package com.tickethub.domain.core.show;

import static java.util.Objects.isNull;

import java.time.OffsetDateTime;
import java.util.HashSet;

import com.tickethub.domain.AggregateRoot;
import com.tickethub.domain.core.partner.PartnerID;
import com.tickethub.domain.core.section.Section;
import com.tickethub.domain.shared.Money;
import com.tickethub.domain.shared.Name;
import com.tickethub.domain.shared.Text;
import com.tickethub.domain.validation.ValidationHandler;

public class Show extends AggregateRoot<ShowID> implements Cloneable {
    private Name name;
    private Text description;

    private OffsetDateTime date;

    private boolean isPublished;

    private long totalSpots;
    private long totalSpotsSold;

    private PartnerID partnerId;

    private HashSet<Section> sections;

    private Show(ShowID id, Name name, Text description, OffsetDateTime date, boolean isPublished, long totalSpots,
            long totalSpotsSold,
            PartnerID partnerId, HashSet<Section> sections) {
        super(id);
        this.name = name;
        this.description = description;
        this.date = date;
        this.isPublished = isPublished;
        this.totalSpots = totalSpots;
        this.totalSpotsSold = totalSpotsSold;
        this.partnerId = partnerId;
        this.sections = sections;
    }

    public static Show create(String name, String description, OffsetDateTime date, boolean isPublished,
            long totalSpots,
            long totalSpotsSold, PartnerID partnerId, HashSet<Section> sections) {
        final ShowID id = ShowID.generate();
        final HashSet<Section> sectionList = isNull(sections) ? new HashSet<>() : new HashSet<>(sections);
        return new Show(id, Name.create(name), Text.create(description), date, isPublished, totalSpots, totalSpotsSold,
                partnerId, sectionList);
    }

    public static Show create(String name, String description, OffsetDateTime date, long totalSpots,
            PartnerID partnerId,
            HashSet<Section> sections) {
        final ShowID id = ShowID.generate();
        final HashSet<Section> sectionList = isNull(sections) ? new HashSet<>() : new HashSet<>(sections);
        return new Show(id, Name.create(name), Text.create(description), date, false, totalSpots, 0, partnerId,
                sectionList);
    }

    public static Show create(String name, String description, OffsetDateTime date, long totalSpots,
            PartnerID partnerId) {
        final ShowID id = ShowID.generate();
        return new Show(id, Name.create(name), Text.create(description), date, false, totalSpots, 0, partnerId,
                new HashSet<>());
    }

    public void addSection(String name, String description, long totalSpots, Money price) {
        final Section section = Section.create(name, description, totalSpots, price);
        this.sections.add(section);
        this.totalSpots += totalSpots;
        this.markAsUpdated();
    }

    public void publish() {
        this.isPublished = true;
        this.markAsUpdated();
    }

    public void unpublish() {
        this.isPublished = false;
        this.markAsUpdated();
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

    public PartnerID getPartnerId() {
        return partnerId;
    }

    public HashSet<Section> getSections() {
        return sections;
    }

    @Override
    public void validate(final ValidationHandler handler) {
        final var validator = new ShowValidator(this, handler);
        validator.validate();
    }

    @Override
    public Show clone() throws CloneNotSupportedException {
        return (Show) super.clone();
    }

}
