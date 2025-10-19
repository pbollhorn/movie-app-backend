package dat.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import jakarta.persistence.EntityManagerFactory;

import dat.dto.TmdbMovieDto;
import dat.entities.Movie;
import dat.dto.TmdbGenreDto;


public class Populator {

    private static final TmdbGenreDto ADVENTURE = new TmdbGenreDto(12, "Adventure");
    private static final TmdbGenreDto FANTASY = new TmdbGenreDto(14, "Fantasy");
    private static final TmdbGenreDto ANIMATION = new TmdbGenreDto(16, "Animation");
    private static final TmdbGenreDto DRAMA = new TmdbGenreDto(18, "Drama");
    private static final TmdbGenreDto HORROR = new TmdbGenreDto(27, "Horror");
    private static final TmdbGenreDto ACTION = new TmdbGenreDto(28, "Action");
    private static final TmdbGenreDto COMEDY = new TmdbGenreDto(35, "Comedy");
    private static final TmdbGenreDto HISTORY = new TmdbGenreDto(36, "History");
    private static final TmdbGenreDto WESTERN = new TmdbGenreDto(37, "Western");
    private static final TmdbGenreDto THRILLER = new TmdbGenreDto(53, "Thriller");
    private static final TmdbGenreDto CRIME = new TmdbGenreDto(80, "Crime");
    private static final TmdbGenreDto DOCUMENTARY = new TmdbGenreDto(99, "Documentary");
    private static final TmdbGenreDto SCI_FI = new TmdbGenreDto(878, "Sci-Fi");
    private static final TmdbGenreDto MYSTERY = new TmdbGenreDto(9648, "Mystery");
    private static final TmdbGenreDto MUSIC = new TmdbGenreDto(10402, "Music");
    private static final TmdbGenreDto ROMANCE = new TmdbGenreDto(10749, "Romance");
    private static final TmdbGenreDto FAMILY = new TmdbGenreDto(10751, "Family");
    private static final TmdbGenreDto WAR = new TmdbGenreDto(10752, "War");
    private static final TmdbGenreDto TV_MOVIE = new TmdbGenreDto(10770, "TV Movie");

    public static TmdbMovieDto[] populate(MovieDao movieDao, GenreDao genreDao, EntityManagerFactory emf) {

        // Populate with all genres
        Set<TmdbGenreDto> tmdbGenreDtos = Set.of(ADVENTURE, FANTASY, ANIMATION, DRAMA, HORROR, ACTION, COMEDY, HISTORY, WESTERN,
                THRILLER, CRIME, DOCUMENTARY, SCI_FI, MYSTERY, MUSIC, ROMANCE, FAMILY, WAR, TV_MOVIE);
        tmdbGenreDtos.forEach(genreDao::update);


        // Populate with movie "Jurassic Park"
        TmdbMovieDto m1 = new TmdbMovieDto(329, "Jurassic Park", "Jurassic Park", false,
                false, "en", 7.96, 17095, LocalDate.of(1993, 6, 11),
                "/jzt9HuhIAdH9qp0K2MA1m5V8sgq.jpg", "/63viWuPfYQjRYLSZSZNq7dglJP5.jpg",
                List.of(ADVENTURE, SCI_FI), "A wealthy entrepreneur secretly creates...",
                null, 127, "An adventure 65 million years in the making.", "Released", null);
        movieDao.create(new Movie(m1));

        // Populate with movie "In the Middle of the Night"
        TmdbMovieDto m2 = new TmdbMovieDto(33416, "In the Middle of the Night", "Midt om natten", false,
                false, "da", 6.538, 26, LocalDate.of(1984, 3, 9),
                "/ljx2Ds6VIiwtCImKJGk4ytAGPpA.jpg", "/739gDLbIA4SgEESTd3toHkftJOu.jpg",
                List.of(DRAMA, COMEDY), "Benny and Arnold are homeless...",
                null, 131, "", "Released", null);
        movieDao.create(new Movie(m2));


        return new TmdbMovieDto[]{m1, m2};

    }


}
