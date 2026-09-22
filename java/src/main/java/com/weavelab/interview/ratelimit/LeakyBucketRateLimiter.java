package com.weavelab.interview.ratelimit;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public class LeakyBucketRateLimiter {

    private final int capacity;
    private final double leakRatePerSecond;
    private final Clock clock;

    private double currentLevel;
    private Instant lastUpdated;

    public LeakyBucketRateLimiter(
            int capacity,
            int requestsPerMinute,
            Clock clock) {

        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be greater than zero");
        }

        if (requestsPerMinute <= 0) {
            throw new IllegalArgumentException("requestsPerMinute must be greater than zero");
        }

        this.capacity = capacity;
        this.leakRatePerSecond = requestsPerMinute / 60.0;
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
        this.lastUpdated = clock.instant();
    }

    public synchronized boolean tryAcquire() {
        leak();

        if (currentLevel + 1.0 > capacity) {
            return false;
        }

        currentLevel += 1.0;
        return true;
    }

    private void leak() {
        Instant now = clock.instant();

        double elapsedSeconds =
                Duration.between(lastUpdated, now).toNanos()
                        / 1_000_000_000.0;

        if (elapsedSeconds <= 0.0) {
            return;
        }

        double leakedAmount = elapsedSeconds * leakRatePerSecond;

        currentLevel = Math.max(
                0.0,
                currentLevel - leakedAmount);

        lastUpdated = now;
    }
}