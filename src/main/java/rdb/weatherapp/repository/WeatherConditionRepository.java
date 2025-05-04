package rdb.weatherapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rdb.weatherapp.model.WeatherCondition;

import java.util.Optional;

public interface WeatherConditionRepository extends JpaRepository<WeatherCondition, Long> {
    Optional<WeatherCondition> findByWeatherIdAndMainAndDescriptionAndIcon(Integer weatherId, String main, String description, String icon);
}
