CREATE TABLE weather_condition
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    weather_id  INT,
    main        VARCHAR(50),
    description VARCHAR(255),
    icon        VARCHAR(10),
    UNIQUE (weather_id, main, description, icon)
);

ALTER TABLE weather_info
    ADD COLUMN condition_id BIGINT,
    ADD CONSTRAINT fk_condition FOREIGN KEY (condition_id) REFERENCES weather_condition (id),
    DROP COLUMN weather_id,
    DROP COLUMN main,
    DROP COLUMN description,
    DROP COLUMN icon;
