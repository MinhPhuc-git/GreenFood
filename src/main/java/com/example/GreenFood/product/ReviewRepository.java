package com.example.GreenFood.product;

import com.example.GreenFood.model.Review;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    List<Review> findByProductIdOrderByReviewDateDesc(int productId);

    long countByProductId(int productId);

    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r WHERE r.product.id = :productId")
    Double averageRatingByProductId(@Param("productId") int productId);
}
