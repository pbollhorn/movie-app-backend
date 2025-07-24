package dat;

import dat.dto.MovieOverviewDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
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

        try (EntityManager em = emf.createEntityManager()) {
            String sql = "CREATE EXTENSION IF NOT EXISTS pg_trgm";
            em.getTransaction().begin();
            Query query = em.createNativeQuery(sql);
            query.executeUpdate();
            em.getTransaction().commit();
        }

    }

}