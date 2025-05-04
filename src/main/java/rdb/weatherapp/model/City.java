package rdb.weatherapp.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "city")
@NoArgsConstructor
@Data
public class City {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "city_id", nullable = false)
    private Integer cityId;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "country", nullable = false)
    private String country;
    @Column(name = "lat", nullable = false)
    private Float lat;
    @Column(name = "lon", nullable = false)
    private Float lon;
    private Float elevation;

    public City(Integer cityId, String name, String country, Float lat, Float lon, Float elevation) {
        this.cityId = cityId;
        this.name = name;
        this.country = country;
        this.lat = lat;
        this.lon = lon;
        this.elevation = elevation;
    }
}