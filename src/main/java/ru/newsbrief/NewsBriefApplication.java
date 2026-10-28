package ru.newsbrief;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class NewsBriefApplication {
    public static void main(String[] args) {
        SpringApplication.run(NewsBriefApplication.class, args);
    }
}
