package rdb.weatherapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import rdb.weatherapp.dto.*;
import rdb.weatherapp.model.WeatherRecord;
import rdb.weatherapp.service.impl.WeatherServiceImpl;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/weather")
@RequiredArgsConstructor
@Validated
public class WeatherController {

    private final WeatherServiceImpl weatherService;

    @GetMapping("/history")
    @Operation(
            summary = "Dle zadání uživatele stahovat data o aktuálním počasí, n dní do minulosti pro dané místo",
            parameters = {
                    @io.swagger.v3.oas.annotations.Parameter(
                            name = "daysBack",
                            description = "Počet dní do minulosti, pro které chcete stáhnout data."
                    ),
                    @io.swagger.v3.oas.annotations.Parameter(
                            name = "cityName",
                            description = "Název města. **Povinné, pokud nejsou zadány souřadnice.**"
                    ),
                    @io.swagger.v3.oas.annotations.Parameter(
                            name = "lat",
                            description = "Zeměpisná šířka. **Povinná, pokud není zadáno město.**"
                    ),
                    @io.swagger.v3.oas.annotations.Parameter(
                            name = "lon",
                            description = "Zeměpisná délka. **Povinná, pokud není zadáno město.**"
                    )
            }
    )
    @Valid
    public List<WeatherRecordDto> getWeatherHistory(
            @RequestParam @Max(7) @Min(1) int daysBack,
            @RequestParam(required = false, name = "lat") Float latitude,
            @RequestParam(required = false, name = "lon") Float longitude,
            @RequestParam(required = false) String cityName
    ) {
        List<WeatherRecord> records;
        if (latitude != null && longitude != null) {
            records = weatherService.getOrFetchWeather(
                    latitude, longitude, daysBack
            );
        } else if (cityName != null && !cityName.isEmpty()) {
            records = weatherService.getOrFetchWeather(
                    cityName, daysBack
            );
        } else throw new IllegalArgumentException("Either city or latitude and longitude must be provided.");

        return records.stream().map(record -> {
            var place = record.getPlace();
            var cityDto = new CityDto(place.getName(), place.getCountry(), place.getLat(), place.getLon());

            List<WeatherConditionDto> conditionDtos = record.getWeatherInfoList().stream()
                    .map(info -> new WeatherConditionDto(
                            info.getCondition().getMain(),
                            info.getCondition().getDescription(),
                            info.getCondition().getIcon()
                    ))
                    .toList();

            return new WeatherRecordDto(
                    record.getTimestamp(),
                    record.getTempMin(),
                    record.getTempMax(),
                    record.getTemp(),
                    record.getFeelsLike(),
                    record.getPressure(),
                    record.getHumidity(),
                    record.getWindSpeed(),
                    record.getWindDeg(),
                    record.getRain1h(),
                    record.getClouds(),
                    cityDto,
                    conditionDtos
            );
        }).toList();
    }

    @GetMapping("/rain")
    @Operation(
            summary = "Všechna místa kde v daný den nebo rozmezí dnů pršelo s danou intenzitou."
    )
    public ResponseEntity<List<RainyCityDto>> getCitiesWithRain(
            @RequestParam float intensity,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(weatherService.findCitiesWithRainIntensity(intensity, from, to));
    }

    @GetMapping("/stable")
    @Operation(
            summary = "Najít dny v definovaném rozmezí, kdy pro dané místo bylo stále stejné počasí zadané jako parametr a pro tyto dny z JSON získat parametry clouds a cnt"
    )
    public ResponseEntity<List<StableDayDto>> getStableWeatherDays(
            @RequestParam String city,
            @RequestParam String weatherType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(weatherService.findStableWeatherDays(city, weatherType, from, to));
    }

    @GetMapping("/diff")
    @Operation(
            summary = "Najít místo, kde v daný den byl největší rozdíl teplot"
    )
    public ResponseEntity<TempDiffCityDto> getCityWithMaxTempDiff(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(weatherService.findCityWithMaxTempDiff(date));
    }

    @GetMapping("/genTest")
    @Operation(
            summary = "Vygenerovat testovací data pro dané město",
            parameters = {
                    @io.swagger.v3.oas.annotations.Parameter(
                            name = "n",
                            description = "Počet testovacích dat, které chcete vygenerovat."
                    ),
                    @io.swagger.v3.oas.annotations.Parameter(
                            name = "city",
                            description = "Název města pro generování testovacích dat."
                    )
            }
    )
    public ResponseEntity<?> genTest(@RequestParam int n, @RequestParam String city) {
        weatherService.generateTestData(n, city);
        return ResponseEntity.status(204).build();
    }
}
