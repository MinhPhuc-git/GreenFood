package com.example.GreenFood.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RoboflowConfig {

    @Value("${roboflow.api-key:}")
    private String apiKey;

    @Value("${roboflow.model:}")
    private String model;

    @Value("${roboflow.version:1}")
    private String version;

    @Value("${roboflow.confidence:40}")
    private int confidence;

    @Value("${roboflow.api-url:https://detect.roboflow.com}")
    private String apiUrl;

    public String getApiKey() {
        return apiKey;
    }

    public String getModel() {
        return model;
    }

    public String getVersion() {
        return version;
    }

    public int getConfidence() {
        return confidence;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank()
                && model != null && !model.isBlank();
    }
}
