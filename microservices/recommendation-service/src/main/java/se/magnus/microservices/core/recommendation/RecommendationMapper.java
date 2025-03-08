package se.magnus.microservices.core.recommendation;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import se.magnus.microservices.api.core.recommendation.Recommendation;

@Mapper(componentModel = "spring")
public interface RecommendationMapper {

    @Mapping(target = "rate", source = "rating")
    @Mapping(target = "serviceAddress", ignore = true)
    Recommendation entityToApi(RecommendationEntity entity);

    @Mapping(target = "rating", source = "rate")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    RecommendationEntity apiToEntity(Recommendation api);

    List<Recommendation> entityToApi(List<RecommendationEntity> apiList);

    List<RecommendationEntity> apiToEntity(List<Recommendation> entityList);
}
