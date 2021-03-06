package com.sutporject.crypto.model;

import androidx.annotation.NonNull;

public class Crypto {
    private int id;
    private String name;
    private String symbol;
    private double price;
    private double changesSinceLastHour;
    private double changesSinceLastDay;
    private double changesSinceLastWeek;


    public Crypto(int id, String name, String symbol, double price, double changesSinceLastHour, double changesSinceLastDay, double changesSinceLastWeek) {
        this.id = id;
        this.name = name;
        this.symbol = symbol;
        this.price = price;
        this.changesSinceLastHour = changesSinceLastHour;
        this.changesSinceLastDay = changesSinceLastDay;
        this.changesSinceLastWeek = changesSinceLastWeek;
    }

    public int getId() { return this.id;}

    public String getName() {
        return this.name;
    }

    public String getSymbol() {
        return this.symbol;
    }

    public double getPrice() {
        return this.price;
    }

    public double getChangesSinceLastHour() {
        return this.changesSinceLastHour;
    }

    public double getChangesSinceLastDay() {
        return this.changesSinceLastDay;
    }

    public double getChangesSinceLastWeek() {
        return this.changesSinceLastWeek;
    }

    @NonNull
    @Override
    public String toString() {
        return new String(this.name +"  "+this.symbol+"  "+this.price+"  "+this.changesSinceLastHour+"  "+
                this.changesSinceLastDay +"  "+ this.changesSinceLastWeek);
    }
}
