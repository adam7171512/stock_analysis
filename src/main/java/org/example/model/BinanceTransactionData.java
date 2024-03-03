package org.example.model;

import java.time.OffsetDateTime;

public record BinanceTransactionData(long id, double price, double qty, double baseQty, OffsetDateTime time, boolean isBuyerMaker){
}
