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
        return map.getOrDefault("db-schema", "public");
    }

    /**
     * @return lock timeout in milliseconds.
     */
    public int lockTimeoutMillis() {
        return Optional.ofNullable(map.get("lock-timeout-millis"))
                .map(Integer::parseInt)
                .orElse(60_000);
    }

    public long lockId() {
        return UUID
                .nameUUIDFromBytes("dams-publisher-core".getBytes(java.nio.charset.StandardCharsets.UTF_8))
                .getMostSignificantBits();
    }

    public String changeLogTableName() {
        return new SqlIdentifierSanitized(
                map.getOrDefault("db-changelog-table-name", "dams_3db_changelog")
        ).value();
    }

    public String lockTableName() {
        return new SqlIdentifierSanitized(
                map.getOrDefault("db-lock-table-name", "dams_db_lock")
        ).value();
    }

    public String migrationPath() {
        return map.getOrDefault("migration-path", "migrations");
    }

    public String tableName(String tableName) {
        return new SqlIdentifierSanitized(
                map.getOrDefault(tableName, tableName)
        ).value();
    }
}