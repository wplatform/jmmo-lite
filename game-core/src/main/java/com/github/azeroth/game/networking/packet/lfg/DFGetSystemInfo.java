package com.github.azeroth.game.networking.packet.lfg;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class DFGetSystemInfo extends ClientPacket {
    public byte partyIndex;
    public boolean player;

    public DFGetSystemInfo(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        player = this.readBit();
        partyIndex = this.readUInt8();
    }
}
