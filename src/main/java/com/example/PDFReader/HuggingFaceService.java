package com.example.PDFReader;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HuggingFaceService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public HuggingFaceService(WebClient webClient, ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
    }

    public Mono<Map<String, List<String>>> extractInvoiceDetails(String invoiceText) {
        return webClient.post()
                .uri("/dbmdz/bert-large-cased-finetuned-conll03-english")
                .bodyValue(new HuggingFaceRequest(invoiceText))
                .retrieve()
                .bodyToMono(String.class)
                .map(this::processResponse);
    }

    private Map<String, List<String>> processResponse(String response) {
        try {
            HuggingFaceResponse huggingFaceResponse = objectMapper.readValue(response, HuggingFaceResponse.class);
            Map<String, List<String>> entities = huggingFaceResponse.getEntities();
            Map<String, List<String>> extractedInfo = new HashMap<>();

            if (entities.containsKey("Kods")) {
                extractedInfo.put("Items", entities.get("Kods"));
            }
            if (entities.containsKey("Daudz.*")) {
                extractedInfo.put("Quantities", entities.get("Daudz.*"));
            }
            if (entities.containsKey("Cena")) {
                extractedInfo.put("Prices", entities.get("Cena"));
            }

            return extractedInfo;
        } catch (IOException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
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

        public void setEntities(Map<String, List<String>> entities) {
            this.entities = entities;
        }
    }
}

