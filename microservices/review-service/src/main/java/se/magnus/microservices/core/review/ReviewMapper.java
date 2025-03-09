package se.magnus.microservices.core.review;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import se.magnus.microservices.api.core.review.Review;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(target = "serviceAddress", ignore = true)
    Review entityToApi(ReviewEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    ReviewEntity apiToEntity(Review api);

    List<Review> entityToApi(List<ReviewEntity> entities);

    List<ReviewEntity> apiToEntity(List<Review> api);
}
