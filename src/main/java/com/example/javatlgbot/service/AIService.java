package com.example.javatlgbot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
@Slf4j
public class AIService {

    @Value("${ai.api.key}")
    private String apiKey;

    @Value("${ai.api.base_url}")
    private String baseUrl;

    // comma-separated in application.yml, tried in order
    @Value("${ai.api.models}")
    private String[] models;

    @Value("${bot.system_prompt}")
    private String systemPrompt;

    @Autowired
    private WebSearchService webSearchService;

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    public String sendAIRequest(String userMessage) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return "Please provide a message or question for me to help you with.";
        }
        
        log.info("Processing AI request: {}", userMessage);
        
        // First, perform web search to get relevant information
        String searchContext = webSearchService.getSearchContextForAI(userMessage);
        log.info("Web search completed for query: {}", userMessage);
        
        String finalResponse = null;
        String lastError = null;
        
        // Try each model in sequence
        for (String model : models) {
            try {
                log.info("Trying model: {}", model);
                String requestBody = createRequestBody(searchContext, model);
                
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl + "/chat/completions"))
                        .header("Authorization", "Bearer " + apiKey)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .timeout(Duration.ofSeconds(30))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    String aiResponse = parseAIResponse(response.body());
                    if (aiResponse != null && !aiResponse.trim().isEmpty()) {
                        finalResponse = aiResponse;
                        log.info("Successfully got response from model: {}", model);
                        break; // Success, exit the loop
                    } else {
                        lastError = "Empty response from model: " + model;
                        log.warn("Empty response from model: {}", model);
                    }
                } else {
                    lastError = "HTTP " + response.statusCode() + " from model: " + model;
                    log.warn("Model {} failed with status: {} - {}", model, response.statusCode(), response.body());
                }
                
            } catch (IOException | InterruptedException e) {
                lastError = "Exception with model " + model + ": " + e.getMessage();
                log.warn("Error calling model {}: {}", model, e.getMessage());
            }
        }
        
        if (finalResponse != null) {
            return finalResponse;
        } else {
            log.error("All models failed. Last error: {}", lastError);
            return "Sorry, all AI models are currently unavailable. Please try again later.";
        }
    }

    private String createRequestBody(String userMessage, String model) {
        return String.format(
            "{\n" +
            "    \"model\": \"%s\",\n" +
            "    \"messages\": [\n" +
            "        {\n" +
            "            \"role\": \"system\",\n" +
            "            \"content\": \"%s\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"role\": \"user\",\n" +
            "            \"content\": \"%s\"\n" +
            "        }\n" +
            "    ]\n" +
            "}", model, escapeJson(systemPrompt), escapeJson(userMessage));
    }

    private String parseAIResponse(String responseBody) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(responseBody);
            
            JsonNode choices = rootNode.get("choices");
            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode firstChoice = choices.get(0);
                JsonNode message = firstChoice.get("message");
                if (message != null) {
                    JsonNode content = message.get("content");
                    if (content != null) {
                        return content.asText();
                    }
                }
            }
            
            log.error("Unexpected response format from AI API: {}", responseBody);
            return "Sorry, I received an unexpected response format. Please try again.";
            
        } catch (Exception e) {
            log.error("Error parsing AI response", e);
            return "Sorry, I had trouble processing the AI response. Please try again.";
        }
    }

    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
} 