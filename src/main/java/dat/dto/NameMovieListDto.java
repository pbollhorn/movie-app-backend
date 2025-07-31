package dat.dto;

import java.util.List;

public record NameMovieListDto(String name, List<MovieOverviewDto> movies) {
}
