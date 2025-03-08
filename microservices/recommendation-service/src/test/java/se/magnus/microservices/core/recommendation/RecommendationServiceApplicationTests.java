package se.magnus.microservices.core.recommendation;

import static org.junit.Assert.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Mono;
import se.magnus.microservices.api.core.recommendation.Recommendation;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class RecommendationServiceApplicationTests extends MongoDbTestBase {

    @Autowired
    private WebTestClient client;

    @Autowired
    private RecommendationRepository repository;

    @BeforeEach
    void setupDb() {
        repository.deleteAll();
    }

    @Test
    void getRecommendationsByProductId() {
        int productId = 1;

        postAndVerifyRecommendation(productId, 1, OK);
        postAndVerifyRecommendation(productId, 2, OK);
        postAndVerifyRecommendation(productId, 3, OK);

        assertEquals(3, repository.findByProductId(productId).size());

        getAndVerifyRecommendation(productId, OK)
                .jsonPath("$.length()").isEqualTo(3)
                .jsonPath("$[2].productId").isEqualTo(productId)
                .jsonPath("$[2].recommendationId").isEqualTo(3);
    }

    @Test
    void duplicateError() {
        int productId = 1;
        int recommendationId = 1;

        postAndVerifyRecommendation(productId, recommendationId, OK)
                .jsonPath("$.productId").isEqualTo(productId)
                .jsonPath("$.recommendationId").isEqualTo(recommendationId);
        assertEquals(1, repository.count());

        postAndVerifyRecommendation(productId, recommendationId, UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/recommendation")
                .jsonPath("$.message").isEqualTo("Duplicate key, Product Id: 1, Recommendation Id:1");
        assertEquals(1, repository.count());
    }

    @Test
    void delete() {
        int productId = 1;
        int recommendationId = 1;

        postAndVerifyRecommendation(productId, recommendationId, OK);
        assertEquals(1, repository.findByProductId(productId).size());

        deleteAndVerifyRecommendation(productId, OK);
        assertEquals(0, repository.findByProductId(productId).size());

        deleteAndVerifyRecommendation(productId, OK);
    }

    @Test
    void getRecommendationsMissingParameter() {
        getAndVerifyRecommendation("", BAD_REQUEST)
                .jsonPath("$.path").isEqualTo("/recommendation")
                .jsonPath("$.message").isEqualTo("Required query parameter 'productId' is not present.");
    }

    @Test
    void getRecommendationsInvalidParameter() {
        getAndVerifyRecommendation("?productId=no-integer", BAD_REQUEST)
                .jsonPath("$.path").isEqualTo("/recommendation")
                .jsonPath("$.message").isEqualTo("Type mismatch.");
    }

    @Test
    void getRecommendationsNotFound() {
        int productIdNotFound = 113;
        getAndVerifyRecommendation(productIdNotFound, OK)
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    void getRecommendationsInvalidParameterNegativeValue() {
        int productIdInvalid = -1;
        getAndVerifyRecommendation(productIdInvalid, UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/recommendation")
                .jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
    }

    private WebTestClient.BodyContentSpec postAndVerifyRecommendation(int productId, int recommendationId,
            HttpStatus expectedStatus) {
        Recommendation recommendation = new Recommendation(productId, recommendationId, "Author" + recommendationId,
                recommendationId, "Content " + recommendationId, "SA");
        return client.post()
                .uri("/recommendation")
                .body(Mono.just(recommendation), Recommendation.class)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody();
    }

    private WebTestClient.BodyContentSpec getAndVerifyRecommendation(int productId, HttpStatus expectedStatus) {
        return getAndVerifyRecommendation("?productId=" + productId, expectedStatus);
    }

    private WebTestClient.BodyContentSpec getAndVerifyRecommendation(String productIdQuery, HttpStatus expectedStatus) {
        return client.get()
                .uri("/recommendation" + productIdQuery)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody();
    }

    private WebTestClient.BodyContentSpec deleteAndVerifyRecommendation(int productId, HttpStatus expectedStatus) {
        return client.delete()
                .uri("/recommendation?productId=" + productId)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectBody();
    }
}
