CREATE EVENT IF NOT EXISTS delete_old_weather_data
    ON SCHEDULE EVERY 1 DAY
        STARTS CURRENT_TIMESTAMP + INTERVAL 1 HOUR
    DO
    BEGIN
        DELETE FROM weather_info
        WHERE weather_record_id IN (
            SELECT id FROM weather_record
            WHERE timestamp < NOW() - INTERVAL 30 DAY
        );

        DELETE FROM weather_record
        WHERE timestamp < NOW() - INTERVAL 30 DAY;
    END;