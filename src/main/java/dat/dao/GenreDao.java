package dat.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import dat.dto.TmdbGenreDto;
import dat.entities.Genre;

public class GenreDao {

    private static GenreDao instance;
    private static EntityManagerFactory emf;

    private GenreDao(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public static GenreDao getInstance(EntityManagerFactory emf) {
        if (instance == null) {
            instance = new GenreDao(emf);
        }
        return instance;
    }

    // Update genre (or create it if it does not already exist)
    public Genre update(TmdbGenreDto tmdbGenreDto) {

        try (EntityManager em = emf.createEntityManager()) {

            em.getTransaction().begin();
            Genre genre = em.merge(new Genre(tmdbGenreDto));
            em.getTransaction().commit();

            return genre;
        }

    }

}