package org.example.model;

public enum Timeframe {
    M1(1),
    M5(5),
    M15(15),
    M30(30),
    H1(60),
    H4(240),
    D1(1440),
    W1(10080),
    MN1(43200);

    private int minutes;

    Timeframe(int minutes) {
        this.minutes = minutes;
    }

    public int getMinutes() {
        return minutes;
    }
}
