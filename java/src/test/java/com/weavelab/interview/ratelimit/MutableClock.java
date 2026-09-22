package com.weavelab.interview.ratelimit;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

public class MutableClock extends Clock {

    private Instant currentInstant;
    private final ZoneId zone;

    public MutableClock(Instant currentInstant) {
        this(currentInstant, ZoneId.of("UTC"));
    }

    private MutableClock(
            Instant currentInstant,
            ZoneId zone) {

        this.currentInstant = currentInstant;
        this.zone = zone;
    }

    public void advance(Duration duration) {
        currentInstant = currentInstant.plus(duration);
    }

    @Override
    public ZoneId getZone() {
        return zone;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return new MutableClock(currentInstant, zone);
    }

    @Override
    public Instant instant() {
        return currentInstant;
    }
}
