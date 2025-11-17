package com.quanna.eureka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
@EnableEurekaServer
@RestController
@RequestMapping("/api")
public class EurekaServerApplication {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private WebClient.Builder webClientBuilder;

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }

    @GetMapping("/instance-info/{serviceName}")
    public Map<String, Object> getInstanceInfo(@PathVariable String serviceName) {
        try {
            String url = "http://" + serviceName + "/instance-info";
            Object response = restTemplate.getForObject(url, Object.class);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("serviceName", serviceName);
            result.put("instanceInfo", response);
            result.put("timestamp", System.currentTimeMillis());

            return result;
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("serviceName", serviceName);
            error.put("error", e.getMessage());
            error.put("timestamp", System.currentTimeMillis());
            return error;
        }
    }

    @GetMapping("/load-balance-compare/{serviceName}/{count}")
    public Mono<String> loadBalanceCompare(@PathVariable String serviceName, @PathVariable int count) {
        // Run both blocking and reactive tests to compare performance

        return Mono.fromCallable(() -> {
                    // Blocking test
                    long blockingStart = System.currentTimeMillis();
                    Map<String, Integer> blockingCounts = new HashMap<>();

                    for (int i = 0; i < count; i++) {
                        try {
                            String url = "http://" + serviceName + "/instance-info";
                            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
                            String instanceId = response != null ? (String) response.get("instanceId") : "unknown";
                            blockingCounts.put(instanceId, blockingCounts.getOrDefault(instanceId, 0) + 1);
                        } catch (Exception e) {
                            // Ignore errors for comparison
                        }
                    }

                    long blockingTime = System.currentTimeMillis() - blockingStart;

                    Map<String, Object> blockingResult = new HashMap<>();
                    blockingResult.put("executionTimeMs", blockingTime);
                    blockingResult.put("instanceDistribution", blockingCounts);
                    blockingResult.put("type", "blocking-resttemplate");

                    return blockingResult;
                })
                .flatMap(blockingResult -> {
                    // Reactive test
                    long reactiveStart = System.currentTimeMillis();
                    WebClient webClient = webClientBuilder.build();
                    ConcurrentHashMap<String, Integer> reactiveCounts = new ConcurrentHashMap<>();

                    return Flux.range(0, count)
                            .flatMap(i ->
                                    webClient.get()
                                            .uri("http://" + serviceName + "/instance-info")
                                            .retrieve()
                                            .bodyToMono(Map.class)
                                            .doOnNext(response -> {
                                                String instanceId = response != null ? (String) response.get("instanceId") : "unknown";
                                                reactiveCounts.merge(instanceId, 1, Integer::sum);
                                            })
                                            .onErrorResume(error -> Mono.just(new HashMap<>()))
                            )
                            .collectList()
                            .map(responses -> {
                                long reactiveTime = System.currentTimeMillis() - reactiveStart;

                                Map<String, Object> reactiveResult = new HashMap<>();
                                reactiveResult.put("executionTimeMs", reactiveTime);
                                reactiveResult.put("instanceDistribution", reactiveCounts);
                                reactiveResult.put("type", "reactive-webclient");

                                // Comparison result
                                Map<String, Object> comparison = new HashMap<>();
                                comparison.put("serviceName", serviceName);
                                comparison.put("totalRequests", count);
                                comparison.put("blocking", blockingResult);
                                comparison.put("reactive", reactiveResult);

                                long blockingTimeMs = (Long) blockingResult.get("executionTimeMs");
                                long improvement = ((blockingTimeMs - reactiveTime) * 100) / blockingTimeMs;
                                comparison.put("performanceImprovement", improvement + "%");
                                comparison.put("fasterBy", blockingTimeMs - reactiveTime + "ms");
                                comparison.put("timestamp", System.currentTimeMillis());

                                com.google.gson.Gson gson = new com.google.gson.Gson();
                                return gson.toJson(comparison);
                            });
                });
    }
}


