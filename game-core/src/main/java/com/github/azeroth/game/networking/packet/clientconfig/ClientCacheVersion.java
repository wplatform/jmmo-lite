package com.github.azeroth.game.networking.packet.clientconfig;


public class ClientCacheVersion extends ServerPacket {
    public int cacheVersion = 0;

    public ClientCacheVersion() {
        super(ServerOpcode.cacheVersion);
    }

    @Override
    public void write() {
        this.writeInt32(cacheVersion);
    }
}
