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


}