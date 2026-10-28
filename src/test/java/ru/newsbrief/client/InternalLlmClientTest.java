package ru.newsbrief.client;

import ru.newsbrief.llm.LlmResult;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class InternalLlmClientTest {
    private final InternalLlmClient client = new InternalLlmClient();

    @Test
    void createsDeterministicOfflineSummaryFromArticleMarkers() {
        String prompt = "ЗАГОЛОВОК: Заголовок теста\nЖЕЛАЕМАЯ ДЛИНА: стандартная\nКЛЮЧЕВЫЕ ТЕЗИСЫ: ВКЛЮЧИТЬ\nТЕКСТ СТАТЬИ:\n<<<ARTICLE>>>\nПервое предложение содержит важные данные о проекте. Второе предложение рассказывает о планах участников. Третье предложение описывает следующий этап работы.\n<<<END ARTICLE>>>";
        LlmResult result = client.generate(prompt);
        assertTrue(result.text().contains("Заголовок теста"));
        assertTrue(result.text().contains("Ключевые тезисы"));
        assertEquals("Internal demo (offline)", result.provider());
        assertFalse(result.fallbackUsed());
    }

    @Test
    void respectsOptionToOmitKeyPoints() {
        LlmResult result = client.generate("ЗАГОЛОВОК: Тест\nЖЕЛАЕМАЯ ДЛИНА: короткая\nКЛЮЧЕВЫЕ ТЕЗИСЫ: НЕ ВКЛЮЧАТЬ\n<<<ARTICLE>>>\nПервое предложение в статье содержит длинное и важное описание события для проверки. Второе предложение содержит сведения о последующих действиях команды.\n<<<END ARTICLE>>>");
        assertFalse(result.text().contains("Ключевые тезисы"));
    }
}
