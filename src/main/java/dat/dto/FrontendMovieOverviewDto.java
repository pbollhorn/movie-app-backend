package dat.dto;

import java.time.LocalDate;

public record FrontendMovieOverviewDto(Integer id,
                                       String title,
                                       String originalTitle,
                                       LocalDate releaseDate,
                                       Double rating,
                                       String posterPath,
                                       Boolean likes) {

    // Constructor which automatically sets "likes" to null
    public FrontendMovieOverviewDto(Integer id,
                                    String title,
                                    String originalTitle,
                                    LocalDate releaseDate,
                                    Double rating,
                                    String posterPath) {
        this(id, title, originalTitle, releaseDate, rating, posterPath, null);
    }

}