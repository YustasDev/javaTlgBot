package com.example.javatlgbot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class WebSearchService {

    @Value("${google.search.api.key}")
    private String apiKey;

    @Value("${google.search.engine.id}")
    private String searchEngineId;

    @Value("${google.search.amount_chunks}")
    private Long amount_chunks;
 



    private static final String GOOGLE_SEARCH_URL = "https://www.googleapis.com/customsearch/v1";
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    public String searchWeb(String query) {
        if (query == null || query.trim().isEmpty()) {
            return "No search query provided.";
        }

        try {
            String searchUrl = buildSearchUrl(query);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(searchUrl))
                    .header("Accept", "application/json")
                    .GET()
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return parseSearchResults(response.body());
            } else {
                log.error("Search API failed with status: {} - {}", response.statusCode(), response.body());
                return "Sorry, I couldn't search the web right now. Please try again later.";
            }

        } catch (IOException | InterruptedException e) {
            log.error("Error during web search", e);
            return "Sorry, I encountered an error while searching the web.";
        }
    }

    private String buildSearchUrl(String query) {
        return String.format("%s?key=%s&cx=%s&q=%s&num=5",
                GOOGLE_SEARCH_URL,
                apiKey,
                searchEngineId,
                java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8));
    }

    private String parseSearchResults(String responseBody) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(responseBody);

            JsonNode items = rootNode.get("items");
            if (items == null || !items.isArray() || items.size() == 0) {
                return "No relevant information found for your query.";
            }

            StringBuilder result = new StringBuilder();
            result.append("🌐 **Web Search Results:**\n\n");

            for (int i = 0; i < Math.min(items.size(), amount_chunks); i++) {
                JsonNode item = items.get(i);
                String title = item.get("title").asText();
                String snippet = item.get("snippet").asText();
                String link = item.get("link").asText();

                result.append(String.format("**%d. %s**\n", i + 1, title));
                result.append(String.format("%s\n", snippet));
                result.append(String.format("🔗 %s\n\n", link));
            }

            return result.toString();

        } catch (Exception e) {
            log.error("Error parsing search results", e);
            return "Sorry, I had trouble processing the search results.";
        }
    }

    public String getSearchContextForAI(String query) {
        String searchResults = searchWeb(query);
        
        // Format the search results for AI context
        return String.format(
            "Based on the following web search results for '%s':\n\n%s\n\n" +
            "Please provide a comprehensive answer using this information. " +
            "Include relevant details and cite the sources when appropriate.",
            query, searchResults
        );
    }
} 