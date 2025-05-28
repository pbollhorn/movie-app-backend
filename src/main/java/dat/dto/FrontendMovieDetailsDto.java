package dat.dto;

import java.time.LocalDate;
import java.util.List;

public record FrontendMovieDetailsDto(Integer id,
                                      String title,
                                      String originalTitle,
                                      String originalLanguage,
                                      LocalDate releaseDate,
                                      Double rating,
                                      String backdropPath,
                                      String overview,
                                      String[] genres) {
}
