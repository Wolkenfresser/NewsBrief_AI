package ru.newsbrief.llm;

/** Provider-neutral contract. Implementations return plain text plus safe execution metadata. */
public interface LlmClient {
    String providerName();
    LlmResult generate(String input);
}
