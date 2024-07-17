package com.example.PDFReader;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class HuggingFaceConfig {

    private final String huggingFaceApiKey = System.getenv("HUGGINGFACE_API_KEY");

    @Bean
    public WebClient huggingFaceWebClient() {
        return WebClient.builder()
                .baseUrl("https://api-inference.huggingface.co/models")
                .defaultHeader("Authorization", "Bearer " + huggingFaceApiKey)
                .build();
    }
}
