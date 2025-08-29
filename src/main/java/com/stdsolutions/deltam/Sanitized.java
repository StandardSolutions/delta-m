package com.stdsolutions.deltam;

/**
 * Base interface for all sanitizers
 */
public interface Sanitized<T> {
    T value() throws IllegalArgumentException;
}
