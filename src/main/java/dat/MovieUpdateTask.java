package dat;

import java.util.Set;

import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dat.config.HibernateConfig;
import dat.dao.GenreDao;
import dat.dao.MovieDao;
import dat.dao.PersonDao;
import dat.dto.TmdbCreditDto;
import dat.dto.TmdbMovieDto;
import dat.dto.TmdbGenreDto;
import dat.entities.Genre;
import dat.entities.Movie;
import dat.entities.Person;
import dat.exceptions.ApiException;
import dat.services.TmdbService;

public class MovieUpdateTask implements Runnable {

    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    private static final GenreDao genreDao = GenreDao.getInstance(emf);
    private static final MovieDao movieDao = MovieDao.getInstance(emf);
    private static final PersonDao personDao = PersonDao.getInstance(emf);

    private static final Logger logger = LoggerFactory.getLogger(MovieUpdateTask.class);

    @Override
    public void run() {

        logger.info("Started MovieUpdateTask");
        long startTime = System.currentTimeMillis();

        // Get all movieIds currently in database
        Set<Integer> movieIds = movieDao.getAllMovieIds();

//        // Add new movies from TMDB
//        movieIds.addAll(TmdbService.discoverMovieIds());

        movieIds = Set.of(85);

        for (int movieId : movieIds) {

            TmdbMovieDto movieDto;
            try {
                movieDto = TmdbService.getMovieDetails(movieId);
            } catch (ApiException e) {
                logger.info("Caught ApiException: " + e.getCode() + " " + e.getMessage());
                if (e.getCode() == 429) {
                    logger.error("Stopping MovieUpdateTask immediately due to code 429 from TMDB");
                    return;
                }
                if (e.getCode() == 404) {
                    logger.info("Deleting movie with id=" + movieId + " due to code 404 from TMDB");
                    movieDao.deleteById(movieId);
                }
                continue;
            }

            Movie movie = new Movie(movieDto);

            // It may seem wasteful to overwrite genres for each movie, but this
            // allows for TMDB genres to change in the middle of an update without affecting this code
            // e.g. if TMDB ads a new genre in the middle of one of my updates
            int rankInMovie = 0;
            for (TmdbGenreDto g : movieDto.genres()) {
                Genre genre = genreDao.update(g);
                movie.addGenre(genre, rankInMovie);
                rankInMovie++;
            }

            rankInMovie = 0;
            for (TmdbCreditDto c : movieDto.credits().cast()) {
                // This creates the cast member as a person in the database
                // (or overwrites if already in database)
                Person person = personDao.update(c);
                movie.addCredit(c.id(), person, "Cast", "Cast Member", c.character(), rankInMovie);
                rankInMovie++;
            }
            for (TmdbCreditDto c : movieDto.credits().crew()) {
                // This creates the crew member as a person in the database
                // (or overwrites if already in database)
                Person person = personDao.update(c);
                movie.addCredit(c.id(), person, c.department(), c.job(), null, rankInMovie);
                rankInMovie++;
            }

            movie.setLastTmdbSyncToNow();
            movieDao.update(movie);

            // TODO: After update, unused MovieGenres, Credits and Ratings are removed, but there may be roque Genres and Persons
        }


        // Delete unwanted movies
        movieIds = movieDao.getUnwantedMovieIds();
        for (int movieId : movieIds) {
            logger.info("Deleting movie with id=" + movieId + " because it is no longer wanted");
            movieDao.deleteById(movieId);
        }


        logger.info("Finished MovieUpdateTask, milliseconds it took: " + (System.currentTimeMillis() - startTime));

    }

}
