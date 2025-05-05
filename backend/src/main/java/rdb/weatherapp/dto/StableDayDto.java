package rdb.weatherapp.dto;

import java.time.LocalDate;

public record StableDayDto(String cityName, LocalDate date, Integer clouds, Integer cnt, String weatherType) {
}
