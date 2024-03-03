//package org.example;
//
//import org.example.model.Ohlc;
//import org.example.model.VolumePriceBar;
//import org.junit.jupiter.api.Test;
//
//import java.math.BigDecimal;
//import java.time.OffsetDateTime;
//import java.time.ZoneOffset;
//import java.util.Arrays;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class BarConverterTest {
//
//    @Test
//    void testSingleOhlcFitsVolume() {
//        List<Ohlc> ohlcs = List.of(new Ohlc("TEST", OffsetDateTime.of(2024, 2, 27, 0, 0, 0, 0, ZoneOffset.UTC), new BigDecimal("100"), new BigDecimal("105"), new BigDecimal("95"), new BigDecimal("102"), new BigDecimal("0"), new BigDecimal("10")));
//        BigDecimal volumeSize = new BigDecimal("10");
//        List<VolumePriceBar> result = BarConverter.ohlcToVolumePriceBars(ohlcs, volumeSize);
//
//        assertEquals(1, result.size());
//        VolumePriceBar bar = result.get(0);
//        assertEquals(10D, bar.volume());
//        assertEquals(, bar.open());
//        assertEquals(new BigDecimal("102"), bar.close());
//        assertEquals(new BigDecimal("95"), bar.low());
//        assertEquals(new BigDecimal("105"), bar.high());
//    }
//
//    @Test
//    void testSingleOhlcRequiresSplitting() {
//        List<Ohlc> ohlcs = List.of(
//                new Ohlc("TEST", OffsetDateTime.of(2024, 2, 27, 0, 0, 0, 0, ZoneOffset.UTC), new BigDecimal("100"), new BigDecimal("105"), new BigDecimal("95"), new BigDecimal("102"), new BigDecimal("0"), new BigDecimal("20"))
//        );
//        BigDecimal volumeSize = new BigDecimal("10");
//        List<VolumePriceBar> result = BarConverter.ohlcToVolumePriceBars(ohlcs, volumeSize);
//        assertEquals(2, result.size());
//    }
//
//    @Test
//    void testMultipleOhlcsAggregateToFitsVolume() {
//        // Assuming a list of OHLCs that together match exactly one volume size
//    }
//
//    @Test
//    void testEmptyOhlcList() {
//        List<Ohlc> ohlcs = Arrays.asList();
//        BigDecimal volumeSize = new BigDecimal("10");
//        List<VolumePriceBar> result = BarConverter.ohlcToVolumePriceBars(ohlcs, volumeSize);
//        assertTrue(result.isEmpty());
//    }
//
//    // Add more test cases as needed to cover different scenarios
//}
