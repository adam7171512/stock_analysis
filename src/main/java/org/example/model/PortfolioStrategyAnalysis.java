package org.example.model;

import model.Dividend;
import org.example.persistence.IDividendRepository;
import org.example.persistence.IOhlcRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

public class PortfolioStrategyAnalysis {

    private List<PortfolioSnapshot> portfolioCompositionChanges;
    private IDividendRepository dividendRepository;
    private IOhlcRepository ohlcRepository;
    private Map<LocalDate, Set<Dividend>> dividends;
    private Set<String> tickers;
    private Map<LocalDate, PortfolioSnapshot> portfolioSnapshots;
    private Map<LocalDate, Set<String>> repurchaseDates;
    private List<Trade> trades;
    private int rebalanceInterval;
    private LocalDate lastRebalanceDate;

//    private Set<LocalDate> tradingDays;

    private FreeCashManager freeCashManager;
    private int repurchaseInterval;
    private TimeSeriesTracker timeSeriesTracker;

    public PortfolioStrategyAnalysis(Set<String> tickers,
                                     int rebalanceInterval,
                                     int repurchaseInterval,
                                     IDividendRepository dividendRepository,
                                     IOhlcRepository ohlcRepository
                                     ) {
        this.tickers = tickers;
        this.dividendRepository = dividendRepository;
        this.ohlcRepository = ohlcRepository;
        this.trades = new ArrayList<>();
        this.portfolioSnapshots = new TreeMap<>();
        this.dividends = new HashMap<>();
        this.repurchaseDates = new HashMap<>();
        this.rebalanceInterval = rebalanceInterval;
        this.repurchaseInterval = repurchaseInterval;
        this.portfolioCompositionChanges = new ArrayList<>();
        this.freeCashManager = new FreeCashManager();
        this.timeSeriesTracker = new TimeSeriesTracker(ohlcRepository);
    }

    public PortfolioRunResult analyse(LocalDate startDate, LocalDate endDate, BigDecimal startingBalance){

        Set<LocalDate> tradingDays = getTradingDays(startDate, endDate);
        LocalDate adjustedStartDate = adjustStartDate(tradingDays, startDate);
        LocalDate adjustedEndDate = adjustEndDate(tradingDays, endDate);

        PortfolioSnapshot startingSnapShot = prepareStartingData(adjustedStartDate, adjustedEndDate, startingBalance);
        Map<LocalDate, PortfolioSnapshot> snapshotMap =
                progressSessionBySession(startingSnapShot, tradingDays);

        return new PortfolioRunResult(snapshotMap);
    }

    private Map<LocalDate, PortfolioSnapshot> progressSessionBySession(
            PortfolioSnapshot startingSnapshot,
            Set<LocalDate> tradingSessions
    ){

        if (startingSnapshot.getHoldings().isEmpty()) {
            throw new IllegalStateException("Portfolio is empty");
        }

        List<LocalDate> tradingSessionsList = new ArrayList<>(tradingSessions).stream().sorted().toList();

        Map<LocalDate, PortfolioSnapshot> portfolioSnapshots = new TreeMap<>();
        portfolioSnapshots.put(tradingSessionsList.get(0), startingSnapshot);

        PortfolioSnapshot currentSnapshot = startingSnapshot;


        for (LocalDate session: tradingSessionsList){
            currentSnapshot = nextSession(tradingSessions, session, currentSnapshot);
            portfolioSnapshots.put(session, currentSnapshot);
        }

        return portfolioSnapshots;

//        LocalDate startDate = tradingSessions.get(0);
//        LocalDate nextDayDate = startDate.plusDays(1);
//        while (nextDayDate.isBefore(endDate)) {
//            if (tradingDays.contains(nextDayDate)) {
//                currentSnapshot = nextSession(nextDayDate, currentSnapshot);
//                portfolioSnapshots.put(nextDayDate, currentSnapshot);
//            }
//            nextDayDate = nextDayDate.plusDays(1);
//        }
        }

    private PortfolioSnapshot nextSession(Set<LocalDate> tradingDays, LocalDate nextSessionDate, PortfolioSnapshot fromSnapshot){


        PortfolioSnapshot updatedSnapshot = updatePrices(nextSessionDate, fromSnapshot);
        if (this.repurchaseInterval > 0 && this.repurchaseDates.containsKey(nextSessionDate)){
            updatedSnapshot = handleRepurchases(nextSessionDate, updatedSnapshot);
        }

        if (dividends.containsKey(nextSessionDate)){
            if (dividends.containsKey(nextSessionDate))
            {
                updatedSnapshot = handleDividends(tradingDays, nextSessionDate, updatedSnapshot);
            }
        }

        long daysTillLastRebalance = Duration.between(
                lastRebalanceDate.atStartOfDay(),
                nextSessionDate.atStartOfDay()
        ).toDays();

        if (this.rebalanceInterval > 0 && daysTillLastRebalance > rebalanceInterval) {
//            System.out.println("Rebalancing portfolio for " + nextSessionDate);
            updatedSnapshot = rebalancePortfolio(nextSessionDate, updatedSnapshot);
        }

        return updatedSnapshot;
    }

    private PortfolioSnapshot handleRepurchases(LocalDate date, PortfolioSnapshot currentSnapshot){

        System.out.println("Handling repurchases for " + date);

        if (currentSnapshot.getCash().compareTo(BigDecimal.TEN) < 0){
            System.out.println("Not enough cash to repurchase");
            return currentSnapshot;
        }



        BigDecimal availableCash = this.freeCashManager.getCurrentBalance(date);
        System.out.println("Available cash: " + availableCash);

        long assetsToRepurchase = getTotalNumberOfAssetsWaitingForRepurchase();

        System.out.println("Assets to repurchase: " + assetsToRepurchase);

        if (assetsToRepurchase == 0){
            return currentSnapshot;
        }



        BigDecimal amountPerAsset = availableCash.divide(BigDecimal.valueOf(assetsToRepurchase), 2, RoundingMode.HALF_DOWN);

        System.out.println("Amount per asset: " + amountPerAsset);

        Set<Holding> currentHoldings = currentSnapshot.getHoldings();

        for (String ticker: this.repurchaseDates.get(date)){

            BigDecimal price = getCurrentPrice(ticker, date);
            BigDecimal amount = amountPerAsset.divide(price, 2, RoundingMode.HALF_UP);
            currentHoldings.add(new Holding(ticker, amount, price));
            this.freeCashManager.withdraw(amountPerAsset, date);
        }

        System.out.println("Available cash after repurchases: " + this.freeCashManager.getCurrentBalance(date));

        this.repurchaseDates.remove(date);

        PortfolioSnapshot updatedSnapshot = new PortfolioSnapshot(date, currentHoldings, this.freeCashManager.getCurrentBalance(date));
        portfolioCompositionChanges.add(updatedSnapshot);

        return updatedSnapshot;
    }

    private long getTotalNumberOfAssetsWaitingForRepurchase(){
        return this.repurchaseDates.values().stream().mapToLong(Set::size).sum();
    }

    private PortfolioSnapshot handleDividends(Set<LocalDate> tradingDays, LocalDate date, PortfolioSnapshot currentSnapshot){


//        System.out.println("Handling dividends for " + date);
//        System.out.println("Current snapshot :\n" + currentSnapshot);

        Set<Dividend> divs = this.dividends.get(date);

        Set<Holding> holdings = currentSnapshot.getHoldings();
        Set<Holding> affectedHoldings = new HashSet<>();

        BigDecimal cashFromDividends = BigDecimal.ZERO;

        for(Dividend dividend: divs){

            System.out.println("Handling dividend for " + dividend.getName());

            if (currentSnapshot.getHoldings().stream().map(Holding::ticker).noneMatch(t -> t.equals(dividend.getName()))){
                continue;
            }

            BigDecimal dividendValue = dividend.getAmount().multiply(BigDecimal.ONE.subtract(FeeManager.getDividendTax()));

            Holding affectedHolding = holdings.stream().filter(h -> h.ticker().equals(dividend.getName())).findFirst().orElseThrow();
            BigDecimal dividendProfit = affectedHolding.size().multiply(dividendValue);
            cashFromDividends = cashFromDividends.add(dividendProfit);
            affectedHoldings.add(affectedHolding);
        }

        System.out.println("Cash from dividends: " + cashFromDividends);

        BigDecimal cashFromAssetDisposal = BigDecimal.ZERO;


        LocalDate repurchaseDate = calculateRepurchaseDate(tradingDays, date, this.repurchaseInterval);
        this.repurchaseDates.put(repurchaseDate, new HashSet<>());

        if (this.repurchaseInterval > 0){
            for(Holding holding: affectedHoldings){
                BigDecimal currentAssetPrice = getCurrentPrice(holding.ticker(), date);
                BigDecimal currentAssetValue = currentAssetPrice.multiply(holding.size());
                cashFromAssetDisposal = cashFromAssetDisposal.add(currentAssetValue);
                this.repurchaseDates.get(repurchaseDate).add(holding.ticker());
                holdings.removeAll(affectedHoldings);
            }
        }

        System.out.println("Cash from asset disposal: " + cashFromAssetDisposal);

        BigDecimal cashProceedings = cashFromDividends.add(cashFromAssetDisposal);

        freeCashManager.deposit(cashProceedings, date);


        PortfolioSnapshot adjustedSnapshot = new PortfolioSnapshot(date, holdings, freeCashManager.getCurrentBalance(date));

        portfolioCompositionChanges.add(adjustedSnapshot);

//        System.out.println("Finished handling dividends for " + date);
//        System.out.println("Current snapshot :\n" + adjustedSnapshot);


        return adjustedSnapshot;
    }

    private LocalDate calculateRepurchaseDate(Set<LocalDate> tradingDays, LocalDate sellDate, int repurchaseInterval){
        LocalDate repurchaseDate = sellDate.plusDays(repurchaseInterval);
        int counter = 0;
        while (!tradingDays.contains(repurchaseDate) && counter < 5){
            repurchaseDate = repurchaseDate.plusDays(1);
            counter++;
        }

        while (!tradingDays.contains(repurchaseDate)){
            repurchaseDate = repurchaseDate.minusDays(1);
        }

        System.out.println("Calculated repurchase date: " + repurchaseDate);

        return repurchaseDate;
    }

    private PortfolioSnapshot updatePrices(LocalDate date, PortfolioSnapshot currentSnapshot){

//        System.out.println("Updating prices for " + date);

        Map<String, BigDecimal> prices = getCurrentPrices(tickers, date);

        Set<Holding> holdings = currentSnapshot.getHoldings();
        Set<Holding> updatedHoldings = new HashSet<>();

        for(Holding holding: holdings){
            BigDecimal oldAssetPrice = holding.price();
            BigDecimal currentAssetPrice = prices.get(holding.ticker());

            System.out.println(date);
            System.out.println(holding.ticker());

            // detecting splits and joins
            if (currentAssetPrice.subtract(oldAssetPrice).abs().compareTo(oldAssetPrice.multiply(BigDecimal.valueOf(0.7))) > 0){
//                System.out.println("Detected split or join for " + holding.ticker());
//                System.out.println("Old price: " + oldAssetPrice);
//                System.out.println("New price: " + currentAssetPrice);
//                System.out.println("Old size: " + holding.size());
//                System.out.println("New size: " + holding.size().multiply(oldAssetPrice).divide(currentAssetPrice, 2, RoundingMode.HALF_UP));
                holding = new Holding(holding.ticker(), holding.size().multiply(oldAssetPrice).divide(currentAssetPrice, 2, RoundingMode.HALF_UP), currentAssetPrice);
            }
            else {
                holding = new Holding(holding.ticker(), holding.size(), currentAssetPrice);
            }
            updatedHoldings.add(holding);
        }

        return new PortfolioSnapshot(date, updatedHoldings, freeCashManager.getCurrentBalance(date));
    }

    //Todo: likely here is a bug causing value degradation
    private PortfolioSnapshot rebalancePortfolio(LocalDate rebalanceDate, PortfolioSnapshot currentSnapshot){


        System.out.println("Rebalancing portfolio for " + rebalanceDate);
//        System.out.println("Current snapshot :\n" + currentSnapshot);

        Map<String, BigDecimal> prices = getCurrentPrices(tickers, rebalanceDate);
        // excluding cash
        int currentHoldings = currentSnapshot.getHoldings().size();
        BigDecimal totalAssetsValue = currentSnapshot.getCurrentValueOfAssets(prices);
        BigDecimal targetValuePerAsset = totalAssetsValue.divide(BigDecimal.valueOf(currentHoldings), RoundingMode.FLOOR);

        Set<Holding> rebalancedHoldings = new HashSet<>();

        for (Holding holding: currentSnapshot.getHoldings()){
            BigDecimal currentAssetPrice = prices.get(holding.ticker());

            //Todo: add transactions
//            BigDecimal currentAssetValue = holding.getCurrentValue(currentAssetPrice);
//            BigDecimal difference = currentAssetValue.subtract(targetValuePerAsset);
//            BigDecimal differencePercentage = difference.divide(targetValuePerAsset, 2, RoundingMode.FLOOR);
            if (currentSnapshot.getValueAtSnapshot().doubleValue() > 30000){
//                System.out.println("Difference percentage is too big: " + differencePercentage);
//                System.out.println("Current asset value: " + currentAssetValue);
//                System.out.println("Target value per asset: " + targetValuePerAsset);
//                System.out.println("Current asset price: " + currentAssetPrice);
//                System.out.println("Current asset size: " + holding.size());
//                System.out.println("Current asset ticker: " + holding.ticker());
//                System.out.println("Current snapshot: " + currentSnapshot);
//                System.out.println("Rebalance date: " + rebalanceDate);
//                System.out.println("Prices: " + prices);
//                System.out.println("Current holdings: " + currentSnapshot.getHoldings());
//                System.out.println("Target value: " + targetValuePerAsset);
//                System.out.println("Total assets value: " + totalAssetsValue);
//                System.out.println("Current holdings size: " + currentHoldings);
//                System.out.println("Current holdings value: " + currentSnapshot.getCurrentValueOfAssets(prices));
//                System.out.println("Current cash: " + freeCashManager.getCurrentBalance(rebalanceDate));
            }

            BigDecimal newPositionSize = targetValuePerAsset.divide(currentAssetPrice, RoundingMode.FLOOR);
            rebalancedHoldings.add(new Holding(holding.ticker(), newPositionSize, currentAssetPrice));
        }
        PortfolioSnapshot updatedSnapshot = new PortfolioSnapshot(rebalanceDate, rebalancedHoldings, freeCashManager.getCurrentBalance(rebalanceDate));
        portfolioCompositionChanges.add(updatedSnapshot);
        this.lastRebalanceDate = rebalanceDate;
        return updatedSnapshot;
    }


    private PortfolioSnapshot prepareStartingData(LocalDate startDate, LocalDate endDate, BigDecimal startingBalance){
        populateDividends(startDate, endDate);

        this.timeSeriesTracker.preparePriceData(tickers, startDate, endDate);
        this.tickers = this.timeSeriesTracker.verifyTickers(this.tickers);

        BigDecimal startingInvestmentPerTicker = startingBalance.divide(BigDecimal.valueOf(tickers.size()), RoundingMode.FLOOR);
        Set<Holding> holdings = new HashSet<>();

        Map<String, BigDecimal> prices = getCurrentPrices(tickers, startDate);

        for (String ticker : tickers) {
            BigDecimal tickerPrice = prices.get(ticker);
            BigDecimal numberOfShares = startingInvestmentPerTicker.divide(tickerPrice, 2,  RoundingMode.FLOOR);
            holdings.add(new Holding(ticker, numberOfShares, tickerPrice));
        }
        //Todo : move last rebalance date to portfolio snapshot
        PortfolioSnapshot startingSnapshot = new PortfolioSnapshot(startDate, holdings, BigDecimal.ZERO);
//        portfolioCompositionChanges.add(startingSnapshot);
//        portfolioSnapshots.put(startDate, startingSnapshot);
        lastRebalanceDate = startDate;
        return startingSnapshot;
    }

    private LocalDate adjustStartDate(Set<LocalDate> tradingDays, LocalDate startDate){
        while (!tradingDays.contains(startDate)){
            startDate = startDate.plusDays(1);
        }
        return startDate;
    }

    private LocalDate adjustEndDate(Set<LocalDate> tradingDays, LocalDate endDate){
        while (!tradingDays.contains(endDate)){
            endDate = endDate.minusDays(1);
        }
        return endDate;
    }

    private Set<LocalDate> getTradingDays(LocalDate startDate, LocalDate endDate){
        //Todo: change this
        return ohlcRepository.getTradingDays("PKNORLEN", startDate, endDate);
    }

    private Map<String, BigDecimal> getCurrentPrices(Set<String> tickers, LocalDate date) {

        return this.timeSeriesTracker.getCurrentPrices(tickers, date);


//        Map<String, BigDecimal> prices = new HashMap<>();
//        for (String ticker: tickers){
//            BigDecimal price = ohlcRepository.getClosestOhlc(Timeframe.D1, ticker, date).getClose();
//            prices.put(ticker, price);
//        }
//        return prices;
    }

    private BigDecimal getCurrentPrice(String ticker, LocalDate date){
        return this.timeSeriesTracker.getCurrentPrice(ticker, date);
//        return ohlcRepository.getOhlcAt(Timeframe.D1, ticker, date).getClose();
    }

    private Set<String> getVerifiedTickers(Set<String> tickers, Set<LocalDate> tradingDays){
//        Set<String> verifiedTickers = new HashSet<>(tickers);
//        for (String ticker : tickers) {
//            if (ohlcRepository.getClosestOhlc(Timeframe.D1, ticker, from) == null) {
//                verifiedTickers.remove(ticker);
//                System.err.println("No OHLC for " + ticker + " on " + from);
//            }
//            if (ohlcRepository.getClosestOhlc(Timeframe.D1, ticker, to) == null) {
//                verifiedTickers.remove(ticker);
//                System.err.println("No OHLC for " + ticker + " on " + to);
//            }
//        }
//        this.tickers = verifiedTickers;
        Set<String> verifiedTickers = new HashSet<>(tickers);
        for (LocalDate localDate: tradingDays){
            for (String ticker : tickers){
                if (this.timeSeriesTracker.getCurrentPrice(ticker, localDate) == null){
                    verifiedTickers.remove(ticker);
                    System.err.println("No OHLC for " + ticker + " on " + localDate);
                }
            }
        }
        return verifiedTickers;
    }


    private void populateDividends(LocalDate startDate, LocalDate endDate){
        for (String ticker : tickers) {
            List<Dividend> divs = dividendRepository.getDividends(ticker, startDate, endDate);
            for (Dividend div : divs) {
                if (dividends.containsKey(div.getExDate())) {
                    dividends.get(div.getExDate()).add(div);
                } else {
                    Set<Dividend> divSet = new HashSet<>();
                    divSet.add(div);
                    dividends.put(div.getExDate(), divSet);
                }
            }
        }
    }

    public List<PortfolioSnapshot> getPortfolioCompositionChanges() {
        return portfolioCompositionChanges;
    }
}
