package com.cebbus.util;

import com.cebbus.binance.RadarJob;
import com.cebbus.binance.Speculator;
import com.cebbus.binance.SpeculatorJob;
import com.cebbus.dto.CsIntervalAdapter;
import com.cebbus.properties.Radar;
import com.cebbus.properties.Symbol;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

@Slf4j
public class TaskScheduler {

    private static final TaskScheduler INSTANCE = new TaskScheduler();

    private Scheduler scheduler;

    private TaskScheduler() {
        try {
            SchedulerFactory schedulerFactory = new StdSchedulerFactory();
            scheduler = schedulerFactory.getScheduler();
            scheduler.start();
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
            System.exit(-1);
        }
    }

    public static TaskScheduler getInstance() {
        return INSTANCE;
    }

    public Date scheduleSpeculator(Speculator speculator) {
        Symbol symbol = speculator.getSymbol();
        String symbolName = symbol.getName();
        String symbolBase = symbol.getBase();
        CsIntervalAdapter interval = symbol.getInterval();

        JobDataMap dataMap = new JobDataMap(Map.of("speculator", speculator));

        JobDetail job = buildJob(SpeculatorJob.class, symbolBase, symbolName, dataMap);
        CronTrigger trigger = createTrigger(interval.getCron(), symbolBase, interval.name());

        return schedule(job, trigger);
    }

    public Date scheduleRadar(Radar radar) {
        if (!radar.isActive()) {
            log.info("Radar is inactive!");
            return null;
        }

        String key = "radar";
        CsIntervalAdapter interval = radar.getInterval();
        JobDataMap dataMap = new JobDataMap(Map.of(key, radar));

        JobDetail job = buildJob(RadarJob.class, key, key.toUpperCase(), dataMap);
        CronTrigger trigger = createTrigger(interval.getRadarCron(), key, interval.name());

        return schedule(job, trigger);
    }

    private Date schedule(JobDetail job, Trigger trigger) {
        try {
            Date nextFireTime = scheduler.scheduleJob(job, trigger);
            log.info("{} will be triggered at {}", job.getKey().getName(), nextFireTime);

            return nextFireTime;
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
            System.exit(-1);
        }

        return null;
    }

    private JobDetail buildJob(Class<? extends Job> clazz, String group, String name, JobDataMap dataMap) {
        return JobBuilder.newJob(clazz)
                .withIdentity(name, group)
                .setJobData(dataMap)
                .build();
    }

    private CronTrigger createTrigger(String cron, String group, String name) {
        CronScheduleBuilder cronBuilder = CronScheduleBuilder
                .cronSchedule(cron)
                .inTimeZone(TimeZone.getTimeZone(DateTimeUtil.ZONE));

        return TriggerBuilder.newTrigger()
                .startNow()
                .withIdentity(name, group)
                .withSchedule(cronBuilder)
                .build();
    }
}