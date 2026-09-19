package com.tickethub.domain.auth;

import java.util.List;

public interface TokenIssuer {
    IssuedToken issueAccess(String subject, List<String> authorities, String ownerId);
}
