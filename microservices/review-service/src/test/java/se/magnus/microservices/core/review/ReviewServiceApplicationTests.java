package se.magnus.microservices.core.review;

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
import se.magnus.microservices.api.core.review.Review;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class ReviewServiceApplicationTests extends MysqlTestBase {

    @Autowired
    private WebTestClient client;

    @Autowired
    private ReviewRepository repository;

    @BeforeEach
    void setupDb() {
        repository.deleteAll();
    }

    @Test
    void getReviewsByProductId() {
        int productId = 1;

        assertEquals(0, repository.findByProductId(productId).size());

        postAndVerifyReview(productId, 1, OK);
        postAndVerifyReview(productId, 2, OK);
        postAndVerifyReview(productId, 3, OK);

        assertEquals(3, repository.findByProductId(productId).size());

        getAndVerifyReview(productId, OK)
                .jsonPath("$.length()").isEqualTo(3)
                .jsonPath("$[2].productId").isEqualTo(productId)
                .jsonPath("$[2].reviewId").isEqualTo(3);
    }

    @Test
    void duplicateError() {
        int productId = 1;
        int reviewId = 1;

        assertEquals(0, repository.count());

        postAndVerifyReview(productId, reviewId, OK)
                .jsonPath("$.productId").isEqualTo(productId)
                .jsonPath("$.reviewId").isEqualTo(reviewId);

        assertEquals(1, repository.count());

        postAndVerifyReview(productId, reviewId, UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/review")
                .jsonPath("$.message").isEqualTo("Duplicate key, Product Id: 1, Review Id:1");

        assertEquals(1, repository.count());
    }

    @Test
    void delete() {
        int productId = 1;
        int reviewId = 1;

        postAndVerifyReview(productId, reviewId, OK);
        assertEquals(1, repository.findByProductId(productId).size());

        deleteAndVerifyReview(productId, OK);
        assertEquals(0, repository.findByProductId(productId).size());

        deleteAndVerifyReview(productId, OK);
    }

    @Test
    void getReviewsMissingParameter() {
        getAndVerifyReview("", BAD_REQUEST)
                .jsonPath("$.path").isEqualTo("/review")
                .jsonPath("$.message").isEqualTo("Required query parameter 'productId' is not present.");
    }

    @Test
    void getReviewsInvalidParameter() {
        getAndVerifyReview("?productId=no-integer", BAD_REQUEST)
                .jsonPath("$.path").isEqualTo("/review")
                .jsonPath("$.message").isEqualTo("Type mismatch.");
    }

    @Test
    void getReviewsNotFound() {
        int productIdNotFound = 213;
        getAndVerifyReview(productIdNotFound, OK)
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    void getReviewsInvalidParameterNegativeValue() {
        int productIdInvalid = -1;
        getAndVerifyReview(productIdInvalid, UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/review")
                .jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
    }

    private WebTestClient.BodyContentSpec getAndVerifyReview(int productId, HttpStatus expectedStatus) {
        return getAndVerifyReview("?productId=" + productId, expectedStatus);
    }

    private WebTestClient.BodyContentSpec getAndVerifyReview(String productIdQuery, HttpStatus expectedStatus) {
        return client.get()
                .uri("/review" + productIdQuery)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody();
    }

    private WebTestClient.BodyContentSpec postAndVerifyReview(int productId, int reviewId, HttpStatus expectedStatus) {
        Review review = new Review(productId, reviewId, "Author " + reviewId, "Subject " + reviewId,
                "Content " + reviewId, "SA");
        return client.post()
                .uri("/review")
                .body(Mono.just(review), Review.class)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody();
    }

    private WebTestClient.BodyContentSpec deleteAndVerifyReview(int productId, HttpStatus expectedStatus) {
        return client.delete()
                .uri("/review?productId=" + productId)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectBody();
    }
}
