package ru.newsbrief.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.newsbrief.service.ArticleSummaryService;
import ru.newsbrief.dto.SummaryRequest;
import ru.newsbrief.dto.SummaryResponse;

@RestController
@RequestMapping("/api/articles")
public class ArticleSummaryController {
    private final ArticleSummaryService service;
    public ArticleSummaryController(ArticleSummaryService service) { this.service = service; }

    @PostMapping("/summarize")
    public ResponseEntity<SummaryResponse> summarize(@Valid @RequestBody SummaryRequest request) {
        return ResponseEntity.ok(service.summarize(request));
    }
}
