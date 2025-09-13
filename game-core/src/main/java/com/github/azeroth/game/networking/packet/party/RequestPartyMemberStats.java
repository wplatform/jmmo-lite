package com.github.azeroth.game.networking.packet.party;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class RequestPartyMemberStats extends ClientPacket {
    public byte partyIndex;
    public ObjectGuid targetGUID = ObjectGuid.EMPTY;

    public RequestPartyMemberStats(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        partyIndex = this.readUInt8();
        targetGUID = this.readPackedGuid();
    }
}
