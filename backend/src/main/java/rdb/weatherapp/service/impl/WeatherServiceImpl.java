package rdb.weatherapp.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rdb.weatherapp.dto.RainyCityDto;
import rdb.weatherapp.dto.StableDayDto;
import rdb.weatherapp.dto.TempDiffCityDto;
import rdb.weatherapp.model.City;
import rdb.weatherapp.model.WeatherInfo;
import rdb.weatherapp.model.WeatherMeasurementDocument;
import rdb.weatherapp.model.WeatherMeasurementDocument.WeatherCondition;
import rdb.weatherapp.model.WeatherRecord;
import rdb.weatherapp.repository.*;
import rdb.weatherapp.service.WeatherService;
import rdb.weatherapp.util.WeatherMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

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
    private final WeatherMeasurementMongoRepository mongoRepo;

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


    @Override
    public List<RainyCityDto> findCitiesWithRainIntensity(float intensity, LocalDate from, LocalDate to) {
        LocalDateTime fromDt = from.atStartOfDay();
        LocalDateTime toDt = to.atTime(23, 59, 59);

        List<WeatherMeasurementDocument> docs = mongoRepo.findByRain1hGreaterThanAndTimestampBetween(intensity, fromDt, toDt);

        return docs.stream().collect(Collectors.groupingBy(doc -> doc.getCityName() + doc.getLat() + doc.getLon())).values().stream().map(list -> {
            WeatherMeasurementDocument doc = list.getFirst();
            float maxRain = list.stream().map(WeatherMeasurementDocument::getRain1h).filter(Objects::nonNull).max(Float::compare).orElse(0f);
            return new RainyCityDto(doc.getCityName(), doc.getLat(), doc.getLon(), maxRain);
        }).toList();
    }

    @Override
    public List<StableDayDto> findStableWeatherDays(String city, String weatherType, LocalDate from, LocalDate to) {
        LocalDateTime fromDt = from.atStartOfDay();
        LocalDateTime toDt = to.atTime(23, 59, 59);

        List<WeatherMeasurementDocument> docs = mongoRepo.findByWeatherMainAndCityNameAndTimestampBetween(weatherType, city, fromDt, toDt);

        return docs.stream().collect(Collectors.groupingBy(doc -> doc.getTimestamp().toLocalDate())).entrySet().stream().filter(entry -> entry.getValue().stream().allMatch(doc -> doc.getWeather().stream().anyMatch(w -> w.getMain().equalsIgnoreCase(weatherType)))).map(entry -> {
            int avgClouds = (int) entry.getValue().stream().mapToInt(WeatherMeasurementDocument::getClouds).average().orElse(0);
            return new StableDayDto(city, entry.getKey(), avgClouds, entry.getValue().size(), weatherType);
        }).toList();
    }

    @Override
    public TempDiffCityDto findCityWithMaxTempDiff(LocalDate date) {
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = date.atTime(23, 59, 59);

        List<WeatherMeasurementDocument> docs = mongoRepo.findByTimestampBetween(from, to);

        return docs.stream()
                .collect(Collectors.groupingBy(WeatherMeasurementDocument::getCityName))
                .values().stream()
                .map(records -> {
            float max = records.stream().map(WeatherMeasurementDocument::getTempMax).max(Float::compare).orElse(0f);
            float min = records.stream().map(WeatherMeasurementDocument::getTempMin).min(Float::compare).orElse(0f);
            float diff = Math.abs(max - min);
            WeatherMeasurementDocument ref = records.getFirst();
            return new TempDiffCityDto(ref.getCityName(), ref.getLat(), ref.getLon(), min, max, diff);
        }).max(Comparator.comparing(TempDiffCityDto::tempDiff))
                .orElse(null);
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

    public void generateTestData(int n, String CityName) {
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

            if (!weatherRecordRepository.existsByPlaceAndTimestamp(city, timestamp)) {
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
