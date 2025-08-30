package com.stdsolutions.deltam.metadata;

import com.stdsolutions.deltam.Database;
import com.stdsolutions.deltam.migration.h2.H2Database;
import com.stdsolutions.deltam.migration.postgresql.PgDatabase;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

public final class DatabaseMetadata {

    private final DatabaseType type;

    private final String version;

    public DatabaseMetadata(Connection conn) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();
        this.type = DatabaseType.of(metaData.getDatabaseProductName());
        this.version = metaData.getDatabaseProductVersion();
    }

    public DatabaseType type() {
        return type;
    }

    public Database database() {
        switch (this.type) {
            case POSTGRESQL:
                return new PgDatabase();
            case H2:
                return new H2Database();
            default:
                throw new IllegalArgumentException(
                    String.format(
                        "Unsupported database type: %s",
                        this.type
                    )
                );
        }
    }

    public String version() {
        return version;
    }
}
