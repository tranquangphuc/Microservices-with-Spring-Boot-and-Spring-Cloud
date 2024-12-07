package se.magnus.microservices.core.review;

import java.util.Collections;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import se.magnus.microservices.api.core.review.Review;
import se.magnus.microservices.api.exceptions.InvalidInputException;
import se.magnus.microservices.util.http.ServiceUtil;

@Slf4j
@RestController
public class ReviewService {

    private final ServiceUtil serviceUtil;

    public ReviewService(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    /**
     * Sample usage: "curl $HOST:$PORT/review?productId=1".
     *
     * @param productId Id of the product
     * @return the reviews of the product
     */
    @GetMapping("/review")
    List<Review> getReviews(@RequestParam int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }
        if (productId == 213) {
            log.debug("No reviews found for productId: " + productId);
            return Collections.emptyList();
        }
        List<Review> list = List.of(
                new Review(productId, 1, "Author 1", "Subject 1", "Content 1", serviceUtil.getServiceAddress()),
                new Review(productId, 2, "Author 2", "Subject 2", "Content 2", serviceUtil.getServiceAddress()),
                new Review(productId, 3, "Author 3", "Subject 3", "Content 3", serviceUtil.getServiceAddress()));

        log.debug("/reviews reponse size: {}" + list.size());
        return list;
    }
}
