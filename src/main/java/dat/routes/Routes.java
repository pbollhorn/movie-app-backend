package dat.routes;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

import dat.controllers.MovieController;
import dat.controllers.SecurityController;
import dat.enums.Roles;

public class Routes {

    private static final ObjectMapper jsonMapper = new ObjectMapper();

    public static EndpointGroup getRoutes() {
        return () -> {
            path("movies", movieRoutes());
            path("auth", authRoutes());
            path("roles-test", rolesTestRoutes());
        };
    }

    private static EndpointGroup movieRoutes() {
        return () -> {
            get("/search", MovieController::searchMovies, Roles.ANYONE);
            get("/recommendations", MovieController::getMovieRecommendations, Roles.USER);
            get("/", MovieController::getAllMoviesWithRating, Roles.USER);
            post("/update", MovieController::updateMovies, Roles.USER);  // TODO: Turn into ADMIN endpoint
            get("/person/{id}", MovieController::getMoviesWithPerson, Roles.ANYONE);
            get("/collection/{id}", MovieController::getMoviesInCollection, Roles.ANYONE);
            get("/{id}", MovieController::getMovieDetails, Roles.ANYONE);
            put("/{id}", MovieController::updateOrCreateMovieRating, Roles.USER);
            delete("/{id}", MovieController::deleteMovieRating, Roles.USER);
        };
    }


    private static EndpointGroup authRoutes() {
        return () -> {
            post("/register", SecurityController::register, Roles.ANYONE);
            post("/login", SecurityController::login, Roles.ANYONE);
            get("/verify", SecurityController::verify, Roles.ANYONE);
            get("/tokenlifespan", SecurityController::timeToLive, Roles.ANYONE);
            get("/healthcheck", SecurityController::healthCheck, Roles.ANYONE);
        };
    }

    private static EndpointGroup rolesTestRoutes() {
        return () -> {
            get("/anyone", ctx -> ctx.json(jsonMapper.createObjectNode().put("msg", "Hello from open to ANYONE")), Roles.ANYONE);
            get("/user", ctx -> ctx.json(jsonMapper.createObjectNode().put("msg", "Hello from USER protected")), Roles.USER);
            get("/admin", ctx -> ctx.json(jsonMapper.createObjectNode().put("msg", "Hello from ADMIN protected")), Roles.ADMIN);
        };
    }

}
