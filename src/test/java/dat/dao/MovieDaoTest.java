package dat.dao;

import dat.config.HibernateConfig;
import dat.dto.FrontendMovieDto;
import dat.entities.Genre;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MovieDaoTest {

    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactoryForTest();
    private static final MovieDao movieDao = MovieDao.getInstance(emf);
    private static final GenreDao genreDao = GenreDao.getInstance(emf);


    @BeforeEach
    void setUp() {

        Populator.populate(movieDao, genreDao, emf);

    }

    @Test
    void searchMoviesOpen() {

        List<FrontendMovieDto> movies = movieDao.searchMoviesOpen("nat");
        assertEquals(1, movies.size());
        assertEquals("Midt Om Natten", movies.get(0).title());

        movies = movieDao.searchMoviesOpen("a");
        assertEquals(2, movies.size());
        assertEquals("Baby Doom", movies.get(0).title());
        assertEquals("Midt Om Natten", movies.get(1).title());

        // Negative test: Try to search for movie which does not exist
        movies = movieDao.searchMoviesOpen("does not exist in test db");
        assertEquals(0, movies.size());
    }

}
