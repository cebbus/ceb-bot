package com.cebbus.binance;

import com.binance.api.client.domain.general.SymbolInfo;
import com.cebbus.analysis.TheOracle;
import com.cebbus.analysis.strategy.BaseCebStrategy;
import com.cebbus.binance.order.TradeStatus;
import com.cebbus.dto.CandleDto;
import com.cebbus.dto.CriterionResultDto;
import com.cebbus.dto.CsIntervalAdapter;
import com.cebbus.dto.TradeRowDto;
import com.cebbus.notification.NotificationManager;
import com.cebbus.properties.Radar;
import com.cebbus.properties.Symbol;
import com.cebbus.util.ReflectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

import java.util.*;

@Slf4j
public class RadarJob implements Job {

    private static final int RADAR_LIMIT = 360;

    @Override
    public void execute(JobExecutionContext context) {
        Map<String, List<String>> symbolMap = new TreeMap<>();

        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        Radar radar = (Radar) dataMap.get("radar");
        String quote = radar.getQuote();
        CsIntervalAdapter interval = radar.getInterval();

        log.info("radar started!");

        List<Class<? extends BaseCebStrategy>> strategyList = ReflectionUtil.listStrategyClasses();

        List<SymbolInfo> symbolList = SymbolLoader.getSymbolListByQuoteAsset(quote);
        log.info("radar detected {} symbols!", symbolList.size());

        for (SymbolInfo symbolInfo : symbolList) {
            Speculator speculator = createSpeculator(symbolInfo, interval);

            List<String> inPositionStrategyList = new ArrayList<>();
            for (Class<? extends BaseCebStrategy> strategyClazz : strategyList) {
                String strategy = strategyClazz.getSimpleName();
                speculator.changeStrategy(strategy);

                TheOracle theOracle = speculator.getTheOracle();
                CandleDto lastCandle = theOracle.getLastCandle();
                Optional<TradeRowDto> lastTrade = theOracle.getLastTradeRow(true);
                List<CriterionResultDto> criterionResultList = theOracle.getCriterionResultList(true);

                boolean isInPosition = theOracle.isInPosition(speculator.isActive());
                boolean isProfitable = (double) criterionResultList.get(2).getValue() > 1;
                boolean hasGoodWinRatio = (double) criterionResultList.get(5).getValue() > 0.5;
                boolean isTrendsUp = lastTrade.isPresent() && lastCandle.getClose().doubleValue() > (lastTrade.get().getPrice().doubleValue() * 0.8);

                if (isInPosition && isProfitable && hasGoodWinRatio && isTrendsUp) {
                    inPositionStrategyList.add(strategy);
                }
            }

            if (inPositionStrategyList.size() >= 2) {
                symbolMap.put(speculator.getSymbol().getName(), inPositionStrategyList);
            }
        }

        sendNotification(symbolMap);

        log.info("radar finished!");
    }

    private Speculator createSpeculator(SymbolInfo symbolInfo, CsIntervalAdapter interval) {
        String base = symbolInfo.getBaseAsset();
        String quote = symbolInfo.getQuoteAsset();
        String junkStrategy = "JunkStrategy";

        Symbol symbol = new Symbol(-1, 0, base, quote, junkStrategy, interval, TradeStatus.INACTIVE);
        return new Speculator(symbol, RADAR_LIMIT, true);
    }

    private void sendNotification(Map<String, List<String>> symbolMap) {
        NotificationManager notificationManager = NotificationManager.getInstance();

        StringBuilder message = new StringBuilder();

        symbolMap.forEach((symbol, strategyList) -> message.append("check this symbol ")
                .append(symbol)
                .append(" - ")
                .append("strategy list ")
                .append(strategyList)
                .append(System.lineSeparator()));


        if (message.length() == 0) {
            notificationManager.send("market is fully with shit coins... nothing notable...");
        } else {
            notificationManager.send(message.toString());
        }
    }
}
