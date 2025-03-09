package se.magnus.microservices.core.review;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Data;

@Data
@Entity
@Table(name = "reviews", indexes = {
        @Index(name = "reviews_unique_idx", unique = true, columnList = "productId,reviewId")
})
public class ReviewEntity {
    @Id
    @GeneratedValue
    private int id;

    @Version
    private int version;

    private int productId;

    private int reviewId;

    private String author;

    private String subject;

    private String content;

    public ReviewEntity() {
    }

    public ReviewEntity(int productId, int reviewId, String author, String subject, String content) {
        this.productId = productId;
        this.reviewId = reviewId;
        this.author = author;
        this.subject = subject;
        this.content = content;
    }

}
