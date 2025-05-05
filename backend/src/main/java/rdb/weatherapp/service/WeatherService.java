package rdb.weatherapp.service;

import rdb.weatherapp.dto.WeatherRequestDto;
import rdb.weatherapp.model.WeatherRecord;

import java.util.List;

public interface WeatherService {
    List<WeatherRecord> getOrFetchWeather(WeatherRequestDto request);
}
