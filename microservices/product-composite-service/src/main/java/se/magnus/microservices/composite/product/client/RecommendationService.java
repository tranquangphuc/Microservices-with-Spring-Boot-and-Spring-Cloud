package se.magnus.microservices.composite.product.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import se.magnus.microservices.api.core.recommendation.Recommendation;

@FeignClient(name = "recommendation-service")
public interface RecommendationService {

    @GetMapping(value = "/recommendation")
    List<Recommendation> getRecommendations(@RequestParam int productId);

}
