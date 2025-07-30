package dat;

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

        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory("update"); // change back to update
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
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.createNativeQuery("CREATE EXTENSION IF NOT EXISTS pg_trgm").executeUpdate();
            em.createNativeQuery("CREATE INDEX IF NOT EXISTS idx_movie_title_trgm ON movie USING gin (title gin_trgm_ops)").executeUpdate();
            em.getTransaction().commit();
        }


    }

}