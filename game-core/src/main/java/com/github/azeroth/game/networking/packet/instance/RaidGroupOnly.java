package com.github.azeroth.game.networking.packet.instance;

import com.github.azeroth.game.networking.ServerPacket;

public class RaidGroupOnly extends ServerPacket {
    public int delay;
    public RaidGroupreason reason = RaidGroupReason.values()[0];

    public RaidGroupOnly() {
        super(ServerOpcode.RaidGroupOnly);
    }

    @Override
    public void write() {
        this.writeInt32(delay);
        this.writeInt32((int) reason.getValue());
    }
}
