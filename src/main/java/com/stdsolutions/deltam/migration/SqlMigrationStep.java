package com.stdsolutions.deltam.migration;

import com.stdsolutions.deltam.MigrationStep;
import com.stdsolutions.deltam.options.DamsOptions;

import java.sql.Connection;
import java.sql.SQLException;

public final class SqlMigrationStep implements MigrationStep {
    private final String id;
    private final String description;
    private final String sql;

    public SqlMigrationStep(String id, String description, String sql) {
        this.id = id;
        this.description = description;
        this.sql = sql;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public void execute(Connection connection) throws SQLException {
        try (var stmt = connection.createStatement()) {
            String[] statements = sql.split(";");
            for (String statement : statements) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty()) {
                    stmt.execute(trimmed);
                }
            }
        }
    }
}