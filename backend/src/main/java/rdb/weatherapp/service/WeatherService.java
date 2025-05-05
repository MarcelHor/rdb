package rdb.weatherapp.service;

import rdb.weatherapp.model.WeatherRecord;

import java.util.List;

public interface WeatherService {
    List<WeatherRecord> getOrFetchWeather(float lat, float lon, int daysBack);
    List<WeatherRecord> getOrFetchWeather(String cityName, int daysBack);
}
