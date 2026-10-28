package ru.newsbrief.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public record SummaryRequest(
        @Size(max = 250, message = "Заголовок не должен превышать 250 символов") String title,
        @Size(max = 150, message = "Название источника не должно превышать 150 символов") String sourceName,
        @NotBlank(message = "Вставьте текст статьи")
        @Size(min = 40, max = 25000, message = "Текст статьи должен содержать от 40 до 25 000 символов") String articleText,
        @Pattern(regexp = "short|standard", message = "Выберите короткую или стандартную аннотацию") String length,
        Boolean includeKeyPoints) {
}
