package rdb.weatherapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rdb.weatherapp.model.City;

import java.util.Optional;

public interface CityRepository extends JpaRepository<City, Long> {
    Optional<City> findByLatAndLon(Float lat, Float lon);

    Optional<City> findByNameIgnoreCase(String name);
}
