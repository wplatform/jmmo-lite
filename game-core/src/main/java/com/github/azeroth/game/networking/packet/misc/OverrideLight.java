package com.github.azeroth.game.networking.packet.misc;

import com.github.azeroth.game.networking.ServerPacket;

public class OverrideLight extends ServerPacket {
    public int areaLightID;
    public int transitionMilliseconds;
    public int overrideLightID;

    public overrideLight() {
        super(ServerOpcode.OverrideLight);
    }

    @Override
    public void write() {
        this.writeInt32(areaLightID);
        this.writeInt32(overrideLightID);
        this.writeInt32(transitionMilliseconds);
    }
}
