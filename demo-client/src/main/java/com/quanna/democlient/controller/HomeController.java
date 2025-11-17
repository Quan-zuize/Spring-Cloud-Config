package com.quanna.democlient.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HomeController {

    @Value("${server.port:8080}")
    private String port;

    @Value("${spring.application.name:unknown}")
    private String applicationName;

    @Value("${eureka.instance.instance-id:${spring.application.name}:${server.port}}")
    private String instanceId;

    @GetMapping("/instance-info")
    public Map<String, Object> getInstanceInfo() throws InterruptedException {
        Map<String, Object> info = new HashMap<>();

        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            info.put("hostName", inetAddress.getHostName());
            info.put("hostAddress", inetAddress.getHostAddress());
        } catch (UnknownHostException e) {
            info.put("hostName", "unknown");
            info.put("hostAddress", "unknown");
        }

        info.put("port", port);
        info.put("applicationName", applicationName);
        info.put("instanceId", instanceId);
        info.put("timestamp", System.currentTimeMillis());
        Thread.sleep(100); // Simulate processing delay
        return info;
    }
}
