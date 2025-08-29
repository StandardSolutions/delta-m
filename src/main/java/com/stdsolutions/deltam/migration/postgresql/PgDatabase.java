package com.stdsolutions.deltam.migration.postgresql;

import com.stdsolutions.deltam.AdvisoryLock;
import com.stdsolutions.deltam.ChangeLog;
import com.stdsolutions.deltam.Database;
import com.stdsolutions.deltam.options.DamsOptions;

import java.sql.Connection;

public final class PgDatabase implements Database {

    @Override
    public AdvisoryLock newLock(Connection c, DamsOptions options) {
        return new PgAdvisoryLock(c, options);
    }

    @Override
    public ChangeLog changelog(DamsOptions options) {
        return new PgChangeLog(options);
    }
}