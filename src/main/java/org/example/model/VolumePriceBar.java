package org.example.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record VolumePriceBar(double volume, double open, double close, double low, double high, OffsetDateTime dateStart, OffsetDateTime dateEnd, double avgPrice) {
}
