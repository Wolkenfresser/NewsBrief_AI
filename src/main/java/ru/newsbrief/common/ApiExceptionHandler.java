package ru.newsbrief.common;

import java.time.Instant;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<?> validation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream().findFirst()
                .map(error -> error.getDefaultMessage()).orElse("Проверьте введённые данные");
        return ResponseEntity.badRequest().body(Map.of("timestamp", Instant.now().toString(), "message", message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<?> malformedJson() {
        return ResponseEntity.badRequest().body(Map.of("timestamp", Instant.now().toString(), "message", "Некорректный JSON-запрос"));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<?> unexpected(Exception ex) {
        log.error("Unhandled API exception: {}", ex.getClass().getSimpleName());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(
                "timestamp", Instant.now().toString(),
                "message", "Не удалось обработать статью. Попробуйте ещё раз."));
    }
}
