package dat.dto;

import java.time.LocalDate;

public record FrontendMovieDetailsDto(Integer id,
                                      String title,
                                      String originalTitle,
                                      LocalDate releaseDate,
                                      Double rating,
                                      String backdropPath,
                                      String overview) {
}
