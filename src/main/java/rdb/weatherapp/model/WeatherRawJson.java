package rdb.weatherapp.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "weather_raw")
public class WeatherRawJson {

    @Id
    private String id;

    private String cityName;
    private Float lat;
    private Float lon;
    private LocalDateTime fetchedAt;

    private org.bson.Document json;
}
