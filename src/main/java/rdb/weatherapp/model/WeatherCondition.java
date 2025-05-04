package rdb.weatherapp.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "weather_condition", uniqueConstraints = @UniqueConstraint(columnNames = {"weatherId", "main", "description", "icon"}))
@NoArgsConstructor
@Data
public class WeatherCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer weatherId;

    private String main;
    private String description;
    private String icon;

    public WeatherCondition(Integer weatherId, String main, String description, String icon) {
        this.weatherId = weatherId;
        this.main = main;
        this.description = description;
        this.icon = icon;
    }
}
