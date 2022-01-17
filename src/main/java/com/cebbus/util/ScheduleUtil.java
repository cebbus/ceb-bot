package com.cebbus.util;

import com.binance.api.client.domain.market.CandlestickInterval;
import com.cebbus.analysis.Symbol;
import com.cebbus.binance.Speculator;
import com.cebbus.binance.SpeculatorJob;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.EnumMap;
import java.util.Map;
import java.util.TimeZone;

@Slf4j
public class ScheduleUtil {

    private static Scheduler scheduler;
    private static Map<CandlestickInterval, String> cronMap;

    static {
        try {
            SchedulerFactory schedulerFactory = new StdSchedulerFactory();
            scheduler = schedulerFactory.getScheduler();
            scheduler.start();

            cronMap = new EnumMap<>(CandlestickInterval.class);
            cronMap.put(CandlestickInterval.ONE_MINUTE, "0 0/1 * * * ?");
            cronMap.put(CandlestickInterval.THREE_MINUTES, "0 0/3 * * * ?");
            cronMap.put(CandlestickInterval.FIVE_MINUTES, "0 0/5 * * * ?");
            cronMap.put(CandlestickInterval.FIFTEEN_MINUTES, "0 0/15 * * * ?");
            cronMap.put(CandlestickInterval.HALF_HOURLY, "0 0/30 * * * ?");
            cronMap.put(CandlestickInterval.HOURLY, "0 0 0/1 * * ?");
            cronMap.put(CandlestickInterval.TWO_HOURLY, "0 0 0/2 * * ?");
            cronMap.put(CandlestickInterval.FOUR_HOURLY, "0 0 0/4 * * ?");
            cronMap.put(CandlestickInterval.SIX_HOURLY, "0 0 0/6 * * ?");
            cronMap.put(CandlestickInterval.EIGHT_HOURLY, "0 0 0/8 * * ?");
            cronMap.put(CandlestickInterval.TWELVE_HOURLY, "0 0 0/12 * * ?");
            cronMap.put(CandlestickInterval.DAILY, "0 0 0 * * ?");
            cronMap.put(CandlestickInterval.THREE_DAILY, "0 0 0 1/3 * ?");
            cronMap.put(CandlestickInterval.WEEKLY, "0 0 0 1/7 * ?");
            cronMap.put(CandlestickInterval.MONTHLY, "0 0 0 L * ?");
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
            System.exit(-1);
        }
    }

    private ScheduleUtil() {
    }

    public static void schedule(Speculator speculator) {
        Symbol symbol = speculator.getSymbol();
        String symbolName = symbol.getName();
        String symbolBase = symbol.getBase();
        CandlestickInterval interval = symbol.getInterval();

        JobDataMap dataMap = new JobDataMap();
        dataMap.put("speculator", speculator);

        JobDetail job = JobBuilder.newJob(SpeculatorJob.class)
                .withIdentity(symbolName, symbolBase)
                .setJobData(dataMap)
                .build();

        CronScheduleBuilder cronBuilder = CronScheduleBuilder
                .cronSchedule(cronMap.get(interval))
                .inTimeZone(TimeZone.getTimeZone(DateTimeUtil.ZONE));

        CronTrigger trigger = TriggerBuilder.newTrigger()
                .startNow()
                .withIdentity(interval.name(), symbolBase)
                .withSchedule(cronBuilder)
                .build();

        try {
            scheduler.scheduleJob(job, trigger);
            log.info(symbolName + " will be triggered at " + trigger.getNextFireTime());
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
            System.exit(-1);
        }
    }
}
