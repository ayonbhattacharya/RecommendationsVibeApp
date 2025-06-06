package org.example.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.config.PerplexityConfig;
import org.example.model.MenuRecommendation;
import org.example.model.RecommendationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PerplexityService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final PerplexityConfig perplexityConfig;

    @Value("${perplexity.model:llama-3.1-sonar-small-128k-online}")
    private String model;

    @Value("${perplexity.max.tokens:2000}")
    private int maxTokens;

    @Value("${perplexity.temperature:0.2}")
    private double temperature;

    @Autowired
    public PerplexityService(PerplexityConfig perplexityConfig) {
        this.perplexityConfig = perplexityConfig;
        this.webClient = perplexityConfig.perplexityWebClient();
        this.objectMapper = new ObjectMapper();
    }

    public RecommendationResponse getMenuRecommendations(String userQuery, String location) {
        try {
            System.out.println("Searching for recommendations with query: " + userQuery);

            // Check if API key is valid
            if (!perplexityConfig.isApiKeyValid()) {
                System.out.println("⚠️  Using mock data - Perplexity API key not configured");
                return createMockResponse(userQuery, location);
            }

            String enhancedPrompt = buildSearchPrompt(userQuery, location);
            String perplexityResponse = callPerplexityAPI(enhancedPrompt);

            System.out.println("Perplexity API response received");

            List<MenuRecommendation> recommendations = parseRecommendations(perplexityResponse);

            return new RecommendationResponse(
                userQuery,
                recommendations,
                recommendations.size(),
                location,
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );

        } catch (Exception e) {
            System.err.println("Error getting recommendations: " + e.getMessage());
            e.printStackTrace();
            return createErrorResponse(userQuery, location, e.getMessage());
        }
    }

    private String buildSearchPrompt(String userQuery, String location) {
        return String.format("""
            Based on the user's request: "%s", please provide exactly 5 meal recommendations suitable for lunch, dinner, or general meals.

            Focus on recommending specific dishes/meals with cooking instructions. For each meal recommendation, provide the following information in JSON format:
            {
              "name": "specific dish/meal name",
              "description": "detailed description of the dish, its flavors, and what makes it special",
              "menuLink": "link to online recipe or cooking instructions",
              "imageUrl": "link to appetizing food image of the dish",
              "cuisine": "type of cuisine or cooking style"
            }

            Please ensure:
            1. Focus on actual meals/dishes that can be prepared or enjoyed
            2. Provide diverse cuisine options and meal types (lunch, dinner, snacks)
            3. Include detailed descriptions of flavors, ingredients, and appeal
            4. Find real recipe links or cooking instruction websites when possible
            5. Include appetizing food images that showcase the dish
            6. Consider the user's location context: %s for regional preferences
            7. Return exactly 5 meal recommendations
            8. Make recommendations suitable for home cooking or ordering

            Format the response as a JSON array of 5 objects focusing on meals with cooking instructions.
            """, userQuery, location != null ? location : "general preferences");
    }

    private String callPerplexityAPI(String prompt) {
        try {
            Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                    Map.of("role", "user", "content", prompt)
                ),
                "max_tokens", maxTokens,
                "temperature", temperature,
                "top_p", 0.9
            );

            Mono<String> response = webClient.post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class);

            return response.block();

        } catch (Exception e) {
            throw new RuntimeException("Failed to call Perplexity API: " + e.getMessage(), e);
        }
    }

    private List<MenuRecommendation> parseRecommendations(String apiResponse) {
        List<MenuRecommendation> recommendations = new ArrayList<>();
        
        try {
            JsonNode rootNode = objectMapper.readTree(apiResponse);
            JsonNode contentNode = rootNode.path("choices").get(0).path("message").path("content");
            String content = contentNode.asText();
            
            System.out.println("Parsing content: " + content);
            
            // Extract JSON array from the content
            String jsonContent = extractJsonFromContent(content);
            JsonNode recommendationsArray = objectMapper.readTree(jsonContent);
            
            if (recommendationsArray.isArray()) {
                for (JsonNode recNode : recommendationsArray) {
                    MenuRecommendation recommendation = objectMapper.treeToValue(recNode, MenuRecommendation.class);
                    recommendations.add(recommendation);
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error parsing recommendations: " + e.getMessage());
            // Return mock data if parsing fails
            recommendations = createMockRecommendations();
        }
        
        return recommendations.size() > 5 ? recommendations.subList(0, 5) : recommendations;
    }

    private String extractJsonFromContent(String content) {
        // Find JSON array in the content
        int startIndex = content.indexOf('[');
        int endIndex = content.lastIndexOf(']');
        
        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            return content.substring(startIndex, endIndex + 1);
        }
        
        // If no array found, try to extract individual JSON objects
        return "[]"; // Return empty array if no JSON found
    }

    private List<MenuRecommendation> createMockRecommendations() {
        List<MenuRecommendation> mockRecommendations = new ArrayList<>();
        
        mockRecommendations.add(new MenuRecommendation(
            "Classic Margherita Pizza",
            "Homemade pizza with fresh mozzarella, San Marzano tomatoes, fresh basil, and olive oil. Perfect for lunch or dinner with authentic Italian flavors that are simple yet satisfying.",
            "https://www.allrecipes.com/recipe/83454/margherita-pizza/",
            "https://images.unsplash.com/photo-1574071318508-1cdbab80d002?w=500",
            "Italian"
        ));

        mockRecommendations.add(new MenuRecommendation(
            "Creamy Butter Chicken",
            "Rich and aromatic Indian curry with tender chicken in a creamy tomato-based sauce, served with basmati rice. A comforting dinner option with warm spices and velvety texture.",
            "https://cafedelites.com/butter-chicken/",
            "https://images.unsplash.com/photo-1565557623262-b51c2513a641?w=500",
            "Indian"
        ));

        mockRecommendations.add(new MenuRecommendation(
            "Homemade Chicken Ramen",
            "Comforting Japanese noodle soup with rich chicken broth, soft-boiled eggs, green onions, and tender noodles. Perfect for a warming dinner or satisfying lunch.",
            "https://www.justonecookbook.com/homemade-chashu-miso-ramen/",
            "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=500",
            "Japanese"
        ));

        mockRecommendations.add(new MenuRecommendation(
            "Fish Tacos with Avocado Crema",
            "Fresh and zesty tacos with seasoned white fish, cabbage slaw, avocado crema, and lime. A light yet satisfying meal perfect for lunch or casual dinner.",
            "https://www.foodnetwork.com/recipes/ellie-krieger/fish-tacos-with-avocado-crema-recipe-1946783",
            "https://images.unsplash.com/photo-1565299624946-b28f40a0ca4b?w=500",
            "Mexican"
        ));

        mockRecommendations.add(new MenuRecommendation(
            "Classic Beef Burger",
            "Juicy homemade beef patty with lettuce, tomato, onion, and special sauce on a toasted bun. A satisfying dinner option that's perfect for casual dining and comfort food cravings.",
            "https://www.foodnetwork.com/recipes/bobby-flay/perfect-burger-recipe-1947140",
            "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=500",
            "American"
        ));
        
        return mockRecommendations;
    }

    private RecommendationResponse createMockResponse(String query, String location) {
        return new RecommendationResponse(
            query,
            createMockRecommendations(),
            5,
            location,
            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }

    private RecommendationResponse createErrorResponse(String query, String location, String error) {
        return new RecommendationResponse(
            query,
            createMockRecommendations(),
            5,
            location,
            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }
}
