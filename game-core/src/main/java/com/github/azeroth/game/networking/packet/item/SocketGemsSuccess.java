package com.github.azeroth.game.networking.packet.item;

import com.github.azeroth.game.networking.ServerPacket;

public class SocketGemsSuccess extends ServerPacket {
    public ObjectGuid item = ObjectGuid.EMPTY;

    public SocketGemsSuccess() {
        super(ServerOpcode.SocketGemsSuccess);
    }

    @Override
    public void write() {
        this.writeGuid(item);
    }
}
