package com.quanna.democlient.controller;

import com.quanna.democlient.config.AppConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RefreshScope
public class ConfigController {

    private final AppConfig appConfig;

    @Value("${message:Default Message}")
    private String message;

    @Value("${db.connection.timeout:0}")
    private int dbTimeout;

    @Value("${db.pool.size:0}")
    private int dbPoolSize;

    @Value("${feature.newUI:false}")
    private boolean newUIEnabled;

    @Value("${feature.betaFeatures:false}")
    private boolean betaFeaturesEnabled;

    public ConfigController(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    @GetMapping("/config")
    public Map<String, Object> getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("message", message);
        config.put("app.description", appConfig.getDescription());
        config.put("app.version", appConfig.getVersion());
        config.put("db.connection.timeout", dbTimeout);
        config.put("db.pool.size", dbPoolSize);
        config.put("feature.newUI", newUIEnabled);
        config.put("feature.betaFeatures", betaFeaturesEnabled);
        return config;
    }

    @GetMapping("/message")
    public String getMessage() {
        return message;
    }

    @GetMapping("/features")
    public Map<String, Boolean> getFeatures() {
        Map<String, Boolean> features = new HashMap<>();
        features.put("newUI", newUIEnabled);
        features.put("betaFeatures", betaFeaturesEnabled);
        return features;
    }
}

