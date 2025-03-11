package se.magnus.microservices.composite.product;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import se.magnus.microservices.api.composite.product.ProductAggregate;
import se.magnus.microservices.api.composite.product.RecommendationSummary;
import se.magnus.microservices.api.composite.product.ReviewSummary;
import se.magnus.microservices.api.composite.product.ServiceAddresses;
import se.magnus.microservices.api.core.product.Product;
import se.magnus.microservices.api.core.recommendation.Recommendation;
import se.magnus.microservices.api.core.review.Review;
import se.magnus.microservices.api.exceptions.NotFoundException;
import se.magnus.microservices.composite.product.client.ProductService;
import se.magnus.microservices.composite.product.client.RecommendationService;
import se.magnus.microservices.composite.product.client.ReviewService;
import se.magnus.microservices.util.http.ServiceUtil;

@Slf4j
@Tag(name = "ProductComposite", description = "REST API for composite product information")
@RestController
public class ProductCompositeService {

    private final ServiceUtil serviceUtil;
    private final ProductService productService;
    private final RecommendationService recommendationService;
    private final ReviewService reviewService;

    public ProductCompositeService(
            ServiceUtil serviceUtil,
            ProductService productService,
            RecommendationService recommendationService,
            ReviewService reviewService) {
        this.serviceUtil = serviceUtil;
        this.productService = productService;
        this.recommendationService = recommendationService;
        this.reviewService = reviewService;
    }

    /**
     * Sample usage, see below.
     *
     * curl -X POST $HOST:$PORT/product-composite \
     *   -H "Content-Type: application/json" --data \
     *   '{"productId":123,"name":"product 123","weight":123}'
     *
     * @param body A JSON representation of the new composite product
     */
    @Operation(summary = "${api.product-composite.create-composite-product.description}", description = "${api.product-composite.create-composite-product.notes}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}"),
            @ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}")
    })
    @PostMapping(value = "/product-composite", consumes = "application/json")
    public void createProduct(@RequestBody ProductAggregate body) {
        try {
            log.debug("createCompositeProduct: creates a new composite entity for productId: {}", body.getProductId());

            Product product = new Product(body.getProductId(), body.getName(), body.getWeight(), null);
            productService.createProduct(product);

            if (body.getRecommendations() != null) {
                body.getRecommendations().forEach(it -> {
                    Recommendation recommendation = new Recommendation(body.getProductId(), it.getRecommendationId(),
                            it.getAuthor(), it.getRate(), it.getContent(), null);
                    recommendationService.createRecommendation(recommendation);
                });
            }

            if (body.getReviews() != null) {
                body.getReviews().forEach(it -> {
                    Review review = new Review(body.getProductId(), it.getReviewId(), it.getAuthor(), it.getSubject(),
                            it.getContent(), null);
                    reviewService.createReview(review);
                });
            }

            log.debug("createCompositeProduct: composite entities created for productId: {}", body.getProductId());
        } catch (Exception e) {
            log.warn("createCompositeProduct failed", e);
            throw e;
        }
    }

    /**
     * Sample usage: "curl $HOST:$PORT/product-composite/1".
     *
     * @param productId Id of the product
     * @return the composite product info, if found, else null
     */
    @Operation(
        summary = "${api.product-composite.get-composite-product.description}",
        description = "${api.product-composite.get-composite-product.notes}"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}"),
            @ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}"),
            @ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}"),
            @ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}")
    })
    @GetMapping(value = "/product-composite/{productId}")
    ProductAggregate getProduct(@PathVariable int productId) {
        log.debug("getCompositeProduct: lookup a product aggregate for productId: {}", productId);
        Product product = productService.getProduct(productId);
        if (product == null) {
            throw new NotFoundException("No product found for productId: " + productId);
        }
        List<Recommendation> recommendations = recommendationService.getRecommendations(productId);
        List<Review> reviews = reviewService.getReviews(productId);
        log.debug("getCompositeProduct: aggregate entity found for productId: {}", productId);
        return createProductAggregate(product, recommendations, reviews, serviceUtil.getServiceAddress());
    }

    private ProductAggregate createProductAggregate(
            Product product,
            List<Recommendation> recommendations,
            List<Review> reviews,
            String serviceAddress) {

        // 1. Setup product info
        int productId = product.getProductId();
        String name = product.getName();
        int weight = product.getWeight();

        // 2. Copy summary recommendation info, if available
        List<RecommendationSummary> recommendationSummaries = (recommendations == null) ? null
                : recommendations.stream()
                        .map(r -> new RecommendationSummary(r.getRecommendationId(), r.getAuthor(), r.getRate(),
                                r.getContent()))
                        .collect(Collectors.toList());

        // 3. Copy summary review info, if available
        List<ReviewSummary> reviewSummaries = (reviews == null) ? null
                : reviews.stream()
                        .map(r -> new ReviewSummary(r.getReviewId(), r.getAuthor(), r.getSubject(), r.getContent()))
                        .collect(Collectors.toList());

        // 4. Create info regarding the involved microservices addresses
        String productAddress = product.getServiceAddress();
        String reviewAddress = (reviews != null && reviews.size() > 0) ? reviews.get(0).getServiceAddress() : "";
        String recommendationAddress = (recommendations != null && recommendations.size() > 0)
                ? recommendations.get(0).getServiceAddress()
                : "";
        ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress, productAddress, reviewAddress,
                recommendationAddress);

        return new ProductAggregate(productId, name, weight, recommendationSummaries, reviewSummaries,
                serviceAddresses);
    }

    /**
     * Sample usage: "curl -X DELETE $HOST:$PORT/product-composite/1".
     *
     * @param productId Id of the product
     */
    @Operation(summary = "${api.product-composite.delete-composite-product.description}", description = "${api.product-composite.delete-composite-product.notes}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}"),
            @ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}")
    })
    @DeleteMapping(value = "/product-composite/{productId}")
    public void deleteProduct(@PathVariable int productId) {
        log.debug("deleteCompositeProduct: Deletes a product aggregate for productId: {}", productId);
        productService.deleteProduct(productId);
        recommendationService.deleteRecommendations(productId);
        reviewService.deleteReviews(productId);
        log.debug("deleteCompositeProduct: aggregate entities deleted for productId: {}", productId);
    }

}
