package org.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MenuRecommendation {
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("description")
    private String description;

    @JsonProperty("menuLink")
    private String menuLink;

    @JsonProperty("imageUrl")
    private String imageUrl;

    @JsonProperty("cuisine")
    private String cuisine;

    // Default constructor
    public MenuRecommendation() {}

    // Constructor
    public MenuRecommendation(String name, String description, String menuLink,
                            String imageUrl, String cuisine) {
        this.name = name;
        this.description = description;
        this.menuLink = menuLink;
        this.imageUrl = imageUrl;
        this.cuisine = cuisine;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMenuLink() { return menuLink; }
    public void setMenuLink(String menuLink) { this.menuLink = menuLink; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getCuisine() { return cuisine; }
    public void setCuisine(String cuisine) { this.cuisine = cuisine; }

    @Override
    public String toString() {
        return "MenuRecommendation{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", cuisine='" + cuisine + '\'' +
                ", menuLink='" + menuLink + '\'' +
                '}';
    }
}
