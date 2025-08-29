package com.stdsolutions.deltam.metadata;

public enum DatabaseType {

    POSTGRESQL("postgreSQL"),

    H2("H2");

    DatabaseType(String value) {
        this.value = value;
    }

    public final String value;

    /**
     * Detect database type of jdbc url.
     *
     * @param jdbcUrl jdbc url connection string.
     * @return DatabaseType enum.
     */
    public static DatabaseType ofJdbcUrl(String jdbcUrl) {
        if (jdbcUrl.startsWith("jdbc:postgresql:")) return POSTGRESQL;
        if (jdbcUrl.startsWith("jdbc:h2:")) return H2;
        throw new UnsupportedDatabaseException("Supported Database: PostgreSQL, H2. jdbcUrl: " + jdbcUrl);
    }

    /**
     * Database type from string.
     *
     * @param productName as string.
     * @return DatabaseType enum.
     */
    public static DatabaseType of(String productName) {
        for (DatabaseType databaseType : values()) {
            if (databaseType.name().equalsIgnoreCase(productName)) {
                return databaseType;
            }
        }
        throw new UnsupportedDatabaseException("Supported Database: PostgreSQL, H2. Current database: " + productName);
    }
}