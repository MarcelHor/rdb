package rdb.weatherapp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rdb.weatherapp.dto.WeatherRequestDto;
import rdb.weatherapp.service.impl.WeatherServiceImpl;
import rdb.weatherapp.dto.WeatherRecordDto;
import rdb.weatherapp.dto.WeatherConditionDto;
import rdb.weatherapp.model.WeatherRecord;
import java.util.List;
import rdb.weatherapp.dto.CityDto;

@RestController
@RequestMapping("/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherServiceImpl weatherService;

    @GetMapping("/history")
    public List<WeatherRecordDto> getWeatherHistory(@Valid WeatherRequestDto request) {
        List<WeatherRecord> records = weatherService.getOrFetchWeather(request);

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
}
