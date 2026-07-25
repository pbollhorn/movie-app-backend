package dat.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import dat.dto.TmdbCreditDto;
import dat.entities.Person;

public class PersonDao {

    private static PersonDao instance;
    private static EntityManagerFactory emf;

    private PersonDao(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public static PersonDao getInstance(EntityManagerFactory emf) {
        if (instance == null) {
            instance = new PersonDao(emf);
        }
        return instance;
    }

    // Update person (or create it if it does not already exist)
    public Person update(TmdbCreditDto tmdbCreditDto) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Person person = em.merge(new Person(tmdbCreditDto));
            em.getTransaction().commit();
            return person;
        }

    }

    /**
     * Delete persons in the database that are orphaned.
     * These are persons that have no credits after an update from TMDB.
     * @return Number of deleted persons
     */
    public int deleteOrphanedPersons() {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            int deletedCount = em.createQuery("DELETE FROM Person p WHERE p.credits IS EMPTY")
                    .executeUpdate();
            em.getTransaction().commit();
            return deletedCount;
        }
    }

}