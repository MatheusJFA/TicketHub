package com.tickethub.infrastructure.security;

import java.util.Objects;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.tickethub.domain.core.show.ShowGateway;
import com.tickethub.domain.core.show.ShowID;
import com.tickethub.infrastructure.persistence.SectionDocument;
import com.tickethub.infrastructure.persistence.ShowDocument;
import com.tickethub.infrastructure.persistence.SpotDocument;

@Component("showAccess")
public class ShowAccess {

    private final ShowGateway showGateway;
    private final MongoTemplate mongoTemplate;

    public ShowAccess(final ShowGateway showGateway, final MongoTemplate mongoTemplate) {
        this.showGateway = Objects.requireNonNull(showGateway, "'showGateway' should not be null");
        this.mongoTemplate = Objects.requireNonNull(mongoTemplate, "'mongoTemplate' should not be null");
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
        if (ownerId == null || sectionId == null || sectionId.isBlank()) {
            return false;
        }
        try {
            final var section = mongoTemplate.findById(sectionId, SectionDocument.class,
                    SectionDocument.COLLECTION);
            if (section == null) {
                return false;
            }
            // Fast path: denormalized owner, single indexed read, no join chain.
            if (section.partnerId() != null) {
                return ownerId.equals(section.partnerId());
            }
            if (section.showId() != null) {
                return ownsShow(section.showId());
            }
            // Legacy fallback: sections written before the denormalized links.
            final var show = mongoTemplate.findOne(
                    Query.query(Criteria.where("sectionIds").is(sectionId)),
                    ShowDocument.class, ShowDocument.COLLECTION);
            return show != null && ownerId.equals(show.partnerId());
        } catch (final RuntimeException e) {
            return false;
        }
    }

    private boolean ownsSpot(final String spotId) {
        if (SecuritySupport.isAdmin()) {
            return true;
        }
        final String ownerId = SecuritySupport.ownerId().orElse(null);
        if (ownerId == null || spotId == null || spotId.isBlank()) {
            return false;
        }
        try {
            final var spot = mongoTemplate.findById(spotId, SpotDocument.class, SpotDocument.COLLECTION);
            if (spot == null) {
                return false;
            }
            // Fast path: denormalized owner, single indexed read, no join chain.
            if (spot.partnerId() != null) {
                return ownerId.equals(spot.partnerId());
            }
            if (spot.showId() != null) {
                return ownsShow(spot.showId());
            }
            if (spot.sectionId() != null && ownsSection(spot.sectionId())) {
                return true;
            }
            // Legacy fallback: spots written before the denormalized links.
            final var section = mongoTemplate.findOne(
                    Query.query(Criteria.where("spotIds").is(spotId)),
                    SectionDocument.class, SectionDocument.COLLECTION);
            return section != null && ownsSection(section.id());
        } catch (final RuntimeException e) {
            return false;
        }
    }

    private boolean ownsShow(final String showId) {
        if (SecuritySupport.isAdmin()) {
            return true;
        }
        final String ownerId = SecuritySupport.ownerId().orElse(null);
        if (ownerId == null || showId == null || showId.isBlank()) {
            return false;
        }
        try {
            return showGateway.findById(ShowID.from(showId))
                    .map(show -> show.getPartnerId() != null
                            && ownerId.equals(show.getPartnerId().getValue()))
                    .orElse(false);
        } catch (final RuntimeException e) {
            return false;
        }
    }

    private boolean isOwner(final String partnerId) {
        return partnerId != null && !partnerId.isBlank()
                && SecuritySupport.ownerId().map(partnerId::equals).orElse(false);
    }
}
