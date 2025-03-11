package se.magnus.microservices.composite.product.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import se.magnus.microservices.api.core.recommendation.Recommendation;

@FeignClient(name = "recommendation-service")
public interface RecommendationService {

    @GetMapping("/recommendation")
    List<Recommendation> getRecommendations(@RequestParam int productId);

    @PostMapping("/recommendation")
    Recommendation createRecommendation(@RequestBody Recommendation recommendation);

    @DeleteMapping("/recommendation")
    void deleteRecommendations(@RequestParam int productId);

}
