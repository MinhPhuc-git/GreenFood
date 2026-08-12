package com.example.GreenFood.product;

import com.example.GreenFood.model.Category;
import com.example.GreenFood.model.Customer;
import com.example.GreenFood.model.Product;
import com.example.GreenFood.model.Recipe;
import com.example.GreenFood.model.Review;
import com.example.GreenFood.product.ProductService;
import com.example.GreenFood.product.ReviewService;
import com.fasterxml.jackson.annotation.JsonAlias;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.io.IOException;

@RestController
@RequestMapping("/api")
public class ProductController {
    private final ProductService productService;
    private final ReviewService reviewService;

    public ProductController(ProductService productService, ReviewService reviewService) {
        this.productService = productService;
        this.reviewService = reviewService;
    }

    public record ProductRequest(
            @JsonAlias("category_id") Integer categoryId,
            String name,
            BigDecimal price,
            BigDecimal stock,
            String unit,
            String status,
            String description) {
    }

    public record ProductResponse(
            int id,
            int categoryId,
            String categoryName,
            String name,
            BigDecimal price,
            BigDecimal stock,
            String unit,
            String status,
            String description,
            double averageRating,
            long totalReviews,
            String imageUrl) {
    }

    private static final String[] PRODUCT_IMAGES = {
            "/img/caixoanorganic.webp",
            "/img/otchuongdo.webp",
            "/img/supcachuatrung.jpg",
            "/img/shakshuka.webp",
            "/img/omeletteraucu.png",
            "/img/intro.png"
    };

    public record CategoryRequest(String name) {
    }

    public record CategoryResponse(int id, String name) {
    }

    public record ReviewRequest(Integer customerId, int rating, String comment) {
    }

    public record ReviewResponse(int id, int productId, Integer customerId, int rating, String comment, LocalDateTime reviewDate) {
    }

    public record RecipeResponse(
            int id,
            String name,
            String description,
            String ingredients,
            String instructions,
            String relatedKeywords,
            String status,
            String imageUrl) {
    }

    public record RecipeSuggestionResponse(
            int id,
            String name,
            String description,
            String ingredients,
            String instructions,
            String relatedKeywords,
            String status,
            String imageUrl,
            int matchPercent) {
    }

    public record RecipeRequest(
            String name,
            String description,
            String ingredients,
            String instructions,
            String relatedKeywords,
            String status) {
    }

    @GetMapping("/products")
    public Page<ProductResponse> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return productService.searchProducts(keyword, categoryId, minPrice, maxPrice, page, size)
                .map(this::toProductResponse);
    }

    @GetMapping("/products/{id}")
    public ProductResponse getProduct(@PathVariable int id) {
        return toProductResponse(productService.getProduct(id));
    }

    @GetMapping("/categories")
    public List<CategoryResponse> getCategories() {
        return productService.getCategories().stream().map(this::toCategoryResponse).toList();
    }

    @GetMapping("/products/{productId}/reviews")
    public List<ReviewResponse> getProductReviews(@PathVariable int productId) {
        return reviewService.getProductReviews(productId).stream().map(this::toReviewResponse).toList();
    }

    @PostMapping("/products/{productId}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse createReview(@PathVariable int productId, @RequestBody ReviewRequest request) {
        return toReviewResponse(reviewService.createReview(productId, request.customerId(), request.rating(), request.comment()));
    }

    @GetMapping("/products/{productId}/rating")
    public ReviewService.ReviewSummary getReviewSummary(@PathVariable int productId) {
        return reviewService.getReviewSummary(productId);
    }

    @GetMapping("/recipes")
    public List<RecipeResponse> getRecipes(@RequestParam(required = false) String keyword) {
        return productService.getRecipes(keyword).stream().map(this::toRecipeResponse).toList();
    }

    @GetMapping("/recipes/suggest")
    public List<RecipeSuggestionResponse> suggestRecipes(
            @RequestParam("ingredient") List<String> ingredients,
            @RequestParam(defaultValue = "24") int limit) {
        return productService.suggestRecipesByIngredients(ingredients, limit).stream()
                .map(match -> toRecipeSuggestionResponse(match.recipe(), match.matchPercent()))
                .toList();
    }

    @GetMapping("/recipes/{id}")
    public RecipeResponse getRecipe(@PathVariable int id) {
        return toRecipeResponse(productService.getRecipe(id));
    }

    @GetMapping("/recipes/{id}/related-products")
    public List<ProductResponse> getRecipeRelatedProducts(
            @PathVariable int id,
            @RequestParam(defaultValue = "6") int limit) {
        return productService.suggestRelatedProducts(id, limit).stream().map(this::toProductResponse).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/admin/products", consumes = {"multipart/form-data"})
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(
            @RequestParam(value = "categoryId", defaultValue = "0") int categoryId,
            @RequestParam("name") String name,
            @RequestParam("price") BigDecimal price,
            @RequestParam(value = "stock", defaultValue = "0") BigDecimal stock,
            @RequestParam("unit") String unit,
            @RequestParam("status") String status,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {
        
        if (stock != null && stock.signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "SỐ lượng không được âm");
        }
        if (price != null && price.signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Giá tiền không được âm");
        }
        
        Product product = new Product();
        product.setCategoryId(categoryId);
        product.setName(name);
        product.setPrice(price);
        product.setStock(stock);
        product.setUnit(unit);
        product.setStatus(status);
        product.setDescription(description);
        
        Product saved = productService.createProduct(product);
        
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageId = String.valueOf(saved.getId());
            saveImageFile(imageFile, "img_product", imageId + ".png");
            saved.setImage(imageId);
            productService.updateProduct(saved.getId(), saved);
        }
        
        return toProductResponse(saved);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/admin/products/{id}", consumes = {"multipart/form-data"})
    public ProductResponse updateProduct(
            @PathVariable int id,
            @RequestParam(value = "categoryId", defaultValue = "0") int categoryId,
            @RequestParam("name") String name,
            @RequestParam("price") BigDecimal price,
            @RequestParam(value = "stock", defaultValue = "0") BigDecimal stock,
            @RequestParam("unit") String unit,
            @RequestParam("status") String status,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {
            
        if (stock != null && stock.signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Số lượng không được âm");
        }
        if (price != null && price.signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Giá tiền không được âm");
        }
        
        Product product = new Product();
        product.setCategoryId(categoryId);
        product.setName(name);
        product.setPrice(price);
        product.setStock(stock);
        product.setUnit(unit);
        product.setStatus(status);
        product.setDescription(description);
        
        Product updated = productService.updateProduct(id, product);
        
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageId = String.valueOf(updated.getId());
            saveImageFile(imageFile, "img_product", imageId + ".png");
            updated.setImage(imageId);
            productService.updateProduct(updated.getId(), updated);
        }
        
        return toProductResponse(updated);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable int id) {
        productService.deleteProduct(id);
        deleteImageFile("img_product", id + ".png");
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse createCategory(@RequestBody CategoryRequest request) {
        Category category = new Category();
        category.setName(request.name());
        return toCategoryResponse(productService.createCategory(category));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/categories/{id}")
    public CategoryResponse updateCategory(@PathVariable int id, @RequestBody CategoryRequest request) {
        Category category = new Category();
        category.setName(request.name());
        return toCategoryResponse(productService.updateCategory(id, category));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable int id) {
        productService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/admin/recipes", consumes = {"multipart/form-data"})
    @ResponseStatus(HttpStatus.CREATED)
    public RecipeResponse createRecipe(
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("ingredients") String ingredients,
            @RequestParam("instructions") String instructions,
            @RequestParam(value = "relatedKeywords", required = false) String relatedKeywords,
            @RequestParam("status") String status,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {
            
        try {
            Recipe recipe = new Recipe();
            recipe.setName(name);
            recipe.setDescription(description);
            recipe.setIngredients(ingredients);
            recipe.setInstructions(instructions);
            recipe.setRelatedKeywords(relatedKeywords);
            recipe.setStatus(status);
            
            Recipe saved = productService.createRecipe(recipe);
            
            if (imageFile != null && !imageFile.isEmpty()) {
                saveImageFile(imageFile, "img_dishes", saved.getId() + ".png");
            }
            
            return toRecipeResponse(saved);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/admin/recipes/{id}", consumes = {"multipart/form-data"})
    public RecipeResponse updateRecipe(
            @PathVariable int id,
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("ingredients") String ingredients,
            @RequestParam("instructions") String instructions,
            @RequestParam(value = "relatedKeywords", required = false) String relatedKeywords,
            @RequestParam("status") String status,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {
            
        Recipe recipe = new Recipe();
        recipe.setName(name);
        recipe.setDescription(description);
        recipe.setIngredients(ingredients);
        recipe.setInstructions(instructions);
        recipe.setRelatedKeywords(relatedKeywords);
        recipe.setStatus(status);
        
        Recipe updated = productService.updateRecipe(id, recipe);
        
        if (imageFile != null && !imageFile.isEmpty()) {
            saveImageFile(imageFile, "img_dishes", updated.getId() + ".png");
        }
        
        return toRecipeResponse(updated);
    }
    
    private String saveImageFile(MultipartFile file, String folder, String filename) {
        try {
            Path uploadPath = Paths.get("src/main/resources/static/" + folder);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return filename;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/recipes/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable int id) {
        productService.deleteRecipe(id);
        deleteImageFile("img_dishes", id + ".png");
        return ResponseEntity.noContent().build();
    }
    
    private void deleteImageFile(String folder, String filename) {
        try {
            Path filePath = Paths.get("src/main/resources/static/" + folder + "/" + filename);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Product toProduct(ProductRequest request) {
        Product product = new Product();
        product.setCategoryId(request.categoryId() == null ? 0 : request.categoryId());
        product.setName(request.name());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setUnit(request.unit());
        product.setStatus(request.status());
        product.setDescription(request.description());
        return product;
    }

    private ProductResponse toProductResponse(Product product) {
        ReviewService.ReviewSummary summary = reviewService.getReviewSummary(product.getId());
        Category category = product.getCategory();
        int categoryId = product.getCategoryId();
        if (category != null) {
            categoryId = category.getId();
        }
        String categoryName = category == null ? null : category.getName();
        return new ProductResponse(
                product.getId(),
                categoryId,
                categoryName,
                product.getName(),
                product.getPrice(),
                product.getStock(),
                product.getUnit(),
                product.getStatus(),
                product.getDescription(),
                summary.averageRating(),
                summary.totalReviews(),
                productImageUrl(product.getId()));
    }

    private String productImageUrl(int productId) {
        Product p = productService.getProduct(productId);
        if (p != null && p.getImage() != null && !p.getImage().isBlank()) {
            String imgName = p.getImage();
            if (!imgName.matches(".*\\.(png|jpg|jpeg|webp)$")) {
                imgName += ".png";
            }
            return "/img_product/" + imgName;
        }
        String imgId = productService.getProductImageId(productId);
        if (imgId != null && !imgId.isBlank()) {
            return "/img_product/" + imgId + ".png";
        }
        int index = Math.abs(productId) % PRODUCT_IMAGES.length;
        return PRODUCT_IMAGES[index];
    }

    private CategoryResponse toCategoryResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName());
    }

    private Recipe toRecipe(RecipeRequest request) {
        Recipe recipe = new Recipe();
        recipe.setName(request.name());
        recipe.setDescription(request.description());
        recipe.setIngredients(request.ingredients());
        recipe.setInstructions(request.instructions());
        recipe.setRelatedKeywords(request.relatedKeywords());
        recipe.setStatus(request.status());
        return recipe;
    }

    private ReviewResponse toReviewResponse(Review review) {
        Product product = review.getProduct();
        Customer customer = review.getCustomer();
        return new ReviewResponse(
                review.getId(),
                product == null ? 0 : product.getId(),
                customer == null ? null : customer.getId(),
                review.getRating(),
                review.getComment(),
                review.getReviewDate());
    }

    private RecipeResponse toRecipeResponse(Recipe recipe) {
        return new RecipeResponse(
                recipe.getId(),
                recipe.getName(),
                recipe.getDescription(),
                recipe.getIngredients(),
                recipe.getInstructions(),
                recipe.getRelatedKeywords(),
                recipe.getStatus(),
                recipeImageUrl(recipe.getId()));
    }

    private RecipeSuggestionResponse toRecipeSuggestionResponse(Recipe recipe, int matchPercent) {
        return new RecipeSuggestionResponse(
                recipe.getId(),
                recipe.getName(),
                recipe.getDescription(),
                recipe.getIngredients(),
                recipe.getInstructions(),
                recipe.getRelatedKeywords(),
                recipe.getStatus(),
                recipeImageUrl(recipe.getId()),
                matchPercent);
    }

    private static final String[] RECIPE_IMAGES = {
            "/img/supcachuatrung.jpg",
            "/img/shakshuka.webp",
            "/img/omeletteraucu.png",
            "/img/otchuongdo.webp",
            "/img/caixoanorganic.webp"
    };

    private String recipeImageUrl(int recipeId) {
        // According to user: image is saved at /img_dishes/{id}.png
        Path path = Paths.get("src/main/resources/static/img_dishes/" + recipeId + ".png");
        if (Files.exists(path)) {
            return "/img_dishes/" + recipeId + ".png";
        }
        
        String imgUrl = productService.getRecipeImageUrl(recipeId);
        if (imgUrl != null && !imgUrl.isBlank()) {
            if (imgUrl.startsWith("http") || imgUrl.startsWith("/")) {
                return imgUrl;
            }
            return "/img/" + imgUrl;
        }
        return RECIPE_IMAGES[Math.abs(recipeId) % RECIPE_IMAGES.length];
    }
}
