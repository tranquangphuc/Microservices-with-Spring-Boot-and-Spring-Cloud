package se.magnus.microservices.core.recommendation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;

@DataMongoTest
public class PersistenceTests extends MongoDbTestBase {

    @Autowired
    private RecommendationRepository repository;

    private RecommendationEntity savedEntity;

    @BeforeEach
    void setupDb() {
        repository.deleteAll();

        RecommendationEntity entity = new RecommendationEntity(1, 2, "a", 3, "c");
        savedEntity = repository.save(entity);

        assertEquals(entity, savedEntity);
    }

    @Test
    void create() {
        RecommendationEntity newEntity = new RecommendationEntity(2, 1, "a", 3, "c");
        repository.save(newEntity);

        RecommendationEntity foundEntity = repository.findById(newEntity.getId()).get();
        assertEquals(foundEntity, newEntity);

        assertEquals(2, repository.count());
    }

    @Test
    void update() {
        savedEntity.setAuthor("a2");
        repository.save(savedEntity);

        RecommendationEntity foundEnity = repository.findById(savedEntity.getId()).get();
        assertEquals(1, (long) foundEnity.getVersion());
        assertEquals("a2", foundEnity.getAuthor());
    }

    @Test
    void delete() {
        repository.delete(savedEntity);
        assertFalse(repository.existsById(savedEntity.getId()));
    }

    @Test
    void getByProductId() {
        List<RecommendationEntity> entities = repository.findByProductId(savedEntity.getProductId());
        assertEquals(1, entities.size());
        assertEquals(savedEntity, entities.get(0));
    }

    @Test
    void duplicateError() {
        RecommendationEntity entity = new RecommendationEntity(1, 2, "a2", 5, "abc");
        assertThrows(DuplicateKeyException.class, () -> repository.save(entity));
    }

    @Test
    void optimisticLockError() {
        RecommendationEntity entity1 = repository.findById(savedEntity.getId()).get();
        RecommendationEntity entity2 = repository.findById(savedEntity.getId()).get();

        entity1.setAuthor("a1");
        repository.save(entity1);

        entity2.setAuthor("a2");
        assertThrows(OptimisticLockingFailureException.class, () -> repository.save(entity2));

        RecommendationEntity updateEntity = repository.findById(savedEntity.getId()).get();
        assertEquals(1, (long) updateEntity.getVersion());
        assertEquals("a1", updateEntity.getAuthor());
    }
}
