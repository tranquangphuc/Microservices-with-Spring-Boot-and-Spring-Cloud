package se.magnus.microservices.core.review;

import java.util.Collections;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    private final ReviewRepository repository;
    private final ReviewMapper mapper;

    public ReviewService(ServiceUtil serviceUtil, ReviewRepository repository, ReviewMapper mapper) {
        this.serviceUtil = serviceUtil;
        this.repository = repository;
        this.mapper = mapper;
    }

    /**
     * Sample usage, see below.
     *
     * curl -X POST $HOST:$PORT/review \
     *   -H "Content-Type: application/json" --data \
     *   '{"productId":123,"reviewId":456,"author":"me","subject":"yada, yada, yada","content":"yada, yada, yada"}'
     *
     * @param body A JSON representation of the new review
     * @return A JSON representation of the newly created review
     */
    @PostMapping(value = "/review", consumes = "application/json", produces = "application/json")
    public Review createReview(@RequestBody Review body) {
        try {
            ReviewEntity entity = mapper.apiToEntity(body);
            ReviewEntity newEntity = repository.save(entity);
            log.debug("createReview: created a review entity: {}/{}", body.getProductId(), body.getReviewId());
            return mapper.entityToApi(newEntity);
        } catch (DataIntegrityViolationException e) {
            throw new InvalidInputException(
                    "Duplicate key, Product Id: " + body.getProductId() + ", Review Id:" + body.getReviewId());
        }
    }

    /**
     * Sample usage: "curl $HOST:$PORT/review?productId=1".
     *
     * @param productId Id of the product
     * @return the reviews of the product
     */
    @GetMapping(value = "/review", produces = "application/json")
    public List<Review> getReviews(@RequestParam int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }
        List<ReviewEntity> entities = repository.findByProductId(productId);
        List<Review> list = mapper.entityToApi(entities);
        list.forEach(it -> it.setServiceAddress(serviceUtil.getServiceAddress()));
        log.debug("getReviews: response size: {}", list.size());
        return list;
    }

    /**
     * Sample usage: "curl -X DELETE $HOST:$PORT/review?productId=1".
     *
     * @param productId Id of the product
     */
    @DeleteMapping(value = "/review")
    public void deleteReviews(@RequestParam(value = "productId", required = true) int productId) {
        log.debug("deleteReviews: tries to delete reviews for the product with productId: {}", productId);
        repository.deleteAll(repository.findByProductId(productId));
    }

}
