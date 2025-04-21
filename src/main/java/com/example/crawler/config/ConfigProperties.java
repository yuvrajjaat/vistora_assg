package com.example.crawler.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "crawler.config")
public class ConfigProperties {
    private String path;
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
}