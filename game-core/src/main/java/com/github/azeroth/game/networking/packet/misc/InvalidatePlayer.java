package com.github.azeroth.game.networking.packet.misc;


public class InvalidatePlayer extends ServerPacket {
    public ObjectGuid guid = ObjectGuid.EMPTY;

    public InvalidatePlayer() {
        super(ServerOpcode.InvalidatePlayer);
    }

    @Override
    public void write() {
        this.writeGuid(guid);
    }
}
