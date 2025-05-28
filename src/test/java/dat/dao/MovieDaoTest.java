package dat.dao;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dat.config.HibernateConfig;
import dat.dto.FrontendMovieOverviewDto;

public class MovieDaoTest {

    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactoryForTest();
    private static final MovieDao movieDao = MovieDao.getInstance(emf);
    private static final GenreDao genreDao = GenreDao.getInstance(emf);

    // Max number of movies when searching and getting recommendations
    private static final int MOVIE_LIMIT = 25;

    @BeforeEach
    void setUp() {

        Populator.populate(movieDao, genreDao, emf);

    }

    @Test
    void searchMoviesOpen() {

        List<FrontendMovieOverviewDto> movies = movieDao.searchMoviesOpen("nat", MOVIE_LIMIT);
        assertEquals(1, movies.size());
        assertEquals("Midt Om Natten", movies.get(0).title());

        movies = movieDao.searchMoviesOpen("a", MOVIE_LIMIT);
        assertEquals(2, movies.size());
        assertEquals("Baby Doom", movies.get(0).title());
        assertEquals("Midt Om Natten", movies.get(1).title());

        // Negative test: Try to search for movie which does not exist
        movies = movieDao.searchMoviesOpen("does not exist in test db", MOVIE_LIMIT);
        assertEquals(0, movies.size());
    }

    @AfterEach
    void tearDown() {

        try (EntityManager em = emf.createEntityManager()) {
            // Delete everything from tables and reset id's to start with 1
            em.getTransaction().begin();
            em.createNativeQuery("DELETE FROM movie_genre").executeUpdate();
            em.createNativeQuery("DELETE FROM movie").executeUpdate();
            em.createNativeQuery("DELETE FROM genre").executeUpdate();
//            em.createNativeQuery("ALTER SEQUENCE movie_id_seq RESTART WITH 1").executeUpdate();
//            em.createNativeQuery("ALTER SEQUENCE hotel_id_seq RESTART WITH 1").executeUpdate();
            em.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
