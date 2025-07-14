package dat.utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import dat.exceptions.ApiException;

public class TmdbApiReader {

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final String TmdbApiReadAccessToken = PropertyReader.getPropertyValue("TMDB_API_READ_ACCESS_TOKEN");

    public String getDataFromTmdb(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(new URI(url))
                    .header("Authorization", "Bearer " + TmdbApiReadAccessToken)
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return response.body();
            } else {
                throw new ApiException(response.statusCode(), "GET request failed. Status code: " + response.statusCode());
                //System.out.println("GET request failed. Status code: " + response.statusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error fetching data from API", e);
        }
    }
}