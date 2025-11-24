package com.quanna.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        Instant startTime = Instant.now();

        logger.info("Incoming request: {} {} from {}",
                request.getMethod(),
                request.getURI(),
                request.getRemoteAddress());

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            ServerHttpResponse response = exchange.getResponse();
            Duration duration = Duration.between(startTime, Instant.now());

            logger.info("Outgoing response: {} {} - Status: {} - Duration: {}ms",
                    request.getMethod(),
                    request.getURI(),
                    response.getStatusCode(),
                    duration.toMillis());
        }));
    }

    @Override
    public int getOrder() {
        return -1; // Execute before other filters
    }
}

