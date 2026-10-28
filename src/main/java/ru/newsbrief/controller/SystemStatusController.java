package ru.newsbrief.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.newsbrief.llm.AiGateway;

@RestController
@RequestMapping("/api/system")
public class SystemStatusController {
    private final AiGateway gateway;
    public SystemStatusController(AiGateway gateway) { this.gateway = gateway; }

    @GetMapping("/status")
    public Map<String, Object> status() {
        boolean configured = gateway.yandexConfigured();
        String provider = gateway.activeProvider();
        String mode = "yandex".equals(provider) && configured ? "yandex" : "demo";
        return Map.of(
                "status", "UP",
                "configuredProvider", provider,
                "yandexConfigured", configured,
                "effectiveMode", mode,
                "message", "Сервис принимает статьи и готов к генерации аннотаций");
    }
}
