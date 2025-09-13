package com.github.azeroth.game.networking.packet.misc;


public class ServerTimeOffset extends ServerPacket {
    public long time;

    public ServerTimeOffset() {
        super(ServerOpcode.ServerTimeOffset);
    }

    @Override
    public void write() {
        this.writeInt64(time);
    }
}
