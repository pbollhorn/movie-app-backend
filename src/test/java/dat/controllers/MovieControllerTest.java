package dat.controllers;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import dat.config.ApplicationConfig;
import dat.routes.Routes;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class MovieControllerTest {

    @BeforeAll
    static void setupAll() {
        ApplicationConfig
                .getInstance()
                .initiateServer()
                .setRoute(Routes.getRoutes())
                .handleException()
                .setApiExceptionHandling()
                .checkSecurityRoles()
                .startServer(7777);

        RestAssured.baseURI = "http://localhost:7777/api";
    }


//    @Test
//    void updateOrCreateMovieRating() {
//
//
//    }


}
