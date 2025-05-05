package rdb.weatherapp.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rdb.weatherapp.dto.WeatherRequestDto;
import rdb.weatherapp.model.City;
import rdb.weatherapp.model.WeatherInfo;
import rdb.weatherapp.model.WeatherMeasurementDocument;
import rdb.weatherapp.model.WeatherMeasurementDocument.WeatherCondition;
import rdb.weatherapp.model.WeatherRecord;
import rdb.weatherapp.repository.*;
import rdb.weatherapp.service.WeatherService;
import rdb.weatherapp.util.WeatherMapper;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherServiceImpl implements WeatherService {

    private final WeatherApiClient weatherApiClient;
    private final CityRepository cityRepository;
    private final WeatherRecordRepository weatherRecordRepository;
    private final WeatherMapper weatherMapper;
    private final WeatherMeasurementMongoRepository weatherMeasurementMongoRepository;
    private final WeatherConditionRepository weatherConditionRepository;
    private final WeatherInfoRepository weatherInfoRepository;

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
            log.info("All records found in cache");
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

    public void generateTestData(int n, String CityName){
        Random random = new Random();
        City city = cityRepository.findByNameIgnoreCase(CityName).orElseThrow();

        for (int i = 0; i < n; i++) {
            LocalDateTime timestamp = LocalDateTime.now().minusDays(random.nextInt(30));
            float temp = random.nextFloat() * 40 - 10;
            float tempMin = temp - 3;
            float tempMax = temp + 3;
            float feelsLike = temp - 1 + random.nextFloat() * 2;
            short pressure = (short) (980 + random.nextInt(40));
            short humidity = (short) (30 + random.nextInt(70));
            float windSpeed = random.nextFloat() * 20;
            short windDeg = (short) random.nextInt(360);
            float rain1h = random.nextFloat() < 0.3 ? random.nextFloat() * 10 : 0f;
            short clouds = (short) (random.nextInt(101));

            WeatherRecord record = new WeatherRecord(
                    city,
                    timestamp,
                    tempMin,
                    tempMax,
                    temp,
                    feelsLike,
                    pressure,
                    humidity,
                    windSpeed,
                    windDeg,
                    rain1h,
                    clouds
            );

            if(!weatherRecordRepository.existsByPlaceAndTimestamp(city, timestamp)) {
                record = weatherRecordRepository.save(record);
                rdb.weatherapp.model.WeatherCondition condition = weatherConditionRepository.findByWeatherIdAndMainAndDescriptionAndIcon(800, "Clear", "clear sky", "01d")
                        .orElse(null);
                if (condition == null) {
                    condition = new rdb.weatherapp.model.WeatherCondition(800, "Clear", "clear sky", "01d");
                    condition = weatherConditionRepository.save(condition);
                }
                WeatherInfo weatherInfo = new WeatherInfo(record, condition);
                weatherInfoRepository.save(weatherInfo);

                WeatherMeasurementDocument doc = new WeatherMeasurementDocument();
                doc.setCityName(city.getName());
                doc.setLat(city.getLat());
                doc.setLon(city.getLon());
                doc.setTimestamp(timestamp);
                doc.setTemp(temp);
                doc.setTempMin(tempMin);
                doc.setTempMax(tempMax);
                doc.setFeelsLike(feelsLike);
                doc.setPressure(pressure);
                doc.setHumidity(humidity);
                doc.setWindSpeed(windSpeed);
                doc.setWindDeg(windDeg);
                doc.setRain1h(rain1h);
                doc.setClouds(clouds);
                WeatherCondition cond = new WeatherCondition();
                cond.setMain(condition.getMain());
                cond.setDescription(condition.getDescription());
                cond.setIcon(condition.getIcon());
                List<WeatherCondition> conditions = new ArrayList<>();
                conditions.add(cond);
                doc.setWeather(conditions);
                weatherMeasurementMongoRepository.save(doc);
            }
        }
    }
}
