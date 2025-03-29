package dat.dao;

import dat.config.HibernateConfig;
import dat.entities.Genre;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

public class MovieDaoTest {

    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactoryForTest();
    private static final MovieDao movieDao = MovieDao.getInstance(emf);
    private static final GenreDao genreDao = GenreDao.getInstance(emf);


    @BeforeEach
    void setUp() {

        Populator.populate(movieDao, genreDao);

    }

    @Test
    void test() {

    }

}
