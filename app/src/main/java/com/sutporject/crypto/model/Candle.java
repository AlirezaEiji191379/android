package com.sutporject.crypto.model;

import androidx.annotation.NonNull;

public class Candle {
    private String symbol;
    private double open;
    private double close;
    private double high;
    private double low;

    public Candle(String symbol, double open, double close, double high, double low) {
        this.symbol = symbol;
        this.open = open;
        this.close = close;
        this.high = high;
        this.low = low;
    }

    @NonNull
    @Override
    public String toString() {
        return new StringBuilder("").append(symbol+" ").append(open+" ").append(close+" ").append(high+" ").append(low+" ").toString();
    }
}
