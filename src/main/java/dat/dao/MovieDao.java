package dat.dao;

import dat.dto.FrontendMovieDto;
import dat.entities.Account;
import dat.entities.AccountMovieRating;
import dat.entities.Genre;
import dat.entities.Movie;
import dat.exceptions.DaoException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import java.util.List;

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

    public List<Genre> readGenresByMovieTitle(String title) {
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "SELECT g FROM Movie m JOIN m.genres g WHERE m.title = :title";
            TypedQuery query = em.createQuery(jpql, Genre.class);
            query.setParameter("title", title);
            return query.getResultList();
        } catch (RuntimeException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Movie> readMoviesByGenre(String genre) {
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "SELECT m FROM Movie m JOIN m.genres g WHERE g.name = :genre";
            TypedQuery<Movie> query = em.createQuery(jpql, Movie.class);
            query.setParameter("genre", genre);
            return query.getResultList();
        } catch (RuntimeException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Movie> searchMoviesByKeywordInTitleOrOrignalTitle(String keyword) {
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "SELECT m FROM Movie m WHERE LOWER(m.title) LIKE :title OR LOWER(m.originalTitle) LIKE :title";
            TypedQuery<Movie> query = em.createQuery(jpql, Movie.class);
            query.setParameter("title", "%" + keyword.toLowerCase() + "%");
            return query.getResultList();
        } catch (RuntimeException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Double getAverageRatingOfAllMovies() {
        try (EntityManager em = emf.createEntityManager()) {
            String jqpl = "SELECT AVG(m.voteAverage) FROM Movie m WHERE m.voteCount>0";
            TypedQuery<Double> query = em.createQuery(jqpl, Double.class);
            return query.getSingleResult();
        } catch (RuntimeException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Movie> getHighestRatedMovies(int number) {
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "SELECT m FROM Movie m WHERE m.voteCount>=100 ORDER BY m.voteAverage DESC LIMIT :number";
            TypedQuery<Movie> query = em.createQuery(jpql, Movie.class);
            query.setParameter("number", number);
            return query.getResultList();
        } catch (RuntimeException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Movie> getLowestRatedMovies(int number) {
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "SELECT m FROM Movie m WHERE m.voteCount>=100 ORDER BY m.voteAverage LIMIT :number";
            TypedQuery<Movie> query = em.createQuery(jpql, Movie.class);
            query.setParameter("number", number);
            return query.getResultList();
        } catch (RuntimeException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Movie> getMostPopularMovies(int number) {
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "SELECT m FROM Movie m ORDER BY m.popularity DESC LIMIT :number";
            TypedQuery<Movie> query = em.createQuery(jpql, Movie.class);
            query.setParameter("number", number);
            return query.getResultList();
        } catch (RuntimeException e) {
            e.printStackTrace();
            return null;
        }
    }


    public List<FrontendMovieDto> getMoviesByTextInTitle(String text) {

        String jpql = "SELECT NEW dat.dto.FrontendMovieDto(m.id, m.title, m.originalTitle, m.releaseDate, m.rating, m.posterPath, NULL) FROM Movie m WHERE LOWER(m.title) LIKE :title";

        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<FrontendMovieDto> query = em.createQuery(jpql, FrontendMovieDto.class);
            query.setParameter("title", "%" + text.toLowerCase() + "%");
            return query.getResultList();
        }

    }


    public List<FrontendMovieDto> getMoviesAndRatings(int accountId) {

        String jpql = "SELECT NEW dat.dto.FrontendMovieDto(m.id, m.title, m.originalTitle, m.releaseDate, m.rating, m.posterPath, r.rating) FROM AccountMovieRating r JOIN Movie m ON r.movie.id=m.id WHERE r.account.id=:accountId";

        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<FrontendMovieDto> query = em.createQuery(jpql, FrontendMovieDto.class);
            query.setParameter("accountId", accountId);
            return query.getResultList();
        }

    }


    public void updateOrCreateRating(int accountId, int movieId, boolean rating) {

        try (EntityManager em = emf.createEntityManager()) {

            em.getTransaction().begin();

            // First try to update existing rating
            String jpql = "UPDATE AccountMovieRating r SET r.rating =:rating WHERE r.account.id=:accountId AND r.movie.id = :movieId";
            Query query = em.createQuery(jpql);
            query.setParameter("accountId", accountId);
            query.setParameter("movieId", movieId);
            query.setParameter("rating", rating);
            int rowsAffected = query.executeUpdate();

            // Create new rating, if rating does not already exist
            if (rowsAffected == 0) {
                Account account = em.find(Account.class, accountId);
                Movie movie = em.find(Movie.class, movieId);
                em.persist(new AccountMovieRating(null, account, movie, rating));
            }

            em.getTransaction().commit();

        }

    }


    public void deleteRating(int accountId, int movieId) {

        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "DELETE FROM AccountMovieRating r WHERE r.account.id = :accountId AND r.movie.id = :movieId";
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
    public List<FrontendMovieDto> getRecommendations(int accountId, int limit) {

        try (EntityManager em = emf.createEntityManager()) {

//            // Get list of id of movies, which the user likes
            String jpql = "SELECT r.movie.id FROM AccountMovieRating r WHERE r.account.id = :accountId AND rating=true";
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
                    (SELECT a.movie.id FROM AccountMovieRating a WHERE a.account.id=:accountId)
                    ORDER BY m.voteAverage DESC LIMIT :limit""";
            query = em.createQuery(jpql, Integer.class);
            query.setParameter("movieIds", movieIds);
            query.setParameter("accountId", accountId);
            query.setParameter("limit", limit);
            movieIds = query.getResultList();

            // Now finally get the data for all these movieIds
            jpql = "SELECT NEW dat.dto.FrontendMovieDto(m.id, m.title, m.originalTitle, m.releaseDate, m.rating, m.posterPath, NULL) FROM Movie m WHERE m.id IN :movieIds";
            TypedQuery<FrontendMovieDto> newQuery = em.createQuery(jpql, FrontendMovieDto.class);
            newQuery.setParameter("movieIds", movieIds);
            List<FrontendMovieDto> recommendations = newQuery.getResultList();

            return recommendations;

        }
    }

}