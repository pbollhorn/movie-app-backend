package dat.dto;

import java.time.LocalDate;
import java.util.List;

import dat.entities.Movie;

public record FrontendMovieDetailsDto(Integer id,
                                      String title,
                                      String originalTitle,
                                      String originalLanguage,
                                      LocalDate releaseDate,
                                      Double rating,
                                      String backdropPath,
                                      String overview,
                                      String[] genres,
                                      List<FrontendPersonDto> persons) {


    // Constructor which constructs from Movie entity
    public FrontendMovieDetailsDto(Movie m, List<FrontendPersonDto> persons) {
        this(m.getId(),
                m.getTitle(),
                m.getOriginalTitle(),
                m.getOriginalLanguage(),
                m.getReleaseDate(),
                m.getRating(),
                m.getBackdropPath(),
                m.getOverview(),
                m.getGenresAsStringArray(),
                List.copyOf(persons)); // TODO: This should create an unmodifiable list
    }

}