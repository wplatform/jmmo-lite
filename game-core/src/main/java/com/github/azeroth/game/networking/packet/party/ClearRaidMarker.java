package com.github.azeroth.game.networking.packet.party;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class ClearRaidMarker extends ClientPacket {
    public byte markerId;

    public ClearRaidMarker(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        markerId = this.readUInt8();
    }
}
