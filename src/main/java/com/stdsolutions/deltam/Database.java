package com.stdsolutions.deltam;

import com.stdsolutions.deltam.options.DamsOptions;

import java.sql.Connection;

public interface Database {

    AdvisoryLock newLock(Connection c, DamsOptions options);

    ChangeLog changelog(DamsOptions options);
}