package com.github.azeroth.game.networking.packet.instance;

import com.github.azeroth.game.networking.ServerPacket;

public class InstanceResetFailed extends ServerPacket {
    public int mapID;
    public resetFailedReason resetFailedReason = Framework.Constants.resetFailedReason.values()[0];

    public InstanceResetFailed() {
        super(ServerOpcode.InstanceResetFailed);
    }

    @Override
    public void write() {
        this.writeInt32(mapID);
        this.writeBits(resetFailedReason, 2);
        this.flushBits();
    }
}
