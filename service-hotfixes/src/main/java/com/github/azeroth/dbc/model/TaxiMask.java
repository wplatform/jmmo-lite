package com.github.azeroth.dbc.model;

public class TaxiMask {

    private final byte[] data;

    public TaxiMask(int size) {
        this.data = new byte[size];
    }


    public final byte get(int i) {
        return data[i];
    }

    public final void set(int i, byte value) {
        data[i] = value;
    }

    public final int size() {
        return data.length;
    }

    public final byte[] data() {
        return data;
    }

}
