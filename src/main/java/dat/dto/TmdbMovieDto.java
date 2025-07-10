package dat.dto;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import dat.enums.Gender;
import dat.services.TmdbService;

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
                           List<GenreDto> genres,
                           String overview,
                           TmdbCreditLists credits
) {

    private record TmdbCreditLists(
            List<TmdbCreditDto> cast,
            List<TmdbCreditDto> crew) {
    }

    private record TmdbCreditDto(@JsonProperty("id")
                                 Integer personId,
                                 String name,
                                 Gender gender,
                                 Double popularity,
                                 String job,
                                 String character) {
    }

}