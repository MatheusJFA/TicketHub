package com.tickethub.infrastructure.authentication.persistence;

import com.tickethub.domain.authentication.RefreshSession;
import java.time.Instant;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("refresh_sessions")
public record RefreshSessionDocument(
        @Id String id,
        String familyId,
        String tokenHash,
        String subject,
        List<String> authorities,
        String ownerId,
        Instant createdAt,
        Instant expiresAt,
        boolean revoked,
        String replacedByTokenHash) {

    public static final String COLLECTION = "refresh_sessions";

    public static RefreshSessionDocument from(final RefreshSession session) {
        return new RefreshSessionDocument(
                session.getId(),
                session.getFamilyId(),
                session.getTokenHash(),
                session.getSubject(),
                session.getAuthorities(),
                session.getOwnerId(),
                session.getCreatedAt(),
                session.getExpiresAt(),
                session.isRevoked(),
                session.getReplacedByTokenHash());
    }

    public RefreshSession toDomain() {
        return RefreshSession.reconstitute(
                id,
                familyId,
                tokenHash,
                subject,
                authorities,
                ownerId,
                createdAt,
                expiresAt,
                revoked,
                replacedByTokenHash);
    }
}
