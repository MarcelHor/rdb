package rdb.weatherapp.dto;

public record RainyCityDto(
        String name,
        double lat,
        double lon,
        float maxRain1h
) {}
