package org.example.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record VolumePriceBar(BigDecimal volume, BigDecimal open, BigDecimal close, BigDecimal low, BigDecimal high, OffsetDateTime dateStart, OffsetDateTime dateEnd) {
}
