package dat.dao;

import java.util.List;

import dat.dto.FrontendPersonDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import dat.entities.Account;
import dat.entities.AccountMovieLikes;
import dat.entities.Movie;
import dat.dto.FrontendMovieDetailsDto;
import dat.dto.FrontendMovieOverviewDto;


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

    public List<FrontendMovieOverviewDto> searchMoviesOpen(String text, int limit) {

        try (EntityManager em = emf.createEntityManager()) {

            String jpql = "SELECT NEW dat.dto.FrontendMovieOverviewDto(m) FROM Movie m WHERE LOWER(m.title) LIKE :title OR LOWER(m.originalTitle) LIKE :title ORDER BY m.title LIMIT :limit";
            TypedQuery<FrontendMovieOverviewDto> query = em.createQuery(jpql, FrontendMovieOverviewDto.class);
            query.setParameter("title", "%" + text.toLowerCase() + "%");
            query.setParameter("limit", limit);
            return query.getResultList();

        }

    }


    public List<FrontendMovieOverviewDto> searchMovies(String text, int accountId, int limit) {

        try (EntityManager em = emf.createEntityManager()) {

            String jpql = """
                    SELECT NEW dat.dto.FrontendMovieOverviewDto(m, (SELECT a.likes FROM AccountMovieLikes a WHERE a.movie.id=m.id AND a.account.id=:accountId))
                    FROM Movie m WHERE LOWER(m.title) LIKE :title OR LOWER(m.originalTitle) LIKE :title ORDER BY m.title LIMIT :limit""";

            TypedQuery<FrontendMovieOverviewDto> query = em.createQuery(jpql, FrontendMovieOverviewDto.class);
            query.setParameter("title", "%" + text.toLowerCase() + "%");
            query.setParameter("accountId", accountId);
            query.setParameter("limit", limit);
            return query.getResultList();

        }

    }


    public List<FrontendMovieOverviewDto> getAllMoviesWithLikes(int accountId) {

        try (EntityManager em = emf.createEntityManager()) {

            String jpql = """
                    SELECT NEW dat.dto.FrontendMovieOverviewDto(m, a.likes)
                    FROM AccountMovieLikes a JOIN Movie m ON a.movie.id=m.id WHERE a.account.id=:accountId ORDER BY m.title""";

            TypedQuery<FrontendMovieOverviewDto> query = em.createQuery(jpql, FrontendMovieOverviewDto.class);
            query.setParameter("accountId", accountId);
            return query.getResultList();
        }

    }

    public FrontendMovieDetailsDto getMovieDetails(int movieId) {

        try (EntityManager em = emf.createEntityManager()) {

            // Find Movie entity, TODO: Some error handling in case m is null
            Movie m = em.find(Movie.class, movieId);

            // Find persons list
            String jpql = """
                    SELECT NEW dat.dto.FrontendPersonDto(p.id, p.name, c.job, c.character)
                    FROM Person p JOIN Credit c ON c.person.id=p.id WHERE c.movie.id=:movieId ORDER BY c.rankInMovie""";

            TypedQuery<FrontendPersonDto> query = em.createQuery(jpql, FrontendPersonDto.class);
            query.setParameter("movieId", movieId);
            List<FrontendPersonDto> persons = query.getResultList();

            return new FrontendMovieDetailsDto(m, persons);

        }

    }


    public void updateOrCreateMovieLike(int accountId, int movieId, boolean likes) {

        try (EntityManager em = emf.createEntityManager()) {

            em.getTransaction().begin();

            // First try to update existing rating
            String jpql = "UPDATE AccountMovieLikes a SET a.likes =:likes WHERE a.account.id=:accountId AND a.movie.id = :movieId";
            Query query = em.createQuery(jpql);
            query.setParameter("accountId", accountId);
            query.setParameter("movieId", movieId);
            query.setParameter("likes", likes);
            int rowsAffected = query.executeUpdate();

            // Create new rating, if rating does not already exist
            if (rowsAffected == 0) {
                Account account = em.find(Account.class, accountId);
                Movie movie = em.find(Movie.class, movieId);
                em.persist(new AccountMovieLikes(null, account, movie, likes));
            }

            em.getTransaction().commit();

        }

    }


    public void deleteMovieLike(int accountId, int movieId) {

        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "DELETE FROM AccountMovieLikes a WHERE a.account.id = :accountId AND a.movie.id = :movieId";
            em.getTransaction().begin();
            Query query = em.createQuery(jpql);
            query.setParameter("accountId", accountId);
            query.setParameter("movieId", movieId);
            int rowsAffected = query.executeUpdate();
            em.getTransaction().commit();
        }

    }


    public List<FrontendMovieOverviewDto> getMovieRecommendations(int accountId, int limit) {

        try (EntityManager em = emf.createEntityManager()) {

            // Get list of id of movies, which the user likes
            String jpql = "SELECT a.movie.id FROM AccountMovieLikes a WHERE a.account.id = :accountId AND likes=TRUE";
            TypedQuery<Integer> query = em.createQuery(jpql, Integer.class);
            query.setParameter("accountId", accountId);
            List<Integer> movieIds = query.getResultList();

            // Get list of director ids which have made movies, which the user likes
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

            // Exclude movies already liked or disliked by user. and ORDER BY and LIMIT
            jpql = """
                    SELECT m.id FROM Movie m WHERE m.id IN :movieIds AND m.id NOT IN
                    (SELECT a.movie.id FROM AccountMovieLikes a WHERE a.account.id=:accountId)
                    ORDER BY m.rating DESC NULLS LAST LIMIT :limit""";
            query = em.createQuery(jpql, Integer.class);
            query.setParameter("movieIds", movieIds);
            query.setParameter("accountId", accountId);
            query.setParameter("limit", limit);
            movieIds = query.getResultList();

            jpql = "SELECT NEW dat.dto.FrontendMovieOverviewDto(m) FROM Movie m WHERE m.id IN :movieIds ORDER BY m.rating DESC NULLS LAST";
            TypedQuery<FrontendMovieOverviewDto> newQuery = em.createQuery(jpql, FrontendMovieOverviewDto.class);
            newQuery.setParameter("movieIds", movieIds);
            List<FrontendMovieOverviewDto> recommendations = newQuery.getResultList();

            return recommendations;

        }
    }

}