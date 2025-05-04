package rdb.weatherapp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rdb.weatherapp.dto.WeatherRequestDto;
import rdb.weatherapp.service.WeatherService;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping("/current")
    public ResponseEntity<?> getCurrentWeather(@Valid WeatherRequestDto request) {
        boolean hasCity = request.cityName() != null && !request.cityName().isBlank();
        boolean hasCoords = request.latitude() != null && request.longitude() != null;

        if (!hasCity && !hasCoords) {
            return ResponseEntity.badRequest().body("Either city name or coordinates must be provided.");
        }
        return ResponseEntity.ok(weatherService.getOrFetchWeather(request));
    }
}
