package ru.newsbrief.llm;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Component;
import ru.newsbrief.client.YandexAgentClient;

/** Resilience boundary for the remote provider. The Gateway owns the fallback policy. */
@Component
public class YandexInvoker {
    private final YandexAgentClient client;
    public YandexInvoker(YandexAgentClient client) { this.client = client; }

    @Retry(name = "yandexAgent")
    @CircuitBreaker(name = "yandexAgent")
    public LlmResult generate(String input) { return client.generate(input); }
}
