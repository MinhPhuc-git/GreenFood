package com.example.GreenFood.product;

import com.example.GreenFood.model.Category;
import com.example.GreenFood.model.Product;
import com.example.GreenFood.model.Recipe;
import com.example.GreenFood.product.CategoryRepository;
import com.example.GreenFood.product.ProductRepository;
import com.example.GreenFood.product.RecipeRepository;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final RecipeRepository recipeRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository, RecipeRepository recipeRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.recipeRepository = recipeRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAllWithCategory();
    }

    public Page<Product> searchProducts(String keyword, Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice, int page, int size) {
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Giá nhỏ nhất phải nhỏ hơn hoặc bằng giá lớn nhất");
        }
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1), Sort.by("id").descending());
        return productRepository.searchProducts(keyword, categoryId, minPrice, maxPrice, pageable);
    }

    public Product getProduct(int id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm"));
    }

    @Transactional
    public Product createProduct(Product product) {
        if (product.getStock() != null && product.getStock().signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Số lượng tồn kho không được âm");
        }
        if (product.getPrice() != null && product.getPrice().signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Giá không được âm");
        }
        ensureCategoryExists(product.getCategoryId());
        product.setId(0);
        Product saved = productRepository.save(product);
        productRepository.syncCategoryIdColumn(saved.getId(), saved.getCategoryId());
        return saved;
    }

    @Transactional
    public Product updateProduct(int id, Product request) {
        if (request.getStock() != null && request.getStock().signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Số lượng tồn kho không được âm");
        }
        if (request.getPrice() != null && request.getPrice().signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Giá không được âm");
        }
        Product product = getProduct(id);
        ensureCategoryExists(request.getCategoryId());
        product.setCategoryId(request.getCategoryId());
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setUnit(request.getUnit());
        product.setStatus(request.getStatus());
        product.setDescription(request.getDescription());
        Product saved = productRepository.save(product);
        productRepository.syncCategoryIdColumn(saved.getId(), saved.getCategoryId());
        return saved;
    }

    @Transactional
    public void deleteProduct(int id) {
        Product product = getProduct(id);
        
        // Xóa tất cả liên kết trước khi xóa sản phẩm
        productRepository.deleteOrderItemsByProductId(id);   // xóa orderitem (NOT NULL nên không thể NULL hoá)
        productRepository.deleteCartItemsByProductId(id);
        productRepository.deleteImageProductsByProductId(id);
        productRepository.deleteReviewsByProductId(id);
        
        productRepository.delete(product);
    }

    public List<Category> getCategories() {
        return categoryRepository.findAll(Sort.by("name").ascending());
    }

    public Category getCategory(int id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy danh mục"));
    }

    public Category createCategory(Category category) {
        category.setId(0);
        return categoryRepository.save(category);
    }

    public Category updateCategory(int id, Category request) {
        Category category = getCategory(id);
        category.setName(request.getName());
        return categoryRepository.save(category);
    }

    public void deleteCategory(int id) {
        Category category = getCategory(id);
        categoryRepository.delete(category);
    }

    public List<Recipe> getRecipes(String keyword) {
        List<Recipe> recipes;
        if (keyword == null || keyword.isBlank()) {
            recipes = recipeRepository.findAll(Sort.by("id").descending());
        } else {
            recipes = recipeRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword);
        }
        return recipes.stream().filter(this::isRecipeVisible).toList();
    }

    private boolean isRecipeVisible(Recipe recipe) {
        String status = recipe.getStatus();
        return status == null
                || status.isBlank()
                || "active".equalsIgnoreCase(status.trim());
    }

    public record RecipeMatch(Recipe recipe, int matchCount, int matchPercent) {
    }

    public List<RecipeMatch> suggestRecipesByIngredients(List<String> ingredients, int limit) {
        if (ingredients == null || ingredients.isEmpty()) {
            return List.of();
        }

        int normalizedLimit = Math.max(limit, 1);
        return recipeRepository.findAll().stream()
                .filter(this::isRecipeVisible)
                .map(recipe -> toRecipeMatch(recipe, ingredients))
                .filter(match -> match.matchCount() > 0)
                .sorted(Comparator.comparingInt(RecipeMatch::matchPercent).reversed()
                        .thenComparingInt(RecipeMatch::matchCount).reversed())
                .limit(normalizedLimit)
                .toList();
    }

    private RecipeMatch toRecipeMatch(Recipe recipe, List<String> ingredients) {
        String haystack = buildRecipeSearchText(recipe);
        int matchCount = 0;
        for (String ingredient : ingredients) {
            if (ingredient != null && !ingredient.isBlank()
                    && haystack.contains(ingredient.toLowerCase(Locale.ROOT).trim())) {
                matchCount++;
            }
        }
        int matchPercent = ingredients.isEmpty()
                ? 0
                : (int) Math.round((matchCount * 100.0) / ingredients.size());
        return new RecipeMatch(recipe, matchCount, matchPercent);
    }

    private String buildRecipeSearchText(Recipe recipe) {
        return (safeText(recipe.getName()) + " "
                + safeText(recipe.getDescription()) + " "
                + safeText(recipe.getIngredients()) + " "
                + safeText(recipe.getRelatedKeywords()))
                .toLowerCase(Locale.ROOT);
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    public Recipe getRecipe(int id) {
        return recipeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy công thức"));
    }

    public Recipe createRecipe(Recipe recipe) {
        recipe.setId(0);
        return recipeRepository.save(recipe);
    }

    public Recipe updateRecipe(int id, Recipe request) {
        Recipe recipe = getRecipe(id);
        recipe.setName(request.getName());
        recipe.setDescription(request.getDescription());
        recipe.setIngredients(request.getIngredients());
        recipe.setInstructions(request.getInstructions());
        recipe.setRelatedKeywords(request.getRelatedKeywords());
        recipe.setStatus(request.getStatus());
        return recipeRepository.save(recipe);
    }

    public void deleteRecipe(int id) {
        recipeRepository.delete(getRecipe(id));
    }

    public List<Product> suggestRelatedProducts(int recipeId, int limit) {
        Recipe recipe = getRecipe(recipeId);
        List<String> keywords = buildRecipeKeywords(recipe);
        return keywords.stream()
                .flatMap(keyword -> productRepository.findRelatedProducts(keyword, PageRequest.of(0, Math.max(limit, 1))).stream())
                .distinct()
                .limit(Math.max(limit, 1))
                .toList();
    }

    private List<String> buildRecipeKeywords(Recipe recipe) {
        if (recipe.getRelatedKeywords() != null && !recipe.getRelatedKeywords().isBlank()) {
            return Arrays.stream(recipe.getRelatedKeywords().split(","))
                    .map(String::trim)
                    .filter(keyword -> !keyword.isBlank())
                    .toList();
        }
        return Arrays.stream((recipe.getName() + " " + recipe.getIngredients()).split("\\s+"))
                .map(String::trim)
                .filter(keyword -> keyword.length() >= 3)
                .limit(5)
                .toList();
    }

    private void ensureCategoryExists(int categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Danh mục không tồn tại");
        }
    }

    public String getProductImageId(int productId) {
        try {
            return productRepository.findImageIdByProductId(productId);
        } catch (Exception e) {
            return null;
        }
    }

    public String getRecipeImageUrl(int recipeId) {
        try {
            return recipeRepository.findImageUrlByRecipeId(recipeId);
        } catch (Exception e) {
            return null;
        }
    }
}
