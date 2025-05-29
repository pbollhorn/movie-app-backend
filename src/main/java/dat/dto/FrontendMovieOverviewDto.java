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
                m.getGenreArray(),
                null);
    }


    // Constructor which automatically sets "likes" to null
    public FrontendMovieOverviewDto(Integer id,
                                    String title,
                                    String originalTitle,
                                    String originalLanguage,
                                    LocalDate releaseDate,
                                    Double rating,
                                    String posterPath,
                                    String[] genres) {
        this(id, title, originalTitle, originalLanguage, releaseDate, rating, posterPath, genres, null);
    }

}