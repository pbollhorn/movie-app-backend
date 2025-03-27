package dat.dao;

import jakarta.persistence.EntityManagerFactory;

import dat.entities.Genre;

public class GenreDao extends AbstractDao<Genre, Integer> {

    private static GenreDao instance;

    private GenreDao(EntityManagerFactory emf) {
        super(Genre.class, emf);
    }

    public static GenreDao getInstance(EntityManagerFactory emf) {
        if (instance == null) {
            instance = new GenreDao(emf);
        }
        return instance;
    }


}