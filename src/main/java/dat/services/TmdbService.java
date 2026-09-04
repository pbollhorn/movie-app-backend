package dat.services;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.time.LocalDate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import dat.dto.TmdbMovieDto;
import dat.exceptions.ApiException;
import dat.utils.PropertyReader;

public class TmdbService {

    private static final int YEAR_OF_FIRST_MOVIE = 1874;
    private static final int MINIMUM_VOTE_COUNT = 10;

    // TMDB says that approx. 50 requests per second are allowed: https://developer.themoviedb.org/docs/rate-limiting
    // To be on the safe side, this code pauses for 1/40 of a second between requests
    // This rate-limiting also allows the backend to focus on serving requests from frontend
    private static final int MAX_REQUESTS_PER_SECOND = 40;
    private static final long PAUSE_MILLISECONDS = 1000 / MAX_REQUESTS_PER_SECOND;

    private static final String TmdbApiReadAccessToken = PropertyReader.getPropertyValue("TMDB_API_READ_ACCESS_TOKEN");
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = configureObjectMapper();

    private static ObjectMapper configureObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    public static Set<Integer> discoverMovieIds() {

        LocalDate today = LocalDate.now();

        Set<Integer> movieIds = new HashSet<>();

        for (int year = YEAR_OF_FIRST_MOVIE; year <= today.getYear(); year++) {

            for (int page = 1; ; page++) {

                String url = "https://api.themoviedb.org/3/discover/movie?&sort_by=primary_release_date.asc" +
                        "&include_adult=false&include_video=false" +
                        "&vote_count.gte=" + MINIMUM_VOTE_COUNT +
                        "&primary_release_date.lte=" + today +
                        "&primary_release_year=" + year +
                        "&page=" + page;
                String json = getDataFromTmdb(url);

                try {
                    JsonNode results = objectMapper.readTree(json).path("results");
                    results.forEach(node -> movieIds.add(node.path("id").asInt()));

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

    /**
     * Get ids of trending movies from TMDB.
     * I.e. the top 20 most popular movies released within the last year.
     * Please note: This method does not use TMDB's trending endpoint
     *
     * @return Set of movie ids
     */
    public static Set<Integer> discoverTrendingMovieIds() {

        LocalDate today = LocalDate.now();
        LocalDate oneYearAgo = LocalDate.now().minusDays(365);

        Set<Integer> trendingMovieIds = new HashSet<>();

        String url = "https://api.themoviedb.org/3/discover/movie?&sort_by=popularity.desc" +
                "&include_adult=false&include_video=false" +
                "&vote_count.gte=" + MINIMUM_VOTE_COUNT +
                "&primary_release_date.lte=" + today +
                "&primary_release_date.gte=" + oneYearAgo;
        String json = getDataFromTmdb(url);

        try {
            JsonNode results = objectMapper.readTree(json).path("results");
            results.forEach(node -> trendingMovieIds.add(node.path("id").asInt()));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }

        return trendingMovieIds;
    }

    /**
     * Get ids of trending movies from TMDB for a specific genreId.
     * I.e. the top 20 most popular movies, with that genre, released within the last year.
     * Please note: This method does not use TMDB's trending endpoint
     *
     * @return Set of movie ids
     */
    public static Set<Integer> discoverTrendingMovieIdsByGenreId(int genreId) {

        LocalDate today = LocalDate.now();
        LocalDate oneYearAgo = LocalDate.now().minusDays(365);

        Set<Integer> trendingMovieIds = new HashSet<>();

        String url = "https://api.themoviedb.org/3/discover/movie?&sort_by=popularity.desc" +
                "&include_adult=false&include_video=false" +
                "&vote_count.gte=" + MINIMUM_VOTE_COUNT +
                "&primary_release_date.lte=" + today +
                "&primary_release_date.gte=" + oneYearAgo +
                "&with_genres=" + genreId;
        String json = getDataFromTmdb(url);

        try {
            JsonNode results = objectMapper.readTree(json).path("results");
            results.forEach(node -> trendingMovieIds.add(node.path("id").asInt()));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }

        return trendingMovieIds;
    }

    public static TmdbMovieDto getMovieDetails(int movieId) {

        String url = "https://api.themoviedb.org/3/movie/" + movieId + "?append_to_response=credits";
        String json = getDataFromTmdb(url);

        TmdbMovieDto movieDto;

        try {
            movieDto = objectMapper.readValue(json, TmdbMovieDto.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }

        return movieDto;

    }


    /**
     * Performs a GET request to TMDB's API to get data.
     *
     * @param url URL for the GET request
     * @return The data returned from TMDB's API as a string
     */
    private static String getDataFromTmdb(String url) {

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(new URI(url))
                    .header("Authorization", "Bearer " + TmdbApiReadAccessToken)
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            try {
                Thread.sleep(PAUSE_MILLISECONDS);
            } catch (InterruptedException e) {
            }

            if (response.statusCode() == 200) {
                return response.body();
            } else {
                throw new ApiException(response.statusCode(), "GET request did not return 200. Status code: " + response.statusCode());
            }
        } catch (URISyntaxException | InterruptedException | IOException e) {
            throw new ApiException(0, "Encountered problem with the GET request to TMDB: " + url, e);
        }
    }

}