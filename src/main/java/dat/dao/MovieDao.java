package dat.dao;

import java.util.*;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import dat.entities.Account;
import dat.entities.Rating;
import dat.entities.Movie;
import dat.dto.*;


public class MovieDao extends AbstractDao<Movie, Integer> {

    private static MovieDao instance;

    private MovieDao(EntityManagerFactory emf) {
        super(Movie.class, emf);
    }

    public static MovieDao getInstance(EntityManagerFactory emf) {
        if (instance == null) {
            instance = new MovieDao(emf);
        }
        return instance;
    }

    public Set<Integer> getAllMovieIds() {

        try (EntityManager em = emf.createEntityManager()) {

            String jpql = "SELECT m.id FROM Movie m";
            TypedQuery<Integer> query = em.createQuery(jpql, Integer.class);
            return new HashSet<Integer>(query.getResultList());

        }

    }


    public List<MovieOverviewDto> searchMovies(String title, Integer accountId, int limit) {

        try (EntityManager em = emf.createEntityManager()) {

            String sql = """
                    SELECT id FROM movie
                    WHERE :title % title
                    ORDER BY SIMILARITY(:title, title) DESC, votecount DESC
                    LIMIT :limit""";
            Query firstQuery = em.createNativeQuery(sql, Integer.class);
            firstQuery.setParameter("title", title);
            firstQuery.setParameter("limit", limit);
            List<Integer> movieIds = firstQuery.getResultList();


            sql = """
                    SELECT id FROM movie
                    WHERE :title <<% title AND id NOT IN (:movieIds)
                    ORDER BY STRICT_WORD_SIMILARITY(:title, title) DESC, votecount DESC
                    LIMIT :limit""";
            Query secondQuery = em.createNativeQuery(sql, Integer.class);
            secondQuery.setParameter("title", title);
            secondQuery.setParameter("limit", limit);
            secondQuery.setParameter("movieIds", movieIds);
            movieIds.addAll(secondQuery.getResultList());

            String jpql = """
                    SELECT NEW dat.dto.MovieOverviewDto(m,
                    (SELECT r.rating FROM Rating r WHERE r.movie.id=m.id AND r.account.id=:accountId))
                    FROM Movie m WHERE m.id IN :movieIds""";

            TypedQuery<MovieOverviewDto> thirdQuery = em.createQuery(jpql, MovieOverviewDto.class);
            thirdQuery.setParameter("accountId", accountId);
            thirdQuery.setParameter("movieIds", movieIds);
            List<MovieOverviewDto> movieDtos = thirdQuery.getResultList();

            // Map from ID to DTO for lookup
            Map<Integer, MovieOverviewDto> dtoMap = movieDtos.stream()
                    .collect(Collectors.toMap(dto -> dto.id(), dto -> dto));

            // Rebuild list in correct order
            return movieIds.stream()
                    .map(dtoMap::get)
                    .collect(Collectors.toList());

        }

    }

    public NameMovieListDto getMoviesWithPerson(int personId, Integer accountId) {

        try (EntityManager em = emf.createEntityManager()) {

            String jpql = "SELECT p.name FROM Person p WHERE p.id = :personId";
            TypedQuery<String> query = em.createQuery(jpql, String.class);
            query.setParameter("personId", personId);
            String name = query.getSingleResult();

            jpql = """
                    SELECT DISTINCT NEW dat.dto.MovieOverviewDto(m,
                    (SELECT r.rating FROM Rating r WHERE r.movie.id=m.id AND r.account.id=:accountId))
                    FROM Movie m JOIN Credit c ON m.id=c.movie.id
                    WHERE c.person.id=:personId ORDER BY m.releaseDate""";
            TypedQuery<MovieOverviewDto> newQuery = em.createQuery(jpql, MovieOverviewDto.class);
            newQuery.setParameter("personId", personId);
            newQuery.setParameter("accountId", accountId);
            List<MovieOverviewDto> movieList = newQuery.getResultList();

            return new NameMovieListDto(name, movieList);

        }

    }

    public NameMovieListDto getMoviesInCollection(int collectionId, Integer accountId) {

        try (EntityManager em = emf.createEntityManager()) {

            String jpql = "SELECT c.name FROM Collection c WHERE c.id = :collectionId";
            TypedQuery<String> query = em.createQuery(jpql, String.class);
            query.setParameter("collectionId", collectionId);
            String name = query.getSingleResult();

            jpql = """
                    SELECT NEW dat.dto.MovieOverviewDto(m,
                    (SELECT r.rating FROM Rating r WHERE r.movie.id=m.id AND r.account.id=:accountId))
                    FROM Movie m
                    WHERE m.collection.id=:collectionId
                    ORDER BY m.releaseDate""";
            TypedQuery<MovieOverviewDto> newQuery = em.createQuery(jpql, MovieOverviewDto.class);
            newQuery.setParameter("collectionId", collectionId);
            newQuery.setParameter("accountId", accountId);
            List<MovieOverviewDto> movieList = newQuery.getResultList();

            return new NameMovieListDto(name, movieList);

        }

    }


    public List<MovieOverviewDto> getAllMoviesWithRating(int accountId) {

        try (EntityManager em = emf.createEntityManager()) {

            String jpql = """
                    SELECT NEW dat.dto.MovieOverviewDto(m, r.rating)
                    FROM Rating r JOIN Movie m ON r.movie.id = m.id WHERE r.account.id =:accountId
                    ORDER BY m.title """;

            TypedQuery<MovieOverviewDto> query = em.createQuery(jpql, MovieOverviewDto.class);
            query.setParameter("accountId", accountId);
            return query.getResultList();
        }

    }

    public MovieDetailsDto getMovieDetails(int movieId) {

        try (EntityManager em = emf.createEntityManager()) {

            // Find Movie entity, TODO: Some error handling in case m is null
            Movie m = em.find(Movie.class, movieId);

            // Find persons list
            String jpql = """
                    SELECT NEW dat.dto.CreditDto(c.id, p.id, p.name, c.job, c.department, c.character)
                    FROM Person p JOIN Credit c ON c.person.id = p.id WHERE c.movie.id =:movieId
                    ORDER BY c.rankInMovie """;

            TypedQuery<CreditDto> query = em.createQuery(jpql, CreditDto.class);
            query.setParameter("movieId", movieId);
            List<CreditDto> credits = query.getResultList();

            return new MovieDetailsDto(m, credits);

        }

    }


    public void updateOrCreateMovieRating(int accountId, int movieId, boolean rating) {

        try (EntityManager em = emf.createEntityManager()) {

            em.getTransaction().begin();

            // First try to update existing rating
            String jpql = "UPDATE Rating r SET r.rating =:rating WHERE r.account.id=:accountId AND r.movie.id = :movieId";
            Query query = em.createQuery(jpql);
            query.setParameter("accountId", accountId);
            query.setParameter("movieId", movieId);
            query.setParameter("rating", rating);
            int rowsAffected = query.executeUpdate();

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
            Query query = em.createQuery(jpql);
            query.setParameter("accountId", accountId);
            query.setParameter("movieId", movieId);
            int rowsAffected = query.executeUpdate();
            em.getTransaction().commit();
        }

    }


    public List<MovieOverviewDto> getMovieRecommendations(int accountId, int limit) {

        try (EntityManager em = emf.createEntityManager()) {

            // Get list of id of movies, which the user have rated positively
            String jpql = "SELECT r.movie.id FROM Rating r WHERE r.account.id = :accountId AND rating=TRUE";
            TypedQuery<Integer> query = em.createQuery(jpql, Integer.class);
            query.setParameter("accountId", accountId);
            List<Integer> movieIds = query.getResultList();

            // Get list of director ids which have made movies, which the user have rated positively
            jpql = "SELECT p.id FROM Person p JOIN Credit c ON c.person.id=p.id WHERE c.job='Director' AND c.movie.id IN :movieIds";
            query = em.createQuery(jpql, Integer.class);
            query.setParameter("movieIds", movieIds);
            List<Integer> directorIds = query.getResultList();

            // Select all movies from these directors
            jpql = "SELECT m.id FROM Movie m JOIN Credit c ON c.movie.id=m.id WHERE c.job='Director' AND c.person.id IN :directorIds";
            query = em.createQuery(jpql, Integer.class);
            query.setParameter("directorIds", directorIds);
            movieIds = query.getResultList();
            movieIds.forEach(System.out::println);

            // Exclude movies already rated by user. and ORDER BY and LIMIT
            jpql = """
                    SELECT m.id FROM Movie m WHERE m.id IN :movieIds AND m.id NOT IN
                            (SELECT r.movie.id FROM Rating r WHERE r.account.id =:accountId)
                    ORDER BY m.voteAverage DESC NULLS LAST LIMIT:
                    limit """;
            query = em.createQuery(jpql, Integer.class);
            query.setParameter("movieIds", movieIds);
            query.setParameter("accountId", accountId);
            query.setParameter("limit", limit);
            movieIds = query.getResultList();

            jpql = "SELECT NEW dat.dto.MovieOverviewDto(m) FROM Movie m WHERE m.id IN :movieIds ORDER BY m.voteAverage DESC NULLS LAST";
            TypedQuery<MovieOverviewDto> newQuery = em.createQuery(jpql, MovieOverviewDto.class);
            newQuery.setParameter("movieIds", movieIds);
            List<MovieOverviewDto> recommendations = newQuery.getResultList();

            return recommendations;

        }
    }

}