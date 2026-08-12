package com.example.GreenFood.product;

import com.example.GreenFood.model.Recipe;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Integer> {
    List<Recipe> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description);

    @org.springframework.data.jpa.repository.Query(value = "SELECT i.imgURL FROM imagerecipe ir JOIN image i ON ir.image_id = i.id WHERE ir.recipe_id = :recipeId LIMIT 1", nativeQuery = true)
    String findImageUrlByRecipeId(@org.springframework.data.repository.query.Param("recipeId") int recipeId);
}
