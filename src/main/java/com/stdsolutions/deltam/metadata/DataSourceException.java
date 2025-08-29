package com.stdsolutions.deltam.metadata;

/**
 * Exception thrown when database type is not supported
 * or cannot be determined from JDBC URL.
 */
public class DataSourceException extends RuntimeException {

    public DataSourceException(String message) {
        super(message);
    }

    public DataSourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataSourceException(Throwable cause) {
        super(cause);
    }
} 