package ru.newsbrief.llm;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.newsbrief.config.LlmProperties;
import ru.newsbrief.config.YandexProperties;
import ru.newsbrief.client.InternalLlmClient;

/** Central provider selection, safe fallback, and provider-level observability. */
@Component
public class AiGateway {
    private static final Logger log = LoggerFactory.getLogger(AiGateway.class);
    private final YandexInvoker yandexInvoker;
    private final InternalLlmClient internalClient;
    private final LlmProperties llmProperties;
    private final YandexProperties yandexProperties;
    private final MeterRegistry registry;

    public AiGateway(YandexInvoker yandexInvoker, InternalLlmClient internalClient,
                     LlmProperties llmProperties, YandexProperties yandexProperties,
                     MeterRegistry registry) {
        this.yandexInvoker = yandexInvoker;
        this.internalClient = internalClient;
        this.llmProperties = llmProperties;
        this.yandexProperties = yandexProperties;
        this.registry = registry;
    }

    public LlmResult generateSummary(String prompt) {
        String selected = llmProperties.normalizedProvider();
        if ("internal".equals(selected)) return tracked(internalClient.generate(prompt), "internal", "success");
        if (!"yandex".equals(selected)) throw new IllegalStateException("Unsupported app.llm.active-provider: " + selected);
        if (!yandexProperties.isReady()) {
            String reason = yandexProperties.hasCredentials() ? "configuration_incomplete" : "credentials_missing";
            log.warn("Yandex provider is not fully configured; using internal demo provider");
            return fallback(prompt, reason);
        }
        try {
            return tracked(yandexInvoker.generate(prompt), "yandex", "success");
        } catch (Exception ex) {
            // Do not log article text, external response body, or authorization headers.
            log.warn("Yandex provider failed ({}); switching to internal provider", ex.getClass().getSimpleName());
            return fallback(prompt, "provider_error");
        }
    }

    public String activeProvider() { return llmProperties.normalizedProvider(); }
    public boolean yandexConfigured() { return yandexProperties.isReady(); }

    private LlmResult fallback(String prompt, String reason) {
        Counter.builder("ai.gateway.requests").tag("provider", "yandex").tag("status", reason).register(registry).increment();
        Counter.builder("ai.gateway.requests").tag("provider", "internal").tag("status", "fallback").register(registry).increment();
        LlmResult result = internalClient.generate(prompt);
        return new LlmResult(result.text(), "Internal fallback (" + reason + ")", true);
    }

    private LlmResult tracked(LlmResult result, String providerKey, String status) {
        Counter.builder("ai.gateway.requests").tag("provider", providerKey).tag("status", status).register(registry).increment();
        return result;
    }
}
