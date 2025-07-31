package dat.dto;

import java.time.LocalDate;
import java.util.List;

import dat.entities.Movie;

public record MovieDetailsDto(Integer id,
                              String title,
                              String originalTitle,
                              String originalLanguage,
                              LocalDate releaseDate,
                              Double voteAverage,
                              Integer voteCount,
                              String backdropPath,
                              String overview,
                              Integer runtime,
                              String[] genres,
                              CollectionDto collection,
                              List<CreditDto> credits) {

    public record CollectionDto(Integer id, String name) {
    }

    // Constructor which constructs from Movie entity and list of credits
    public MovieDetailsDto(Movie m, List<CreditDto> credits) {
        this(m.getId(),
                m.getTitle(),
                m.getOriginalTitle(),
                m.getOriginalLanguage(),
                m.getReleaseDate(),
                m.getVoteAverage(),
                m.getVoteCount(),
                m.getBackdropPath(),
                m.getOverview(),
                m.getRuntime(),
                m.getGenresAsStringArray(),
                m.getCollection() != null ? new CollectionDto(m.getCollection().getId(), m.getCollection().getName()) : null,
                List.copyOf(credits)); // TODO: This should create an unmodifiable list
    }

}