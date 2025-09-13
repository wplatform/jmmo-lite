package com.github.azeroth.game.networking.packet.party;

import com.github.azeroth.game.networking.ServerPacket;

public class BroadcastSummonResponse extends ServerPacket {
    public ObjectGuid target = ObjectGuid.EMPTY;
    public boolean accepted;

    public BroadcastSummonResponse() {
        super(ServerOpcode.BroadcastSummonResponse);
    }

    @Override
    public void write() {
        this.writeGuid(target);
        this.writeBit(accepted);
        this.flushBits();
    }
}
