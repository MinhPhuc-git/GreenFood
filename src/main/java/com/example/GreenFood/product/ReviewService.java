package com.example.GreenFood.product;

import com.example.GreenFood.model.Product;
import com.example.GreenFood.model.Review;
import com.example.GreenFood.user.CustomerRepository;
import com.example.GreenFood.product.ProductRepository;
import com.example.GreenFood.product.ReviewRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;

    public ReviewService(ReviewRepository reviewRepository, ProductRepository productRepository, CustomerRepository customerRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
    }

    public record ReviewSummary(double averageRating, long totalReviews) {
    }

    public List<Review> getProductReviews(int productId) {
        ensureProductExists(productId);
        return reviewRepository.findByProductIdOrderByReviewDateDesc(productId);
    }

    public Review createReview(int productId, Integer customerId, int rating, String comment) {
        if (rating < 1 || rating > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Đánh giá phải nằm trong khoảng 1 đến 5");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm"));

        Review review = new Review();
        review.setProduct(product);
        review.setRating(rating);
        review.setComment(comment);
        review.setReviewDate(LocalDateTime.now());
        if (customerId != null) {
            review.setCustomer(customerRepository.getReferenceById(customerId));
        }
        return reviewRepository.save(review);
    }

    public ReviewSummary getReviewSummary(int productId) {
        ensureProductExists(productId);
        double average = reviewRepository.averageRatingByProductId(productId);
        long total = reviewRepository.countByProductId(productId);
        return new ReviewSummary(Math.round(average * 10.0) / 10.0, total);
    }

    private void ensureProductExists(int productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm");
        }
    }
}
