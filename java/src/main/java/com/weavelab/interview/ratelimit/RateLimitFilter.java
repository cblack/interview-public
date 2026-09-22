package com.weavelab.interview.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Clock;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int CAPACITY = 10;
    private static final int REQUESTS_PER_MINUTE = 10;

    private final LeakyBucketRateLimiter rateLimiter;

    public RateLimitFilter() {
        this(new LeakyBucketRateLimiter(
                CAPACITY,
                REQUESTS_PER_MINUTE,
                Clock.systemUTC()));
    }

    RateLimitFilter(LeakyBucketRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        if (!rateLimiter.tryAcquire()) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"error\":\"rate limit exceeded\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "/health".equals(request.getRequestURI());
    }
}