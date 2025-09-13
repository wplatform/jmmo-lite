package com.github.azeroth.game.networking.packet.misc;


import com.github.azeroth.game.networking.ServerPacket;

public class WeatherPkt extends ServerPacket {
    private final boolean abrupt;
    private final float intensity;
    private final WeatherState weatherID;


    public WeatherPkt(WeatherState weatherID, float intensity) {
        this(weatherID, intensity, false);
    }

    public WeatherPkt(WeatherState weatherID) {
        this(weatherID, 0.0f, false);
    }

    public WeatherPkt() {
        this(0, 0.0f, false);
    }

    public WeatherPkt(WeatherState weatherID, float intensity, boolean abrupt) {
        super(ServerOpcode.Weather);
        weatherID = weatherID;
        intensity = intensity;
        abrupt = abrupt;
    }

    @Override
    public void write() {
        this.writeInt32((int) weatherID.getValue());
        this.writeFloat(intensity);
        this.writeBit(abrupt);

        this.flushBits();
    }
}
