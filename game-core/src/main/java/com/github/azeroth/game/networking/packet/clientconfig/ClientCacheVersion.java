package com.github.azeroth.game.networking.packet.clientconfig;


import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class ClientCacheVersion extends ServerPacket {
    public int cacheVersion = 0;

    public ClientCacheVersion() {
        super(ServerOpCode.SMSG_CACHE_VERSION);
    }

    @Override
    public void write() {
        this.writeInt32(cacheVersion);
    }
}
