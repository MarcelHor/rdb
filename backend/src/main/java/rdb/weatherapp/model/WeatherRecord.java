package rdb.weatherapp.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "weather_record")
@NoArgsConstructor
@Data
public class WeatherRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "place_id")
    private City place;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    private Float tempMin;
    private Float tempMax;
    private Float temp;
    private Float feelsLike;

    private Short pressure;
    private Short humidity;

    private Float windSpeed;
    private Short windDeg;

    @Column(name = "rain_1h")
    private Float rain1h;
    private Short clouds;

    @OneToMany(mappedBy = "weatherRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<WeatherInfo> weatherInfoList = new ArrayList<>();


    public WeatherRecord(City place, LocalDateTime timestamp, Float tempMin, Float tempMax, Float temp, Float feelsLike, Short pressure, Short humidity, Float windSpeed, Short windDeg, Float rain1h, Short clouds) {
        this.place = place;
        this.timestamp = timestamp;
        this.tempMin = tempMin;
        this.tempMax = tempMax;
        this.temp = temp;
        this.feelsLike = feelsLike;
        this.pressure = pressure;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.windDeg = windDeg;
        this.rain1h = rain1h;
        this.clouds = clouds;
    }
}
