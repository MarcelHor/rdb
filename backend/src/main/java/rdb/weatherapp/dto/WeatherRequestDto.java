package rdb.weatherapp.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record WeatherRequestDto(Double latitude, Double longitude,
                                @Min(value = 1, message = "Count must be at least 1") @Max(value = 7, message = "Count must not exceed 7") int daysBack,
                                String cityName) {
}
