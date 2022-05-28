package com.cebbus.util;

import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Slf4j
public class DateTimeUtil {

    public static final ZoneId ZONE = ZoneId.of("GMT");

    private DateTimeUtil() {
    }

    public static ZonedDateTime millisToZonedTime(long time) {
        return millisToZonedTime(time, ZONE);
    }

    public static ZonedDateTime millisToSystemTime(long time) {
        return millisToZonedTime(time, ZoneId.systemDefault());
    }

    public static ZonedDateTime millisToZonedTime(long time, ZoneId zoneId) {
        Instant instant = Instant.ofEpochMilli(time);
        return ZonedDateTime.ofInstant(instant, zoneId);
    }

    public static Long zonedTimeToMillis(ZonedDateTime time) {
        return time.toInstant().toEpochMilli();
    }
}
