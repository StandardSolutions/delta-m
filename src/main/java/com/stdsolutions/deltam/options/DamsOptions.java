package com.stdsolutions.deltam.options;

import com.stdsolutions.deltam.sanitized.SqlIdentifierSanitized;

import java.util.Optional;
import java.util.UUID;

public class DamsOptions extends Options {

    public DamsOptions(String... args) {
        super(args);
    }

    public DamsOptions(Iterable<String> args) {
        super(args);
    }

    public String schema() {
        return map.getOrDefault("schema-name", "public");
    }

    public long lockId() {
        return UUID
                .nameUUIDFromBytes("delta-m".getBytes(java.nio.charset.StandardCharsets.UTF_8))
                .getMostSignificantBits();
    }

    public String lockTableName() {
        return new SqlIdentifierSanitized(
                map.getOrDefault("lock-table-name", "delta_m_lock")
        ).value();
    }

    /**
     * @return lock timeout in milliseconds.
     */
    public int lockTimeoutMillis() {
        return Optional.ofNullable(map.get("lock-timeout-millis"))
                .map(Integer::parseInt)
                .orElse(60_000);
    }

    public String changeLogTableName() {
        return new SqlIdentifierSanitized(
                map.getOrDefault("changelog-table-name", "delta_m_changelog")
        ).value();
    }


    public String migrationPath() {
        return map.getOrDefault("migration-path", "db/delta-m");
    }

    public String tableName(String tableName) {
        return new SqlIdentifierSanitized(
                map.getOrDefault(tableName, tableName)
        ).value();
    }
}