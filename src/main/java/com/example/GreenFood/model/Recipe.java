package com.example.GreenFood.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "recipe")
public class Recipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    @Column(length = 1000)
    private String description;

    @Column(length = 2000)
    private String ingredients;

    @Column(length = 4000)
    private String instructions;

    private String relatedKeywords;
    private String status;

    // Cột img trong DB = ID của ảnh (trigger tự set img = recipe.id)
    // Dùng để hiển thị ảnh: /img_dishses/{img}.png
    private int img;

    public Recipe() {
    }

    public Recipe(String name, String description, String ingredients, String instructions, String relatedKeywords, String status) {
        this.name = name;
        this.description = description;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.relatedKeywords = relatedKeywords;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }
    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
    public String getRelatedKeywords() { return relatedKeywords; }
    public void setRelatedKeywords(String relatedKeywords) { this.relatedKeywords = relatedKeywords; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getImg() { return img; }
    public void setImg(int img) { this.img = img; }
}
