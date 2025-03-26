package dat.dto;

import java.time.LocalDate;

public record FrontendMovieDto(Integer id,
                               String title,
                               String originalTitle,
                               LocalDate releaseDate,
                               Double rating,
                               String posterPath,
                               Boolean likes) {
}