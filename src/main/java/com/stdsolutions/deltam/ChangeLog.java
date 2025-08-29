package com.stdsolutions.deltam;

import java.sql.Connection;
import java.sql.SQLException;

public interface ChangeLog {
    void ensureExist(Connection c) throws SQLException;

    boolean has(Connection c, MigrationStep migration) throws SQLException;

    void append(Connection c, MigrationStep migration) throws SQLException;
}
