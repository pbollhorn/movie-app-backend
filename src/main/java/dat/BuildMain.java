package dat;

import java.util.*;

import jakarta.persistence.EntityManagerFactory;

import dat.config.HibernateConfig;
import dat.dao.GenreDao;
import dat.dao.MovieDao;
import dat.dao.PersonDao;
import dat.dto.GenreDto;
import dat.dto.TmdbCreditDto;
import dat.dto.TmdbMovieDto;
import dat.entities.Genre;
import dat.entities.Movie;
import dat.entities.Person;
import dat.services.TmdbService;

public class BuildMain {

    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory("create");
    private static final GenreDao genreDao = GenreDao.getInstance(emf);
    private static final MovieDao movieDao = MovieDao.getInstance(emf);
    private static final PersonDao personDao = PersonDao.getInstance(emf);

    // TMDB says that approx. 50 requests per second are allowed: https://developer.themoviedb.org/docs/rate-limiting
    // To be on the safe side, this program limits to 40 requests per second
    private static final int MAX_REQUESTS_PER_SECOND = 40;
    private static final long DELAY_MILLISECONDS = 1000 / MAX_REQUESTS_PER_SECOND;

    public static void main(String[] args) {

        long startTime = System.currentTimeMillis();

        // Get all genres from TMDB and persist them in database
        Map<Integer, Genre> genreMap = new HashMap<>();
        for (GenreDto g : TmdbService.getGenres()) {
            Genre genre = genreDao.create(new Genre(g.id(), g.name()));
            genreMap.put(g.id(), genre);
            System.out.println("Got and persisted genre: " + genre);
        }

        Set<Integer> movieIds = TmdbService.getMovieIds(DELAY_MILLISECONDS);

        for (int movieId : movieIds) {

            TmdbMovieDto movieDto = TmdbService.getMovieDetails(movieId);

            List<Genre> genresForThisMovie = movieDto.genres().stream().map(g -> genreMap.get(g.id())).toList();
            Movie movie = new Movie(movieDto, genresForThisMovie);

            System.out.println(movieDto.collection());

            int rankInMovie = 0;
            for (TmdbCreditDto c : movieDto.credits().cast()) {
                // This creates the cast member as a person in the database
                // (or overwrites with same data if already in database)
                Person person = personDao.update(new Person(c));
                movie.addCredit(c.creditId(), person, "Actor", "Acting", c.character(), rankInMovie);
                rankInMovie++;
            }
            for (TmdbCreditDto c : movieDto.credits().crew()) {
                // This creates the crew member as a person in the database
                // (or overwrites with same data if already in database)
                Person person = personDao.update(new Person(c));
                movie.addCredit(c.creditId(), person, c.job(), c.department(), null, rankInMovie);
                rankInMovie++;
            }

            movie = movieDao.create(movie);

            System.out.println("Got and persisted movie: " + movie);
        }

        System.out.println("Milliseconds it took: " + (System.currentTimeMillis() - startTime));

        emf.close();

    }

}