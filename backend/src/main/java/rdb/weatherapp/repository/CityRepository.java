package rdb.weatherapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rdb.weatherapp.model.City;

import java.util.Optional;

public interface CityRepository extends JpaRepository<City, Long> {
    @Query(value = """
    SELECT * FROM city
    WHERE CAST(lat AS CHAR) = :lat
      AND CAST(lon AS CHAR) = :lon
""", nativeQuery = true)
    Optional<City> findByLatAndLon(@Param("lat") String lat, @Param("lon") String lon);

    Optional<City> findByNameIgnoreCase(String name);
}
