package com.github.azeroth.game.networking.packet.movement;


public class MoveSetActiveMover extends ServerPacket {
    public ObjectGuid moverGUID = ObjectGuid.EMPTY;

    public MoveSetActiveMover() {
        super(ServerOpcode.MoveSetActiveMover);
    }

    @Override
    public void write() {
        this.writeGuid(moverGUID);
    }
}
