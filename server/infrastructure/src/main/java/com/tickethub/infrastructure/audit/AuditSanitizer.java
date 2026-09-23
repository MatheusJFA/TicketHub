package com.tickethub.infrastructure.audit;

import static org.apache.commons.lang3.StringUtils.left;
import static org.apache.commons.lang3.StringUtils.length;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Redacts sensitive values (passwords, tokens, secrets) from the audit input
 * summary so credentials never land in logs or in the {@code audit_logs}
 * collection. Also truncates overlong summaries.
 */
public final class AuditSanitizer {

    public static final int MAX_INPUT_LENGTH = 2000;

    private static final String MASK = "***";
    private static final String TRUNCATION_MARKER = "...[truncated]";

    // Filtra: pares chave=valor/JSON sensiveis (password, secret, token, api-key, authorization) para mascarar.
    private static final Pattern KEY_VALUE = Pattern.compile(
            "(?i)([\"']?)(password|passwd|pwd|secret|client[_-]?secret|token|access[_-]?token|refresh[_-]?token|authorization|api[_-]?key)\\1(\\s*[=:]\\s*)([\"']?)([^,\"'}\\]\\s]+|\"[^\"]*\"|'[^']*')\\4");

    private AuditSanitizer() {}

    public static Optional<String> sanitize(final String input) {
        return Optional.ofNullable(input).map(AuditSanitizer::mask).map(AuditSanitizer::truncate);
    }

    private static String mask(final String input) {
        return KEY_VALUE.matcher(input).replaceAll("$1$2$1$3$4" + MASK + "$4");
    }

    private static String truncate(final String masked) {
        if (length(masked) > MAX_INPUT_LENGTH) {
            return left(masked, MAX_INPUT_LENGTH) + TRUNCATION_MARKER;
        }
        return masked;
    }
}
