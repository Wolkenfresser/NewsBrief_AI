package ru.newsbrief.llm;

public record LlmResult(String text, String provider, boolean fallbackUsed) {
    public LlmResult {
        if (text == null) text = "";
        if (provider == null || provider.isBlank()) provider = "unknown";
    }
}
