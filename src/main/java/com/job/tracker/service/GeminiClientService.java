package com.job.tracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Thin wrapper around Google's Gemini API (generateContent endpoint), which has
 * a genuinely free tier (no credit card, resets daily) — unlike Anthropic/OpenAI,
 * which require a paid key from the first call.
 *
 * Kept the same complete(prompt, maxTokens) signature as the old
 * AnthropicClientService on purpose, so AiResumeFeedbackService didn't need to
 * change at all — only this class and application.yml did.
 */
@Service
public class GeminiClientService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String model;
    private final String apiKey;

    public GeminiClientService(@Value("${gemini.base-url}") String baseUrl,
                                @Value("${gemini.model}") String model,
                                @Value("${gemini.api-key}") String apiKey) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.model = model;
        this.apiKey = apiKey;
    }

    /**
     * Sends a single prompt to Gemini and returns the plain text reply.
     */
    public String complete(String prompt, int maxTokens) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "GEMINI_API_KEY is not set — get a free key at aistudio.google.com and add it as an environment variable");
        }

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", prompt)))
                ),
                "generationConfig", Map.of("maxOutputTokens", maxTokens)
        );

        String rawResponse = restClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/{model}:generateContent")
                        .queryParam("key", apiKey)
                        .build(model))
                .header("content-type", "application/json")
                .body(requestBody)
                .retrieve()
                .body(String.class);

        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            return root.path("candidates").path(0)
                    .path("content").path("parts").path(0)
                    .path("text").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Gemini API response: " + rawResponse, e);
        }
    }
}
