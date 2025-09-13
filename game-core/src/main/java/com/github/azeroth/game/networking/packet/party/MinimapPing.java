package com.github.azeroth.game.networking.packet.party;

import com.github.azeroth.game.networking.ServerPacket;

public class MinimapPing extends ServerPacket {
    public ObjectGuid sender = ObjectGuid.EMPTY;
    public float positionX;
    public float positionY;

    public MinimapPing() {
        super(ServerOpcode.MinimapPing);
    }

    @Override
    public void write() {
        this.writeGuid(sender);
        this.writeFloat(positionX);
        this.writeFloat(positionY);
    }
}
