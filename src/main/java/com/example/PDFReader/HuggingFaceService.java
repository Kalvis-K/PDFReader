package com.example.PDFReader;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HuggingFaceService {

    private final WebClient webClient;

    public HuggingFaceService(@Value("${huggingface.api.key}") String apiKey) {
        this.webClient = WebClient.builder()
                .baseUrl("https://api-inference.huggingface.co/models")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    public Mono<String> extractInvoiceData(String model, String invoiceText) {
        return webClient.post()
                .uri("/{model}", model)
                .bodyValue(new HuggingFaceRequest(invoiceText))
                .retrieve()
                .bodyToMono(String.class);
    }

    private Map<String, List<String>> processHuggingFaceResponse(HuggingFaceResponse response) {
        // Example processing logic based on model output
        Map<String, List<String>> extractedInfo = new HashMap<>();

        // Assuming the response provides named entity recognition (NER) results
        List<String> items = response.getEntitiesOfType("ITEM");
        List<String> quantities = response.getEntitiesOfType("QUANTITY");
        List<String> prices = response.getEntitiesOfType("PRICE");

        extractedInfo.put("Items", items);
        extractedInfo.put("Quantities", quantities);
        extractedInfo.put("Prices", prices);

        return extractedInfo;
    }

    private static class HuggingFaceRequest {
        private final String inputs;
        private final Parameters parameters;

        public HuggingFaceRequest(String inputs) {
            this.inputs = inputs;
            this.parameters = new Parameters(50); // Adjust as necessary
        }

        public String getInputs() {
            return inputs;
        }

        public Parameters getParameters() {
            return parameters;
        }

        private static class Parameters {
            private final int max_length;

            public Parameters(int max_length) {
                this.max_length = max_length;
            }

            public int getMax_length() {
                return max_length;
            }
        }
    }

    private static class HuggingFaceResponse {
        private Map<String, List<String>> entities;

        public Map<String, List<String>> getEntities() {
            return entities;
        }

        public List<String> getEntitiesOfType(String type) {
            return entities.getOrDefault(type, Collections.emptyList());
        }
    }
}

