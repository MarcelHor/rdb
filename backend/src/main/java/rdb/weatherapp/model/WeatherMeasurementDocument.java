package rdb.weatherapp.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.TimeSeries;


import java.time.LocalDateTime;
import java.util.List;

@Data
@TimeSeries(
        collection = "weather_raw",
        timeField = "timestamp",
        expireAfter = "30d"
)
public class WeatherMeasurementDocument {
    @Id
    private String id;

    private String cityName;
    private Float lat;
    private Float lon;
    private LocalDateTime timestamp;

    private float temp;
    private float tempMin;
    private float tempMax;
    private float feelsLike;
    private int pressure;
    private int humidity;
    private float windSpeed;
    private int windDeg;
    private Float rain1h;
    private int clouds;

    private List<WeatherCondition> weather;

    @Data
    public static class WeatherCondition {
        private String main;
        private String description;
        private String icon;
    }
}
