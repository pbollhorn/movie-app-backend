package dat.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import jakarta.persistence.EntityManagerFactory;

import dat.dto.TmdbMovieDto;
import dat.entities.Genre;
import dat.entities.Movie;
import dat.dto.TmdbGenreDto;


public class Populator {

    private static final Genre ADVENTURE = new Genre(12, "Adventure");
    private static final Genre FANTASY = new Genre(14, "Fantasy");
    private static final Genre ANIMATION = new Genre(16, "Animation");
    private static final Genre DRAMA = new Genre(18, "Drama");
    private static final Genre HORROR = new Genre(27, "Horror");
    private static final Genre ACTION = new Genre(28, "Action");
    private static final Genre COMEDY = new Genre(35, "Comedy");
    private static final Genre HISTORY = new Genre(36, "History");
    private static final Genre WESTERN = new Genre(37, "Western");
    private static final Genre THRILLER = new Genre(53, "Thriller");
    private static final Genre CRIME = new Genre(80, "Crime");
    private static final Genre DOCUMENTARY = new Genre(99, "Documentary");
    private static final Genre SCIENCE_FICTION = new Genre(878, "Sci-Fi");
    private static final Genre MYSTERY = new Genre(9648, "Mystery");
    private static final Genre MUSIC = new Genre(10402, "Music");
    private static final Genre ROMANCE = new Genre(10749, "Romance");
    private static final Genre FAMILY = new Genre(10751, "Family");
    private static final Genre WAR = new Genre(10752, "War");
    private static final Genre TV_MOVIE = new Genre(10770, "TV Movie");

    public static TmdbMovieDto[] populate(MovieDao movieDao, GenreDao genreDao, EntityManagerFactory emf) {

        // Populate with all genres
        Set<Genre> genres = Set.of(ADVENTURE, FANTASY, ANIMATION, DRAMA, HORROR, ACTION, COMEDY, HISTORY, WESTERN,
                THRILLER, CRIME, DOCUMENTARY, SCIENCE_FICTION, MYSTERY, MUSIC, ROMANCE, FAMILY, WAR, TV_MOVIE);
        genres.forEach(genreDao::create);

        // Populate with two movies
        TmdbMovieDto m1 = new TmdbMovieDto(23588, "Baby Doom", "Baby Doom", false,
                false, "da", 5.7, 6, LocalDate.of(1998, 3, 20),
                null, "/sf3AHsBHmyEcRxCA3pU7ggA5csk.jpg",
                List.of(new TmdbGenreDto(35, "Comedy")), "Max er en super nørd...",
                null, 81, null, "Released", null);
        movieDao.create(new Movie(m1));

        TmdbMovieDto m2 = new TmdbMovieDto(33416, "Midt Om Natten", "Midt om natten", false,
                false, "da", 6.5, 24, LocalDate.of(1984, 3, 9),
                "/ljx2Ds6VIiwtCImKJGk4ytAGPpA.jpg", "/739gDLbIA4SgEESTd3toHkftJOu.jpg",
                List.of(new TmdbGenreDto(18, "Drama"), new TmdbGenreDto(35, "Comedy")), "I 80'ernes Danmark...",
                null, 131, null, "Released", null);
        movieDao.create(new Movie(m2));


//        try (EntityManager em = emf.createEntityManager()) {
//            em.getTransaction().begin();
//            // Create test user with user role
//            Account account1 = new Account("testuser1", "password1");
//            account1.addRole(Roles.USER);
//            em.persist(account1);
//            em.getTransaction().commit();
//        }


        return new TmdbMovieDto[]{m1, m2};
//        return new TmdbMovieDto[]{m1};

    }


}
