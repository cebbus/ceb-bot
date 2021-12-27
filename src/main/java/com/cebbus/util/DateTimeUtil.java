package com.cebbus.util;

import lombok.extern.slf4j.Slf4j;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.RegularTimePeriod;
import org.ta4j.core.Bar;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
public class DateTimeUtil {

    private static final ZoneId ZONE = ZoneId.of("GMT");

    private DateTimeUtil() {
    }

    public static ZonedDateTime millisToZonedTime(long time) {
        Instant instant = Instant.ofEpochMilli(time);
        return ZonedDateTime.ofInstant(instant, ZONE);
    }

    public static RegularTimePeriod getBarPeriod(Bar bar) {
        ZonedDateTime beginTime = bar.getBeginTime();
        LocalDateTime localDateTime = beginTime.toLocalDateTime().plus(1, ChronoUnit.MILLIS);
        Timestamp timestamp = Timestamp.valueOf(localDateTime);
        return new Millisecond(timestamp);
    }
}
