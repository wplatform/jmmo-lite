package com.github.azeroth.game.networking.packet.party;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class ChangeSubGroup extends ClientPacket {
    public ObjectGuid targetGUID = ObjectGuid.EMPTY;
    public byte partyIndex;
    public byte newSubGroup;

    public ChangeSubGroup(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        targetGUID = this.readPackedGuid();
        partyIndex = this.readByte();
        newSubGroup = this.readUInt8();
    }
}
