package dat.dto;

public record FrontendMovieDto(Integer id,
                               String title,
                               String originalTitle,
                               Integer year,
                               Double rating,
                               String posterPath,
                               Boolean userRating) {

}