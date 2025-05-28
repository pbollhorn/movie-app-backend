package dat.dto;

import java.time.LocalDate;

public record FrontendMovieDetailsDto(Integer id,
                                      String title,
                                      String originalTitle,
                                      String originalLanguage,
                                      LocalDate releaseDate,
                                      Double rating,
                                      String backdropPath,
                                      String overview) {
}
