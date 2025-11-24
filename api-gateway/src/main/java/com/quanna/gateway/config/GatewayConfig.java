package com.quanna.gateway.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class GatewayConfig {

    @Value("${rate-limit.default.capacity:20}")
    private long defaultCapacity;

    @Value("${rate-limit.default.refill-duration-seconds:1}")
    private long defaultRefillDurationSeconds;

    @Value("${rate-limit.demo-client.capacity:50}")
    private long demoClientCapacity;

    @Value("${rate-limit.demo-client.refill-duration-seconds:1}")
    private long demoClientRefillDurationSeconds;

    /**
     * Key resolver dựa trên IP address của client
     */
    @Bean
    @Primary
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String ip = Optional.ofNullable(exchange.getRequest().getHeaders().getFirst("X-Forwarded-For"))
                    .map(val -> val.split(",")[0].trim())
                    .orElseGet(() -> exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());
            return Mono.just(ip);
        };
    }

    /**
     * Key resolver dựa trên User ID từ header (dành cho authenticated requests)
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-ID");
            return Mono.just(userId != null ? userId : "anonymous");
        };
    }

    /**
     * Default rate limiter: Configurable capacity and refill duration
     */
    @Bean
    @Primary
    public RateLimiter<Bucket4jRateLimiter.Config> defaultRateLimiter() {
        return new Bucket4jRateLimiter(defaultCapacity, Duration.ofSeconds(defaultRefillDurationSeconds));
    }

    /**
     * Rate limiter cho demo-client: Configurable capacity and refill duration
     */
    @Bean("demoClientRateLimiter")
    public RateLimiter<Bucket4jRateLimiter.Config> demoClientRateLimiter() {
        return new Bucket4jRateLimiter(demoClientCapacity, Duration.ofSeconds(demoClientRefillDurationSeconds));
    }

    /**
     * Implementation of RateLimiter using Bucket4j
     */
    static class Bucket4jRateLimiter implements RateLimiter<Bucket4jRateLimiter.Config> {
        private final long capacity;
        private final Duration refillDuration;
        private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

        public Bucket4jRateLimiter(long capacity, Duration refillDuration) {
            this.capacity = capacity;
            this.refillDuration = refillDuration;
        }

        @Override
        public Mono<Response> isAllowed(String routeId, String id) {
            Bucket bucket = buckets.computeIfAbsent(id, k -> createBucket());

            // Try to consume 1 token
            var probe = bucket.tryConsumeAndReturnRemaining(1);

            boolean allowed = probe.isConsumed();
            long remainingTokens = probe.getRemainingTokens();

            // Calculate reset time in seconds
            long nanosToWait = probe.getNanosToWaitForRefill();
            long resetTimeSeconds = System.currentTimeMillis() / 1000 + (nanosToWait / 1_000_000_000);

            Map<String, String> headers = Map.of(
                "X-RateLimit-Remaining", String.valueOf(remainingTokens),
                "X-RateLimit-Limit", String.valueOf(capacity),
                "X-RateLimit-Reset", String.valueOf(resetTimeSeconds)
            );

            return Mono.just(new Response(allowed, headers));
        }

        @Override
        public Map<String, Config> getConfig() {
            Config config = new Config();
            config.setCapacity(capacity);
            config.setRefillDuration(refillDuration.toString());
            config.setImplementation("Bucket4j");

            return Map.of("default", config);
        }

        @Override
        public Class<Config> getConfigClass() {
            return Config.class;
        }

        @Override
        public Config newConfig() {
            return new Config();
        }

        private Bucket createBucket() {
            // Use the newer Bucket4j API with BandwidthBuilder
            Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(capacity, refillDuration)
                .build();

            // Build and return bucket
            return Bucket.builder()
                .addLimit(limit)
                .build();
        }

        /**
         * Configuration class for Bucket4j Rate Limiter
         */
        public static class Config {
            private long capacity;
            private String refillDuration;
            private String implementation;

            public long getCapacity() {
                return capacity;
            }

            public void setCapacity(long capacity) {
                this.capacity = capacity;
            }

            public String getRefillDuration() {
                return refillDuration;
            }

            public void setRefillDuration(String refillDuration) {
                this.refillDuration = refillDuration;
            }

            public String getImplementation() {
                return implementation;
            }

            public void setImplementation(String implementation) {
                this.implementation = implementation;
            }
        }
    }
}




