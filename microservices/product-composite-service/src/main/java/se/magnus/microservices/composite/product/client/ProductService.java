package se.magnus.microservices.composite.product.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import se.magnus.microservices.api.core.product.Product;

@FeignClient(name = "product-service", fallbackFactory = ProductServiceFallback.class)
public interface ProductService {

    @GetMapping("/product/{productId}")
    Product getProduct(@PathVariable int productId);

}
