package dat.controllers;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import dat.dto.MovieOverviewDto;
import dat.dto.NameMovieListDto;
import io.javalin.http.Context;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dat.dao.MovieDao;
import dat.dto.MovieDetailsDto;
import dat.MovieUpdateTask;

public class MovieController {

    // Max number of movies when searching and getting recommendations
    private static final int MOVIE_LIMIT = 25;

    private final MovieDao movieDao;
    private static final Logger logger = LoggerFactory.getLogger(MovieController.class);
    private final SecurityController securityController;

    public MovieController(EntityManagerFactory emf, SecurityController securityController) {
        movieDao = MovieDao.getInstance(emf);
        this.securityController = securityController;
    }

    public void searchMovies(Context ctx) {

        Integer accountId = securityController.getAccountIdFromToken(ctx);
        String text = ctx.queryParam("text");

        List<MovieOverviewDto> movies = movieDao.searchMovies(text, accountId, MOVIE_LIMIT);
        ctx.json(movies);
    }

    public void getMoviesWithPerson(Context ctx) {

        Integer accountId = securityController.getAccountIdFromToken(ctx);
        int personId = Integer.parseInt(ctx.pathParam("id"));

        NameMovieListDto person = movieDao.getMoviesWithPerson(personId, accountId);
        ctx.json(person);
    }

    public void getMoviesInCollection(Context ctx) {

        Integer accountId = securityController.getAccountIdFromToken(ctx);
        int collectionId = Integer.parseInt(ctx.pathParam("id"));

        NameMovieListDto collection = movieDao.getMoviesInCollection(collectionId, accountId);
        ctx.json(collection);
    }

    public void getAllMoviesWithRating(Context ctx) {
        int accountId = securityController.getAccountIdFromToken(ctx);
        List<MovieOverviewDto> movies = movieDao.getAllMoviesWithRating(accountId);
        ctx.json(movies);
    }

    public void getMovieDetails(Context ctx) {
        int movieId = Integer.parseInt(ctx.pathParam("id"));
        MovieDetailsDto movieDetails = movieDao.getMovieDetails(movieId);
        ctx.json(movieDetails);
    }

    public void updateOrCreateMovieRating(Context ctx) {
        int accountId = securityController.getAccountIdFromToken(ctx);
        int movieId = Integer.parseInt(ctx.pathParam("id"));
        Boolean rating = ctx.bodyAsClass(JsonNode.class).get("rating").asBoolean();
        movieDao.updateOrCreateMovieRating(accountId, movieId, rating);
    }

    public void deleteMovieRating(Context ctx) {
        int accountId = securityController.getAccountIdFromToken(ctx);
        int movieId = Integer.parseInt(ctx.pathParam("id"));
        movieDao.deleteMovieRating(accountId, movieId);
    }

    public void getMovieRecommendations(Context ctx) {
        int accountId = securityController.getAccountIdFromToken(ctx);
        List<MovieOverviewDto> movies = movieDao.getMovieRecommendations(accountId, MOVIE_LIMIT);
        ctx.json(movies);
    }

    public void updateMovies(Context ctx) {
        Thread thread = new Thread(new MovieUpdateTask());
        thread.start();
        ctx.json("Started MovieUpdateTask");
    }

}