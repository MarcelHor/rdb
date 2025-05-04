package rdb.weatherapp.util;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import rdb.weatherapp.model.City;
import rdb.weatherapp.model.WeatherCondition;
import rdb.weatherapp.model.WeatherInfo;
import rdb.weatherapp.model.WeatherRecord;
import rdb.weatherapp.repository.WeatherConditionRepository;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class WeatherMapper {

    private final WeatherConditionRepository conditionRepository;

    public WeatherRecord fromJson(JsonNode entry, City city, LocalDateTime timestamp) {
        JsonNode main = entry.get("main");
        JsonNode wind = entry.get("wind");
        JsonNode rain = entry.get("rain");
        JsonNode clouds = entry.get("clouds");
        JsonNode weatherArray = entry.get("weather");

        Float rain1h = (rain != null && rain.has("1h")) ? (float) rain.get("1h").asDouble() : null;
        Short cloudsVal = (clouds != null) ? (short) clouds.get("all").asInt() : null;

        WeatherRecord record = new WeatherRecord(city, timestamp, (float) main.get("temp_min").asDouble(), (float) main.get("temp_max").asDouble(), (float) main.get("temp").asDouble(), (float) main.get("feels_like").asDouble(), (short) main.get("pressure").asInt(), (short) main.get("humidity").asInt(), (float) wind.get("speed").asDouble(), (short) wind.get("deg").asInt(), rain1h, cloudsVal);

        if (weatherArray != null && weatherArray.isArray()) {
            for (JsonNode w : weatherArray) {
                int weatherId = w.get("id").asInt();
                String mainText = w.get("main").asText();
                String description = w.get("description").asText();
                String icon = w.get("icon").asText();

                WeatherCondition condition = conditionRepository.findByWeatherIdAndMainAndDescriptionAndIcon(weatherId, mainText, description, icon).orElseGet(() -> {
                    WeatherCondition newCondition = new WeatherCondition(weatherId, mainText, description, icon);
                    return conditionRepository.save(newCondition);
                });

                WeatherInfo info = new WeatherInfo();
                info.setWeatherRecord(record);
                info.setCondition(condition);
                record.getWeatherInfoList().add(info);
            }
        }

        return record;
    }
}
