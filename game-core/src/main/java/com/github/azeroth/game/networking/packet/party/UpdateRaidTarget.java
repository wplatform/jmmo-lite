package com.github.azeroth.game.networking.packet.party;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class UpdateRaidTarget extends ClientPacket {
    public byte partyIndex;
    public ObjectGuid target = ObjectGuid.EMPTY;
    public byte symbol;

    public UpdateRaidTarget(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        partyIndex = this.readByte();
        target = this.readPackedGuid();
        symbol = this.readByte();
    }
}
