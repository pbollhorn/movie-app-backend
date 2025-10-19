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
    private static final Genre SCI_FI = new Genre(878, "Sci-Fi");
    private static final Genre MYSTERY = new Genre(9648, "Mystery");
    private static final Genre MUSIC = new Genre(10402, "Music");
    private static final Genre ROMANCE = new Genre(10749, "Romance");
    private static final Genre FAMILY = new Genre(10751, "Family");
    private static final Genre WAR = new Genre(10752, "War");
    private static final Genre TV_MOVIE = new Genre(10770, "TV Movie");

    public static TmdbMovieDto[] populate(MovieDao movieDao, GenreDao genreDao, EntityManagerFactory emf) {

        // Populate with all genres
        Set<Genre> genres = Set.of(ADVENTURE, FANTASY, ANIMATION, DRAMA, HORROR, ACTION, COMEDY, HISTORY, WESTERN,
                THRILLER, CRIME, DOCUMENTARY, SCI_FI, MYSTERY, MUSIC, ROMANCE, FAMILY, WAR, TV_MOVIE);
        genres.forEach(genreDao::create);

        // Populate with two movies
        TmdbMovieDto m1 = new TmdbMovieDto(329, "Jurassic Park", "Jurassic Park", false,
                false, "en", 7.96, 17095, LocalDate.of(1993, 6, 11),
                "/jzt9HuhIAdH9qp0K2MA1m5V8sgq.jpg", "/63viWuPfYQjRYLSZSZNq7dglJP5.jpg",
                List.of(new TmdbGenreDto(12, "Adventure"), new TmdbGenreDto(878, "Sci-Fi")), "A wealthy entrepreneur secretly creates...",
                null, 127, "An adventure 65 million years in the making.", "Released", null);
        movieDao.create(new Movie(m1));

        TmdbMovieDto m2 = new TmdbMovieDto(33416, "In the Middle of the Night", "Midt om natten", false,
                false, "da", 6.538, 26, LocalDate.of(1984, 3, 9),
                "/ljx2Ds6VIiwtCImKJGk4ytAGPpA.jpg", "/739gDLbIA4SgEESTd3toHkftJOu.jpg",
                List.of(new TmdbGenreDto(18, "Drama"), new TmdbGenreDto(35, "Comedy")), "Benny and Arnold are homeless...",
                null, 131, "", "Released", null);
        movieDao.create(new Movie(m2));


        return new TmdbMovieDto[]{m1, m2};

    }


}
