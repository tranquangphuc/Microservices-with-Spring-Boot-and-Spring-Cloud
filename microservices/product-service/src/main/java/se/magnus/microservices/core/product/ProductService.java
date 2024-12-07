package se.magnus.microservices.core.product;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import se.magnus.microservices.api.core.product.Product;
import se.magnus.microservices.api.exceptions.InvalidInputException;
import se.magnus.microservices.api.exceptions.NotFoundException;
import se.magnus.microservices.util.http.ServiceUtil;

@Slf4j
@RestController
public class ProductService {

    private final ServiceUtil serviceUtil;

    public ProductService(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    /**
     * Sample usage: "curl $HOST:$PORT/product/1".
     *
     * @param productId Id of the product
     * @return the product, if found, else null
     */
    @GetMapping("/product/{productId}")
    Product getProduct(@PathVariable int productId) {
        log.debug("/product return the found product for productId={}", productId);

        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        if (productId == 13) {
            throw new NotFoundException("No product found for productId: " + productId);
        }

        return new Product(productId, "name-" + productId, 123, serviceUtil.getServiceAddress());
    }
}
