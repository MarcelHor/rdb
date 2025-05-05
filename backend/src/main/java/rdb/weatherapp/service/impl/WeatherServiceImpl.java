package rdb.weatherapp.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rdb.weatherapp.dto.WeatherRequestDto;
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
public class WeatherServiceImpl implements WeatherService {

    private final WeatherApiClient weatherApiClient;
    private final CityRepository cityRepository;
    private final WeatherRecordRepository weatherRecordRepository;
    private final WeatherMapper weatherMapper;
    private final WeatherMeasurementMongoRepository weatherMeasurementMongoRepository;

    @Override
    public List<WeatherRecord> getOrFetchWeather(WeatherRequestDto request) {
        float lat, lon;
        City city;

        // 1. Zjistit mesto podle jmena nebo souradnic

        // JMENO
        if (request.cityName() != null && !request.cityName().isBlank()) {
            // Nejdriv se podivat do db
            city = cityRepository.findByNameIgnoreCase(request.cityName()).orElse(null);
            if (city == null) {
                // Pokud neni v db, ziskat souradnice podle jmena
                JsonNode geo = weatherApiClient.fetchCoordinatesByCityName(request.cityName());
                if (geo.isEmpty()) throw new RuntimeException("City not found via geocoding");

                JsonNode first = geo.get(0);
                lat = (float) first.get("lat").asDouble();
                lon = (float) first.get("lon").asDouble();

                // Podle souradnic ziskat mesto
                JsonNode reverse = weatherApiClient.reverseGeocode(lat, lon);
                JsonNode rev = reverse.get(0);

                // save
                city = new City(rev.get("name").asText(), rev.get("country").asText(), lat, lon, null);
                city = cityRepository.save(city);
            } else {
                lat = city.getLat();
                lon = city.getLon();
            }
            // CORDS
        } else if (request.latitude() != null && request.longitude() != null) {
            lat = request.latitude().floatValue();
            lon = request.longitude().floatValue();

            // Najit mesto podle souradnic
            city = cityRepository.findByLatAndLon(lat, lon).orElse(null);

            if (city == null) {
                // Pokud neni v db, ziskat mesto podle souradnic
                JsonNode reverse = weatherApiClient.reverseGeocode(lat, lon);
                JsonNode rev = reverse.get(0);

                // save
                city = new City(rev.get("name").asText(), rev.get("country").asText(), lat, lon, null);
                city = cityRepository.save(city);
            }
        } else {
            throw new RuntimeException("Either city name or coordinates must be provided");
        }

        // FETCHOVANI HISTORIE
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC).minusHours(2).withMinute(0).withSecond(0).withNano(0);

        // zkontrolvat cache
        List<LocalDateTime> expectedTimestamps = new ArrayList<>();
        for (int i = 0; i < request.daysBack(); i++) {
            expectedTimestamps.add(now.minusDays(i).withHour(12).withMinute(0).withSecond(0).withNano(0));
        }
        List<WeatherRecord> cached = weatherRecordRepository.findAllByPlaceAndTimestampIn(city, expectedTimestamps);
        // nasli jsme stejny pocet zaznamu s pozadovanym casem
        if (cached.size() == expectedTimestamps.size()) {
            System.out.println("All records found in cache");
            return cached;
        }

        // vypocitat range daysback
        long endUnix = now.toEpochSecond(ZoneOffset.UTC);
        long startUnix = now.minusDays(request.daysBack() - 1).toEpochSecond(ZoneOffset.UTC);

        // 3. Fetchni JSON z OpenWeather přes lat/lon
        JsonNode weatherJson = weatherApiClient.fetchWeatherHistory(lat, lon, startUnix, endUnix);

        // 4. Projit JSON a ulozit do SQL i Mongo
        List<WeatherRecord> result = new ArrayList<>();
        for (JsonNode node : weatherJson.path("list")) {
            LocalDateTime timestamp = LocalDateTime.ofEpochSecond(node.get("dt").asLong(), 0, ZoneOffset.UTC);

            // Jen záznamy v poledne
            if (timestamp.getHour() != 12) continue;

            // Pokud už existuje záznam v db, tak ho přidat do výsledku
            if (weatherRecordRepository.existsByPlaceAndTimestamp(city, timestamp)) {
                result.add(weatherRecordRepository.findByPlaceAndTimestamp(city, timestamp));
                continue;
            }

            // Uložit do MongoDB
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

            // Uložit do SQL
            WeatherRecord record = weatherMapper.fromJson(node, city, timestamp);
            weatherRecordRepository.save(record);
            result.add(record);
        }

        return result;
    }
}
