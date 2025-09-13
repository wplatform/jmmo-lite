package com.github.azeroth.game.networking.packet.party;

import com.github.azeroth.game.networking.WorldPacket;

class SetAssistantLeader extends ClientPacket {
    public ObjectGuid target = ObjectGuid.EMPTY;
    public byte partyIndex;
    public boolean apply;

    public SetAssistantLeader(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        partyIndex = this.readUInt8();
        target = this.readPackedGuid();
        apply = this.readBit();
    }
}
