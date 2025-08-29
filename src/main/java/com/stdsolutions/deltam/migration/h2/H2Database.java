package com.stdsolutions.deltam.migration.h2;

import com.stdsolutions.deltam.AdvisoryLock;
import com.stdsolutions.deltam.ChangeLog;
import com.stdsolutions.deltam.Database;
import com.stdsolutions.deltam.options.DamsOptions;

import java.sql.Connection;

public final class H2Database implements Database {

    @Override
    public AdvisoryLock newLock(Connection c, DamsOptions options) {
        return new H2AdvisoryLock(c, options);
    }

    @Override
    public ChangeLog changelog(DamsOptions options) {
        return new H2ChangeLog(options);
    }
}