package com.stdsolutions.deltam;

import java.sql.Connection;

public interface ChangeLog {
    void ensureExist(Connection c);

    boolean has(Connection c, MigrationStep migration);

    void append(Connection c, MigrationStep migration);
}
