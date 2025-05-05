package rdb.weatherapp.dto;

public record TempDiffCityDto(
        String name,
        String country,
        double lat,
        double lon,
        float tempMin,
        float tempMax,
        float tempDiff
) {}