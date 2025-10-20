package dat.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dat.exceptions.DaoException;

public class GenericDao {
    protected final EntityManagerFactory emf;
    private final Logger logger = LoggerFactory.getLogger(GenericDao.class);

    public GenericDao(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public <T> T create(T object) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(object);
            em.getTransaction().commit();
            return object;
        } catch (Exception e) {
            logger.error("Error persisting object to db", e);
            throw new DaoException("Error persisting object to db. ", e);
        }
    }

    public <T> T getById(Class<T> type, Object id) {
        try (EntityManager em = emf.createEntityManager()) {
            T entity = em.find(type, id);
            if (entity == null) {
                throw new EntityNotFoundException("No entity found with id " + id.toString());
            }
            return entity;
        } catch (Exception e) {
            logger.error("Error reading object from db", e);
            throw new DaoException("Error reading object from db", e);
        }
    }

    public <T> T update(T object) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            T updatedEntity = em.merge(object);
            em.getTransaction().commit();
            return updatedEntity;
        } catch (Exception e) {
            logger.error("Error updating object", e);
            throw new DaoException("Error updating object. ", e);
        }
    }

}
