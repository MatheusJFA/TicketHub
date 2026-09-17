package com.tickethub.infrastructure.security;

import java.util.Objects;

import org.springframework.stereotype.Component;

import com.tickethub.domain.core.show.ShowGateway;
import com.tickethub.domain.core.show.ShowID;

@Component("showAccess")
public class ShowAccess {

    private final ShowGateway showGateway;

    public ShowAccess(final ShowGateway showGateway) {
        this.showGateway = Objects.requireNonNull(showGateway, "'showGateway' should not be null");
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
