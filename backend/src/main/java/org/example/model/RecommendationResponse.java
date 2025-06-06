package org.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class RecommendationResponse {
    
    @JsonProperty("query")
    private String query;
    
    @JsonProperty("recommendations")
    private List<MenuRecommendation> recommendations;
    
    @JsonProperty("totalFound")
    private int totalFound;
    
    @JsonProperty("searchLocation")
    private String searchLocation;
    
    @JsonProperty("timestamp")
    private String timestamp;

    // Default constructor
    public RecommendationResponse() {}

    // Constructor
    public RecommendationResponse(String query, List<MenuRecommendation> recommendations, 
                                int totalFound, String searchLocation, String timestamp) {
        this.query = query;
        this.recommendations = recommendations;
        this.totalFound = totalFound;
        this.searchLocation = searchLocation;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }

    public List<MenuRecommendation> getRecommendations() { return recommendations; }
    public void setRecommendations(List<MenuRecommendation> recommendations) { 
        this.recommendations = recommendations; 
    }

    public int getTotalFound() { return totalFound; }
    public void setTotalFound(int totalFound) { this.totalFound = totalFound; }

    public String getSearchLocation() { return searchLocation; }
    public void setSearchLocation(String searchLocation) { this.searchLocation = searchLocation; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "RecommendationResponse{" +
                "query='" + query + '\'' +
                ", totalFound=" + totalFound +
                ", recommendationsCount=" + (recommendations != null ? recommendations.size() : 0) +
                '}';
    }
}
