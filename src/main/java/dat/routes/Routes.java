package dat.routes;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

import dat.controllers.MovieController;
import dat.controllers.GenreController;
import dat.controllers.SecurityController;
import dat.enums.Role;

public class Routes {

    private static final ObjectMapper jsonMapper = new ObjectMapper();

    public static EndpointGroup getRoutes() {
        return () -> {
            path("movies", movieRoutes());
            path("genres", genreRoutes());
            path("auth", authRoutes());
            path("roles-test", rolesTestRoutes());
        };
    }

    private static EndpointGroup movieRoutes() {
        return () -> {
            get("/search", MovieController::searchMovies, Role.ANYONE);
            get("/popular",  MovieController::getPopularMovies, Role.ANYONE);
            get("/top100",  MovieController::getTop100Movies, Role.ANYONE);
            get("/ratings", MovieController::getAllMoviesWithRating, Role.USER);
            get("/recommendations", MovieController::getMovieRecommendations, Role.USER);
            post("/update", MovieController::updateMovies, Role.ADMIN);
            get("/person/{id}", MovieController::getMoviesWithPerson, Role.ANYONE);
            get("/collection/{id}", MovieController::getMoviesInCollection, Role.ANYONE);
            put("/{id}/ratings", MovieController::updateOrCreateMovieRating, Role.USER);
            delete("/{id}/ratings", MovieController::deleteMovieRating, Role.USER);
            get("/{id}", MovieController::getMovieDetails, Role.ANYONE);
        };
    }

    private static EndpointGroup genreRoutes() {
        return () -> {
            get("/", GenreController::getAllGenres, Role.ANYONE);
        };
    }


    private static EndpointGroup authRoutes() {
        return () -> {
            post("/register", SecurityController::register, Role.ANYONE);
            post("/login", SecurityController::login, Role.ANYONE);
            get("/verify", SecurityController::verify, Role.ANYONE);
            get("/tokenlifespan", SecurityController::timeToLive, Role.ANYONE);
            get("/healthcheck", SecurityController::healthCheck, Role.ANYONE);
        };
    }

    private static EndpointGroup rolesTestRoutes() {
        return () -> {
            get("/anyone", ctx -> ctx.json(jsonMapper.createObjectNode().put("msg", "Hello from open to ANYONE")), Role.ANYONE);
            get("/user", ctx -> ctx.json(jsonMapper.createObjectNode().put("msg", "Hello from USER protected")), Role.USER);
            get("/admin", ctx -> ctx.json(jsonMapper.createObjectNode().put("msg", "Hello from ADMIN protected")), Role.ADMIN);
        };
    }

}
