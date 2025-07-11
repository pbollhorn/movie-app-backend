package dat.services;

import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import dat.dto.TmdbMovieDto;
import dat.dto.GenreDto;
import dat.utils.TmdbApiReader;

public class TmdbService {

    private static final ObjectMapper objectMapper = configureObjectMapper();

    private static ObjectMapper configureObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }


    public static Set<GenreDto> getGenres() {

        String url = "https://api.themoviedb.org/3/genre/movie/list";
        String json = new TmdbApiReader().getDataFromTmdb(url);

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

        for (int year = 1900; year <= 2025; year++) {

            for (int page = 1; ; page++) {

                long startTime = System.currentTimeMillis();

                String json = null;
                try {
                    String url = "https://api.themoviedb.org/3/discover/movie?vote_count.gte=10000&" +
                            "include_adult=false&include_video=false&primary_release_year=" + year +
                            "&page=" + page;
                    json = new TmdbApiReader().getDataFromTmdb(url);
                    System.out.println(json);
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

        String json = null;
        try {
            String url = "https://api.themoviedb.org/3/movie/" + movieId + "?append_to_response=credits";
            json = new TmdbApiReader().getDataFromTmdb(url);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        TmdbMovieDto movieDto;

        try {
            movieDto = objectMapper.readValue(json, TmdbMovieDto.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }

        return movieDto;

    }


    private record GenresResponseDto(Set<GenreDto> genres) {
    }


}