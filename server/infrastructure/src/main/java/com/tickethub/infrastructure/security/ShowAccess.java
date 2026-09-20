package com.tickethub.infrastructure.security;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static java.util.Objects.requireNonNull;


import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.tickethub.domain.core.show.ShowGateway;
import com.tickethub.domain.core.show.ShowID;
import com.tickethub.infrastructure.section.persistence.SectionDocument;
import com.tickethub.infrastructure.show.persistence.ShowDocument;
import com.tickethub.infrastructure.spot.persistence.SpotDocument;

@Component("showAccess")
public class ShowAccess {

    private final ShowGateway showGateway;
    private final MongoTemplate mongoTemplate;

    public ShowAccess(final ShowGateway showGateway, final MongoTemplate mongoTemplate) {
        this.showGateway = requireNonNull(showGateway, "'showGateway' should not be null");
        this.mongoTemplate = requireNonNull(mongoTemplate, "'mongoTemplate' should not be null");
    }

    public boolean canCreate(final String partnerId) {
        return SecuritySupport.isAdmin() || isOwner(partnerId);
    }

    public boolean canWrite(final String showId) {
        return ownsShow(showId);
    }

    public boolean canPublish(final String showId) {
        return ownsShow(showId);
    }

    public boolean canDelete(final String showId) {
        return ownsShow(showId);
    }

    public boolean canWriteSection(final String sectionId) {
        return ownsSection(sectionId);
    }

    public boolean canPublishSection(final String sectionId) {
        return ownsSection(sectionId);
    }

    public boolean canDeleteSection(final String sectionId) {
        return ownsSection(sectionId);
    }

    public boolean canWriteSpot(final String spotId) {
        return ownsSpot(spotId);
    }

    public boolean canPublishSpot(final String spotId) {
        return ownsSpot(spotId);
    }

    public boolean canDeleteSpot(final String spotId) {
        return ownsSpot(spotId);
    }

    private boolean ownsSection(final String sectionId) {
        if (SecuritySupport.isAdmin()) {
            return true;
        }
        final String ownerId = SecuritySupport.ownerId().orElse(null);
        if (isBlank(ownerId) || isBlank(sectionId)) {
            return false;
        }
        try {
            final var section = mongoTemplate.findById(sectionId, SectionDocument.class,
                    SectionDocument.COLLECTION);
            if (isNull(section)) {
                return false;
            }
            // Fast path: denormalized owner, single indexed read, no join chain.
            if (nonNull(section.partnerId())) {
                return ownerId.equals(section.partnerId());
            }
            if (nonNull(section.showId())) {
                return ownsShow(section.showId());
            }
            // Legacy fallback: sections written before the denormalized links.
            final var show = mongoTemplate.findOne(
                    Query.query(Criteria.where("sectionIds").is(sectionId)),
                    ShowDocument.class, ShowDocument.COLLECTION);
            return nonNull(show) && ownerId.equals(show.partnerId());
        } catch (final RuntimeException e) {
            return false;
        }
    }

    private boolean ownsSpot(final String spotId) {
        if (SecuritySupport.isAdmin()) {
            return true;
        }
        final String ownerId = SecuritySupport.ownerId().orElse(null);
        if (isBlank(ownerId) || isBlank(spotId)) {
            return false;
        }
        try {
            final var spot = mongoTemplate.findById(spotId, SpotDocument.class, SpotDocument.COLLECTION);
            if (isNull(spot)) {
                return false;
            }
            // Fast path: denormalized owner, single indexed read, no join chain.
            if (nonNull(spot.partnerId())) {
                return ownerId.equals(spot.partnerId());
            }
            if (nonNull(spot.showId())) {
                return ownsShow(spot.showId());
            }
            if (nonNull(spot.sectionId()) && ownsSection(spot.sectionId())) {
                return true;
            }
            // Legacy fallback: spots written before the denormalized links.
            final var section = mongoTemplate.findOne(
                    Query.query(Criteria.where("spotIds").is(spotId)),
                    SectionDocument.class, SectionDocument.COLLECTION);
            return nonNull(section) && ownsSection(section.id());
        } catch (final RuntimeException e) {
            return false;
        }
    }

    private boolean ownsShow(final String showId) {
        if (SecuritySupport.isAdmin()) {
            return true;
        }
        final String ownerId = SecuritySupport.ownerId().orElse(null);
        if (isBlank(ownerId) || isBlank(showId)) {
            return false;
        }
        try {
            return showGateway.findById(ShowID.from(showId))
                    .map(show -> nonNull(show.getPartnerId())
                            && ownerId.equals(show.getPartnerId().getValue()))
                    .orElse(false);
        } catch (final RuntimeException e) {
            return false;
        }
    }

    private boolean isOwner(final String partnerId) {
        return isNotBlank(partnerId)
                && SecuritySupport.ownerId().map(partnerId::equals).orElse(false);
    }
}
