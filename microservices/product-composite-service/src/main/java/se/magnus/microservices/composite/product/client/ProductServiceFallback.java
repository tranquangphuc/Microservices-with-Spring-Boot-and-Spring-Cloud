package se.magnus.microservices.composite.product.client;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import se.magnus.microservices.api.core.product.Product;
import se.magnus.microservices.api.exceptions.InvalidInputException;
import se.magnus.microservices.util.http.HttpErrorInfo;

@Slf4j
@Component
public class ProductServiceFallback implements FallbackFactory<ProductService> {

    private final ObjectMapper om;

    public ProductServiceFallback(ObjectMapper om) {
        this.om = om;
    }

    @Override
    public ProductService create(Throwable cause) {
        log.info("ProductService error: {}", cause.getMessage());
        return new ProductService() {

            @Override
            public Product getProduct(int productId) {
                if (cause instanceof FeignException.UnprocessableEntity exception) {
                    throw new InvalidInputException(getErrorMessage(exception));
                }
                return null;
            }

        };
    }

    private String getErrorMessage(FeignException exception) {
        try {
            return om.readValue(exception.contentUTF8(), HttpErrorInfo.class).getMessage();
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
