package dat.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

public class CollectionDao {

    private static CollectionDao instance;
    private static EntityManagerFactory emf;

    private CollectionDao(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public static CollectionDao getInstance(EntityManagerFactory emf) {
        if (instance == null) {
            instance = new CollectionDao(emf);
        }
        return instance;
    }

    /**
     * Delete orphaned collections in the database.
     * I.e. collections that no longer contains any movies after an update from TMDB.
     * @return Number of deleted collections
     */
    public int deleteOrphanedCollections() {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            String jpql = """
                    DELETE FROM Collection c WHERE c.id NOT IN
                    (SELECT m.collection.id FROM Movie m WHERE m.collection IS NOT NULL)""";
            int deletedCount = em.createQuery(jpql).executeUpdate();
            em.getTransaction().commit();
            return deletedCount;
        }
    }

}
