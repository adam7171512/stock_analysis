package org.example.persistence;

import org.example.model.PortfolioRunResult;
import org.example.model.PortfolioSnapshot;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class PortfolioRunResultToDTOConverter {

    public PortfolioAnalysisDTO convert(PortfolioRunResult result) {
        String analysisDate = LocalDate.now().toString();
        String startingCapital = result.getStartingValue().toString();
        String totalProfit = result.getProfit().toString();
        String startDate = result.getStartDate().toString();
        String endDate = result.getEndDate().toString();
        String roiPercentage = result.getProfitPercent().toString();
        String yearlyAdjustedRoiPercentage = result.getAnnualizedProfitPercent().toString();
        String averageCashBalance = result.getAvgUnInvestedRatio().multiply(result.getStartingValue()).toString();
        List<SnapshotDTO> snapshots = result.getSnapshotList().stream()
                .map(this::toSnapshotDTO)
                .collect(Collectors.toList());

        return new PortfolioAnalysisDTO(
                analysisDate, startingCapital, totalProfit, startDate, endDate, roiPercentage,
                yearlyAdjustedRoiPercentage, averageCashBalance, snapshots
        );
    }

    private SnapshotDTO toSnapshotDTO(PortfolioSnapshot snapshot) {
        String date = snapshot.getDate().toString();
        String cash = snapshot.getCash().toString();
        String value = snapshot.getValueAtSnapshot().toString();
        List<HoldingDTO> holdings = snapshot.getHoldingsList().stream()
                .map(h -> new HoldingDTO(h.ticker(), h.size().toString(), h.getCost().toString()))
                .collect(Collectors.toList());

        return new SnapshotDTO(date, holdings, cash, value);
    }
}
