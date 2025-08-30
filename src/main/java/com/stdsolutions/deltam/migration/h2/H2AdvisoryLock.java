package com.stdsolutions.deltam.migration.h2;

import com.stdsolutions.deltam.AdvisoryLock;
import com.stdsolutions.deltam.options.DamsOptions;

import java.sql.*;

public final class H2AdvisoryLock implements AdvisoryLock {

    private final Connection conn;
    private final DamsOptions options;

    public H2AdvisoryLock(Connection conn, DamsOptions options) {
        this.conn = conn;
        this.options = options;
    }

    @Override
    public void acquire() throws SQLException {
        // Create lock table if it doesn't exist
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(String.format("""
                CREATE TABLE IF NOT EXISTS %s (
                    lock_key BIGINT PRIMARY KEY,
                    acquired_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """, options.lockTableName()));
        }

        // Try to acquire lock by inserting a row
        long lockKey = options.lockId();
        long remaining = options.lockTimeoutMillis();

        while (remaining > 0) {
            try (PreparedStatement ps = conn.prepareStatement(
                String.format("INSERT INTO %s (lock_key) VALUES (?)", options.lockTableName()))) {
                ps.setLong(1, lockKey);
                ps.executeUpdate();
                return; // Successfully acquired lock
            } catch (SQLException e) {
                // Lock already exists, wait and retry
                try {
                    final long sleepStep = 1000L;
                    Thread.sleep(sleepStep);
                    remaining = remaining - sleepStep;
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new SQLException("Interrupted while waiting for advisory lock", ex);
                }
            }
        }
        throw new SQLTimeoutException("Advisory lock timeout for key=" + lockKey);
    }

    @Override
    public void close() throws SQLException {
        // Release lock by deleting the row
        try (PreparedStatement ps = conn.prepareStatement(
            String.format("DELETE FROM %s WHERE lock_key = ?", options.lockTableName()))) {
            ps.setLong(1, options.lockId());
            ps.executeUpdate();
        }
    }
}