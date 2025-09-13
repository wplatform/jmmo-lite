package com.github.azeroth.time;

import java.time.Duration;

public class TimeTracker {
    private Duration expiryTime;

    /**
     * Constructor that sets expiration time using milliseconds
     * @param expiry Expiration time in milliseconds
     */
    public TimeTracker(int expiry) {
        this.expiryTime = Duration.ofMillis(expiry);
    }

    /**
     * Constructor that sets expiration time using Duration
     * @param expiry Expiration time
     */
    public TimeTracker(Duration expiry) {
        this.expiryTime = expiry;
    }

    /**
     * Update remaining time (in milliseconds)
     * @param diff Elapsed time in milliseconds
     */
    public void update(int diff) {
        update(Duration.ofMillis(diff));
    }

    /**
     * Update remaining time
     * @param diff Elapsed time
     */
    public void update(Duration diff) {
        expiryTime = expiryTime.minus(diff);
        if (expiryTime.isNegative()) {
            expiryTime = Duration.ZERO;
        }
    }

    /**
     * Check if the time has expired
     * @return true if the time has expired
     */
    public boolean passed() {
        return expiryTime.isZero() || expiryTime.isNegative();
    }

    /**
     * Reset expiration time (in milliseconds)
     * @param expiry New expiration time in milliseconds
     */
    public void reset(int expiry) {
        reset(Duration.ofMillis(expiry));
    }

    /**
     * Reset expiration time
     * @param expiry New expiration time
     */
    public void reset(Duration expiry) {
        this.expiryTime = expiry;
    }

    /**
     * Get the remaining expiration time
     * @return Remaining expiration time
     */
    public Duration getExpiry() {
        return expiryTime;
    }
}
