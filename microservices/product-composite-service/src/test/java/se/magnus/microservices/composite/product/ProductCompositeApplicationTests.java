package se.magnus.microservices.composite.product;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import se.magnus.microservices.api.core.product.Product;
import se.magnus.microservices.api.core.recommendation.Recommendation;
import se.magnus.microservices.api.core.review.Review;
import se.magnus.microservices.api.exceptions.InvalidInputException;
import se.magnus.microservices.api.exceptions.NotFoundException;
import se.magnus.microservices.composite.product.client.ProductService;
import se.magnus.microservices.composite.product.client.RecommendationService;
import se.magnus.microservices.composite.product.client.ReviewService;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ProductCompositeApplicationTests {

    private static final int PRODUCT_ID_OK = 1;
    private static final int PRODUCT_ID_NOT_FOUND = 13;
    private static final int PRODUCT_ID_INVALID = -1;

    @MockitoBean
    ProductService productService;

    @MockitoBean
    RecommendationService recommendationService;

    @MockitoBean
    ReviewService reviewService;

    @Autowired
    WebTestClient client;

    @BeforeEach
    void setUp() {
        when(productService.getProduct(PRODUCT_ID_OK))
                .thenReturn(new Product(PRODUCT_ID_OK, "name", 1, "mock address"));
        when(recommendationService.getRecommendations(PRODUCT_ID_OK))
                .thenReturn(List.of(new Recommendation(PRODUCT_ID_OK, 1, "author", 1, "content", "mock address")));
        when(reviewService.getReviews(PRODUCT_ID_OK))
                .thenReturn(List.of(new Review(PRODUCT_ID_OK, 1, "author", "subject", "content", "mock address")));
        when(productService.getProduct(PRODUCT_ID_NOT_FOUND))
                .thenThrow(new NotFoundException("NOT FOUND: " + PRODUCT_ID_NOT_FOUND));
        when(productService.getProduct(PRODUCT_ID_INVALID))
                .thenThrow(new InvalidInputException("INVALID: " + PRODUCT_ID_INVALID));
    }

    @Test
    void contextLoads() {
    }

    @Test
    void getProductById() {
        client.get()
                .uri("/product-composite/" + PRODUCT_ID_OK)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.productId").isEqualTo(PRODUCT_ID_OK)
                .jsonPath("$.recommendations.length()").isEqualTo(1)
                .jsonPath("$.reviews.length()").isEqualTo(1);
    }

    @Test
    void getProductNotFound() {
        client.get()
                .uri("/product-composite/" + PRODUCT_ID_NOT_FOUND)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_NOT_FOUND)
                .jsonPath("$.message").isEqualTo("NOT FOUND: " + PRODUCT_ID_NOT_FOUND);
    }

    @Test
    void getProductInvalidInput() {
        client.get()
                .uri("/product-composite/" + PRODUCT_ID_INVALID)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_INVALID)
                .jsonPath("$.message").isEqualTo("INVALID: " + PRODUCT_ID_INVALID);
    }
}
