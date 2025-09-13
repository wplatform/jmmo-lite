package com.github.azeroth.game.networking.packet.combat;

public final class ThreatInfo {
    public ObjectGuid unitGUID = ObjectGuid.EMPTY;
    public long threat;

    public ThreatInfo clone() {
        ThreatInfo varCopy = new ThreatInfo();

        varCopy.unitGUID = this.unitGUID;
        varCopy.threat = this.threat;

        return varCopy;
    }
}
