package com.github.azeroth.game.networking.packet.combat;


public class ThreatRemove extends ServerPacket {
    public ObjectGuid aboutGUID = ObjectGuid.EMPTY; // Unit to remove threat from (e.g. player, pet, guardian)
    public ObjectGuid unitGUID = ObjectGuid.EMPTY; // Unit being attacked (e.g. creature, boss)

    public ThreatRemove() {
        super(ServerOpcode.ThreatRemove);
    }

    @Override
    public void write() {
        this.writeGuid(unitGUID);
        this.writeGuid(aboutGUID);
    }
}
