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
    private static final int MINIMUM_VOTE_COUNT = 10000;

    // TMDB says that approx. 50 requests per second are allowed: https://developer.themoviedb.org/docs/rate-limiting
    // To be on the safe side, this code limits to 40 requests per second
    // This rate-limiting also allows the backend to focus on serving requests from frontend
    private static final int MAX_REQUESTS_PER_SECOND = 40;
    private static final long DELAY_MILLISECONDS = 1000 / MAX_REQUESTS_PER_SECOND;

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

    private static String getDataFromTmdb(String url) {

        long startTime = System.currentTimeMillis();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(new URI(url))
                    .header("Authorization", "Bearer " + TmdbApiReadAccessToken)
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            long timeSpent = System.currentTimeMillis() - startTime;
            long timeToSleep = Math.max(DELAY_MILLISECONDS - timeSpent, 0);
            try {
                Thread.sleep(timeToSleep);
            } catch (InterruptedException e) {
            }

            if (response.statusCode() == 200) {
                return response.body();
            } else {
                throw new ApiException(response.statusCode(), "GET request did not return 200. Status code: " + response.statusCode());
            }
        } catch (URISyntaxException | InterruptedException | IOException e) {
            throw new ApiException(0, "Encountered problem with the request to TMDB", e);
        }
    }

}