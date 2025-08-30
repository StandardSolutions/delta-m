package com.stdsolutions.deltam.migration.h2;

import com.stdsolutions.deltam.ChangeLog;
import com.stdsolutions.deltam.MigrationStep;
import com.stdsolutions.deltam.options.DamsOptions;

import java.sql.*;

public final class H2ChangeLog implements ChangeLog {
    private final DamsOptions options;

    public H2ChangeLog(DamsOptions options) {
        this.options = options;
    }

    @Override
    public void ensureExist(Connection c) throws SQLException {
        try (Statement stmt = c.createStatement()) {
            stmt.execute(String.format("""
                CREATE TABLE IF NOT EXISTS %s (
                    id VARCHAR(255) PRIMARY KEY,
                    description VARCHAR(255) NOT NULL,
                    executed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
                )
                """, options.changeLogTableName()));
        }
    }

    @Override
    public boolean has(Connection c, MigrationStep migration) throws SQLException {
        String query = String.format(
            "SELECT COUNT(*) FROM %s WHERE id = ?",
            options.changeLogTableName()
        );
        try (PreparedStatement stmt = c.prepareStatement(query)) {
            stmt.setString(1, migration.id());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    @Override
    public void append(Connection c, MigrationStep migration) throws SQLException {
        try (PreparedStatement stmt = c.prepareStatement(
            String.format("INSERT INTO %s (id, description) VALUES (?, ?)", options.changeLogTableName()))) {
            stmt.setString(1, migration.id());
            stmt.setString(2, migration.description());
            stmt.executeUpdate();
        }
    }
}