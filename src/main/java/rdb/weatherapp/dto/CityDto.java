package rdb.weatherapp.dto;

public record CityDto(
        String name,
        String country,
        Float lat,
        Float lon
) {}
