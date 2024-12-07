package se.magnus.microservices.core.recommendation;

import java.util.Collections;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import se.magnus.microservices.api.core.recommendation.Recommendation;
import se.magnus.microservices.api.exceptions.InvalidInputException;
import se.magnus.microservices.util.http.ServiceUtil;

@Slf4j
@RestController
public class RecommendationService {

    private final ServiceUtil serviceUtil;

    public RecommendationService(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    /**
     * Sample usage: "curl $HOST:$PORT/recommendation?productId=1".
     *
     * @param productId Id of the product
     * @return the recommendations of the product
     */
    @GetMapping(value = "/recommendation")
    List<Recommendation> getRecommendations(@RequestParam int productId) {

        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        if (productId == 113) {
            log.debug("No recommendations found for productId: {}", productId);
            return Collections.emptyList();
        }

        List<Recommendation> list = List.of(
                new Recommendation(productId, 1, "Author 1", 1, "Content 1", serviceUtil.getServiceAddress()),
                new Recommendation(productId, 2, "Author 2", 2, "Content 2", serviceUtil.getServiceAddress()),
                new Recommendation(productId, 3, "Author 3", 3, "Content 3", serviceUtil.getServiceAddress()));

        log.debug("/recommendation response size: {}", list.size());
        return list;
    }
}
