package rdb.weatherapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rdb.weatherapp.model.City;
import rdb.weatherapp.model.WeatherRecord;
import java.util.List;
import java.time.LocalDateTime;

public interface WeatherRecordRepository extends JpaRepository<WeatherRecord, Long> {
    boolean existsByPlaceAndTimestamp(City place, LocalDateTime timestamp);
    List<WeatherRecord> findAllByPlaceAndTimestampIn(City place, List<LocalDateTime> timestamps);
    WeatherRecord findByPlaceAndTimestamp(City place, LocalDateTime timestamp);
}
