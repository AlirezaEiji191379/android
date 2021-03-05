package com.sutporject.crypto.model;

import androidx.annotation.NonNull;

public class Crypto {
    private String name;
    private String symbol;
    private double price;
    private double changesSinceLastHour;
    private double changesSinceLastDay;
    private double changesSinceLastWeek;


    public Crypto(String name, String symbol, double price, double changesSinceLastHour, double changesSinceLastDay, double changesSinceLastWeek) {
        this.name = name;
        this.symbol = symbol;
        this.price = price;
        this.changesSinceLastHour = changesSinceLastHour;
        this.changesSinceLastDay = changesSinceLastDay;
        this.changesSinceLastWeek = changesSinceLastWeek;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getPrice() {
        return price;
    }

    public double getChangesSinceLastHour() {
        return changesSinceLastHour;
    }

    public double getChangesSinceLastDay() {
        return changesSinceLastDay;
    }

    public double getChangesSinceLastWeek() {
        return changesSinceLastWeek;
    }

    @NonNull
    @Override
    public String toString() {
        return new String(this.name +"  "+this.symbol+"  "+this.price+"  "+this.changesSinceLastHour+"  "+
                this.changesSinceLastDay +"  "+ this.changesSinceLastWeek);
    }
}
