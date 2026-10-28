package ru.newsbrief.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.yandex")
public record YandexProperties(
        String baseUrl,
        String apiKey,
        String iamToken,
        String folderId,
        String model,
        String promptId,
        int connectTimeoutSeconds,
        int readTimeoutSeconds) {

    public boolean hasCredentials() {
        return (apiKey != null && !apiKey.isBlank()) || (iamToken != null && !iamToken.isBlank());
    }

    public boolean isReady() {
        return hasCredentials() && folderId != null && !folderId.isBlank();
    }

    public String authorizationHeader() {
        if (iamToken != null && !iamToken.isBlank()) return "Bearer " + iamToken.trim();
        if (apiKey != null && !apiKey.isBlank()) return "Api-Key " + apiKey.trim();
        throw new IllegalStateException("Yandex credentials are not configured");
    }
}
