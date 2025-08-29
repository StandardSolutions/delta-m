package com.stdsolutions.deltam.sanitized;

import com.stdsolutions.deltam.Sanitized;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Immutable wrapper for sanitized SQL identifiers.
 */
public final class SqlIdentifierSanitized implements Sanitized<String> {

    private final String rawValue;

    /**
     * @param rawValue Untrusted raw identifier
     */
    public SqlIdentifierSanitized(String rawValue) {
        this.rawValue = rawValue;
    }

    /**
     * Returns sanitized value or throws exception
     *
     * @throws IllegalArgumentException if invalid
     */
    public String value() {
        if (rawValue == null || rawValue.isBlank()) {
            throw new IllegalArgumentException("Identifier cannot be null/empty");
        }

        final int maxLength = 63;
        if (rawValue.length() > maxLength) {
            throw new IllegalArgumentException(
                    String.format("Identifier exceeds %d characters", maxLength)
            );
        }

        final String value = rawValue.replace("\"", "").replace("'", "").trim();
        final Pattern ptn = Pattern.compile("^[a-zA-Z_]\\w*$");
        if (!ptn.matcher(value).matches()) {
            throw new IllegalArgumentException(
                    String.format("Invalid SQL identifier: '%s'. Only [a-zA-Z0-9_] allowed", rawValue)
            );
        }
        return value;
    }

    @Override
    public String toString() {
        try {
            return value();
        } catch (IllegalArgumentException e) {
            return "[Invalid: " + rawValue + "]";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SqlIdentifierSanitized that = (SqlIdentifierSanitized) o;
        return Objects.equals(rawValue, that.rawValue);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(rawValue);
    }
}
