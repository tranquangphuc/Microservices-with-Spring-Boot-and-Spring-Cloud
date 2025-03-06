package se.magnus.microservices.core.product;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;

@DataMongoTest
class PersistenceTests extends MongoDbTestBase {

    @Autowired
    private ProductRepository repository;

    private ProductEntity savedEntity;

    @BeforeEach
    void setupDb() {
        repository.deleteAll();

        ProductEntity entity = new ProductEntity(1, "n", 2);
        savedEntity = repository.save(entity);

        assertEquals(entity, savedEntity);
        assertEquals(0, savedEntity.getVersion());
    }

    @Test
    void testCreate() {
        ProductEntity secondEntity = new ProductEntity(2, "n", 2);
        repository.save(secondEntity);
        ProductEntity foundEntity = repository.findByProductId(2).get();
        assertEquals(foundEntity, secondEntity);
        assertEquals(2, repository.count());
    }

    @Test
    void testUpdate() {
        savedEntity.setName("n2");
        repository.save(savedEntity);
        ProductEntity foundEntity = repository.findById(savedEntity.getId()).get();
        assertEquals("n2", foundEntity.getName());
        assertEquals(1, foundEntity.getVersion());
    }

    @Test
    void testDelete() {
        repository.delete(savedEntity);
        assertFalse(repository.existsById(savedEntity.getId()));
    }

    @Test
    void testFindByProductId() {
        Optional<ProductEntity> existingProduct = repository.findByProductId(savedEntity.getProductId());
        assertTrue(existingProduct.isPresent());
        assertEquals(savedEntity, existingProduct.get());
    }

    @Test
    void testDuplicateError() {
        assertThrows(DuplicateKeyException.class,
                () -> repository.save(new ProductEntity(savedEntity.getProductId(), "n2", 2)));
    }

    @Test
    void testOptimisticLock() {
        // Store the saved entity into two separate entity objects
        ProductEntity entity1 = repository.findById(savedEntity.getId()).get();
        ProductEntity entity2 = repository.findById(savedEntity.getId()).get();

        // Update the entity using the first entity object
        entity1.setName("n1");
        repository.save(entity1);

        // Update the entity using the second entity object
        // This should fail since the second entity now hold an old version number
        entity2.setName("n2");
        assertThrows(OptimisticLockingFailureException.class, () -> repository.save(entity2));

        // Get the updated entity from the database and verify its new state
        ProductEntity updatedEntity = repository.findById(savedEntity.getId()).get();
        assertEquals("n1", updatedEntity.getName());
        assertEquals(1, updatedEntity.getVersion());
    }

    @Test
    void testPaging() {
        repository.deleteAll();
        List<ProductEntity> newProducts = IntStream.rangeClosed(1001, 1010)
                .mapToObj(i -> new ProductEntity(i, "name-" + i, i))
                .collect(Collectors.toList());
        repository.saveAll(newProducts);

        Pageable pageable = PageRequest.of(0, 4, Direction.ASC, "productId");
        pageable = testPageable(pageable, List.of(1001, 1002, 1003, 1004), true);
        pageable = testPageable(pageable, List.of(1005, 1006, 1007, 1008), true);
        pageable = testPageable(pageable, List.of(1009, 1010), false);
    }

    private Pageable testPageable(Pageable pageable, List<Integer> expectedProductIds, boolean expectedHasNext) {
        Page<ProductEntity> page = repository.findAll(pageable);
        assertEquals(expectedProductIds, page.getContent().stream().map(it -> it.getProductId()).collect(Collectors.toList()));
        assertEquals(expectedHasNext, page.hasNext());
        return page.nextPageable();
    }
}
