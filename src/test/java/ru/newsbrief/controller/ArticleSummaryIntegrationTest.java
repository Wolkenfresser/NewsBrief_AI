package ru.newsbrief.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {"app.yandex.api-key=", "app.yandex.iam-token=", "app.yandex.prompt-id=", "app.yandex.folder-id=", "app.llm.active-provider=yandex"})
@AutoConfigureMockMvc
class ArticleSummaryIntegrationTest {
    @Autowired private MockMvc mockMvc;

    @Test
    void summarizesArticleWithoutYandexCredentialsUsingDemoFallback() throws Exception {
        mockMvc.perform(post("/api/articles/summarize")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"title":"Открытие центра","sourceName":"Редакция","articleText":"В регионе планируют открыть новый центр обработки данных. Проект будет реализован в несколько этапов после завершения проектной документации. Команда обещает опубликовать сроки реализации после согласования технических условий.","length":"short","includeKeyPoints":true}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.summary", containsString("Открытие центра")))
            .andExpect(jsonPath("$.provider", containsString("Internal fallback")))
            .andExpect(jsonPath("$.fallbackUsed", is(true)))
            .andExpect(jsonPath("$.requestId", not(emptyOrNullString())));
    }

    @Test
    void rejectsArticleThatIsTooShort() throws Exception {
        mockMvc.perform(post("/api/articles/summarize")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"articleText\":\"short\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", not(emptyOrNullString())));
    }

    @Test
    void exposesSafeSystemStatus() throws Exception {
        mockMvc.perform(get("/api/system/status"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status", is("UP")))
            .andExpect(jsonPath("$.effectiveMode", not(emptyOrNullString())));
    }
}
