package se.magnus.microservices.core.product;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import se.magnus.microservices.api.core.product.Product;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(target = "serviceAddress", ignore = true)
    Product entityToApi(ProductEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    ProductEntity apiToEntity(Product api);
}
