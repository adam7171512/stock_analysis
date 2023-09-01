package org.example.model;

import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.example.graphs.PlotData;
import org.example.persistence.IDividendRepository;
import org.example.persistence.IOhlcRepository;
import org.example.persistence.fileSystem.FilesystemDividendRepository;
import org.example.persistence.fileSystem.GpwFileSystemOhlcRepository;
import org.example.persistence.timeScale.TimescaleGpwDividendRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

public class BackTester {

    private BigDecimal entryPrice;
    private Ohlc entryOhlc;
    private BigDecimal exitPrice;
    private BigDecimal takeProfit;
    private Ohlc exitOhlc;

    private IOhlcRepository ohlcRepository;
    private IDividendRepository dividendRepository;
    private BigDecimal stopLoss;
    private BigDecimal dailyTurnoverLowerLimit;
    private BigDecimal dailyTurnoverUpperLimit;
    private Set<String> failedTickers = new CopyOnWriteArraySet<>();

    public BackTester(){
        //Todo: inject repositories
        this.ohlcRepository = new GpwFileSystemOhlcRepository();
//        this.ohlcRepository = new TimescaleGpwOhlcRepository();
        this.dividendRepository = new FilesystemDividendRepository();
    }

    public void setDailyTurnoverLowerLimit(BigDecimal dailyTurnoverLowerLimit) {
        this.dailyTurnoverLowerLimit = dailyTurnoverLowerLimit;
    }

    public void setDailyTurnoverUpperLimit(BigDecimal upperLimit){
        this.dailyTurnoverUpperLimit = upperLimit;
    }
    public StrategyResult testDividendRunOnCompanies(List<String> tickers,
                                                     int sessionBuy,
                                                     int sessionSell,
                                                     double minYield,
                                                     double maxYield,
                                                     ExecutionMoment buyMoment,
                                                     ExecutionMoment sellMoment,
                                                     BigDecimal dailyTurnoverLowerLimit,
                                                     BigDecimal dailyTurnoverUpperLimit,
                                                     LocalDate minDate,
                                                     LocalDate maxDate
    ){

        StrategyResult strategyResult = new StrategyResult();

        for (String ticker : tickers) {

            if (failedTickers.contains(ticker)){
                continue;
            }

            try {
                StrategyResult newResult = testDividendRunOnCompany(ticker, sessionBuy, sessionSell, minYield, maxYield, buyMoment, sellMoment, dailyTurnoverLowerLimit, dailyTurnoverUpperLimit, minDate, maxDate);
                if (newResult == null){
                    continue;
                }
                strategyResult.addTrades(newResult);
            } catch (Exception e) {
                System.out.println("Error for ticker: " + ticker);
                failedTickers.add(ticker);
                e.printStackTrace();
            }
        }
        System.out.println("Run finished! : " + strategyResult);
        return strategyResult;
    }

    public StrategyResult testDividendRunOnCompanies(List<String> tickers, int sessionBuy, int sessionSell){
        return testDividendRunOnCompanies(tickers, sessionBuy, sessionSell, 0, 100, ExecutionMoment.MIDDLE, ExecutionMoment.MIDDLE, BigDecimal.ZERO, BigDecimal.valueOf(1000000000), LocalDate.of(2000, 1, 1), LocalDate.now());
    }

    public StrategyResult testDividendRunOnCompany(String ticker, int sessionBuy, int sessionSell){
        return testDividendRunOnCompany(ticker,
                sessionBuy,
                sessionSell,
                0,
                100,
                ExecutionMoment.CLOSE,
                ExecutionMoment.CLOSE,
                BigDecimal.ZERO,
                BigDecimal.valueOf(1000000000),
                LocalDate.of(2000, 1, 1),
                LocalDate.now());
    }

    public StrategyResult testDividendRunOnCompany(String ticker,
                                                   int sessionBuy,
                                                   int sessionSell,
                                                   double minYield,
                                                   double maxYield,
                                                   ExecutionMoment buyMoment,
                                                    ExecutionMoment sellMoment,
                                                   BigDecimal dailyTurnoverLowerLimit,
                                                    BigDecimal dailyTurnoverUpperLimit,
                                                   LocalDate minDate,
                                                   LocalDate maxDate
    ){
        StrategyResult strategyResult = new StrategyResult();
        List<model.Dividend> dividendList = dividendRepository.getDividends(ticker, minDate, maxDate);

        if (dividendList.size() == 0) {
            return null;
        }

        BigDecimal minY = new BigDecimal(minYield);
        BigDecimal maxY = new BigDecimal(maxYield);

        List<model.Dividend> filteredList = new ArrayList<>();

        // filter out the dividend list
        for (model.Dividend dividend : dividendList) {
            BigDecimal yield = dividend.getYield();
            if (yield.compareTo(minY) > 0 && yield.compareTo(maxY) < 0) {
                filteredList.add(dividend);
            }
        }


        for (model.Dividend dividend : filteredList) {
            StrategyResult newResult = testSingleDividendRun(ticker, dividend, sessionBuy, sessionSell, buyMoment, sellMoment, dailyTurnoverLowerLimit, dailyTurnoverUpperLimit);
            if (newResult == null){
                continue;
            }
            strategyResult.addTrades(newResult);
        }
        return strategyResult;
    }

    private boolean checkIfTurnoverWithinLimits(List<Ohlc> ohlcList, BigDecimal dailyTurnoverLowerLimit, BigDecimal dailyTurnoverUpperLimit){
        BigDecimal avgTurnover =  ohlcList.
                stream().
                map(ohlc -> ohlc.getVolume().
                        multiply(ohlc.getClose())).
                reduce(BigDecimal.ZERO, BigDecimal::add).
                divide(BigDecimal.valueOf(ohlcList.size()), RoundingMode.FLOOR);

        boolean withinLimits = true;

        if (avgTurnover.compareTo(dailyTurnoverLowerLimit) < 0){
            withinLimits= false;
        }
        if (avgTurnover.compareTo(dailyTurnoverUpperLimit) > 0){
            withinLimits = false;
        }

        return withinLimits;
    }

    public StrategyResult testSingleDividendRun(
            String ticker,
            model.Dividend dividend,
            int sessionBuy,
            int sessionSell,
            ExecutionMoment buyMoment,
            ExecutionMoment sellMoment,
            BigDecimal dailyTurnoverLowerLimit,
            BigDecimal dailyTurnoverUpperLimit
    ){
        LocalDate exDate = dividend.getExDate();
        List<Ohlc> ohlcList = ohlcRepository.getOhlcAround(Timeframe.D1, ticker, sessionBuy, sessionSell, exDate);

        if (ohlcList.size() == 0){
            return null;
        }

        if (! checkIfTurnoverWithinLimits(ohlcList, dailyTurnoverLowerLimit, dailyTurnoverUpperLimit)){
            return null;
        }


        Ohlc entryOhlc = ohlcList.get(0);
        Ohlc exitOhlc = ohlcList.get(ohlcList.size()-1);
        boolean soldBeforeExDate = false;
            for (int i = 1; i < ohlcList.size(); i++) {
                if (this.takeProfit != null && this.takeProfit.compareTo(BigDecimal.ZERO) != 0) {
                    if (ohlcList.get(i).getHigh().compareTo(entryPrice.multiply((this.takeProfit).add(BigDecimal.ONE))) >= 0) {
                        exitOhlc = ohlcList.get(i);
                        break;
                    }
                }
                else if (this.stopLoss != null && this.stopLoss.compareTo(BigDecimal.ZERO) != 0){
                    if (ohlcList.get(i).getLow().compareTo(entryPrice.multiply(BigDecimal.ONE.subtract(this.stopLoss))) <= 0
                    && ohlcList.get(i).getLow().compareTo(BigDecimal.ZERO) != 0
                    ) {
                        System.out.println("Stop loss triggered" + ohlcList.get(i).getLow() + " " + entryPrice.multiply(BigDecimal.ONE.subtract(this.stopLoss)));
                        exitOhlc = ohlcList.get(i);
                        break;
                    }
                }
        }
        OffsetDateTime dividendDate = OffsetDateTime.of(dividend.getExDate().atStartOfDay(), OffsetDateTime.now().getOffset());

        exitPrice = switch (sellMoment) {
            case OPEN -> exitOhlc.getOpen();
            case HIGH -> exitOhlc.getHigh();
            case LOW -> exitOhlc.getLow();
            case CLOSE -> exitOhlc.getClose();
            case MIDDLE -> exitOhlc.getHigh().add(exitOhlc.getLow()).divide(BigDecimal.valueOf(2), 4, RoundingMode.HALF_DOWN);
        };

        entryPrice = switch (buyMoment) {
            case OPEN -> entryOhlc.getOpen();
            case HIGH -> entryOhlc.getHigh();
            case LOW -> entryOhlc.getLow();
            case CLOSE -> entryOhlc.getClose();
            case MIDDLE -> entryOhlc.getHigh().add(entryOhlc.getLow()).divide(BigDecimal.valueOf(2), 4, RoundingMode.HALF_UP);
        };

        if (exitOhlc.getDate().isBefore(dividendDate))
        {
            soldBeforeExDate = true;
        }


        StrategyResult strategyResult = new StrategyResult();

        Trade trade = new Trade(
                entryOhlc,
                exitOhlc,
                entryPrice,
                exitPrice,
                BigDecimal.valueOf(100).divide(entryPrice, 2, BigDecimal.ROUND_HALF_UP)
        );

        if (!soldBeforeExDate && sessionBuy < 0 && sessionSell >= 0) {
            trade.setDividend(dividend);
        }

        strategyResult.addTrades(new StrategyResult(List.of(trade)));
        return strategyResult;
    }

    public void setTakeProfit(BigDecimal takeProfit) {
        this.takeProfit = takeProfit;
    }

    public void setStopLoss(BigDecimal stopLoss) {
        this.stopLoss = stopLoss;
    }

    public PlotData getPlotDataMatrix(
            List<String> companies,
            int sessionBuyMin,
            int sessionBuyMax,
            int sessionSellMin,
            int sessionSellMax,
            double yieldMin,
            double yieldMax,
            BigDecimal dailyTurnoverLowerLimit,
            BigDecimal dailyTurnoverUpperLimit,
            ExecutionMoment buyMoment,
            ExecutionMoment sellMoment,
            LocalDate minDate,
            LocalDate maxDate
    ){
        List<Integer> sampleSizes = new ArrayList<>();

        double[][] roiMatrix = new double[sessionBuyMax-sessionBuyMin+1][sessionSellMax-sessionSellMin+1];
        for (int i = sessionBuyMin; i <= sessionBuyMax; i++) {
            for (int j = sessionSellMin; j <= sessionSellMax; j++) {
                StrategyResult strategyResult = testDividendRunOnCompanies(companies, i, j, yieldMin, yieldMax, buyMoment, sellMoment, dailyTurnoverLowerLimit, dailyTurnoverUpperLimit, minDate, maxDate);
                roiMatrix[i-sessionBuyMin][j-sessionSellMin] = strategyResult.getAvgRoiYearlyAdjustedReturn().floatValue();
                sampleSizes.add(strategyResult.getTrades().size());
            }
        }

        BigDecimal avgSampleSize = sampleSizes.stream().map(BigDecimal::valueOf).reduce(BigDecimal.ZERO, BigDecimal::add).divide(BigDecimal.valueOf(sampleSizes.size()), 2, RoundingMode.HALF_UP);

        String[] rows = new String[sessionBuyMax-sessionBuyMin+1];
        String[] cols = new String[sessionSellMax-sessionSellMin+1];

        for (int i = sessionBuyMin; i <= sessionBuyMax; i++) {
            rows[i-sessionBuyMin] = String.valueOf(i);
        }

        for (int i = sessionSellMin; i <= sessionSellMax; i++) {
            cols[i-sessionSellMin] = String.valueOf(i);
        }

        String rowLabel = "Buy session relative to ex-dividend date";
        String colLabel = "Sell session relative to ex-dividend date";

        PlotData plotData = new PlotData(
               "Yield matrix" + " buy: " + buyMoment + " sell: " + sellMoment + " Min turnover: " + dailyTurnoverLowerLimit + " Max turnover: " + dailyTurnoverUpperLimit + " Min yield " + yieldMin + " Max yield " + yieldMax + " Min date " + minDate + " Max date " + maxDate + " Avg sample size " + avgSampleSize,
                roiMatrix,
                rows,
                cols,
                rowLabel,
                colLabel
        );
        return plotData;
    }
}
