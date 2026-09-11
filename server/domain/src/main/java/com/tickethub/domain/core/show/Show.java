package com.tickethub.domain.core.show;

import java.util.HashSet;
import static java.util.Objects.isNull;

import com.tickethub.domain.AggregateRoot;
import com.tickethub.domain.core.partner.PartnerID;
import com.tickethub.domain.core.section.Section;
import com.tickethub.domain.shared.Name;
import com.tickethub.domain.shared.Text;
import com.tickethub.domain.validation.ValidationHandler;

public class Show extends AggregateRoot<ShowID> implements Cloneable {
    private Name name;
    private Text description;
    private boolean isPublished;

    private long totalSpots;
    private long totalSpotsSold;
    private PartnerID partnerId;

    private HashSet<Section> sections;

    private Show(ShowID id, Name name, Text description, boolean isPublished, long totalSpots, long totalSpotsSold, PartnerID partnerId, HashSet<Section> sections) {
        super(id);
        this.name = name;
        this.description = description;
        this.isPublished = isPublished;
        this.totalSpots = totalSpots;
        this.totalSpotsSold = totalSpotsSold;
        this.partnerId = partnerId;
        this.sections = sections;
    }

    public static Show create(String name, String description, boolean isPublished, long totalSpots, long totalSpotsSold, PartnerID partnerId, HashSet<Section> sections) {
        final ShowID id = ShowID.generate();
        final HashSet<Section> sectionList = isNull(sections) ? new HashSet<>() : new HashSet<>(sections);
        return new Show(id, Name.create(name), Text.create(description), isPublished, totalSpots, totalSpotsSold, partnerId, sectionList);
    }

    public static Show create(String name, String description, long totalSpots, PartnerID partnerId, HashSet<Section> sections) {
        final ShowID id = ShowID.generate();
        final HashSet<Section> sectionList = isNull(sections) ? new HashSet<>() : new HashSet<>(sections);
        return new Show(id, Name.create(name), Text.create(description), false, totalSpots, 0, partnerId, sectionList);
    }

    public void publish() {
        this.isPublished = true;
    }

    public void unpublish() {
        this.isPublished = false;
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
