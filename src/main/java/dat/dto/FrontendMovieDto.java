package dat.dto;

import java.time.LocalDate;

public record FrontendMovieDto(Integer id,
                               String title,
                               String originalTitle,
                               LocalDate releaseDate,
                               Double rating,
                               String posterPath,
                               Boolean likes) {

    // Constructor which automatically sets "likes" to null
    public FrontendMovieDto(Integer id,
                            String title,
                            String originalTitle,
                            LocalDate releaseDate,
                            Double rating,
                            String posterPath) {
        this(id, title, originalTitle, releaseDate, rating, posterPath, null);
    }

}