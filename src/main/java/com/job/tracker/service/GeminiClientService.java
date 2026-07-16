package com.job.tracker.service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class GeminiClientService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String model;
    private final String apiKey;

    public GeminiClientService(@Value("${gemini.base-url}") String baseUrl,
            @Value("${gemini.model}") String model,
            @Value("${gemini.api-key}") String apiKey) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5_000); // 5s to establish connection
        requestFactory.setReadTimeout(45_000); // 45s max to wait for Gemini's full reply

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
        this.model = model;
        this.apiKey = apiKey;
    }

    public String complete(String prompt, int maxTokens) {
        return call(prompt, maxTokens, false);
    }

    public String completeJson(String prompt, int maxTokens) {
        return call(prompt, maxTokens, true);
    }

    private String call(String prompt, int maxTokens, boolean jsonMode) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "GEMINI_API_KEY is not set — get a free key at aistudio.google.com and add it as an environment variable");
        }

        Map<String, Object> generationConfig = jsonMode
                ? Map.of("maxOutputTokens", maxTokens, "responseMimeType", "application/json")
                : Map.of("maxOutputTokens", maxTokens);

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", prompt)))),
                "generationConfig", generationConfig);

        byte[] rawBytes = restClient.post()
                .uri(uriBuilder -> uriBuilder.path("/{model}:generateContent").queryParam("key", apiKey).build(model))
                .header("content-type", "application/json")
                .body(requestBody)
                .retrieve()
                .body(byte[].class);

        String rawResponse = rawBytes == null ? "" : new String(rawBytes, StandardCharsets.UTF_8);

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