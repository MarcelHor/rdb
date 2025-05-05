package rdb.weatherapp.service;

import rdb.weatherapp.dto.RainyCityDto;
import rdb.weatherapp.dto.StableDayDto;
import rdb.weatherapp.dto.TempDiffCityDto;
import rdb.weatherapp.model.WeatherRecord;

import java.time.LocalDate;
import java.util.List;

public interface WeatherService {
    List<RainyCityDto> findCitiesWithRainIntensity(float intensity, LocalDate from, LocalDate to);

    List<StableDayDto> findStableWeatherDays(String city, String weatherType, LocalDate from, LocalDate to);

    TempDiffCityDto findCityWithMaxTempDiff(LocalDate date);

    List<WeatherRecord> getOrFetchWeather(float lat, float lon, int daysBack);

    List<WeatherRecord> getOrFetchWeather(String cityName, int daysBack);
}
