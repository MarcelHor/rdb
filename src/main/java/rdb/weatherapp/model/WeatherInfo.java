package rdb.weatherapp.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "weather_info")
@NoArgsConstructor
@Data
public class WeatherInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "weather_record_id")
    private WeatherRecord weatherRecord;

    private Integer weatherId;
    private String main;
    private String description;
    private String icon;

    public WeatherInfo(Integer weatherId, String main, String description, String icon) {
        this.weatherId = weatherId;
        this.main = main;
        this.description = description;
        this.icon = icon;
    }
}
