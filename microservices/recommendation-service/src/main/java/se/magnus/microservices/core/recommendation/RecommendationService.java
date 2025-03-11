package se.magnus.microservices.core.recommendation;

import java.util.List;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    private final RecommendationRepository repository;
    private final RecommendationMapper mapper;

    public RecommendationService(ServiceUtil serviceUtil,
            RecommendationRepository repository,
            RecommendationMapper mapper) {
        this.serviceUtil = serviceUtil;
        this.repository = repository;
        this.mapper = mapper;
    }

    /**
     * Sample usage, see below.
     *
     * curl -X POST $HOST:$PORT/recommendation \
     *   -H "Content-Type: application/json" --data \
     *   '{"productId":123,"recommendationId":456,"author":"me","rate":5,"content":"yada, yada, yada"}'
     *
     * @param body A JSON representation of the new recommendation
     * @return A JSON representation of the newly created recommendation
     */
    @PostMapping(value = "/recommendation", consumes = "application/json", produces = "application/json")
    public Recommendation createRecommendation(@RequestBody Recommendation body) {
        try {
            RecommendationEntity entity = mapper.apiToEntity(body);
            RecommendationEntity newEntity = repository.save(entity);
            log.debug("createRecommendation: created a recommendation entity: {}/{}",
                    body.getProductId(), body.getRecommendationId());
            return mapper.entityToApi(newEntity);
        } catch (DuplicateKeyException e) {
            throw new InvalidInputException("Duplicate key, Product Id: " + body.getProductId()
                    + ", Recommendation Id:" + body.getRecommendationId());
        }
    }

    /**
     * Sample usage: "curl $HOST:$PORT/recommendation?productId=1".
     *
     * @param productId Id of the product
     * @return the recommendations of the product
     */
    @GetMapping(value = "/recommendation", produces = "application/json")
    public List<Recommendation> getRecommendations(@RequestParam("productId") int productId) {

        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        List<RecommendationEntity> entities = repository.findByProductId(productId);
        List<Recommendation> list = mapper.entityToApi(entities);
        list.forEach(it -> it.setServiceAddress(serviceUtil.getServiceAddress()));

        log.debug("getRecommendations: response size: {}", list.size());

        return list;
    }

    /**
     * Sample usage: "curl -X DELETE $HOST:$PORT/recommendation?productId=1".
     *
     * @param productId Id of the product
     */
    @DeleteMapping(value = "/recommendation")
    public void deleteRecommendations(@RequestParam(value = "productId", required = true) int productId) {
        log.debug("deleteRecommendations: tries to delete recommendations for the product with productId: {}",
                productId);
        repository.deleteAll(repository.findByProductId(productId));
    }
}
