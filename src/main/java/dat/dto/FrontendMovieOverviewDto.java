package dat.dto;

import java.time.LocalDate;

public record FrontendMovieOverviewDto(Integer id,
                                       String title,
                                       String originalTitle,
                                       String originalLanguage,
                                       LocalDate releaseDate,
                                       Double rating,
                                       String posterPath,
                                       Boolean likes) {

    // Constructor which automatically sets "likes" to null
    public FrontendMovieOverviewDto(Integer id,
                                    String title,
                                    String originalTitle,
                                    String originalLanguage,
                                    LocalDate releaseDate,
                                    Double rating,
                                    String posterPath) {
        this(id, title, originalTitle, originalLanguage, releaseDate, rating, posterPath, null);
    }

}