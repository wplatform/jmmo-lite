package com.github.azeroth.game.networking.packet.taxi;


import com.github.azeroth.game.networking.ServerPacket;

class TaxiNodeStatusPkt extends ServerPacket {
    public TaxiNodestatus status = TaxiNodeStatus.values()[0]; // replace with TaxiStatus enum
    public ObjectGuid unit = ObjectGuid.EMPTY;

    public TaxiNodeStatusPkt() {
        super(ServerOpcode.TaxiNodeStatus);
    }

    @Override
    public void write() {
        this.writeGuid(unit);
        this.writeBits(status, 2);
        this.flushBits();
    }
}
