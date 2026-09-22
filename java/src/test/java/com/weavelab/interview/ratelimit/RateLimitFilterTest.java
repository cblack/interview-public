package com.weavelab.interview.ratelimit;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RateLimitFilterTest {

    @Test
    void returns429WhenRateLimitIsExceeded() throws Exception {
        RateLimitFilter filter = new RateLimitFilter();

        FilterChain chain = (request, response) -> {
        };

        for (int i = 0; i < 10; i++) {
            MockHttpServletRequest request =
                    new MockHttpServletRequest("GET", "/api/contacts");

            MockHttpServletResponse response =
                    new MockHttpServletResponse();

            filter.doFilter(request, response, chain);

            assertEquals(200, response.getStatus());
        }

        MockHttpServletRequest request =
                new MockHttpServletRequest("GET", "/api/contacts");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertEquals(429, response.getStatus());
    }

    @Test
    void doesNotRateLimitHealthEndpoint() throws Exception {
        RateLimitFilter filter = new RateLimitFilter();

        FilterChain chain = (request, response) -> {
        };

        for (int i = 0; i < 20; i++) {
            MockHttpServletRequest request =
                    new MockHttpServletRequest("GET", "/health");

            MockHttpServletResponse response =
                    new MockHttpServletResponse();

            filter.doFilter(request, response, chain);

            assertEquals(200, response.getStatus());
        }
    }
}