package com.stdsolutions.deltam;

import java.sql.SQLException;

/**
 * Represents an application-level (advisory) lock that coordinates execution
 * between multiple application instances connected to the same database.
 * <p>
 * Unlike table or row locks that the database acquires automatically,
 * this lock is explicitly requested and released by the application in order
 * to serialize critical sections such as schema migrations or bootstrap tasks.
 * </p>
 * <p>
 * Implementations may use database-specific mechanisms:
 * <ul>
 *   <li>PostgreSQL: {@code pg_advisory_lock},</li>
 *   <li>SQL Server: {@code sp_getapplock},</li>
 *   <li>MySQL: {@code GET_LOCK},</li>
 *   <li>H2 (and others without native advisory locks): a dedicated lock table.</li>
 * </ul>
 * </p>
 *
 * <p>Typical usage:</p>
 * <pre>{@code
 * try (AdvisoryLock lock = dialect.newAdvisoryLock(connection, dataOptions)) {
 *     lock.acquire();
 *     // perform bootstrap / migration
 * }
 * }</pre>
 */
public interface AdvisoryLock extends AutoCloseable {

    /**
     * Attempts to acquire the lock, blocking until either the lock is acquired
     * or the timeout expires.
     *
     * @throws SQLException if a database error occurs or if the timeout is reached
     *                      without successfully acquiring the lock
     */
    void acquire() throws SQLException;

    /**
     * Releases the lock. This method is called automatically when used
     * inside a try-with-resources block.
     *
     * @throws SQLException if a database error occurs during release
     */
    @Override
    void close() throws SQLException;
}