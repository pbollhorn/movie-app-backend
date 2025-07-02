package dat.dto;

import java.time.LocalDate;
import java.util.List;

import dat.entities.Movie;

public record FrontendMovieDetailsDto(Integer id,
                                      String title,
                                      String originalTitle,
                                      String originalLanguage,
                                      LocalDate releaseDate,
                                      Double score,
                                      String backdropPath,
                                      String overview,
                                      String[] genres,
                                      List<FrontendCreditDto> credits) {


    // Constructor which constructs from Movie entity and list of credits
    public FrontendMovieDetailsDto(Movie m, List<FrontendCreditDto> credits) {
        this(m.getId(),
                m.getTitle(),
                m.getOriginalTitle(),
                m.getOriginalLanguage(),
                m.getReleaseDate(),
                m.getRating(),
                m.getBackdropPath(),
                m.getOverview(),
                m.getGenresAsStringArray(),
                List.copyOf(credits)); // TODO: This should create an unmodifiable list
    }

}