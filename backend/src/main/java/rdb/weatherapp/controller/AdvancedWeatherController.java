package rdb.weatherapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rdb.weatherapp.dto.RainyCityDto;
import rdb.weatherapp.dto.StableDayDto;
import rdb.weatherapp.dto.TempDiffCityDto;
import rdb.weatherapp.service.impl.AdvancedWeatherServiceImpl;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/weather/advanced")
@RequiredArgsConstructor
public class AdvancedWeatherController {

    private final AdvancedWeatherServiceImpl weatherService;

    @GetMapping("/rain")
    public ResponseEntity<List<RainyCityDto>> getCitiesWithRain(
            @RequestParam float intensity,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(weatherService.findCitiesWithRainIntensity(intensity, from, to));
    }

    @GetMapping("/stable")
    public ResponseEntity<List<StableDayDto>> getStableWeatherDays(
            @RequestParam String city,
            @RequestParam String weatherType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(weatherService.findStableWeatherDays(city, weatherType, from, to));
    }

    @GetMapping("/diff")
    public ResponseEntity<TempDiffCityDto> getCityWithMaxTempDiff(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(weatherService.findCityWithMaxTempDiff(date));
    }
}
