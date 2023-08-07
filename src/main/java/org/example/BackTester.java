package org.example;

import org.example.model.Ohlc;
import org.example.model.Timeframe;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
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

    public StrategyResult testDividendRunOnCompanies(List<String> tickers, int sessionBuy, int sessionSell){
        StrategyResult strategyResult = new StrategyResult();

        for (String ticker : tickers) {
            try {
                strategyResult.addTrades(testDividendRunOnCompany(ticker, sessionBuy, sessionSell));
            } catch (Exception e) {
                System.out.println("Error for ticker: " + ticker);
                e.printStackTrace();
            }
        }
        return strategyResult;
    }

    public StrategyResult testDividendRunOnCompany(String ticker, int sessionBuy, int sessionSell){
        StrategyResult strategyResult = new StrategyResult();
        List<model.Dividend> dividendList = dividendRepository.getDividends(ticker);

        for (model.Dividend dividend : dividendList) {
            strategyResult.addTrades(testSingleDividendRun(ticker, dividend, sessionBuy, sessionSell));
        }
        return strategyResult;
    }

    public StrategyResult testSingleDividendRun(String ticker, model.Dividend dividend, int sessionBuy, int sessionSell){
        LocalDate exDate = dividend.getExDate();
        List<Ohlc> ohlcList = ohlcRepository.getOhlcAround(Timeframe.D1, ticker, sessionBuy, sessionSell, exDate);

        BigDecimal entryPrice = ohlcList.get(0).getClose();
        Ohlc entryOhlc = ohlcList.get(0);
        BigDecimal exitPrice = ohlcList.get(ohlcList.size()-1).getClose();
        Ohlc exitOhlc = ohlcList.get(ohlcList.size()-1);
        boolean soldBeforeExDate = false;
            for (int i = 1; i < ohlcList.size(); i++) {
                if (this.takeProfit != null && this.takeProfit.compareTo(BigDecimal.ZERO) != 0) {
                    if (ohlcList.get(i).getHigh().compareTo(entryPrice.multiply((this.takeProfit).add(BigDecimal.ONE))) >= 0) {
                        exitPrice = ohlcList.get(i).getClose();
                        exitOhlc = ohlcList.get(i);
                        break;
                    }
                }
                else if (this.stopLoss != null && this.stopLoss.compareTo(BigDecimal.ZERO) != 0){
                    if (ohlcList.get(i).getLow().compareTo(entryPrice.multiply(BigDecimal.ONE.subtract(this.stopLoss))) <= 0
                    && ohlcList.get(i).getLow().compareTo(BigDecimal.ZERO) != 0
                    ) {
                        System.out.println("Stop loss triggered" + ohlcList.get(i).getLow() + " " + entryPrice.multiply(BigDecimal.ONE.subtract(this.stopLoss)));
                        exitPrice = ohlcList.get(i).getLow();
                        exitOhlc = ohlcList.get(i);
                        break;
                    }
                }
        }
        OffsetDateTime dividendDate = OffsetDateTime.of(dividend.getExDate().atStartOfDay(), OffsetDateTime.now().getOffset());

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
}
