CREATE INDEX idx_city_lat_lon ON city (lat, lon);
CREATE INDEX idx_city_name ON city(name);
CREATE INDEX idx_weather_record_place_timestamp ON weather_record (place_id, timestamp);