package com.stdsolutions.deltam;

import com.stdsolutions.deltam.options.MigrationOptions;

import java.sql.Connection;

public interface Database {

    AdvisoryLock newLock(Connection c, MigrationOptions options);

    ChangeLog changelog(MigrationOptions options);
}