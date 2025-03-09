package se.magnus.microservices.core.review;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PersistenceTests extends MysqlTestBase {

    @Autowired
    private ReviewRepository repository;

    private ReviewEntity savedEntity;

    @BeforeEach
    void setupDb() {
        repository.deleteAll();

        ReviewEntity entity = new ReviewEntity(1, 1, "a", "s", "adr");
        savedEntity = repository.save(entity);

        assertEquals(entity, savedEntity);
    }

    @Test
    void create() {
        ReviewEntity newEntity = new ReviewEntity(1, 2, "a", "s", "ard");
        repository.save(newEntity);

        ReviewEntity foundEntity = repository.findById(newEntity.getId()).get();
        assertEquals(newEntity, foundEntity);

        assertEquals(2, repository.count());
    }

    @Test
    void update() {
        savedEntity.setAuthor("a2");
        repository.save(savedEntity);

        ReviewEntity foundEntity = repository.findById(savedEntity.getId()).get();
        assertEquals(1, foundEntity.getVersion());
        assertEquals("a2", foundEntity.getAuthor());
    }

    @Test
    void delete() {
        repository.delete(savedEntity);
        assertFalse(repository.existsById(savedEntity.getId()));
    }

    @Test
    void getByProductId() {
        List<ReviewEntity> reviews = repository.findByProductId(savedEntity.getProductId());
        assertEquals(1, reviews.size());
        assertEquals(savedEntity, reviews.get(0));
    }

    @Test
    void duplicateError() {
        ReviewEntity entity = new ReviewEntity(1, 1, "a2", "s2", "adr2");
        assertThrows(DataIntegrityViolationException.class, () -> repository.save(entity));
    }

    @Test
    void optimisticLockError() {
        // Store the saved entity in two separate entity objects
        ReviewEntity entity1 = repository.findById(savedEntity.getId()).get();
        ReviewEntity entity2 = repository.findById(savedEntity.getId()).get();

        // Update the entity using the first entity object
        entity1.setAuthor("a1");
        repository.save(entity1);

        // Update the entity using the second entity object.
        entity2.setAuthor("a2");
        assertThrows(OptimisticLockingFailureException.class, () -> repository.save(entity2));

        // Get the updated entity from the database and verify its new sate
        ReviewEntity updatedEntity = repository.findById(savedEntity.getId()).get();
        assertEquals(1, updatedEntity.getVersion());
        assertEquals("a1", updatedEntity.getAuthor());
    }
}
