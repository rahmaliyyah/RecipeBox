package com.example.uap;

public class Recipe {
    private String id;
    private String name;
    private String cookingTime;
    private String ingredients;
    private String instructions;
    private String howToMake;
    private String imageUrl;
    private String userId;

    public Recipe() {

    }

    public Recipe(String name, String cookingTime, String ingredients,
                  String instructions, String imageUrl, String userId) {
        this.name = name;
        this.cookingTime = cookingTime;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.howToMake = instructions; // Set both for compatibility
        this.imageUrl = imageUrl;
        this.userId = userId;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCookingTime() { return cookingTime; }
    public void setCookingTime(String cookingTime) { this.cookingTime = cookingTime; }

    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }

    public String getInstructions() {
        return instructions != null ? instructions : howToMake;
    }
    public void setInstructions(String instructions) {
        this.instructions = instructions;
        this.howToMake = instructions; // Keep both synchronized
    }

    public String getHowToMake() {
        return howToMake != null ? howToMake : instructions;
    }
    public void setHowToMake(String howToMake) {
        this.howToMake = howToMake;
        this.instructions = howToMake; // Keep both synchronized
    }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    @Override
    public String toString() {
        return "Recipe{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", cookingTime='" + cookingTime + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }
}