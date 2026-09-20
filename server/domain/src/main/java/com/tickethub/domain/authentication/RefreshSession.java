package com.tickethub.domain.authentication;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static java.util.Objects.requireNonNull;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import com.tickethub.domain.exception.DomainException;
import java.util.UUID;
public class RefreshSession {

    public static final Duration DEFAULT_TTL = Duration.ofDays(7);

    private final String id;
    private final String familyId;
    private final String tokenHash;
    private final String subject;
    private final List<String> authorities;
    private final String ownerId;
    private final Instant createdAt;
    private final Instant expiresAt;
    private boolean revoked;
    private String replacedByTokenHash;

    private RefreshSession(String id, String familyId, String tokenHash, String subject,
            List<String> authorities, String ownerId, Instant createdAt, Instant expiresAt,
            boolean revoked, String replacedByTokenHash) {
        this.id = requireNonNull(id, "'id' should not be null");
        this.familyId = requireNonNull(familyId, "'familyId' should not be null");
        this.tokenHash = requireNonNull(tokenHash, "'tokenHash' should not be null");
        this.subject = requireNonNull(subject, "'subject' should not be null");
        this.authorities = List.copyOf(requireNonNull(authorities, "'authorities' should not be null"));
        this.ownerId = ownerId;
        this.createdAt = requireNonNull(createdAt, "'createdAt' should not be null");
        this.expiresAt = requireNonNull(expiresAt, "'expiresAt' should not be null");
        this.revoked = revoked;
        this.replacedByTokenHash = replacedByTokenHash;
    }

    public static RefreshSession issue(final String tokenHash, final String subject,
            final List<String> authorities, final String ownerId, final Duration ttl) {
        if (isBlank(tokenHash)) {
            throw new DomainException("'tokenHash' should not be null or blank");
        }
        if (isBlank(subject)) {
            throw new DomainException("'subject' should not be null or blank");
        }
        final var now = Instant.now();
        final var effectiveTtl = isNull(ttl) || ttl.isNegative() || ttl.isZero() ? DEFAULT_TTL : ttl;
        return new RefreshSession(UUID.randomUUID().toString(), UUID.randomUUID().toString(), tokenHash,
                subject, List.copyOf(emptyIfNull(authorities)), ownerId, now,
                now.plus(effectiveTtl), false, null);
    }

    public static RefreshSession reconstitute(final String id, final String familyId, final String tokenHash,
            final String subject, final List<String> authorities, final String ownerId,
            final Instant createdAt, final Instant expiresAt, final boolean revoked,
            final String replacedByTokenHash) {
        return new RefreshSession(id, familyId, tokenHash, subject, authorities, ownerId, createdAt,
                expiresAt, revoked, replacedByTokenHash);
    }

    public RefreshSession rotate(final String newTokenHash, final Duration ttl) {
        if (revoked) {
            throw new DomainException("Refresh session is revoked");
        }
        if (nonNull(replacedByTokenHash)) {
            throw new DomainException("Refresh session was already rotated");
        }
        if (isExpired()) {
            throw new DomainException("Refresh session is expired");
        }
        final RefreshSession next = issue(newTokenHash, subject, authorities, ownerId, ttl);
        final var rotated = new RefreshSession(next.id, familyId, next.tokenHash, next.subject,
                next.authorities, next.ownerId, next.createdAt, next.expiresAt, false, null);
        this.replacedByTokenHash = newTokenHash;
        return rotated;
    }

    public void revoke() {
        this.revoked = true;
    }

    public boolean isExpired() {
        return !Instant.now().isBefore(expiresAt);
    }

    public boolean isRotated() {
        return nonNull(replacedByTokenHash);
    }

    public boolean isActive() {
        return !revoked && !isExpired();
    }

    public String getId() {
        return id;
    }

    public String getFamilyId() {
        return familyId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public String getSubject() {
        return subject;
    }

    public List<String> getAuthorities() {
        return authorities;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public String getReplacedByTokenHash() {
        return replacedByTokenHash;
    }
}
