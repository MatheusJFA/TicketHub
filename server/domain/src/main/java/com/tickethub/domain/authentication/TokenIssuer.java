package com.tickethub.domain.authentication;

import java.util.List;

public interface TokenIssuer {
    IssuedToken issueAccess(String subject, List<String> authorities, String ownerId);
}
