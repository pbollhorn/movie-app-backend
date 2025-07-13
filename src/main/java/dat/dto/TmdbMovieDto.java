package dat.dto;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TmdbMovieDto(Integer id,
                           String title,
                           @JsonProperty("original_title")
                           String originalTitle,
                           Boolean adult,
                           Boolean video,
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
                           List<GenreDto> genres,
                           String overview,
                           TmdbCreditsLists credits,
                           Integer runtime,
                           String tagline,
                           String status,
                           @JsonProperty("belongs_to_collection")
                           TmdbCollection collection

) {

    public record TmdbCreditsLists(
            List<TmdbCreditDto> cast,
            List<TmdbCreditDto> crew) {
    }

    public record TmdbCollection(Integer id, String name) {

    }

}