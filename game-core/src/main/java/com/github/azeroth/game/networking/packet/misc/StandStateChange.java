package com.github.azeroth.game.networking.packet.misc;


import com.github.azeroth.game.networking.WorldPacket;

public class StandStateChange extends ClientPacket {
    public UnitstandStateType standState = UnitStandStateType.values()[0];

    public StandStateChange(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        standState = UnitStandStateType.forValue((byte) this.readUInt());
    }
}
