package dat.dao;

import dat.dto.FrontendMovieDto;
import dat.entities.Genre;
import dat.entities.Movie;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
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

        String jpql = "SELECT NEW dat.dto.FrontendMovieDto(m.id, m.title, m.originalTitle, m.releaseDate, m.rating, m.posterPath) FROM Movie m WHERE LOWER(m.title) LIKE :title";

        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<FrontendMovieDto> query = em.createQuery(jpql, FrontendMovieDto.class);
            query.setParameter("title", "%" + text.toLowerCase() + "%");
            return query.getResultList();
        }

    }


}