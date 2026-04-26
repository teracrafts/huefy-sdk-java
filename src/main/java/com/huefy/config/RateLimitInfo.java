package com.huefy.config;

import java.time.Instant;

/**
 * Rate limit information parsed from API response headers.
 *
 * @param limit     the maximum number of requests allowed in the window
 * @param remaining the number of requests remaining in the current window
 * @param resetAt   the instant at which the rate limit window resets
 */
public record RateLimitInfo(int limit, int remaining, Instant resetAt) {}
