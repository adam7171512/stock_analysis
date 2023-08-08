package org.example;

import org.example.model.ExecutionMoment;
import org.example.model.Ohlc;
import org.example.model.Timeframe;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class BackTester {

    private BigDecimal entryPrice;
    private Ohlc entryOhlc;
    private BigDecimal exitPrice;
    private BigDecimal takeProfit;
    private Ohlc exitOhlc;

    private IOhlcRepository ohlcRepository;
    private IDividendRepository dividendRepository;
    private BigDecimal stopLoss;

    public BackTester(){
        this.ohlcRepository = new FilesystemOhlcRepository("/home/krzyszfot/Desktop/gpw/");
//        this.ohlcRepository = new TimescaleGpwOhlcRepository();
        this.dividendRepository = new TimescaleGpwDividendRepository();
    }

    public StrategyResult testDividendRunOnCompanies(List<String> tickers,
                                                     int sessionBuy,
                                                     int sessionSell,
                                                     double minYield,
                                                     double maxYield,
                                                     ExecutionMoment buyMoment,
                                                     ExecutionMoment sellMoment
    ){

        StrategyResult strategyResult = new StrategyResult();

        for (String ticker : tickers) {
            try {
                StrategyResult newResult = testDividendRunOnCompany(ticker, sessionBuy, sessionSell, minYield, maxYield, buyMoment, sellMoment);
                if (newResult == null){
                    continue;
                }
                strategyResult.addTrades(newResult);
            } catch (Exception e) {
                System.out.println("Error for ticker: " + ticker);
                e.printStackTrace();
            }
        }
        return strategyResult;
    }

    public StrategyResult testDividendRunOnCompanies(List<String> tickers, int sessionBuy, int sessionSell){
        return testDividendRunOnCompanies(tickers, sessionBuy, sessionSell, 0, 100, ExecutionMoment.MIDDLE, ExecutionMoment.MIDDLE);
    }

    public StrategyResult testDividendRunOnCompany(String ticker, int sessionBuy, int sessionSell){
        return testDividendRunOnCompany(ticker, sessionBuy, sessionSell, 0, 100, ExecutionMoment.CLOSE, ExecutionMoment.CLOSE);
    }

    public StrategyResult testDividendRunOnCompany(String ticker,
                                                   int sessionBuy,
                                                   int sessionSell,
                                                   double minYield,
                                                   double maxYield,
                                                   ExecutionMoment buyMoment,
                                                    ExecutionMoment sellMoment
    ){
        StrategyResult strategyResult = new StrategyResult();
        List<model.Dividend> dividendList = dividendRepository.getDividends(ticker);

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
            StrategyResult newResult = testSingleDividendRun(ticker, dividend, sessionBuy, sessionSell, buyMoment, sellMoment);
            if (newResult == null){
                continue;
            }
            strategyResult.addTrades(testSingleDividendRun(ticker, dividend, sessionBuy, sessionSell, buyMoment, sellMoment));
        }
        return strategyResult;
    }

    public StrategyResult testSingleDividendRun(
            String ticker,
            model.Dividend dividend,
            int sessionBuy,
            int sessionSell,
            ExecutionMoment buyMoment,
            ExecutionMoment sellMoment
    ){
        LocalDate exDate = dividend.getExDate();
        List<Ohlc> ohlcList = ohlcRepository.getOhlcAround(Timeframe.D1, ticker, sessionBuy, sessionSell, exDate);

        if (ohlcList.size() == 0){
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

    public float[][] getRoiMatrix(List<String> companies, int sessionBuyMin, int sessionBuyMax, int sessionSellMin, int sessionSellMax){
        float[][] roiMatrix = new float[sessionBuyMax-sessionBuyMin+1][sessionSellMax-sessionSellMin+1];
        for (int i = sessionBuyMin; i <= sessionBuyMax; i++) {
            for (int j = sessionSellMin; j <= sessionSellMax; j++) {
                StrategyResult strategyResult = testDividendRunOnCompanies(companies, i, j);
                roiMatrix[i-sessionBuyMin][j-sessionSellMin] = strategyResult.getAvgRoiYearlyAdjustedReturn().floatValue();
            }
        }


        // print the matrix

        for (int i = 0; i < roiMatrix.length; i++) {
            for (int j = 0; j < roiMatrix[i].length; j++) {
                System.out.print(roiMatrix[i][j] + " ");
            }
            System.out.println();
        }

        return roiMatrix;
    }
}
