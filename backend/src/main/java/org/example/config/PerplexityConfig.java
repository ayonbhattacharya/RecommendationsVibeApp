package org.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

import javax.annotation.PostConstruct;

@Configuration
public class PerplexityConfig {

    @Value("${perplexity.api.key:}")
    private String apiKey;

    @Value("${perplexity.api.url:https://api.perplexity.ai/chat/completions}")
    private String apiUrl;

    @PostConstruct
    public void validateConfiguration() {
        if (apiKey == null || apiKey.trim().isEmpty() || "YOUR_PERPLEXITY_API_KEY_HERE".equals(apiKey)) {
            System.out.println("⚠️  WARNING: Perplexity API key is not configured!");
            System.out.println("📝 Please set your Perplexity API key in one of the following ways:");
            System.out.println("   1. Update application.properties: perplexity.api.key=your_actual_key");
            System.out.println("   2. Set environment variable: PERPLEXITY_API_KEY=your_actual_key");
            System.out.println("   3. Pass as JVM argument: -Dperplexity.api.key=your_actual_key");
            System.out.println("🔗 Get your API key from: https://www.perplexity.ai/settings/api");
            System.out.println("💡 The service will use mock data until a valid API key is provided.");
            System.out.println("----------------------------------------");
        } else {
            System.out.println("✅ Perplexity API key configured successfully");
            System.out.println("🔗 API URL: " + apiUrl);
            System.out.println("🔑 API Key: " + maskApiKey(apiKey));
        }
    }

    @Bean
    public WebClient perplexityWebClient() {
        return WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

    private String maskApiKey(String key) {
        if (key == null || key.length() < 8) {
            return "***";
        }
        return key.substring(0, 4) + "..." + key.substring(key.length() - 4);
    }

    // Getters
    public String getApiKey() {
        return apiKey;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public boolean isApiKeyValid() {
        return apiKey != null && 
               !apiKey.trim().isEmpty() && 
               !"YOUR_PERPLEXITY_API_KEY_HERE".equals(apiKey) &&
               apiKey.length() > 10; // Basic validation
    }
}
