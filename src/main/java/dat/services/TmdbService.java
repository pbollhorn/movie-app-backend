package dat.services;

import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import dat.dto.TmdbMovieDto;
import dat.dto.GenreDto;
import dat.dto.CreditDto;
import dat.utils.DataAPIReader;
import dat.utils.PropertyReader;

public class TmdbService {

    private static final String ApiKey = PropertyReader.getPropertyValue("TMDB_API_KEY", "config.properties");

    public static Set<GenreDto> getGenres() {

        String url = "https://api.themoviedb.org/3/genre/movie/list?api_key=" + ApiKey;
        String json = new DataAPIReader().getDataFromClient(url);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        GenresResponseDto response;

        try {
            response = objectMapper.readValue(json, GenresResponseDto.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }

        return response.genres;

    }


    public static Set<TmdbMovieDto> getDanishMoviesFromYear(int year, long delayMilliseconds) {

        Set<TmdbMovieDto> movies = new HashSet<>();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JavaTimeModule());

        for (int page = 1; ; page++) {

            long startTime = System.currentTimeMillis();

            String url = "https://api.themoviedb.org/3/discover/movie?with_original_language=da&include_adult=false&include_video=false&primary_release_date.gte=" + year + "-01-01&primary_release_date.lte=" + year + "-12-31&page=" + page + "&api_key=" + ApiKey;
            String json = new DataAPIReader().getDataFromClient(url);

            MoviesResponseDto response;

            try {
                response = objectMapper.readValue(json, MoviesResponseDto.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return null;
            }

            movies.addAll(response.results);

            if (response.results.size() < 20) {
                break;
            }

            long timeSpent = System.currentTimeMillis() - startTime;
            long timeToSleep = Math.max(delayMilliseconds - timeSpent, 0);
            try {
                Thread.sleep(timeToSleep);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        return movies;

    }


    public static Set<CreditDto> getCreditsForMovie(int movieId) {

        String url = "https://api.themoviedb.org/3/movie/" + movieId + "/credits?api_key=" + ApiKey;
        String json = new DataAPIReader().getDataFromClient(url);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        CreditsResponseDto response;

        try {
            response = objectMapper.readValue(json, CreditsResponseDto.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }

        Set<CreditDto> credits = new HashSet<>();

        for (CreditDto c : response.cast) {
            credits.add(new CreditDto(c.personId(), c.name(), c.gender(), c.popularity(), "Actor", c.character()));
        }

        credits.addAll(response.crew);

        return credits;

    }

    private record CreditsResponseDto(
            Set<CreditDto> cast,
            Set<CreditDto> crew) {
    }

    private record MoviesResponseDto(Set<TmdbMovieDto> results) {
    }

    private record GenresResponseDto(Set<GenreDto> genres) {
    }

}