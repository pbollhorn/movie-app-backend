//package dat.services;
//
//import java.time.LocalDate;
//import java.util.Set;
//
//import org.junit.jupiter.api.Test;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//import dat.dto.CreditDto;
//import dat.dto.GenreDto;
//import dat.dto.TmdbMovieDto;
//import dat.enums.Gender;
//
//public class TmdbServiceTest {
//
//    private static final int MAX_REQUESTS_PER_SECOND = 30;
//    private static final long DELAY_MILLISECONDS = 1000 / MAX_REQUESTS_PER_SECOND;
//
//    @Test
//    void getGenres() {
//
//        Set<GenreDto> genreDtos = TmdbService.getGenres();
//
//        GenreDto genreDto = genreDtos.stream()
//                .filter(g -> g.id() == 99)
//                .findFirst()
//                .orElse(null);
//
//        assertEquals("Documentary", genreDto.name());
//
//    }
//
//
//    @Test
//    void getDanishMoviesFromYear() {
//
//        Set<TmdbMovieDto> movieDtos = TmdbService.getDanishMoviesFromYear(2020, DELAY_MILLISECONDS);
//        System.out.println("Number of movies in 2020: " + movieDtos.size());
//        assertEquals(145, movieDtos.size());
//
//        TmdbMovieDto movieDto = movieDtos.stream()
//                .filter(m -> m.id() == 1275299)
//                .findFirst()
//                .orElse(null);
//
//        assertEquals("Badabing og Bang - Hurra, årtiet er slut!", movieDto.title());
//        assertEquals(LocalDate.of(2020, 1, 1), movieDto.releaseDate());
//
//
//    }
//
//    @Test
//    void getCreditsForMovie() {
//
//        Set<CreditDto> creditDtos = TmdbService.getCreditsForMovie(659940);
//
//        CreditDto creditDto = creditDtos.stream()
//                .filter(c -> c.personId() == 4455)
//                .findFirst()
//                .orElse(null);
//
//        assertEquals("Ulrich Thomsen", creditDto.name());
//        assertEquals(Gender.MAN, creditDto.gender());
//
//    }
//
//
//}