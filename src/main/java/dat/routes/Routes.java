package dat.routes;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

import dat.controllers.MovieController;
import dat.controllers.SecurityController;
import dat.enums.Roles;

public class Routes {
    private final MovieController movieController;
    private final SecurityController securityController;
    private final ObjectMapper jsonMapper = new ObjectMapper();

    public Routes(MovieController movieController, SecurityController securityController) {
        this.movieController = movieController;
        this.securityController = securityController;
    }

    public EndpointGroup getRoutes() {
        return () -> {
            path("movies", movieRoutes());
            path("auth", authRoutes());
            path("roles-test", rolesTestRoutes());
        };
    }

    private EndpointGroup movieRoutes() {
        return () -> {
            get("/search-open", movieController::searchMoviesOpen, Roles.ANYONE);
            get("/search", movieController::searchMovies, Roles.USER);
            get("/recommendations", movieController::getMovieRecommendations, Roles.USER);
            get("/", movieController::getAllMoviesWithRating, Roles.USER);
            post("/update", movieController::updateMovies, Roles.USER);  // TODO: Turn into ADMIN endpoint
            get("/person/{id}", movieController::getMoviesWithPerson, Roles.ANYONE); // TODO: Should also be able to supply ratings
            get("/collection/{id}", movieController::getMoviesInCollection, Roles.ANYONE); // TODO: Should also be able to supply ratings'
            get("/{id}", movieController::getMovieDetails, Roles.ANYONE);
            put("/{id}", movieController::updateOrCreateMovieRating, Roles.USER);
            delete("/{id}", movieController::deleteMovieRating, Roles.USER);
        };
    }


    private EndpointGroup authRoutes() {
        return () -> {
            post("/register", securityController::register, Roles.ANYONE);
            post("/login", securityController::login, Roles.ANYONE);
            get("/verify", securityController::verify, Roles.ANYONE);
            get("/tokenlifespan", securityController::timeToLive, Roles.ANYONE);
            get("/healthcheck", securityController::healthCheck, Roles.ANYONE);
        };
    }

    private EndpointGroup rolesTestRoutes() {
        return () -> {
            get("/anyone", ctx -> ctx.json(jsonMapper.createObjectNode().put("msg", "Hello from open to ANYONE")), Roles.ANYONE);
            get("/user", ctx -> ctx.json(jsonMapper.createObjectNode().put("msg", "Hello from USER protected")), Roles.USER);
            get("/admin", ctx -> ctx.json(jsonMapper.createObjectNode().put("msg", "Hello from ADMIN protected")), Roles.ADMIN);
        };
    }

}
