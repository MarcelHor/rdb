package rdb.weatherapp.model;

import jakarta.persistence.*;
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

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "country", nullable = false)
    private String country;

    @Column(name = "lat", nullable = false)
    private Float lat;

    @Column(name = "lon", nullable = false)
    private Float lon;

    private Float elevation;

    public City(String name, String country, Float lat, Float lon, Float elevation) {
        this.name = name;
        this.country = country;
        this.lat = lat;
        this.lon = lon;
        this.elevation = elevation;
    }
}
