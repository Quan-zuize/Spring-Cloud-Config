package com.quanna.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "spring.cloud.config.server.native")
public class NativeConfigProps {
    private List<String> searchLocations;

    public List<String> getSearchLocations() {
        return searchLocations;
    }
    public void setSearchLocations(List<String> searchLocations) {
        this.searchLocations = searchLocations;
    }
}

