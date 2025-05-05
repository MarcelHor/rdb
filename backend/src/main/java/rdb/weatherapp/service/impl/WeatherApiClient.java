package rdb.weatherapp.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class WeatherApiClient {
    private static final String HISTORY_BASE_URL = "https://history.openweathermap.org/data/2.5/history/city";
    private static final String GEO_BASE_URL = "https://api.openweathermap.org/geo/1.0";

    private final RestTemplate restTemplate;

    @Value("${openweathermap.apikey}")
    private String apiKey;

    public JsonNode fetchWeatherHistory(double lat, double lon, long start, long end) {
        String url = String.format(Locale.US, HISTORY_BASE_URL + "?lat=%f&lon=%f&type=hour&start=%d&end=%d&appid=%s&units=metric", lat, lon, start, end, apiKey);
        return restTemplate.getForObject(url, JsonNode.class);
    }

    public JsonNode fetchCoordinatesByCityName(String cityName) {
        String url = String.format(GEO_BASE_URL + "/direct?q=%s&limit=1&appid=%s", cityName, apiKey);
        return restTemplate.getForObject(url, JsonNode.class);
    }

    public JsonNode reverseGeocode(double lat, double lon) {
        String url = String.format(Locale.US, GEO_BASE_URL + "/reverse?lat=%f&lon=%f&limit=1&appid=%s", lat, lon, apiKey);
        return restTemplate.getForObject(url, JsonNode.class);
    }
}
