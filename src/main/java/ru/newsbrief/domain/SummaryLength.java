package ru.newsbrief.domain;

/** Domain policy for the target size of a news feed annotation. */
public enum SummaryLength {
    SHORT("короткая (примерно до 350–400 символов)"),
    STANDARD("стандартная (примерно до 700 символов)");

    private final String promptLabel;
    SummaryLength(String promptLabel) { this.promptLabel = promptLabel; }
    public String promptLabel() { return promptLabel; }

    public static SummaryLength from(String raw) {
        return raw != null && raw.equalsIgnoreCase("short") ? SHORT : STANDARD;
    }
}
