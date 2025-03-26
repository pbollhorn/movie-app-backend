package dat;

import dat.config.ApplicationConfig;
import dat.config.HibernateConfig;
import dat.controllers.HotelController;
import dat.controllers.MovieController;
import dat.controllers.SecurityController;
import dat.routes.Routes;
import jakarta.persistence.EntityManagerFactory;


public class Main {
    private final static EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory("update");


    public static void main(String[] args) {
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
    }
}