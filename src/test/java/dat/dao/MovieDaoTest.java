package dat.dao;

import java.util.List;
import java.util.Set;

import dat.dto.MovieDetailsDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import dat.config.HibernateConfig;
import dat.dto.MovieOverviewDto;

public class MovieDaoTest {

    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactoryForTest();
    private static final MovieDao movieDao = MovieDao.getInstance();
    private static final GenreDao genreDao = GenreDao.getInstance(emf);

    // Max number of movies when searching and getting recommendations
    private static final int MOVIE_LIMIT = 50;

    @BeforeEach
    void setUp() {

        Populator.populate(movieDao, genreDao, emf);

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

    @Test
    void getAllMovieIds() {

        // Positive tests
        Set<Integer> movieIds = movieDao.getAllMovieIds();
        assertEquals(2, movieIds.size());
        assertTrue(movieIds.contains(329));

        // Negative test
        assertFalse(movieIds.contains(333));
    }


    @Test
    void getMovieDetails() {

        // Positive test
        MovieDetailsDto movieDetailsDto = movieDao.getMovieDetails(329);
        assertEquals(329, movieDetailsDto.id());
        assertEquals("Jurassic Park", movieDetailsDto.title());
        assertEquals("Jurassic Park", movieDetailsDto.originalTitle());

        // Another positive test
        movieDetailsDto = movieDao.getMovieDetails(33416);
        assertEquals(33416, movieDetailsDto.id());
        assertEquals("In the Middle of the Night", movieDetailsDto.title());
        assertEquals("Midt om natten", movieDetailsDto.originalTitle());


    }


//    @Test
//    void searchMoviesOpen() {
//
//        List<MovieOverviewDto> movies = movieDao.searchMovies("nat",0, MOVIE_LIMIT);
//        assertEquals(1, movies.size());
//        assertEquals("Midt Om Natten", movies.get(0).title());
//
//        movies = movieDao.searchMovies("a",0, MOVIE_LIMIT);
//        assertEquals(2, movies.size());
//        assertEquals("Baby Doom", movies.get(0).title());
//        assertEquals("Midt Om Natten", movies.get(1).title());
//
//        // Negative test: Try to search for movie which does not exist
//        movies = movieDao.searchMovies("does not exist in test db",0, MOVIE_LIMIT);
//        assertEquals(0, movies.size());
//    }


}
