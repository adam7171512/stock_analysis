package org.example.persistence.timeScale;

import org.example.BarConverter;
import org.example.model.VolumePriceBar;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TimeScaleBinanceRepositoryTest {

    TimeScaleBinanceRepository timeScaleBinanceRepository = new TimeScaleBinanceRepository();

    @Test
    void getAllTransactions() {
        var timeStart = System.currentTimeMillis();
        var result = timeScaleBinanceRepository.getAllTransactions();
        var timeEnd = System.currentTimeMillis();
        assertNotNull(result);
        System.out.println("Time: " + (timeEnd - timeStart) + "ms");
        List<VolumePriceBar> bars = BarConverter.binanceTransactionsDataToVolumePriceBars(result, 100);
        bars.forEach(System.out::println);
        System.out.println(bars.size());
    }
}