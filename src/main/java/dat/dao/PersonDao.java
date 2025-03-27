package dat.dao;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import dat.entities.Person;

public class PersonDao extends AbstractDao<Person, Integer> {

    private static PersonDao instance;

    private PersonDao(EntityManagerFactory emf) {
        super(Person.class, emf);
    }

    public static PersonDao getInstance(EntityManagerFactory emf) {
        if (instance == null) {
            instance = new PersonDao(emf);
        }
        return instance;
    }


    public List<Person> readPersonsByMovieId(int movieId) {
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "SELECT p FROM Credit c JOIN c.person p WHERE c.movie.id=:movieId";
            TypedQuery<Person> query = em.createQuery(jpql, Person.class);
            query.setParameter("movieId", movieId);
            return query.getResultList();
        } catch (RuntimeException e) {
            e.printStackTrace();
            return null;
        }
    }


    public List<Person> readPersonsByMovieIdAndJob(int movieId, String job) {
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "SELECT p FROM Credit c JOIN c.person p WHERE c.movie.id=:movieId AND c.job=:job";
            TypedQuery<Person> query = em.createQuery(jpql, Person.class);
            query.setParameter("movieId", movieId);
            query.setParameter("job", job);
            return query.getResultList();
        } catch (RuntimeException e) {
            e.printStackTrace();
            return null;
        }
    }


}