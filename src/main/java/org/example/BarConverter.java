package org.example;

import org.example.model.Ohlc;
import org.example.model.VolumePriceBar;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class BarConverter {

    public static List<VolumePriceBar> ohlcToVolumePriceBars(List<Ohlc> ohlcs, BigDecimal volumeSize) {

        LinkedList<Ohlc> copyOhlcs = new LinkedList<>(ohlcs);

        LinkedList<VolumePriceBar> volumePriceBars = new LinkedList<>();

        List<Ohlc> currentBatch = new ArrayList<>();

        BigDecimal remainingVolume = volumeSize;
        ListIterator<Ohlc> iterator = copyOhlcs.listIterator();
        Ohlc nextOhlc = null;
        while (iterator.hasNext() || nextOhlc != null) {
            if (nextOhlc == null) {
                nextOhlc = iterator.next(); // Safely get the next Ohlc if not already set
            }            // If the accumulated volume is less than the volume size, we add the ohlc to the current batch
            if (remainingVolume.compareTo(nextOhlc.getVolume()) > 0){
                currentBatch.add(nextOhlc);
                remainingVolume = remainingVolume.subtract(nextOhlc.getVolume());
                nextOhlc = null;
            } else
            // We need to split the current ohlc bar
            {
                Ohlc firstPart = new Ohlc(
                        nextOhlc.getTicker(),
                        nextOhlc.getDate(),
                        nextOhlc.getOpen(),
                        nextOhlc.getHigh(),
                        nextOhlc.getLow(),
                        nextOhlc.getClose(),
                        nextOhlc.getDifference(), // FIXME: remove this difference field
                        remainingVolume
                );
                Ohlc secondPart = new Ohlc(
                        nextOhlc.getTicker(),
                        nextOhlc.getDate(),
                        nextOhlc.getOpen(),
                        nextOhlc.getHigh(),
                        nextOhlc.getLow(),
                        nextOhlc.getClose(),
                        nextOhlc.getDifference(),
                        nextOhlc.getVolume().subtract(remainingVolume)
                );
                currentBatch.add(firstPart);
                volumePriceBars.add(createVolumePriceBar(currentBatch, volumeSize));
                currentBatch = new ArrayList<>();
                nextOhlc = secondPart;
            }
        }
        return volumePriceBars;
    }

    private static VolumePriceBar createVolumePriceBar(List<Ohlc> ohlcs, BigDecimal volumeSize){
        VolumePriceBar bar = new VolumePriceBar(
                ohlcs.stream().map(Ohlc::getVolume).reduce(BigDecimal.ZERO, BigDecimal::add),
                ohlcs.get(0).getOpen(),
                ohlcs.get(ohlcs.size() - 1).getClose(),
                ohlcs.stream().map(Ohlc::getLow).min(BigDecimal::compareTo).get(),
                ohlcs.stream().map(Ohlc::getHigh).max(BigDecimal::compareTo).get(),
                ohlcs.get(0).getDate(),
                ohlcs.get(ohlcs.size() - 1).getDate()
        );
        if (bar.volume().compareTo(volumeSize) != 0){
            throw new RuntimeException("Volume size mismatch: " + bar.volume() + " " + volumeSize);
        }
        return bar;
    }
}
