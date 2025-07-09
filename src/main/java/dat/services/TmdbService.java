package dat.services;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import dat.dto.TmdbMovieDto;
import dat.dto.GenreDto;
import dat.dto.CreditDto;
import dat.utils.DataAPIReader;
import dat.utils.PropertyReader;
import dat.enums.Gender;

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

    public static Set<Integer> getMovieIds(long delayMilliseconds) {

        Set<Integer> movieIds = new HashSet<>();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JavaTimeModule());


        for (int year = 2024; year <= 2025; year++) {

            for (int page = 1; ; page++) {

                long startTime = System.currentTimeMillis();

                String json = null;
                try {
                    String url = "https://api.themoviedb.org/3/discover/movie?vote_count.gte=1000&" +
                            "include_adult=false&include_video=false&primary_release_year=" +
                            year + "&page=" + page + "&api_key=" + ApiKey;
                    json = new DataAPIReader().getDataFromClient(url);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }


                try {
                    JsonNode results = objectMapper.readTree(json).path("results");
                    results.forEach(node -> movieIds.add(node.path("id").asInt()));

                    long timeSpent = System.currentTimeMillis() - startTime;
                    long timeToSleep = Math.max(delayMilliseconds - timeSpent, 0);
                    try {
                        Thread.sleep(timeToSleep);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (results.size() < 20) {
                        break;
                    }
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    return null;
                }

            }


        }

        return movieIds;

    }

    public static TmdbMovieDto getMovieDetails(int movieId) {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JavaTimeModule());

        String json = null;
        try {
            String url = "https://api.themoviedb.org/3/movie/" + movieId + "?api_key=" + ApiKey;
            json = new DataAPIReader().getDataFromClient(url);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        TmdbMovieDto movie;

        try {
            movie = objectMapper.readValue(json, TmdbMovieDto.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }

        return movie;

    }


    public static Set<TmdbMovieDto> getDanishMoviesFromYear(int year, long delayMilliseconds) {

        Set<TmdbMovieDto> movies = new HashSet<>();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JavaTimeModule());

        for (int page = 1; ; page++) {

            long startTime = System.currentTimeMillis();

            String json = null;
            try {
                String url = "https://api.themoviedb.org/3/discover/movie?with_origin_country=DK&" +
                        "include_adult=false&include_video=false&primary_release_date.gte=" +
                        year + "-01-01&primary_release_date.lte=" + year + "-12-31&page=" + page + "&api_key=" + ApiKey;
                json = new DataAPIReader().getDataFromClient(url);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }


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


    public static List<CreditDto> getCreditsForMovie(int movieId) {

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

        List<CreditDto> credits = new LinkedList<>();
        int rankInMovie = 0;
        for (TmdbCreditDto c : response.cast) {
            credits.add(new CreditDto(c.personId(), c.name(), c.gender(), "Actor", c.character(), rankInMovie));
            rankInMovie++;
        }
        for (TmdbCreditDto c : response.crew) {
            credits.add(new CreditDto(c.personId(), c.name(), c.gender(), c.job(), null, rankInMovie));
            rankInMovie++;
        }

        return credits;

    }

    private record TmdbCreditDto(@JsonProperty("id")
                                 Integer personId,
                                 String name,
                                 Gender gender,
                                 Double popularity,
                                 String job,
                                 String character) {
    }

    private record CreditsResponseDto(
            List<TmdbCreditDto> cast,
            List<TmdbCreditDto> crew) {
    }

    private record MoviesResponseDto(Set<TmdbMovieDto> results) {
    }

    private record GenresResponseDto(Set<GenreDto> genres) {
    }


}