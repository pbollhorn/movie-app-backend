package dat.dao;

import java.util.List;

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

    /**
     * Get all genres from the database.
     * @return List of TmdbGenreDto
     */
    public List<TmdbGenreDto> getAllGenres() {
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "SELECT NEW dat.dto.TmdbGenreDto(g.id, g.name) FROM Genre g ORDER BY g.name";
            List<TmdbGenreDto> genres = em.createQuery(jpql, TmdbGenreDto.class).getResultList();
            return genres;
        }
    }

    /**
     * Get all genre ids from the database
     * @return List of genre ids
     */
    public List<Integer> getAllGenreIds() {
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "SELECT g.id FROM Genre g ORDER BY g.name";
            List<Integer> genreIds = em.createQuery(jpql, Integer.class).getResultList();
            return genreIds;
        }
    }

    /**
     * Delete genres in the database that are orphaned.
     * These are genres that are not associated with any movie after an update from TMDB.
     * @return Number of deleted genres
     */
    public int deleteOrphanedGenres() {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            String jpql = "DELETE FROM Genre g WHERE g.id NOT IN (SELECT mg.genre.id FROM MovieGenre mg)";
            int deletedCount = em.createQuery(jpql).executeUpdate();
            em.getTransaction().commit();
            return deletedCount;
        }
    }

}