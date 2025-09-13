package com.github.azeroth.game.networking.packet.voidstorage;

import com.github.azeroth.game.networking.ServerPacket;

public class VoidStorageFailed extends ServerPacket {
    public byte reason = 0;

    public VoidStorageFailed() {
        super(ServerOpcode.VoidStorageFailed);
    }

    @Override
    public void write() {
        this.writeInt8(reason);
    }
}
