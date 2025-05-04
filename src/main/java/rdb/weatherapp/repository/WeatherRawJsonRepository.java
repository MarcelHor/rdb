package rdb.weatherapp.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import rdb.weatherapp.model.WeatherRawJson;

public interface WeatherRawJsonRepository extends MongoRepository<WeatherRawJson, String> {
}
