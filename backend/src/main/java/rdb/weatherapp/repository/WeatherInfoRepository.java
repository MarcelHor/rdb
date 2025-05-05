package rdb.weatherapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rdb.weatherapp.model.WeatherInfo;

public interface WeatherInfoRepository extends JpaRepository<WeatherInfo, Long> {
}
