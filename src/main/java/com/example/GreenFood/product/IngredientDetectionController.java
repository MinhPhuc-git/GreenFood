package com.example.GreenFood.product;

import com.example.GreenFood.model.Recipe;
import com.example.GreenFood.product.ProductService;
import com.example.GreenFood.product.RoboflowService;
import com.example.GreenFood.product.RoboflowService.DetectedIngredient;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ingredients")
public class IngredientDetectionController {

    private final RoboflowService roboflowService;
    private final ProductService productService;

    public IngredientDetectionController(RoboflowService roboflowService, ProductService productService) {
        this.roboflowService = roboflowService;
        this.productService = productService;
    }

    public record DetectedIngredientResponse(String name, String className, double confidence) {
    }

    public record RecipeSuggestionResponse(
            int id,
            String name,
            String description,
            String ingredients,
            String instructions,
            String relatedKeywords,
            String status,
            int matchPercent) {
    }

    public record DetectionResponse(
            List<DetectedIngredientResponse> ingredients,
            List<RecipeSuggestionResponse> recipes) {
    }

    @PostMapping(value = "/detect", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DetectionResponse detectIngredients(@RequestParam("image") MultipartFile image) {
        List<DetectedIngredient> detected = roboflowService.detectIngredients(image);
        List<String> ingredientNames = detected.stream().map(DetectedIngredient::name).toList();
        List<ProductService.RecipeMatch> recipeMatches = productService.suggestRecipesByIngredients(ingredientNames, 12);

        return new DetectionResponse(
                detected.stream().map(this::toIngredientResponse).toList(),
                recipeMatches.stream().map(this::toRecipeResponse).toList());
    }

    private DetectedIngredientResponse toIngredientResponse(DetectedIngredient ingredient) {
        return new DetectedIngredientResponse(
                ingredient.name(),
                ingredient.className(),
                Math.round(ingredient.confidence() * 1000.0) / 1000.0);
    }

    private RecipeSuggestionResponse toRecipeResponse(ProductService.RecipeMatch match) {
        Recipe recipe = match.recipe();
        return new RecipeSuggestionResponse(
                recipe.getId(),
                recipe.getName(),
                recipe.getDescription(),
                recipe.getIngredients(),
                recipe.getInstructions(),
                recipe.getRelatedKeywords(),
                recipe.getStatus(),
                match.matchPercent());
    }
}
