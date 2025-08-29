package com.stdsolutions.deltam.metadata;

/**
 * Exception thrown when database type is not supported
 * or cannot be determined from JDBC URL.
 */
public class UnsupportedDatabaseException extends RuntimeException {
    
    public UnsupportedDatabaseException(String message) {
        super(message);
    }
    
    public UnsupportedDatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public UnsupportedDatabaseException(Throwable cause) {
        super(cause);
    }
} 