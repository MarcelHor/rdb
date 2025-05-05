package rdb.weatherapp.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rdb.weatherapp.model.City;
import rdb.weatherapp.model.WeatherMeasurementDocument;
import rdb.weatherapp.model.WeatherMeasurementDocument.WeatherCondition;
import rdb.weatherapp.model.WeatherRecord;
import rdb.weatherapp.repository.CityRepository;
import rdb.weatherapp.repository.WeatherMeasurementMongoRepository;
import rdb.weatherapp.repository.WeatherRecordRepository;
import rdb.weatherapp.service.WeatherService;
import rdb.weatherapp.util.WeatherMapper;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherServiceImpl implements WeatherService {

    private final WeatherApiClient weatherApiClient;
    private final CityRepository cityRepository;
    private final WeatherRecordRepository weatherRecordRepository;
    private final WeatherMapper weatherMapper;
    private final WeatherMeasurementMongoRepository weatherMeasurementMongoRepository;

    @Override
    public List<WeatherRecord> getOrFetchWeather(float lat, float lon, int daysBack) {
        var city = resolveCityByCoordinates((float) lat, (float) lon);
        return fetchAndCacheWeather(city, daysBack);
    }

    @Override
    public List<WeatherRecord> getOrFetchWeather(String cityName, int daysBack) {
        var city = resolveCityByName(cityName);
        return fetchAndCacheWeather(city, daysBack);
    }

    /**
     * mame city s danejma souradnicema? great return it. otherwise pomoci souradnic lookupni mesto a uloz do db
     */
    private City resolveCityByCoordinates(float lat, float lon) {
        var city = cityRepository.findByLatAndLon(lat, lon);

        if (city.isEmpty()) {
            JsonNode reverse = weatherApiClient.reverseGeocode(lat, lon);
            JsonNode rev = reverse.get(0);
            return cityRepository.save(new City(rev.get("name").asText(), rev.get("country").asText(), lat, lon, null));
        }
        return city.get();
    }

    /**
     * mame city se jmenem? great return it. otherwise lookupni souradnice danyho mesta a uloz do db
     */
    private City resolveCityByName(String cityName) {
        var city = cityRepository.findByNameIgnoreCase(cityName);
        if (city.isEmpty()) {
            JsonNode geo = weatherApiClient.fetchCoordinatesByCityName(cityName);
            if (geo.isEmpty()) throw new RuntimeException("City not found via geocoding");

            JsonNode first = geo.get(0);
            float lat = (float) first.get("lat").asDouble();
            float lon = (float) first.get("lon").asDouble();

            JsonNode reverse = weatherApiClient.reverseGeocode(lat, lon);
            JsonNode rev = reverse.get(0);
            return cityRepository.save(new City(rev.get("name").asText(), rev.get("country").asText(), lat, lon, null));
        }
        return city.get();
    }

    /**
     *
     */
    private List<WeatherRecord> fetchAndCacheWeather(City city, int daysBack) {
        float lat = city.getLat();
        float lon = city.getLon();

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC).minusHours(2).withMinute(0).withSecond(0).withNano(0);

        List<LocalDateTime> expectedTimestamps = new ArrayList<>();
        for (int i = 0; i < daysBack; i++) {
            expectedTimestamps.add(now.minusDays(i).withHour(12).withMinute(0).withSecond(0).withNano(0));
        }

        List<WeatherRecord> cached = weatherRecordRepository.findAllByPlaceAndTimestampIn(city, expectedTimestamps);
        if (cached.size() == expectedTimestamps.size()) {
            log.info("All records found in cache");
            return cached;
        }

        long endUnix = now.toEpochSecond(ZoneOffset.UTC);
        long startUnix = now.minusDays(daysBack - 1).toEpochSecond(ZoneOffset.UTC);

        JsonNode weatherJson = weatherApiClient.fetchWeatherHistory(lat, lon, startUnix, endUnix);

        List<WeatherRecord> result = new ArrayList<>();
        for (JsonNode node : weatherJson.path("list")) {
            LocalDateTime timestamp = LocalDateTime.ofEpochSecond(node.get("dt").asLong(), 0, ZoneOffset.UTC);
            if (timestamp.getHour() != 12) continue;

            if (weatherRecordRepository.existsByPlaceAndTimestamp(city, timestamp)) {
                result.add(weatherRecordRepository.findByPlaceAndTimestamp(city, timestamp));
                continue;
            }

            WeatherMeasurementDocument doc = new WeatherMeasurementDocument();
            doc.setCityName(city.getName());
            doc.setLat(lat);
            doc.setLon(lon);
            doc.setTimestamp(timestamp);
            doc.setTemp((float) node.path("main").path("temp").asDouble());
            doc.setTempMin((float) node.path("main").path("temp_min").asDouble());
            doc.setTempMax((float) node.path("main").path("temp_max").asDouble());
            doc.setFeelsLike((float) node.path("main").path("feels_like").asDouble());
            doc.setPressure(node.path("main").path("pressure").asInt());
            doc.setHumidity(node.path("main").path("humidity").asInt());
            doc.setWindSpeed((float) node.path("wind").path("speed").asDouble());
            doc.setWindDeg(node.path("wind").path("deg").asInt());

            if (node.has("rain")) {
                doc.setRain1h((float) node.path("rain").path("1h").asDouble(0));
            }

            doc.setClouds(node.path("clouds").path("all").asInt());

            List<WeatherCondition> conditions = new ArrayList<>();
            for (JsonNode w : node.path("weather")) {
                WeatherCondition condition = new WeatherCondition();
                condition.setMain(w.path("main").asText());
                condition.setDescription(w.path("description").asText());
                condition.setIcon(w.path("icon").asText());
                conditions.add(condition);
            }
            doc.setWeather(conditions);
            weatherMeasurementMongoRepository.save(doc);

            WeatherRecord record = weatherMapper.fromJson(node, city, timestamp);
            weatherRecordRepository.save(record);
            result.add(record);
        }

        return result;
    }
}
