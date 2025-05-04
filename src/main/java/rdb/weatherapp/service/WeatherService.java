package rdb.weatherapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rdb.weatherapp.dto.WeatherRequestDto;
import rdb.weatherapp.model.City;
import rdb.weatherapp.model.WeatherRecord;
import rdb.weatherapp.repository.CityRepository;
import rdb.weatherapp.repository.WeatherRecordRepository;
import rdb.weatherapp.util.WeatherMapper;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WeatherService {

    private final WeatherApiClient weatherApiClient;
    private final CityRepository cityRepository;
    private final WeatherRecordRepository weatherRecordRepository;
    private final WeatherMapper weatherMapper;

    public List<WeatherRecord> getOrFetchWeather(WeatherRequestDto request) {
        float lat, lon;
        City city;

        // 1. Zjistit mesto podle jmena nebo souradnic

        //JMENO
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

                //save
                city = new City(rev.get("name").asText(), rev.get("country").asText(), lat, lon, null);
                city = cityRepository.save(city);
            } else {
                lat = city.getLat();
                lon = city.getLon();
            }
        //CORDS
        } else if (request.latitude() != null && request.longitude() != null) {
            lat = request.latitude().floatValue();
            lon = request.longitude().floatValue();

            // Najit mesto podle souradnic
            city = cityRepository.findByLatAndLon(lat, lon).orElse(null);

            if (city == null) {
                // Pokud neni v db, ziskat mesto podle souradnic
                JsonNode reverse = weatherApiClient.reverseGeocode(lat, lon);
                JsonNode rev = reverse.get(0);

                //save
                city = new City(rev.get("name").asText(), rev.get("country").asText(), lat, lon, null);
                city = cityRepository.save(city);
            }
        } else {
            throw new RuntimeException("Either city name or coordinates must be provided");
        }

        //FETCHOVANI HISTORIE

        // 2. vypocitat daysBack
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC).minusHours(2).withMinute(0).withSecond(0).withNano(0);
        long endUnix = now.toEpochSecond(ZoneOffset.UTC);
        long startUnix = now.minusDays(request.daysBack() - 1).toEpochSecond(ZoneOffset.UTC);

        // 3. Fetchni JSON z OpenWeather přes lat/lon
        JsonNode weatherJson = weatherApiClient.fetchWeatherHistory(lat, lon, startUnix, endUnix);

        // 4. Projit JSON
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

            // Vytvoř nový záznam z JSONu a ulož
            WeatherRecord record = weatherMapper.fromJson(node, city, timestamp);
            weatherRecordRepository.save(record);
            result.add(record);
        }

        return result;
    }
}