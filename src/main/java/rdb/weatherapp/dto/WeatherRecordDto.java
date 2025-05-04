package rdb.weatherapp.dto;

import java.time.LocalDateTime;
import java.util.List;

public record WeatherRecordDto(
        LocalDateTime timestamp,
        Float tempMin,
        Float tempMax,
        Float temp,
        Float feelsLike,
        Short pressure,
        Short humidity,
        Float windSpeed,
        Short windDeg,
        Float rain1h,
        Short clouds,
        CityDto city,
        List<WeatherConditionDto> conditions
) {}
