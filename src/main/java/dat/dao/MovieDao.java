package dat.dao;

import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDate;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import dat.entities.Account;
import dat.entities.Rating;
import dat.entities.Movie;
import dat.dto.*;

public class MovieDao {

    private static final int MAX_DAYS_POPULAR = 365;
    private static final int MIN_VOTE_COUNT = 5000;
    private static final double MEAN_TMDB_SCORE = 6.031;

    private static MovieDao instance;
    private static EntityManagerFactory emf;

    private MovieDao(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public static MovieDao getInstance(EntityManagerFactory emf) {
        if (instance == null) {
            instance = new MovieDao(emf);
        }
        return instance;
    }

    // Update movie (or create it if it does not already exist)
    public Movie update(Movie movie) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            movie = em.merge(movie);
            em.getTransaction().commit();
            return movie;
        } catch (RuntimeException e) {
            e.printStackTrace();
            return null;
        }
    }


    public Set<Integer> getAllMovieIds() {

        try (EntityManager em = emf.createEntityManager()) {

            String jpql = "SELECT m.id FROM Movie m";
            return new HashSet<>(em.createQuery(jpql, Integer.class).getResultList());

        }

    }


    public List<MovieOverviewDto> searchMovies(String title, Integer accountId, int limit) {

        try (EntityManager em = emf.createEntityManager()) {

            String sql = """
                    SELECT id FROM movie
                    WHERE TRIM(LOWER(:title)) = LOWER(title)
                    ORDER BY votecount DESC""";
            List<Integer> movieIds = em.createNativeQuery(sql, Integer.class)
                    .setParameter("title", title)
                    .setMaxResults(limit)
                    .getResultList();

            sql = """
                    SELECT id FROM movie
                    WHERE :title <<% title AND id NOT IN :movieIds
                    ORDER BY STRICT_WORD_SIMILARITY(:title, title) DESC, votecount DESC""";
            movieIds.addAll(em.createNativeQuery(sql, Integer.class)
                    .setParameter("title", title)
                    .setParameter("movieIds", movieIds.isEmpty() ? List.of(0) : movieIds)
                    .setMaxResults(limit - movieIds.size())
                    .getResultList());

            String jpql = """
                    SELECT NEW dat.dto.MovieOverviewDto(m, r.rating)
                    FROM Movie m
                    LEFT JOIN Rating r ON r.movie.id = m.id AND r.account.id = :accountId
                    WHERE m.id IN :movieIds""";
            List<MovieOverviewDto> movieDtos = em.createQuery(jpql, MovieOverviewDto.class)
                    .setParameter("accountId", accountId)
                    .setParameter("movieIds", movieIds)
                    .getResultList();

            // Map from ID to DTO for lookup
            Map<Integer, MovieOverviewDto> dtoMap = movieDtos.stream()
                    .collect(Collectors.toMap(dto -> dto.id(), dto -> dto));

            // Rebuild list in correct order
            return movieIds.stream()
                    .map(dtoMap::get)
                    .collect(Collectors.toList());

        }

    }


    public List<MovieOverviewDto> getPopularMovies(Integer accountId) {

        LocalDate cutoffDate = LocalDate.now().minusDays(MAX_DAYS_POPULAR);

        try (EntityManager em = emf.createEntityManager()) {

            String jpql = """
                    SELECT NEW dat.dto.MovieOverviewDto(m, r.rating)
                    FROM Movie m
                    LEFT JOIN Rating r ON r.movie.id = m.id AND r.account.id = :accountId
                    WHERE m.releaseDate >= :cutoffDate
                    ORDER BY m.popularity * ( 1.0 - 1.0 * DATEDIFF(DAY, m.releaseDate, CURRENT_DATE)/:maxDays ) DESC NULLS last""";
            List<MovieOverviewDto> movies = em.createQuery(jpql, MovieOverviewDto.class)
                    .setParameter("accountId", accountId)
                    .setParameter("cutoffDate", cutoffDate)
                    .setParameter("maxDays", MAX_DAYS_POPULAR)
                    .setMaxResults(20)
                    .getResultList();

            return movies;

        }
    }


    public List<MovieOverviewDto> getPopularMoviesByGenre(int genreId, Integer accountId) {

        LocalDate cutoffDate = LocalDate.now().minusDays(MAX_DAYS_POPULAR);

        try (EntityManager em = emf.createEntityManager()) {

            String jpql = """
                    SELECT NEW dat.dto.MovieOverviewDto(mg.movie, r.rating)
                    FROM MovieGenre mg
                    LEFT JOIN Rating r ON r.movie.id=mg.movie.id AND r.account.id=:accountId
                    WHERE mg.movie.releaseDate >= :cutoffDate AND mg.genre.id=:genreId
                    ORDER BY mg.movie.popularity * ( 1.0 - 1.0 * DATEDIFF(DAY, m.releaseDate, CURRENT_DATE)/:maxDays ) DESC NULLS last""";
            List<MovieOverviewDto> movies = em.createQuery(jpql, MovieOverviewDto.class)
                    .setParameter("accountId", accountId)
                    .setParameter("cutoffDate", cutoffDate)
                    .setParameter("maxDays", MAX_DAYS_POPULAR)
                    .setParameter("genreId", genreId)
                    .setMaxResults(20)
                    .getResultList();

            return movies;

        }
    }


    /**
     * Gets the top 100 movies ranked by the IMDb weighted rating formula
     * (see <a href="https://web.archive.org/web/20260311072638/https://help.imdb.com/article/imdb/track-movies-tv/ratings-faq/G67Y87TFYYP6TWAV">IMDb Ratings FAQ</a>).
     *
     * @param accountId Account ID to get this app's (True, False, Null) ratings for the {@link MovieOverviewDto} instances
     * @return List of top 100 ranked {@link MovieOverviewDto} instances
     * @see
     */
    public List<MovieOverviewDto> getTop100Movies(Integer accountId) {

        try (EntityManager em = emf.createEntityManager()) {

            String jpql = """
                    SELECT NEW dat.dto.MovieOverviewDto(m, r.rating)
                    FROM Movie m
                    LEFT JOIN Rating r ON r.movie.id = m.id AND r.account.id = :accountId
                    ORDER BY (m.voteAverage * m.voteCount / (m.voteCount + :minVotes)) +
                    (1.0 * :mean * :minVotes / (m.voteCount + :minVotes)) DESC""";
            List<MovieOverviewDto> movies = em.createQuery(jpql, MovieOverviewDto.class)
                    .setParameter("accountId", accountId)
                    .setParameter("minVotes", MIN_VOTE_COUNT)
                    .setParameter("mean", MEAN_TMDB_SCORE)
                    .setMaxResults(100)
                    .getResultList();

            return movies;

        }
    }


    public List<MovieOverviewDto> getTop100MoviesByGenre(int genreId, Integer accountId) {

        try (EntityManager em = emf.createEntityManager()) {

            String jpql = """
                    SELECT NEW dat.dto.MovieOverviewDto(mg.movie, r.rating)
                    FROM MovieGenre mg
                    LEFT JOIN Rating r ON r.movie.id=mg.movie.id AND r.account.id=:accountId
                    WHERE mg.genre.id=:genreId
                    ORDER BY (mg.movie.voteAverage * mg.movie.voteCount / (mg.movie.voteCount + :minVotes)) +
                    (1.0 * :mean * :minVotes / (mg.movie.voteCount + :minVotes)) DESC""";
            List<MovieOverviewDto> movies = em.createQuery(jpql, MovieOverviewDto.class)
                    .setParameter("accountId", accountId)
                    .setParameter("minVotes", MIN_VOTE_COUNT)
                    .setParameter("mean", MEAN_TMDB_SCORE)
                    .setParameter("genreId", genreId)
                    .setMaxResults(100)
                    .getResultList();

            return movies;

        }
    }

    public NameMovieListDto getMoviesWithPerson(int personId, Integer accountId) {

        try (EntityManager em = emf.createEntityManager()) {

            String jpql = "SELECT p.name FROM Person p WHERE p.id = :personId";
            String personName = em.createQuery(jpql, String.class)
                    .setParameter("personId", personId)
                    .getSingleResult();

            jpql = """
                    SELECT DISTINCT NEW dat.dto.MovieOverviewDto(m, r.rating)
                    FROM Movie m
                    LEFT JOIN Rating r ON r.movie.id = m.id AND r.account.id = :accountId
                    JOIN Credit c ON m.id=c.movie.id
                    WHERE c.person.id=:personId ORDER BY m.releaseDate""";
            List<MovieOverviewDto> movies = em.createQuery(jpql, MovieOverviewDto.class)
                    .setParameter("personId", personId)
                    .setParameter("accountId", accountId)
                    .getResultList();

            return new NameMovieListDto(personName, movies);

        }

    }


    public NameMovieListDto getMoviesInCollection(int collectionId, Integer accountId) {

        try (EntityManager em = emf.createEntityManager()) {

            String jpql = "SELECT c.name FROM Collection c WHERE c.id = :collectionId";
            String collectionName = em.createQuery(jpql, String.class)
                    .setParameter("collectionId", collectionId)
                    .getSingleResult();

            jpql = """
                    SELECT NEW dat.dto.MovieOverviewDto(m, r.rating)
                    FROM Movie m
                    LEFT JOIN Rating r ON r.movie.id = m.id AND r.account.id = :accountId
                    WHERE m.collection.id=:collectionId
                    ORDER BY m.releaseDate""";
            List<MovieOverviewDto> movies = em.createQuery(jpql, MovieOverviewDto.class)
                    .setParameter("collectionId", collectionId)
                    .setParameter("accountId", accountId)
                    .getResultList();

            return new NameMovieListDto(collectionName, movies);

        }

    }


    public List<MovieOverviewDto> getAllMoviesWithRating(int accountId) {

        try (EntityManager em = emf.createEntityManager()) {

            String jpql = """
                    SELECT NEW dat.dto.MovieOverviewDto(m, r.rating)
                    FROM Rating r JOIN Movie m ON r.movie.id = m.id WHERE r.account.id =:accountId
                    ORDER BY m.title""";
            List<MovieOverviewDto> movies = em.createQuery(jpql, MovieOverviewDto.class)
                    .setParameter("accountId", accountId)
                    .getResultList();

            return movies;
        }

    }


    public MovieDetailsDto getMovieDetails(int movieId) {

        try (EntityManager em = emf.createEntityManager()) {

            // Find Movie entity, TODO: Some error handling in case m is null
            Movie m = em.find(Movie.class, movieId);

            // Find credits for movie
            String jpql = """
                    SELECT NEW dat.dto.TmdbCreditDto(c.id, p.id, p.name, c.department, c.job, c.character)
                    FROM Credit c JOIN Person p ON c.person.id = p.id WHERE c.movie.id =:movieId
                    ORDER BY c.rankInMovie""";
            List<TmdbCreditDto> TmdbCreditDtos = em.createQuery(jpql, TmdbCreditDto.class)
                    .setParameter("movieId", movieId)
                    .getResultList();

            // Merge credits together within same department
            LinkedHashMap<String, CreditDto> creditMap = new LinkedHashMap<String, CreditDto>();
            for (TmdbCreditDto c : TmdbCreditDtos) {
                String id = c.department() + "_" + c.personId(); // String with this format "department_personId"
                CreditDto credit = creditMap.get(id);
                if (credit == null) {
                    List<String> jobsInDepartment = new ArrayList<>();
                    List<String> characters = new ArrayList<>();
                    credit = new CreditDto(id, c.personId(), c.name(), c.department(), jobsInDepartment, characters);
                    creditMap.put(id, credit);
                }
                credit.jobsInDepartment().add(c.job());
                if ("Cast".equals(c.department())) {
                    if (!c.character().isBlank()) {
                        credit.characters().add(c.character());
                    }
                    credit.jobsInDepartment().subList(1, credit.jobsInDepartment().size()).clear(); // Avoids multiple "Cast Member" jobs
                }
            }
            List<CreditDto> credits = creditMap.values().stream().toList();

            return new MovieDetailsDto(m, credits);

        }

    }


    public void updateOrCreateMovieRating(int accountId, int movieId, boolean rating) {

        try (EntityManager em = emf.createEntityManager()) {

            em.getTransaction().begin();

            // First try to update existing rating
            String jpql = "UPDATE Rating r SET r.rating =:rating WHERE r.account.id=:accountId AND r.movie.id = :movieId";
            int rowsAffected = em.createQuery(jpql)
                    .setParameter("accountId", accountId)
                    .setParameter("movieId", movieId)
                    .setParameter("rating", rating)
                    .executeUpdate();

            // Create new rating, if rating does not already exist
            if (rowsAffected == 0) {
                Account account = em.find(Account.class, accountId);
                Movie movie = em.find(Movie.class, movieId);
                em.persist(new Rating(account, movie, rating));
            }

            em.getTransaction().commit();

        }

    }


    public void deleteMovieRating(int accountId, int movieId) {

        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "DELETE FROM Rating r WHERE r.account.id = :accountId AND r.movie.id = :movieId";
            em.getTransaction().begin();
            int rowsAffected = em.createQuery(jpql)
                    .setParameter("accountId", accountId)
                    .setParameter("movieId", movieId)
                    .executeUpdate();
            em.getTransaction().commit();
        }

    }


    public List<MovieOverviewDto> getMovieRecommendations(int accountId, int limit) {

        try (EntityManager em = emf.createEntityManager()) {

            // Get list of id of movies, which the user have rated positively
            String jpql = "SELECT r.movie.id FROM Rating r WHERE r.account.id = :accountId AND rating=TRUE";
            List<Integer> movieIds = em.createQuery(jpql, Integer.class)
                    .setParameter("accountId", accountId)
                    .getResultList();

            // Get list of director ids which have made movies, which the user have rated positively
            jpql = "SELECT p.id FROM Person p JOIN Credit c ON c.person.id=p.id WHERE c.job='Director' AND c.movie.id IN :movieIds";
            List<Integer> directorIds = em.createQuery(jpql, Integer.class)
                    .setParameter("movieIds", movieIds)
                    .getResultList();

            // Select all movies from these directors
            jpql = "SELECT m.id FROM Movie m JOIN Credit c ON c.movie.id=m.id WHERE c.job='Director' AND c.person.id IN :directorIds";
            movieIds = em.createQuery(jpql, Integer.class)
                    .setParameter("directorIds", directorIds)
                    .getResultList();

            // Exclude movies already rated by user. and ORDER BY and LIMIT
            jpql = """
                    SELECT m.id FROM Movie m WHERE m.id IN :movieIds AND m.id NOT IN
                            (SELECT r.movie.id FROM Rating r WHERE r.account.id =:accountId)
                    ORDER BY m.voteAverage DESC NULLS LAST""";
            movieIds = em.createQuery(jpql, Integer.class)
                    .setParameter("movieIds", movieIds)
                    .setParameter("accountId", accountId)
                    .setMaxResults(limit)
                    .getResultList();

            jpql = "SELECT NEW dat.dto.MovieOverviewDto(m) FROM Movie m WHERE m.id IN :movieIds ORDER BY m.voteAverage DESC NULLS LAST";
            List<MovieOverviewDto> recommendations = em.createQuery(jpql, MovieOverviewDto.class)
                    .setParameter("movieIds", movieIds)
                    .getResultList();

            return recommendations;

        }
    }

}