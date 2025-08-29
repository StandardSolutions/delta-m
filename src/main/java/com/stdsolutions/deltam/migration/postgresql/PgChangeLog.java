package com.stdsolutions.deltam.migration.postgresql;

import com.stdsolutions.deltam.ChangeLog;
import com.stdsolutions.deltam.MigrationStep;

import java.sql.Connection;

public final class PgChangeLog implements ChangeLog {
    @Override
    public void ensureExist(Connection c) {


    }

    @Override
    public boolean has(Connection c, MigrationStep migration) {
        return false;
    }

    @Override
    public void append(Connection c, MigrationStep migration) {

    }
}