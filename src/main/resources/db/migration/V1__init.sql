CREATE TABLE place (
    id SERIAL PRIMARY KEY,
    city_id INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    lat REAL NOT NULL,
    lon REAL NOT NULL,
    elevation SMALLINT
);

CREATE TABLE weather_record (
    id SERIAL PRIMARY KEY,
    place_id INT NOT NULL REFERENCES place(id),
    timestamp TIMESTAMP NOT NULL,

    temp_min REAL,
    temp_max REAL,
    temp REAL,
    feels_like REAL,

    pressure SMALLINT,
    humidity SMALLINT,

    wind_speed REAL,
    wind_deg SMALLINT,

    rain_1h REAL,
    rain_3h REAL,
    clouds SMALLINT
);

CREATE TABLE weather_info (
    id SERIAL PRIMARY KEY,
    weather_record_id INT NOT NULL REFERENCES weather_record(id),
    weather_id INTEGER,
    main VARCHAR(50),
    description VARCHAR(255),
    icon VARCHAR(10)
);