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

    public Person update(TmdbCreditDto tmdbCreditDto) {

        try (EntityManager em = emf.createEntityManager()) {

            em.getTransaction().begin();
            Person person = em.merge(new Person(tmdbCreditDto));
            em.getTransaction().commit();

            return person;
        }

    }

}