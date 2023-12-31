package org.example;

import org.example.model.BackTester;
import org.example.model.ExecutionMoment;
import org.example.model.StrategyResult;
import org.example.model.Trade;
import org.example.persistence.timeScale.TimescaleGpwDividendRepository;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BackTesterTest {
    /*
    Please note that those are not real tests, but rather early stage experiments, test functions used as convenience.
     */

    @Test
    public void testDividendRunOnCompany() {
        BackTester backTester = new BackTester();
//        backTester.setTakeProfit(BigDecimal.valueOf(0.03));
//        backTester.setStopLoss(BigDecimal.valueOf(0.03));
        StrategyResult strategyResult = backTester.testDividendRunOnCompany(
                "FERRO",
                0,
                30,
                0.02,
                0.2,
                ExecutionMoment.CLOSE,
                ExecutionMoment.OPEN,
                BigDecimal.ZERO,
                BigDecimal.valueOf(999999999),
                LocalDate.of(2019, 1, 1),
                LocalDate.of(2020, 1, 1)
        );
        System.out.println(strategyResult.getTrades());
        System.out.println(strategyResult.getSummary());
        System.out.println(strategyResult.getAvgRoi());
        System.out.println(strategyResult.getAvgRoiYearlyAdjustedReturn());
    }

    @Test
    public void testDividendRunOnCompanies() {
        TimescaleGpwDividendRepository timescaleGpwDividendRepository = new TimescaleGpwDividendRepository();
        BackTester backTester = new BackTester();
        backTester.setDailyTurnoverLowerLimit(BigDecimal.valueOf(100000));
        backTester.setDailyTurnoverUpperLimit(BigDecimal.valueOf(2000000));
        StrategyResult strategyResult = backTester.testDividendRunOnCompanies(timescaleGpwDividendRepository.getTickers(), 10, 30, 0.02, 0.2, ExecutionMoment.CLOSE, ExecutionMoment.CLOSE, BigDecimal.ZERO, BigDecimal.valueOf(999999999), LocalDate.of(2019, 1, 1), LocalDate.now());
        System.out.println(strategyResult.getTrades());
        System.out.println(strategyResult.getSummary());
        System.out.println(strategyResult.getAvgRoi());
        System.out.println(strategyResult.getAvgRoiYearlyAdjustedReturn());

        // draw a graph of the cumulative return from trades over number of trades
        List<BigDecimal> profitList = strategyResult.getTrades().stream().map(Trade::getProfit).toList();

        // use swing to draw a graph

        List<BigDecimal> cumulativeProfits = new ArrayList<>();
        BigDecimal cumulativeProfit = BigDecimal.ZERO;
        for (BigDecimal profit : profitList) {
            cumulativeProfit = cumulativeProfit.add(profit);
            cumulativeProfits.add(cumulativeProfit);
        }

        // Use Swing to draw the graph
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Cumulative Returns Over Trades");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new GraphPanel(cumulativeProfits));
            frame.pack();
            frame.setVisible(true);
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