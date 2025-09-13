package com.github.azeroth.game.networking.packet.party;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class SetPartyLeader extends ClientPacket {
    public byte partyIndex;
    public ObjectGuid targetGUID = ObjectGuid.EMPTY;

    public SetPartyLeader(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        partyIndex = this.readByte();
        targetGUID = this.readPackedGuid();
    }
}
