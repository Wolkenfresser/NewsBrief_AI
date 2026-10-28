package ru.newsbrief.client;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.newsbrief.llm.LlmClient;
import ru.newsbrief.llm.LlmResult;

/** Deterministic offline implementation for demos, tests, and fallback. */
@Component
public class InternalLlmClient implements LlmClient {
    private static final Logger log = LoggerFactory.getLogger(InternalLlmClient.class);
    private static final String OPEN = "<<<ARTICLE>>>";
    private static final String CLOSE = "<<<END ARTICLE>>>";

    @Override public String providerName() { return "internal"; }

    @Override
    public LlmResult generate(String input) {
        String article = extractBetween(input, OPEN, CLOSE);
        String title = extractLine(input, "ЗАГОЛОВОК:");
        String size = extractLine(input, "ЖЕЛАЕМАЯ ДЛИНА:");
        boolean includeKeyPoints = !extractLine(input, "КЛЮЧЕВЫЕ ТЕЗИСЫ:").equalsIgnoreCase("НЕ ВКЛЮЧАТЬ");
        if (article.isBlank()) article = input == null ? "" : input;
        article = article.replaceAll("(?m)^\\s*Источник:.*$", "").trim();

        List<String> sentences = sentences(article);
        int sentenceLimit = size.toLowerCase(Locale.ROOT).contains("корот") ? 2 : 4;
        List<String> selected = sentences.stream().limit(sentenceLimit).toList();
        if (selected.isEmpty()) selected = List.of(article.length() > 520 ? article.substring(0, 520) + "…" : article);

        String abstractText = String.join(" ", selected);
        int max = size.toLowerCase(Locale.ROOT).contains("корот") ? 380 : 720;
        if (abstractText.length() > max) abstractText = article.substring(0, Math.min(max - 1, article.length())).stripTrailing() + "…";

        StringBuilder output = new StringBuilder();
        output.append("## ").append(title.isBlank() ? "Аннотация статьи" : title).append("\n\n");
        output.append("> Демо-режим: аннотация сформирована локальным алгоритмом без внешней LLM.\n\n");
        output.append("### Краткая аннотация\n\n").append(abstractText).append("\n\n");
        if (includeKeyPoints) {
            output.append("### Ключевые тезисы\n");
            selected.stream().limit(3).forEach(s -> output.append("- ").append(trimSentence(s, 220)).append("\n"));
            output.append("\n");
        }
        output.append("### Редакционная пометка\n\nПроверьте факты, даты, имена и числа по исходной статье перед публикацией.");
        log.info("Generated demo summary using internal provider; source content is not logged");
        return new LlmResult(output.toString(), "Internal demo (offline)", false);
    }

    private static List<String> sentences(String text) {
        BreakIterator iterator = BreakIterator.getSentenceInstance(new Locale("ru", "RU"));
        iterator.setText(text);
        List<String> out = new ArrayList<>();
        int start = iterator.first();
        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
            String s = text.substring(start, end).replaceAll("\\s+", " ").trim();
            if (s.length() >= 18) out.add(s);
        }
        if (out.isEmpty() && !text.isBlank()) {
            Arrays.stream(text.split("\\n+")).map(String::trim).filter(s -> s.length() >= 18).forEach(out::add);
        }
        return out;
    }

    private static String extractBetween(String text, String open, String close) {
        if (text == null) return "";
        int start = text.indexOf(open);
        int end = text.indexOf(close);
        return start >= 0 && end > start ? text.substring(start + open.length(), end).trim() : "";
    }

    private static String extractLine(String text, String prefix) {
        if (text == null) return "";
        return text.lines().filter(line -> line.startsWith(prefix)).map(line -> line.substring(prefix.length()).trim()).findFirst().orElse("");
    }

    private static String trimSentence(String sentence, int max) {
        return sentence.length() <= max ? sentence : sentence.substring(0, max - 1).stripTrailing() + "…";
    }
}
