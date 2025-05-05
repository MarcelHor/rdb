package rdb.weatherapp.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rdb.weatherapp.dto.RainyCityDto;
import rdb.weatherapp.dto.StableDayDto;
import rdb.weatherapp.dto.TempDiffCityDto;
import rdb.weatherapp.model.WeatherMeasurementDocument;
import rdb.weatherapp.repository.WeatherMeasurementMongoRepository;
import rdb.weatherapp.service.AdvancedWeatherService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdvancedWeatherServiceImpl implements AdvancedWeatherService {

    private final WeatherMeasurementMongoRepository mongoRepo;

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

        return docs.stream().collect(Collectors.groupingBy(WeatherMeasurementDocument::getCityName)).values().stream().map(records -> {
            float max = records.stream().map(WeatherMeasurementDocument::getTempMax).max(Float::compare).orElse(0f);
            float min = records.stream().map(WeatherMeasurementDocument::getTempMin).min(Float::compare).orElse(0f);
            float diff = Math.abs(max - min);
            WeatherMeasurementDocument ref = records.getFirst();
            return new TempDiffCityDto(ref.getCityName(), null, ref.getLat(), ref.getLon(), min, max, diff);
        }).max(Comparator.comparing(TempDiffCityDto::tempDiff)).orElse(null);
    }
}
