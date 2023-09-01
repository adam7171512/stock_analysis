package org.example.model;

import org.example.persistence.IDividendRepository;
import org.example.persistence.IOhlcRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public class StockPerformanceEvaluator {

    private final IOhlcRepository ohlcRepository;
    private final IDividendRepository dividendRepository;

    public StockPerformanceEvaluator(IOhlcRepository ohlcRepository, IDividendRepository dividendRepository) {
        this.ohlcRepository = ohlcRepository;
        this.dividendRepository = dividendRepository;
    }

    public BigDecimal getAnnualizedRoiForPeriod(String ticker, LocalDate from, LocalDate to) {
        BigDecimal roi = getRoiForPeriod(ticker, from, to);
        if (roi == null) {
            return null;
        }
        return getAnnualizedRoi(roi, from, to);
    }

    public BigDecimal getAvgAnnualizedRoiForPeriod(Set<String> tickers, LocalDate from, LocalDate to) {
        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;
        for (String ticker : tickers) {
            BigDecimal annualizedRoiForPeriod = getAnnualizedRoiForPeriod(ticker, from, to);
            if (annualizedRoiForPeriod != null) {
                sum = sum.add(annualizedRoiForPeriod);
                count++;
            }
        }
        System.out.println(tickers + " " + tickers.size() + " " + count);
        return sum.divide(BigDecimal.valueOf(count), 4, RoundingMode.FLOOR);
    }

    public BigDecimal getRoiForPeriod(String ticker, LocalDate from, LocalDate to){
        Ohlc fromOhlc = ohlcRepository.getClosestOhlc(Timeframe.D1, ticker, from);
        Ohlc toOhlc = ohlcRepository.getClosestOhlc(Timeframe.D1, ticker, to);

        if (fromOhlc == null || toOhlc == null) {
            return null;
        }

        BigDecimal fromPrice = fromOhlc.getClose();
        BigDecimal toPrice = toOhlc.getClose();

        // find along the way
        List< model.Dividend > dividends = dividendRepository.getDividends(ticker, from, to);
        BigDecimal dividendSum = dividends.stream().map(model.Dividend::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add).multiply(BigDecimal.ONE.subtract(FeeManager.getDividendTax()));

        BigDecimal roi = toPrice.subtract(fromPrice).add(dividendSum).divide(fromPrice, 4, RoundingMode.FLOOR);

        return roi;
    }

    // Todo: fix to compbounding
    private BigDecimal getAnnualizedRoi(BigDecimal roi, LocalDate from, LocalDate to) {
        double days = from.until(to).getDays();
        double years = from.until(to).getYears();
        years = years + days / 365;
        return roi.divide(BigDecimal.valueOf(years), 4, RoundingMode.FLOOR);
    }
}
