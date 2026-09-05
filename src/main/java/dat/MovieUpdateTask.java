package dat;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dat.dao.CollectionDao;
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

    // Initialize DAO singletons
    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    private static final CollectionDao collectionDao = CollectionDao.getInstance(emf);
    private static final GenreDao genreDao = GenreDao.getInstance(emf);
    private static final MovieDao movieDao = MovieDao.getInstance(emf);
    private static final PersonDao personDao = PersonDao.getInstance(emf);

    private static final Logger logger = LoggerFactory.getLogger(MovieUpdateTask.class);

    /**
     * This main method runs the MovieUpdateTask.
     * On the Ubuntu server, a cron job is set to run this main method at the beginning of each week:
     * m  h  dom mon dow  command
     * 39 1  *   *   MON  docker exec MovieAPI java -cp /app.jar dat.MovieUpdateTask
     */
    public static void main(String[] args) {
        try {
            new MovieUpdateTask().run();
        } finally {
            if (emf.isOpen()) {
                emf.close();
            }
        }
    }

    @Override
    public void run() {

        logger.info("Started MovieUpdateTask");
        long startTime = System.currentTimeMillis();

//        // Get all movieIds currently in database
//        Set<Integer> movieIds = movieDao.getAllMovieIds();
//
//        // Add new movies from TMDB
//        movieIds.addAll(TmdbService.discoverMovieIds());

        Set<Integer> movieIds = new HashSet<>();
        movieIds.addAll(movieDao.getTrendingMovieIds());
        movieIds.addAll(TmdbService.discoverTrendingMovieIds());
        for (int genreId : genreDao.getAllGenreIds()) {
            movieIds.addAll(movieDao.getTrendingMovieIdsByGenreId(genreId));
            movieIds.addAll(TmdbService.discoverTrendingMovieIdsByGenreId(genreId));
        }

        logger.info("Requesting details on {} movies from TMDB", movieIds.size());
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

            // After update of Movie, orphaned MovieGenres, Credits and Ratings are deleted
            // But orphaned Genres, Persons and Collections are not deleted, and are therefore deleted in the code below.
        }


        // Delete unwanted movies
        try {
            int deletedCount = movieDao.deleteUnwantedMovies();
            logger.info("Deleted " + deletedCount + " unwanted movies");
        } catch (Exception e) {
            logger.error("Failed to delete unwanted movies", e);
        }

        // Delete orphaned genres
        try {
            int deletedCount = genreDao.deleteOrphanedGenres();
            logger.info("Deleted " + deletedCount + " orphaned genres");
        } catch (Exception e) {
            logger.error("Failed to delete orphaned genres", e);
        }

        // Delete orphaned persons
        try {
            int deletedCount = personDao.deleteOrphanedPersons();
            logger.info("Deleted " + deletedCount + " orphaned persons");
        } catch (Exception e) {
            logger.error("Failed to delete orphaned persons", e);
        }

        // Delete orphaned collections
        try {
            int deletedCount = collectionDao.deleteOrphanedCollections();
            logger.info("Deleted " + deletedCount + " orphaned collections");
        } catch (Exception e) {
            logger.error("Failed to delete orphaned collections", e);
        }

        logger.info("Finished MovieUpdateTask, seconds it took: " + (System.currentTimeMillis() - startTime) / 1000);

    }

}
