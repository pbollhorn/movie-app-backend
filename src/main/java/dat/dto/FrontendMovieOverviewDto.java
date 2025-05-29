package dat.dto;

import dat.entities.Movie;

import java.time.LocalDate;

public record FrontendMovieOverviewDto(Integer id,
                                       String title,
                                       String originalTitle,
                                       String originalLanguage,
                                       LocalDate releaseDate,
                                       Double rating,
                                       String posterPath,
                                       String[] genres,
                                       Boolean likes) {

    // Constructor which constructs from Movie entity and automatically sets "likes" to null
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

    // Constructor which constructs from Movie entity and a likes value
    public FrontendMovieOverviewDto(Movie m, Boolean likes) {
        this(m.getId(),
                m.getTitle(),
                m.getOriginalTitle(),
                m.getOriginalLanguage(),
                m.getReleaseDate(),
                m.getRating(),
                m.getPosterPath(),
                m.getGenresAsStringArray(),
                likes);
    }

}