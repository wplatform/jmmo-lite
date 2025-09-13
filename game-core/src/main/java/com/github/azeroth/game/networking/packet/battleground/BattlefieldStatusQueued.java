package com.github.azeroth.game.networking.packet.battleground;


public class BattlefieldStatusQueued extends ServerPacket {
    public int averageWaitTime;
    public battlefieldStatusHeader hdr = new battlefieldStatusHeader();
    public boolean asGroup;
    public boolean suspendedQueue;
    public boolean eligibleForMatchmaking;
    public int waitTime;
    public int unused920;

    public BattlefieldStatusQueued() {
        super(ServerOpcode.BattlefieldStatusQueued);
    }

    @Override
    public void write() {
        hdr.write(this);
        this.writeInt32(averageWaitTime);
        this.writeInt32(waitTime);
        this.writeInt32(unused920);
        this.writeBit(asGroup);
        this.writeBit(eligibleForMatchmaking);
        this.writeBit(suspendedQueue);
        this.flushBits();
    }
}
