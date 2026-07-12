package com.bitcoinalert.model;

public class BitcoinPrice {

    private final double usd;
    private final double krw;
    private final double usd24hChange;

    public BitcoinPrice(double usd, double krw, double usd24hChange) {
        this.usd = usd;
        this.krw = krw;
        this.usd24hChange = usd24hChange;
    }

    public double getUsd() { return usd; }
    public double getKrw() { return krw; }
    public double getUsd24hChange() { return usd24hChange; }
}
