package dat.dto;

import java.util.List;

public record PersonMovieListDto(String name, List<MovieOverviewDto> movies) {
}
