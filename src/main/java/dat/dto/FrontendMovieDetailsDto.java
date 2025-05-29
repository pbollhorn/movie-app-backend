package dat.dto;

import java.time.LocalDate;

import dat.entities.Movie;

public record FrontendMovieDetailsDto(Integer id,
                                      String title,
                                      String originalTitle,
                                      String originalLanguage,
                                      LocalDate releaseDate,
                                      Double rating,
                                      String backdropPath,
                                      String overview,
                                      String[] genres) {


    // Constructor which constructs from Movie entity
    public FrontendMovieDetailsDto(Movie m) {
        this(m.getId(),
                m.getTitle(),
                m.getOriginalTitle(),
                m.getOriginalLanguage(),
                m.getReleaseDate(),
                m.getRating(),
                m.getBackdropPath(),
                m.getOverview(),
                m.getGenreArray());
    }

}