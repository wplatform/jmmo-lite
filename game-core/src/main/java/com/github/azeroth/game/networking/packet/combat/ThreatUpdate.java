package com.github.azeroth.game.networking.packet.combat;


import java.util.ArrayList;


public class ThreatUpdate extends ServerPacket {
    public ObjectGuid unitGUID = ObjectGuid.EMPTY;
    public ArrayList<ThreatInfo> threatList = new ArrayList<>();

    public ThreatUpdate() {
        super(ServerOpcode.ThreatUpdate);
    }

    @Override
    public void write() {
        this.writeGuid(unitGUID);
        this.writeInt32(threatList.size());

        for (var threatInfo : threatList) {
            this.writeGuid(threatInfo.unitGUID);
            this.writeInt64(threatInfo.threat);
        }
    }
}
