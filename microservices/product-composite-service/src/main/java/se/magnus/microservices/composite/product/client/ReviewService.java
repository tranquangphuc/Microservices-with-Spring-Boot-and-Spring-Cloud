package se.magnus.microservices.composite.product.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import se.magnus.microservices.api.core.review.Review;

@FeignClient(name = "review-service")
public interface ReviewService {

    @GetMapping("/review")
    List<Review> getReviews(@RequestParam int productId);

    @PostMapping("/review")
    Review createReview(@RequestBody Review review);

    @DeleteMapping("/review")
    void deleteReviews(@RequestParam int productId);

}
