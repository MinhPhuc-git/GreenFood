package com.example.GreenFood.product;

import com.example.GreenFood.model.Product;
import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import java.util.Optional;


@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT p FROM Product p WHERE p.id = :id")
	Optional<Product> findByIdWithLock(@Param("id") int id);
	
    @Query("""
            SELECT p FROM Product p
            WHERE (:keyword IS NULL OR :keyword = ''
                OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
            AND (:categoryId IS NULL OR p.categoryId = :categoryId)
            AND (:minPrice IS NULL OR p.price >= :minPrice)
            AND (:maxPrice IS NULL OR p.price <= :maxPrice)
            """)
    Page<Product> searchProducts(
            @Param("keyword") String keyword,
            @Param("categoryId") Integer categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    @Query("""
            SELECT p FROM Product p
            WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    List<Product> findRelatedProducts(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.category")
    List<Product> findAllWithCategory();

    @Modifying
    @Query(value = "UPDATE product SET category_id = :categoryId WHERE id = :id", nativeQuery = true)
    void syncCategoryIdColumn(@Param("id") int id, @Param("categoryId") int categoryId);

    @Query(value = "SELECT image_id FROM imageproduct WHERE product_id = :productId LIMIT 1", nativeQuery = true)
    String findImageIdByProductId(@Param("productId") int productId);

    @Modifying
    @Query(value = "DELETE FROM orderitem WHERE product_id = :productId", nativeQuery = true)
    void deleteOrderItemsByProductId(@Param("productId") int productId);

    @Modifying
    @Query(value = "DELETE FROM cartitem WHERE product_id = :productId", nativeQuery = true)
    void deleteCartItemsByProductId(@Param("productId") int productId);

    @Modifying
    @Query(value = "DELETE FROM imageproduct WHERE product_id = :productId", nativeQuery = true)
    void deleteImageProductsByProductId(@Param("productId") int productId);

    @Modifying
    @Query(value = "DELETE FROM review WHERE product_id = :productId", nativeQuery = true)
    void deleteReviewsByProductId(@Param("productId") int productId);
}
