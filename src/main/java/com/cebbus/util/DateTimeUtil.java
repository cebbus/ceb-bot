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

@Slf4j
public class DateTimeUtil {

    private static final ZoneId ZONE = ZoneId.of("GMT+3");

    private DateTimeUtil() {
    }

    public static ZonedDateTime millisToZonedTime(long time) {
        Instant instant = Instant.ofEpochMilli(time);
        return ZonedDateTime.ofInstant(instant, ZONE);
    }

    public static RegularTimePeriod getBarPeriod(Bar bar) {
        return zonedTimeToPeriod(bar.getEndTime());
    }

    private static RegularTimePeriod zonedTimeToPeriod(ZonedDateTime dateTime) {
        LocalDateTime localDateTime = dateTime.toLocalDateTime();
        Timestamp timestamp = Timestamp.valueOf(localDateTime);
        return new Millisecond(timestamp);
    }

}
