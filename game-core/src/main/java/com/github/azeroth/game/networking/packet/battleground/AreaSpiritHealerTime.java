package com.github.azeroth.game.networking.packet.battleground;


public class AreaSpiritHealerTime extends ServerPacket {
    public ObjectGuid healerGuid = ObjectGuid.EMPTY;
    public int timeLeft;

    public AreaSpiritHealerTime() {
        super(ServerOpcode.AreaSpiritHealerTime);
    }

    @Override
    public void write() {
        this.writeGuid(healerGuid);
        this.writeInt32(timeLeft);
    }
}
