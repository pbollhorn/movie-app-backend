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
            path("protected", protectedRoutes());
        };
    }

    private EndpointGroup movieRoutes() {
        return () -> {
            get("/search-open", movieController::searchMoviesOpen, Roles.ANYONE);
            get("/search", movieController::searchMovies, Roles.USER);
            get("/recommendations", movieController::getMovieRecommendations, Roles.USER);
            get("/", movieController::getAllMoviesWithRating, Roles.USER);
            get("/{id}", movieController::getMovieDetails, Roles.ANYONE);
            put("/{id}", movieController::updateOrCreateMovieRating, Roles.USER);
            delete("/{id}", movieController::deleteMovieRating, Roles.USER);
        };
    }


    private EndpointGroup authRoutes() {
        return () -> {
            get("/test", ctx -> ctx.json(jsonMapper.createObjectNode().put("msg", "Hello from Open")), Roles.ANYONE);
            get("/healthcheck", securityController::healthCheck, Roles.ANYONE);
            post("/login", securityController::login, Roles.ANYONE);
            post("/register", securityController::register, Roles.ANYONE);
            get("/verify", securityController::verify, Roles.ANYONE);
            get("/tokenlifespan", securityController::timeToLive, Roles.ANYONE);
        };
    }

    private EndpointGroup protectedRoutes() {
        return () -> {
            get("/user_demo", (ctx) -> ctx.json(jsonMapper.createObjectNode().put("msg", "Hello from USER Protected")), Roles.USER);
            get("/admin_demo", (ctx) -> ctx.json(jsonMapper.createObjectNode().put("msg", "Hello from ADMIN Protected")), Roles.ADMIN);
        };
    }

}
