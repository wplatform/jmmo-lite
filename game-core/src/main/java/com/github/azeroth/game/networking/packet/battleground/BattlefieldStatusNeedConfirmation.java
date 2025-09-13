package com.github.azeroth.game.networking.packet.battleground;


public class BattlefieldStatusNeedConfirmation extends ServerPacket {
    public int timeout;
    public int mapid;
    public battlefieldStatusHeader hdr = new battlefieldStatusHeader();
    public byte role;

    public BattlefieldStatusNeedConfirmation() {
        super(ServerOpcode.BattlefieldStatusNeedConfirmation);
    }

    @Override
    public void write() {
        hdr.write(this);
        this.writeInt32(mapid);
        this.writeInt32(timeout);
        this.writeInt8(role);
    }
}
