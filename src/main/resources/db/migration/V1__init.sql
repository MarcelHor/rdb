CREATE TABLE city
(
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    city_id   INT          NOT NULL,
    name      VARCHAR(255) NOT NULL,
    country   VARCHAR(255) NOT NULL,
    lat       FLOAT        NOT NULL,
    lon       FLOAT        NOT NULL,
    elevation FLOAT
);

CREATE TABLE weather_record
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    place_id   BIGINT   NOT NULL,
    timestamp  DATETIME NOT NULL,

    temp_min   FLOAT,
    temp_max   FLOAT,
    temp       FLOAT,
    feels_like FLOAT,

    pressure   SMALLINT,
    humidity   SMALLINT,

    wind_speed FLOAT,
    wind_deg   SMALLINT,

    rain_1h    FLOAT,
    clouds     SMALLINT,

    FOREIGN KEY (place_id) REFERENCES city (id)
);

CREATE TABLE weather_info
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    weather_record_id BIGINT NOT NULL,
    weather_id        INT,
    main              VARCHAR(50),
    description       VARCHAR(255),
    icon              VARCHAR(10),

    FOREIGN KEY (weather_record_id) REFERENCES weather_record (id)
);
