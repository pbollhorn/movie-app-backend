package dat.dto;

import dat.entities.Movie;

import java.time.LocalDate;

public record MovieOverviewDto(Integer id,
                               String title,
                               String originalLanguage,
                               LocalDate releaseDate,
                               Double voteAverage,
                               String posterPath,
                               String[] directors,
                               String[] genres,
                               Boolean rating) {

    // Constructor which constructs from Movie entity and automatically sets "rating" to null
    public MovieOverviewDto(Movie m) {
        this(m.getId(),
                m.getTitle(),
                m.getOriginalLanguage(),
                m.getReleaseDate(),
                m.getVoteAverage(),
                m.getPosterPath(),
                m.getDirectorsAsStringArray(),
                m.getGenresAsStringArray(),
                null);
    }

    // Constructor which constructs from Movie entity and a rating value
    public MovieOverviewDto(Movie m, Boolean rating) {
        this(m.getId(),
                m.getTitle(),
                m.getOriginalLanguage(),
                m.getReleaseDate(),
                m.getVoteAverage(),
                m.getPosterPath(),
                m.getDirectorsAsStringArray(),
                m.getGenresAsStringArray(),
                rating);
    }

}