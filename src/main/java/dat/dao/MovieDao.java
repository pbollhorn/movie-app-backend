package dat.dao;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import dat.dto.FrontendMovieDto;
import dat.entities.Account;
import dat.entities.AccountMovieLikes;
import dat.entities.Movie;

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


    public List<FrontendMovieDto> searchMoviesOpen(String text) {

        try (EntityManager em = emf.createEntityManager()) {

            String jpql = """
                    SELECT NEW dat.dto.FrontendMovieDto(m.id, m.title, m.originalTitle, m.releaseDate, m.rating, m.posterPath, NULL)
                    FROM Movie m WHERE LOWER(m.title) LIKE :title OR LOWER(m.originalTitle) LIKE :title""";

            TypedQuery<FrontendMovieDto> query = em.createQuery(jpql, FrontendMovieDto.class);
            query.setParameter("title", "%" + text.toLowerCase() + "%");
            return query.getResultList();

        }

    }


    public List<FrontendMovieDto> searchMovies(String text, int accountId) {

        try (EntityManager em = emf.createEntityManager()) {

            // TODO: Find a way to get likes to be NULL if user has not liked/disliked yet
            String jpql = """
                    SELECT NEW dat.dto.FrontendMovieDto(m.id, m.title, m.originalTitle, m.releaseDate, m.rating, m.posterPath, a.likes)
                    FROM Movie m JOIN AccountMovieLikes a ON a.movie.id=m.id
                    LOWER(m.title) LIKE :title OR LOWER(m.originalTitle) LIKE :title""";

            TypedQuery<FrontendMovieDto> query = em.createQuery(jpql, FrontendMovieDto.class);
            query.setParameter("title", "%" + text.toLowerCase() + "%");
            query.setParameter("accountId", accountId);
            return query.getResultList();

        }

    }


    public List<FrontendMovieDto> getAllMoviesWithLikes(int accountId) {

        try (EntityManager em = emf.createEntityManager()) {

            String jpql = """
                    SELECT NEW dat.dto.FrontendMovieDto(m.id, m.title, m.originalTitle, m.releaseDate, m.rating, m.posterPath, a.likes)
                    FROM AccountMovieLikes a JOIN Movie m ON a.movie.id=m.id WHERE a.account.id=:accountId""";

            TypedQuery<FrontendMovieDto> query = em.createQuery(jpql, FrontendMovieDto.class);
            query.setParameter("accountId", accountId);
            return query.getResultList();
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

    // TODO: Her skal min algoritme være, der benytter instruktør og rating
    // og bagefter frasortere em man har disliket
    public List<FrontendMovieDto> getMovieRecommendations(int accountId, int limit) {

        try (EntityManager em = emf.createEntityManager()) {

//            // Get list of id of movies, which the user likes
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

            // Exclude movies already liked or disliked by user
            jpql = """
                    SELECT m.id FROM Movie m WHERE m.id IN :movieIds AND m.id NOT IN
                    (SELECT a.movie.id FROM AccountMovieLikes a WHERE a.account.id=:accountId)
                    ORDER BY m.voteAverage DESC LIMIT :limit""";
            query = em.createQuery(jpql, Integer.class);
            query.setParameter("movieIds", movieIds);
            query.setParameter("accountId", accountId);
            query.setParameter("limit", limit);
            movieIds = query.getResultList();

            // Now finally get the data for all these movieIds
            jpql = """
                    SELECT NEW dat.dto.FrontendMovieDto(m.id, m.title, m.originalTitle, m.releaseDate, m.rating, m.posterPath, NULL)
                    FROM Movie m WHERE m.id IN :movieIds ORDER BY m.voteAverage DESC""";
            TypedQuery<FrontendMovieDto> newQuery = em.createQuery(jpql, FrontendMovieDto.class);
            newQuery.setParameter("movieIds", movieIds);
            List<FrontendMovieDto> recommendations = newQuery.getResultList();

            return recommendations;

        }
    }

}