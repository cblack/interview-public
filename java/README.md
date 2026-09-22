## Rate Limiting

A process-local global rate limiter has been added using a Leaky Bucket algorithm.

Configuration:
- Capacity: 10 requests
- Leak rate: 10 requests per minute
- Scope: global across all clients within a single application process
- Rejection response: HTTP 429 Too Many Requests

Implementation notes:
- Bucket state is synchronized for thread-safe concurrent access.
- Leakage is calculated lazily when requests arrive rather than using a background timer.
- Time is injected through `Clock` to allow deterministic tests.
- `/health` is excluded from rate limiting so infrastructure health checks remain available.

This implementation intentionally does not provide distributed rate limiting across multiple application instances.