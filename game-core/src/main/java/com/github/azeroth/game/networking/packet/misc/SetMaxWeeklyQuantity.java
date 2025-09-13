package com.github.azeroth.game.networking.packet.misc;


public class SetMaxWeeklyQuantity extends ServerPacket {
    public int maxWeeklyQuantity;
    public int type;

    public SetMaxWeeklyQuantity() {
        super(ServerOpcode.SetMaxWeeklyQuantity);
    }

    @Override
    public void write() {
        this.writeInt32(type);
        this.writeInt32(maxWeeklyQuantity);
    }
}
