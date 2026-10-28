package ru.newsbrief.dto;

public record SummaryResponse(
        String summary,
        String provider,
        String requestId,
        boolean fallbackUsed,
        long processingTimeMs,
        int outputCharacters) {
}
