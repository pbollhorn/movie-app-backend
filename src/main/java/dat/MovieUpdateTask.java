package dat;

import java.time.LocalDate;
import java.util.HashSet;
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

public class MovieUpdateTask {

    private static final int YEAR_OF_FIRST_MOVIE = 1874;

    // Initialize DAO singletons
    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    private static final CollectionDao collectionDao = CollectionDao.getInstance(emf);
    private static final GenreDao genreDao = GenreDao.getInstance(emf);
    private static final MovieDao movieDao = MovieDao.getInstance(emf);
    private static final PersonDao personDao = PersonDao.getInstance(emf);

    private static final Logger logger = LoggerFactory.getLogger(MovieUpdateTask.class);


    /**
     * Main method for running the MovieUpdateTask.
     *
     * Behavior depends on the provided arguments:
     * - No arguments: Calculates days from YEAR_OF_FIRST_MOVIE to today for a complete update.
     * - One argument: Parses args[0] as an integer specifying the exact number of days to look back.
     *
     * On the Ubuntu server, a cron job is set to run MovieUpdateTask looking back 7 days, at the beginning of each week:
     * m  h  dom mon dow  command
     * 39 1  *   *   MON  docker exec MovieAPI java -cp /app.jar dat.MovieUpdateTask 7
     * 
     * @param args command-line arguments (optional: a single integer for days to look back)
     */
    public static void main(String[] args) {

        int daysToLookBack;

        if (args.length == 0) {
            LocalDate today = LocalDate.now();
            LocalDate startDate = LocalDate.of(YEAR_OF_FIRST_MOVIE, 1, 1);
            daysToLookBack = (int) (today.toEpochDay() - startDate.toEpochDay());
        } else {
            daysToLookBack = Integer.parseInt(args[0]);
        }

        try {
            run(daysToLookBack);
        } finally {
            if (emf.isOpen()) {
                emf.close();
            }
        }
    }


    static private void run(int daysToLookBack) {

        logger.info("Started MovieUpdateTask with daysToLookBack={}", daysToLookBack);
        long startTime = System.currentTimeMillis();

        Set<Integer> movieIds = new HashSet<>();

        // Get all stale movieIds currently in database
        movieIds.addAll(movieDao.getStaleMovieIds());

        // Add new movies from TMDB
        movieIds.addAll(TmdbService.discoverMovieIds(daysToLookBack));

        // Add the trending movies from database and from TMDB
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
                logger.warn("Caught ApiException: code={} message={}", e.getCode(), e.getMessage());
                if (e.getCode() == 429) {
                    logger.error("Stopping MovieUpdateTask immediately due to code 429 from TMDB");
                    return;
                }
                if (e.getCode() == 404) {
                    logger.info("Deleting movie with id={} due to code 404 from TMDB", movieId);
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
        }
        logger.info("Finished updating movies with fresh data from TMDB", movieIds.size());

        // Delete unwanted movies
        try {
            int deletedCount = movieDao.deleteUnwantedMovies();
            logger.info("Deleted {} unwanted movies", deletedCount);
        } catch (Exception e) {
            logger.error("Failed to delete unwanted movies", e);
        }

        // After update of Movie, orphaned MovieGenres, Credits and Ratings are deleted
        // But orphaned Genres, Persons and Collections are not deleted, and are therefore deleted in the code below.

        // Delete orphaned genres
        try {
            int deletedCount = genreDao.deleteOrphanedGenres();
            logger.info("Deleted {} orphaned genres", deletedCount);
        } catch (Exception e) {
            logger.error("Failed to delete orphaned genres", e);
        }

        // Delete orphaned persons
        try {
            int deletedCount = personDao.deleteOrphanedPersons();
            logger.info("Deleted {} orphaned persons", deletedCount);
        } catch (Exception e) {
            logger.error("Failed to delete orphaned persons", e);
        }

        // Delete orphaned collections
        try {
            int deletedCount = collectionDao.deleteOrphanedCollections();
            logger.info("Deleted {} orphaned collections", deletedCount);
        } catch (Exception e) {
            logger.error("Failed to delete orphaned collections", e);
        }

        logger.info("Finished MovieUpdateTask in {} seconds", (System.currentTimeMillis() - startTime) / 1000);

    }

}
