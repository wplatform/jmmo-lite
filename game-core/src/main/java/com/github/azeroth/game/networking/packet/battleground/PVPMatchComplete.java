package com.github.azeroth.game.networking.packet.battleground;

import com.github.azeroth.game.networking.ServerPacket;

public class PVPMatchComplete extends ServerPacket {
    public byte winner;
    public int duration;
    public PVPMatchStatistics logData;
    public int soloShuffleStatus;

    public PVPMatchComplete() {
        super(ServerOpcode.PvpMatchComplete);
    }

    @Override
    public void write() {
        this.writeInt8(winner);
        this.writeInt32(duration);
        this.writeBit(logData != null);
        this.writeBits(soloShuffleStatus, 2);
        this.flushBits();

        if (logData != null) {
            logData.write(this);
        }
    }
}
