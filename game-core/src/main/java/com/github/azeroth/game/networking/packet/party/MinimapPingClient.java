package com.github.azeroth.game.networking.packet.party;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class MinimapPingClient extends ClientPacket {
    public byte partyIndex;
    public float positionX;
    public float positionY;

    public MinimapPingClient(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        positionX = this.readFloat();
        positionY = this.readFloat();
        partyIndex = this.readByte();
    }
}
