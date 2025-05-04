package rdb.weatherapp.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import rdb.weatherapp.model.WeatherMeasurementDocument;

import java.time.LocalDateTime;
import java.util.List;

public interface WeatherMeasurementMongoRepository extends MongoRepository<WeatherMeasurementDocument, String> {
    List<WeatherMeasurementDocument> findByRain1hGreaterThanAndTimestampBetween(float intensity, LocalDateTime from, LocalDateTime to);

    List<WeatherMeasurementDocument> findByWeatherMainAndCityNameAndTimestampBetween(String main, String cityName, LocalDateTime from, LocalDateTime to);

    List<WeatherMeasurementDocument> findByTimestampBetween(LocalDateTime from, LocalDateTime to);
}
