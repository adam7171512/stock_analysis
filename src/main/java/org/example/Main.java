package org.example;

import org.example.model.ExecutionMoment;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) throws IOException {

        TimescaleGpwDividendRepository timescaleGpwDividendRepository = new TimescaleGpwDividendRepository();
        FilesystemOhlcRepository filesystemOhlcRepository = new FilesystemOhlcRepository("/home/krzyszfot/Desktop/stocks/");
        BackTester backTester = new BackTester();

        List<String> companies = timescaleGpwDividendRepository.getTickers();

        // remove all wig20 companies

        List<String> wig20Companies = filesystemOhlcRepository.getCompanies("/home/krzyszfot/Desktop/stocks/wig20.txt");
        companies.removeAll(wig20Companies);

//        //remove all mwig40 companies
//
        List<String> mwig40Companies = filesystemOhlcRepository.getCompanies("/home/krzyszfot/Desktop/stocks/mwig40.txt");
//        companies.removeAll(mwig40Companies);



//        backTester.setTakeProfit(BigDecimal.valueOf(0.2));
//        backTester.setStopLoss(BigDecimal.valueOf(0.15));
        StrategyResult strategyResult = backTester.testDividendRunOnCompanies(
                companies,
                -20,
                0,
                0.0,
                2,
                ExecutionMoment.OPEN,
                ExecutionMoment.CLOSE
        );
//        backTester.getRoiMatrix(companies, -40, -20, -7, 0);
        System.out.println(strategyResult.getTrades());
        System.out.println(strategyResult.getSummary());
        System.out.println(strategyResult.getAvgRoi());
        System.out.println(strategyResult.getAvgRoiYearlyAdjustedReturn());

        // draw a graph of the cumulative return from trades over number of trades
//        List<BigDecimal> profitList = strategyResult.getTrades().stream().map(Trade::getProfit).toList();

        // use swing to draw a graph

//        List<BigDecimal> cumulativeProfits = new ArrayList<>();
//        BigDecimal cumulativeProfit = BigDecimal.ZERO;
//        for (BigDecimal profit : profitList) {
//            cumulativeProfit = cumulativeProfit.add(profit);
//            cumulativeProfits.add(cumulativeProfit);
//        }

        // Use Swing to draw the graph
        SwingUtilities.invokeLater(() -> {
//            JFrame frame = new JFrame("Cumulative Returns Over Trades");
//            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//            frame.add(new GraphPanel(cumulativeProfits));
//            frame.pack();
//            frame.setVisible(true);
        });
    }

    static class GraphPanel extends JPanel {
        private List<BigDecimal> cumulativeProfits;

        public GraphPanel(List<BigDecimal> cumulativeProfits) {
            this.cumulativeProfits = cumulativeProfits;
            setPreferredSize(new Dimension(800, 600));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            int width = getWidth();
            int height = getHeight();

            // Assume that max and min cumulative profit define our graph's vertical boundaries
            BigDecimal maxProfit = cumulativeProfits.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ONE);
            BigDecimal minProfit = cumulativeProfits.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);

            for (int i = 1; i < cumulativeProfits.size(); i++) {
                int x1 = (int) (((double) (i - 1) / cumulativeProfits.size()) * width);
                int x2 = (int) (((double) i / cumulativeProfits.size()) * width);

                int y1 = height - (int) (((cumulativeProfits.get(i - 1).doubleValue() - minProfit.doubleValue()) /
                        (maxProfit.doubleValue() - minProfit.doubleValue())) * height);
                int y2 = height - (int) (((cumulativeProfits.get(i).doubleValue() - minProfit.doubleValue()) /
                        (maxProfit.doubleValue() - minProfit.doubleValue())) * height);

                g.drawLine(x1, y1, x2, y2);
            }
        }

        }
    }
