package dat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.Set;

public record TmdbMovieDto(Integer id,
                           String title,
                           @JsonProperty("original_title")
                           String originalTitle,
                           Boolean adult,
                           @JsonProperty("original_language")
                           String originalLanguage,
                           @JsonProperty("vote_average")
                           Double voteAverage,
                           @JsonProperty("vote_count")
                           Integer voteCount,
                           @JsonProperty("release_date")
                           LocalDate releaseDate,
                           @JsonProperty("backdrop_path")
                           String backdropPath,
                           @JsonProperty("poster_path")
                           String posterPath,
                           @JsonProperty("genre_ids")
                           Set<Integer> genreIds,
                           String overview
) {
}