package com.stdsolutions.deltam.options;

import com.stdsolutions.deltam.files.path.SafeStringPath;
import com.stdsolutions.deltam.files.MigrationPath;
import com.stdsolutions.deltam.files.path.UnprefixedStringPath;
import com.stdsolutions.deltam.sanitized.SqlIdentifierSanitized;

import java.util.Optional;
import java.util.UUID;

public class MigrationOptions extends Options {

    public MigrationOptions(String... args) {
        super(args);
    }

    public MigrationOptions(Iterable<String> args) {
        super(args);
    }

    public String schema() {
        return map.getOrDefault("schema-name", "public");
    }

    public long lockId() {
        return UUID
            .nameUUIDFromBytes("db/delta-m".getBytes(java.nio.charset.StandardCharsets.UTF_8))
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


    public MigrationPath migrationPath() {
        String migrationPath = map.getOrDefault("migration-path", "classpath:db/delta-m");
        return new SafeStringPath(
                new UnprefixedStringPath(migrationPath)
        );
    }

    public String tableName(String tableName) {
        return new SqlIdentifierSanitized(
            map.getOrDefault(tableName, tableName)
        ).value();
    }
}