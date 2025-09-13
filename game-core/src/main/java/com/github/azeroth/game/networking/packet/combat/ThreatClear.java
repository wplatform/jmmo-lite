package com.github.azeroth.game.networking.packet.combat;


public class ThreatClear extends ServerPacket {
    public ObjectGuid unitGUID = ObjectGuid.EMPTY;

    public ThreatClear() {
        super(ServerOpcode.ThreatClear);
    }

    @Override
    public void write() {
        this.writeGuid(unitGUID);
    }
}
