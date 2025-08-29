package com.stdsolutions.deltam.migration.h2;

import com.stdsolutions.deltam.ChangeLog;
import com.stdsolutions.deltam.MigrationStep;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class H2ChangeLog implements ChangeLog {
    @Override
    public void ensureExist(Connection c) {
        try (Statement stmt = c.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS DAMS_CHANGELOG_TABLE (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    version VARCHAR(255) NOT NULL,
                    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    description TEXT
                )
                """);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create changelog table", e);
        }
    }

    @Override
    public boolean has(Connection c, MigrationStep migration) {
        try (PreparedStatement stmt = c.prepareStatement(
            "SELECT COUNT(*) FROM DAMS_CHANGELOG_TABLE WHERE version = ?")) {
            stmt.setString(1, migration.id());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            // If table doesn't exist yet, migration hasn't been run
            return false;
        }
    }

    @Override
    public void append(Connection c, MigrationStep migration) {
        try (PreparedStatement stmt = c.prepareStatement(
            "INSERT INTO DAMS_CHANGELOG_TABLE (version, description) VALUES (?, ?)")) {
            stmt.setString(1, migration.id());
            stmt.setString(2, migration.description());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to append migration to changelog", e);
        }
    }
}