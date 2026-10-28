package ru.newsbrief.client;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import ru.newsbrief.config.YandexProperties;
import ru.newsbrief.llm.LlmClient;
import ru.newsbrief.llm.LlmResult;

/** Adapter for Yandex AI Studio Responses API and saved prompt/agent configurations. */
@Component
public class YandexAgentClient implements LlmClient {
    private static final Logger log = LoggerFactory.getLogger(YandexAgentClient.class);
    private final RestClient client;
    private final YandexProperties properties;

    public YandexAgentClient(RestClient yandexRestClient, YandexProperties properties) {
        this.client = yandexRestClient;
        this.properties = properties;
    }

    @Override public String providerName() { return "yandex"; }

    @Override
    @SuppressWarnings("unchecked")
    public LlmResult generate(String input) {
        if (!properties.hasCredentials()) throw new IllegalStateException("Yandex AI credentials are not configured");
        Map<String, Object> body = new LinkedHashMap<>();
        if (StringUtils.hasText(properties.promptId())) {
            body.put("prompt", Map.of("id", properties.promptId().trim()));
        } else {
            if (!StringUtils.hasText(properties.folderId())) throw new IllegalStateException("YANDEX_FOLDER_ID is required for Yandex AI Studio");
            body.put("model", "gpt://" + properties.folderId().trim() + "/" + safeModel(properties.model()));
            body.put("instructions", "Ты — редакционный AI-помощник новостной медиаплатформы. Пиши на русском. Создавай краткие, нейтральные и точные аннотации ТОЛЬКО по переданному тексту. Не добавляй внешние факты, оценки или сведения, которых нет в статье. Сохраняй точные имена, даты и числа. При неопределённости не делай категоричных утверждений.");
            body.put("temperature", 0.2);
            body.put("max_output_tokens", 1000);
        }
        body.put("input", input);

        log.info("Calling Yandex AI Studio Responses API; article content and credentials are not logged");
        Map<String, Object> response = client.post()
                .uri(responsesEndpoint())
                .header(HttpHeaders.AUTHORIZATION, properties.authorizationHeader())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .headers(headers -> {
                    if (StringUtils.hasText(properties.folderId())) headers.set("OpenAI-Project", properties.folderId().trim());
                })
                .body(body)
                .retrieve()
                .body(Map.class);

        String text = extractOutput(response);
        if (!StringUtils.hasText(text)) throw new IllegalStateException("Yandex AI Studio returned an empty text response");
        return new LlmResult(text.trim(), "Yandex AI Studio Agent", false);
    }

    private String responsesEndpoint() {
        if (!StringUtils.hasText(properties.baseUrl())) {
            throw new IllegalStateException("YANDEX_BASE_URL is not configured");
        }
        // Build an absolute URI to avoid accidentally replacing the configured /v1 path
        // with a root-relative path when RestClient resolves a leading slash.
        return properties.baseUrl().trim().replaceAll("/+$", "") + "/responses";
    }

    private static String safeModel(String model) {
        return StringUtils.hasText(model) ? model.trim() : "yandexgpt-5-lite";
    }

    private static String extractOutput(Map<String, Object> response) {
        if (response == null) return "";
        Object outputText = response.get("output_text");
        if (outputText instanceof String s && StringUtils.hasText(s)) return s.trim();
        Object output = response.get("output");
        if (!(output instanceof List<?> items)) return "";
        List<String> textParts = new ArrayList<>();
        for (Object item : items) {
            if (!(item instanceof Map<?, ?> map)) continue;
            Object content = map.get("content");
            if (!(content instanceof List<?> blocks)) continue;
            for (Object block : blocks) {
                if (block instanceof Map<?, ?> contentMap && contentMap.get("text") instanceof String s && StringUtils.hasText(s)) textParts.add(s.trim());
            }
        }
        return String.join("\n", textParts);
    }
}
