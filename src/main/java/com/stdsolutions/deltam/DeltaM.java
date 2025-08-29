package com.stdsolutions.deltam;

import com.stdsolutions.deltam.metadata.DatabaseMetadata;
import com.stdsolutions.deltam.options.DamsOptions;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public final class DeltaM {

    private final DataSource dataSource;

    private final String[] args;

    public DeltaM(final DataSource dataSource, final String... args) {
        this.dataSource = dataSource;
        this.args = args;
    }

    public void init() throws SQLException, IOException {
        DamsOptions options = new DamsOptions(this.args);
        try (Connection c = dataSource.getConnection()) {
            DatabaseMetadata metadata = new DatabaseMetadata(c);
            Database db = metadata.database();
            try (AdvisoryLock lock = db.newLock(c, options)) {
                lock.acquire();

                ChangeLog changeLog = db.changelog();
                changeLog.ensureExist(c);

                MigrationLoader migrationLoader = new MigrationLoader(options, metadata.type());
                for (MigrationStep migration : migrationLoader.steps()) {
                    if (changeLog.has(c, migration)) {
                        continue;
                    }
                    migration.execute(c);
                    changeLog.append(c, migration);
                }
            }
        }
    }
}