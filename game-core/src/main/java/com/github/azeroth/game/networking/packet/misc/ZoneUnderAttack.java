package com.github.azeroth.game.networking.packet.misc;


public class ZoneUnderAttack extends ServerPacket {
    public int areaID;

    public ZoneUnderAttack() {
        super(ServerOpcode.ZoneUnderAttack);
    }

    @Override
    public void write() {
        this.writeInt32(areaID);
    }
}
