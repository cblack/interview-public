package com.weavelab.interview.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LeakyBucketRateLimiterTest {

    private MutableClock clock;
    private LeakyBucketRateLimiter limiter;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(
                Instant.parse("2026-01-01T00:00:00Z"));

        limiter = new LeakyBucketRateLimiter(
                10,
                10,
                clock);
    }

    @Test
    void allowsRequestsUntilCapacityIsReached() {
        for (int i = 0; i < 10; i++) {
            assertTrue(limiter.tryAcquire());
        }
    }

    @Test
    void rejectsRequestWhenBucketIsFull() {
        for (int i = 0; i < 10; i++) {
            assertTrue(limiter.tryAcquire());
        }

        assertFalse(limiter.tryAcquire());
    }

    @Test
    void allowsRequestAfterOneRequestHasLeaked() {
        for (int i = 0; i < 10; i++) {
            limiter.tryAcquire();
        }

        assertFalse(limiter.tryAcquire());

        clock.advance(Duration.ofSeconds(6));

        assertTrue(limiter.tryAcquire());
    }

    @Test
    void completelyDrainsBucketAfterOneMinute() {
        for (int i = 0; i < 10; i++) {
            limiter.tryAcquire();
        }

        clock.advance(Duration.ofMinutes(1));

        for (int i = 0; i < 10; i++) {
            assertTrue(limiter.tryAcquire());
        }

        assertFalse(limiter.tryAcquire());
    }

    @Test
    void rejectsRequestBeforeEnoughCapacityHasLeaked() {
        for (int i = 0; i < 10; i++) {
            assertTrue(limiter.tryAcquire());
        }

        clock.advance(Duration.ofSeconds(5));

        assertFalse(limiter.tryAcquire());
    }
    
    @Test
    void doesNotExceedCapacityUnderConcurrentRequests()
            throws InterruptedException {

        int threadCount = 100;
        int poolSize = 20;

        java.util.concurrent.ExecutorService executor =
                java.util.concurrent.Executors.newFixedThreadPool(poolSize);

        java.util.concurrent.CountDownLatch ready =
                new java.util.concurrent.CountDownLatch(poolSize);

        java.util.concurrent.CountDownLatch start =
                new java.util.concurrent.CountDownLatch(1);

        java.util.concurrent.atomic.AtomicInteger allowed =
                new java.util.concurrent.atomic.AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                ready.countDown();

                try {
                    start.await();

                    if (limiter.tryAcquire()) {
                        allowed.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        ready.await();
        start.countDown();

        executor.shutdown();

        boolean completed = executor.awaitTermination(
                5,
                java.util.concurrent.TimeUnit.SECONDS);

        org.junit.jupiter.api.Assertions.assertTrue(completed);
        org.junit.jupiter.api.Assertions.assertEquals(
                10,
                allowed.get());
    }
}