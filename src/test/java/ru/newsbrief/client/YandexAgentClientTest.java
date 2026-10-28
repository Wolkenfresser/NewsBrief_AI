package ru.newsbrief.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import ru.newsbrief.config.YandexProperties;
import ru.newsbrief.llm.LlmResult;

class YandexAgentClientTest {
    private static final String BASE_URL = "https://ai.api.cloud.yandex.net/v1";

    @Test
    void sendsApiKeyAndModelAndExtractsOutputText() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        YandexProperties properties = new YandexProperties(
                BASE_URL, "unit-test-key", "", "folder-123", "yandexgpt-5-lite", "", 5, 45);
        server.expect(requestTo(BASE_URL + "/responses"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Api-Key unit-test-key"))
                .andExpect(jsonPath("$.model").value("gpt://folder-123/yandexgpt-5-lite"))
                .andExpect(jsonPath("$.input").value("prompt for article summary"))
                .andRespond(withSuccess("{\"output_text\":\"Короткая аннотация\"}", MediaType.APPLICATION_JSON));

        YandexAgentClient client = new YandexAgentClient(builder.build(), properties);
        LlmResult result = client.generate("prompt for article summary");

        assertEquals("Короткая аннотация", result.text());
        assertEquals("Yandex AI Studio Agent", result.provider());
        assertFalse(result.fallbackUsed());
        server.verify();
    }

    @Test
    void referencesSavedPromptTemplateWhenConfigured() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        YandexProperties properties = new YandexProperties(
                BASE_URL, "unit-test-key", "", "folder-123", "yandexgpt-5-lite", "saved-prompt-7", 5, 45);
        server.expect(requestTo(BASE_URL + "/responses"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.prompt.id").value("saved-prompt-7"))
                .andExpect(jsonPath("$.input").value("prompt for article summary"))
                .andRespond(withSuccess("{\"output\":[{\"type\":\"message\",\"content\":[{\"type\":\"output_text\",\"text\":\"Текст из сохранённого шаблона\"}]}]}", MediaType.APPLICATION_JSON));

        YandexAgentClient client = new YandexAgentClient(builder.build(), properties);
        LlmResult result = client.generate("prompt for article summary");

        assertEquals("Текст из сохранённого шаблона", result.text());
        server.verify();
    }

    @Test
    void refusesToCallApiWithoutCredentials() {
        YandexProperties properties = new YandexProperties(
                BASE_URL, "", "", "folder-123", "yandexgpt-5-lite", "", 5, 45);
        YandexAgentClient client = new YandexAgentClient(RestClient.builder().baseUrl(BASE_URL).build(), properties);
        assertThrows(IllegalStateException.class, () -> client.generate("prompt"));
    }
}
