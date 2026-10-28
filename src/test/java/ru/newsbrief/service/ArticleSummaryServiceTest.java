package ru.newsbrief.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;
import ru.newsbrief.llm.AiGateway;
import ru.newsbrief.llm.LlmResult;
import ru.newsbrief.dto.SummaryRequest;

class ArticleSummaryServiceTest {
    @Test
    void buildsContextualPromptAndReturnsMetadata() {
        AiGateway gateway = mock(AiGateway.class);
        when(gateway.generateSummary(anyString())).thenReturn(new LlmResult("## Аннотация\nТекст.", "Yandex AI Studio Agent", false));
        ArticleSummaryService service = new ArticleSummaryService(gateway);
        var response = service.summarize(new SummaryRequest("Тестовый заголовок", "Редакция", "Текст статьи достаточно длинный для проверки вызова сервиса и генерации аннотации.", "short", true));

        assertTrue(response.summary().contains("Аннотация"));
        assertEquals("Yandex AI Studio Agent", response.provider());
        assertFalse(response.fallbackUsed());
        assertNotNull(response.requestId());
        assertTrue(response.processingTimeMs() >= 0);
        verify(gateway).generateSummary(argThat(prompt -> prompt.contains("Тестовый заголовок")
                && prompt.contains("Редакция") && prompt.contains("<<<ARTICLE>>>")
                && prompt.contains("КЛЮЧЕВЫЕ ТЕЗИСЫ: ВКЛЮЧИТЬ")));
    }

    @Test
    void supportsSummaryWithoutKeyPoints() {
        AiGateway gateway = mock(AiGateway.class);
        when(gateway.generateSummary(anyString())).thenReturn(new LlmResult("Аннотация", "Internal demo (offline)", false));
        var service = new ArticleSummaryService(gateway);
        service.summarize(new SummaryRequest(null, null, "Это демонстрационный текст статьи, который содержит больше сорока символов.", "standard", false));
        verify(gateway).generateSummary(argThat(prompt -> prompt.contains("КЛЮЧЕВЫЕ ТЕЗИСЫ: НЕ ВКЛЮЧАТЬ")));
    }

    @Test
    void rejectsEmptyProviderOutput() {
        AiGateway gateway = mock(AiGateway.class);
        when(gateway.generateSummary(anyString())).thenReturn(new LlmResult(" ", "Yandex AI Studio Agent", false));
        var service = new ArticleSummaryService(gateway);
        assertThrows(IllegalStateException.class, () -> service.summarize(
                new SummaryRequest(null, null, "Текст демонстрационной статьи с достаточным количеством символов для запроса.", null, true)));
    }
}
