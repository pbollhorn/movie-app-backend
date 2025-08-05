package dat;

import dat.utils.PropertyReader;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dat.config.ApplicationConfig;
import dat.config.HibernateConfig;
import dat.controllers.MovieController;
import dat.controllers.SecurityController;
import dat.routes.Routes;

public class Main {

    public static void main(String[] args) {

        Logger logger = LoggerFactory.getLogger(Main.class);
        logger.info("""
                \n\n
                ----------------------------------------------------------
                (\\___/)                                            (\\___/)
                (='.'=)               SERVER STARTED               (='.'=)
                (")_(")                                            (")_(")
                ----------------------------------------------------------\n""");

        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory(); // change back to update
        SecurityController securityController = new SecurityController(emf);
        MovieController movieController = new MovieController(emf, securityController);

        Routes routes = new Routes(movieController, securityController);

        ApplicationConfig
                .getInstance()
                .initiateServer()
                .setRoute(routes.getRoutes())
                .handleException()
                .setApiExceptionHandling()
                .checkSecurityRoles()
                .startServer(7070);

        // Create pg_trgm extension and indexes in database, if they do not already exist
        String DB_NAME = PropertyReader.getPropertyValue("DB_NAME");
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.createNativeQuery("CREATE EXTENSION IF NOT EXISTS pg_trgm").executeUpdate();
            em.createNativeQuery("ALTER DATABASE " + DB_NAME + " SET pg_trgm.similarity_threshold=0.8").executeUpdate();
            em.createNativeQuery("ALTER DATABASE " + DB_NAME + " SET pg_trgm.word_similarity_threshold=0.15").executeUpdate();
            em.createNativeQuery("ALTER DATABASE " + DB_NAME + " SET pg_trgm.strict_word_similarity_threshold=0.15").executeUpdate();
            em.createNativeQuery("CREATE INDEX IF NOT EXISTS idx_movie_title ON movie (LOWER(title))").executeUpdate();
            em.createNativeQuery("CREATE INDEX IF NOT EXISTS idx_movie_title_trgm ON movie USING gin (title gin_trgm_ops)").executeUpdate();
            em.createNativeQuery("CREATE INDEX IF NOT EXISTS idx_credit_movie_id ON credit(movie_id)").executeUpdate();
            em.createNativeQuery("CREATE INDEX IF NOT EXISTS idx_credit_person_id ON credit(person_id)").executeUpdate();
            em.getTransaction().commit();
        }


    }

}