package rdb.weatherapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore
    private WeatherRecord weatherRecord;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "condition_id")
    private WeatherCondition condition;

    public WeatherInfo(WeatherRecord weatherRecord, WeatherCondition condition) {
        this.weatherRecord = weatherRecord;
        this.condition = condition;
    }
}

