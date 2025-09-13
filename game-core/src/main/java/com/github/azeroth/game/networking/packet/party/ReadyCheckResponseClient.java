package com.github.azeroth.game.networking.packet.party;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class ReadyCheckResponseClient extends ClientPacket {

    public byte partyIndex;
    public boolean isReady;

    public ReadyCheckResponseClient(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        partyIndex = this.readUInt8();
        isReady = this.readBit();
    }
}
