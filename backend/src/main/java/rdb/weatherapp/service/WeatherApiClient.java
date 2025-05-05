package rdb.weatherapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class WeatherApiClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${openweathermap.apikey}")
    private String apiKey;

    public JsonNode fetchWeatherHistory(double lat, double lon, long start, long end) {
        String url = String.format(Locale.US, "https://history.openweathermap.org/data/2.5/history/city?lat=%f&lon=%f&type=hour&start=%d&end=%d&appid=%s&units=metric", lat, lon, start, end, apiKey);
        return restTemplate.getForObject(url, JsonNode.class);
    }

    public JsonNode fetchCoordinatesByCityName(String cityName) {
        String url = String.format("https://api.openweathermap.org/geo/1.0/direct?q=%s&limit=1&appid=%s", cityName, apiKey);
        return restTemplate.getForObject(url, JsonNode.class);
    }

    public JsonNode reverseGeocode(double lat, double lon) {
        String url = String.format(Locale.US, "https://api.openweathermap.org/geo/1.0/reverse?lat=%f&lon=%f&limit=1&appid=%s", lat, lon, apiKey);
        return restTemplate.getForObject(url, JsonNode.class);
    }
}
