package com.cebbus.util;

import com.binance.api.client.domain.market.CandlestickInterval;
import com.cebbus.analysis.Symbol;
import com.cebbus.dto.CsIntervalAdapter;
import com.cebbus.binance.Speculator;
import com.cebbus.binance.SpeculatorJob;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Date;
import java.util.TimeZone;

@Slf4j
public class ScheduleUtil {

    private static Scheduler scheduler;

    static {
        try {
            SchedulerFactory schedulerFactory = new StdSchedulerFactory();
            scheduler = schedulerFactory.getScheduler();
            scheduler.start();
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
            System.exit(-1);
        }
    }

    private ScheduleUtil() {
    }

    public static Date schedule(Speculator speculator) {
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
                .cronSchedule(CsIntervalAdapter.getCron(interval))
                .inTimeZone(TimeZone.getTimeZone(DateTimeUtil.ZONE));

        CronTrigger trigger = TriggerBuilder.newTrigger()
                .startNow()
                .withIdentity(interval.name(), symbolBase)
                .withSchedule(cronBuilder)
                .build();

        try {
            Date nextFireTime = scheduler.scheduleJob(job, trigger);
            log.info(symbolName + " will be triggered at " + nextFireTime);

            return nextFireTime;
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
            System.exit(-1);
        }

        return null;
    }
}
