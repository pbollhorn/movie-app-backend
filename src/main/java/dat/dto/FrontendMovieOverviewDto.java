package dat.dto;

import dat.entities.Movie;

import java.time.LocalDate;

public record FrontendMovieOverviewDto(Integer id,
                                       String title,
                                       String originalTitle,
                                       String originalLanguage,
                                       LocalDate releaseDate,
                                       Double score,
                                       String posterPath,
                                       String[] genres,
                                       Boolean rating) {

    // Constructor which constructs from Movie entity and automatically sets "rating" to null
    public FrontendMovieOverviewDto(Movie m) {
        this(m.getId(),
                m.getTitle(),
                m.getOriginalTitle(),
                m.getOriginalLanguage(),
                m.getReleaseDate(),
                m.getRating(),
                m.getPosterPath(),
                m.getGenresAsStringArray(),
                null);
    }

    // Constructor which constructs from Movie entity and a rating value
    public FrontendMovieOverviewDto(Movie m, Boolean rating) {
        this(m.getId(),
                m.getTitle(),
                m.getOriginalTitle(),
                m.getOriginalLanguage(),
                m.getReleaseDate(),
                m.getRating(),
                m.getPosterPath(),
                m.getGenresAsStringArray(),
                rating);
    }

}