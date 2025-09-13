package com.github.azeroth.game.networking.packet.party;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class SetPartyAssignment extends ClientPacket {
    public byte assignment;
    public byte partyIndex;
    public ObjectGuid target = ObjectGuid.EMPTY;
    public boolean set;

    public SetPartyAssignment(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        partyIndex = this.readUInt8();
        assignment = this.readUInt8();
        target = this.readPackedGuid();
        set = this.readBit();
    }
}
