package ru.newsbrief.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.llm")
public record LlmProperties(String activeProvider) {
    public String normalizedProvider() {
        return activeProvider == null || activeProvider.isBlank() ? "yandex" : activeProvider.trim().toLowerCase();
    }
}
