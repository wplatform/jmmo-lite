package com.github.azeroth.game.networking.packet.battleground;


public class BattlefieldStatusActive extends ServerPacket {
    public battlefieldStatusHeader hdr = new battlefieldStatusHeader();
    public int shutdownTimer;
    public byte arenaFaction;
    public boolean leftEarly;
    public int startTimer;
    public int mapid;

    public BattlefieldStatusActive() {
        super(ServerOpcode.BattlefieldStatusActive);
    }

    @Override
    public void write() {
        hdr.write(this);
        this.writeInt32(mapid);
        this.writeInt32(shutdownTimer);
        this.writeInt32(startTimer);
        this.writeBit(arenaFaction != 0);
        this.writeBit(leftEarly);
        this.flushBits();
    }
}
