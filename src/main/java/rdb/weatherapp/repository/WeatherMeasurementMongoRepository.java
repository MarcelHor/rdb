package rdb.weatherapp.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import rdb.weatherapp.model.WeatherMeasurementDocument;

import java.util.List;
import java.time.LocalDateTime;

public interface WeatherMeasurementMongoRepository extends MongoRepository<WeatherMeasurementDocument, String> {
    List<WeatherMeasurementDocument> findByRain1hGreaterThan(float intensity);
    List<WeatherMeasurementDocument> findByWeatherMainAndCityNameAndTimestampBetween(String main, String cityName, LocalDateTime from, LocalDateTime to);
}
