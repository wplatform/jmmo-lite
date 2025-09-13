package com.github.azeroth.game.networking.packet.party;


import com.github.azeroth.game.group.RaidMarker;
import com.github.azeroth.game.networking.ServerPacket;

import java.util.ArrayList;

class RaidMarkersChanged extends ServerPacket {
    public byte partyIndex;

    public int activeMarkers;

    public ArrayList<RaidMarker> raidMarkers = new ArrayList<>();

    public RaidMarkersChanged() {
        super(ServerOpcode.RaidMarkersChanged);
    }

    @Override
    public void write() {
        this.writeInt8(partyIndex);
        this.writeInt32(activeMarkers);

        this.writeBits(raidMarkers.size(), 4);
        this.flushBits();

        for (var raidMarker : raidMarkers) {
            this.writeGuid(raidMarker.transportGUID);
            this.writeInt32(raidMarker.location.getMapId());
            this.writeXYZ(raidMarker.location);
        }
    }
}
