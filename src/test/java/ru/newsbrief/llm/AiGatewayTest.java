package ru.newsbrief.llm;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import ru.newsbrief.config.LlmProperties;
import ru.newsbrief.config.YandexProperties;
import ru.newsbrief.client.InternalLlmClient;

class AiGatewayTest {
    private final InternalLlmClient internal = new InternalLlmClient();

    @Test
    void usesInternalProviderWhenConfiguredExplicitly() {
        YandexInvoker invoker = mock(YandexInvoker.class);
        AiGateway gateway = new AiGateway(invoker, internal, new LlmProperties("internal"),
                new YandexProperties("https://ai.api.cloud.yandex.net/v1", "", "", "folder", "yandexgpt-5-lite", "", 5, 45),
                new SimpleMeterRegistry());
        LlmResult result = gateway.generateSummary("ЗАГОЛОВОК: Тест\n<<<ARTICLE>>>\nСтатья для внутреннего режима содержит достаточно длинное предложение.\n<<<END ARTICLE>>>");
        assertEquals("Internal demo (offline)", result.provider());
        assertFalse(result.fallbackUsed());
        verifyNoInteractions(invoker);
    }

    @Test
    void fallsBackAfterRemoteProviderFailure() {
        YandexInvoker invoker = mock(YandexInvoker.class);
        when(invoker.generate(anyString())).thenThrow(new IllegalStateException("synthetic remote failure"));
        AiGateway gateway = new AiGateway(invoker, internal, new LlmProperties("yandex"),
                new YandexProperties("https://ai.api.cloud.yandex.net/v1", "test-key-not-a-secret", "", "folder", "yandexgpt-5-lite", "", 5, 45),
                new SimpleMeterRegistry());
        LlmResult result = gateway.generateSummary("ЗАГОЛОВОК: Тест\n<<<ARTICLE>>>\nСтатья для проверки fallback содержит достаточно длинное предложение.\n<<<END ARTICLE>>>");
        assertTrue(result.fallbackUsed());
        assertTrue(result.provider().startsWith("Internal fallback"));
        assertTrue(result.text().contains("Тест"));
    }
}
