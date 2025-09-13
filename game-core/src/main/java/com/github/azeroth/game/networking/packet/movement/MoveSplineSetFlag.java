package com.github.azeroth.game.networking.packet.movement;


public class MoveSplineSetFlag extends ServerPacket {
    public ObjectGuid moverGUID = ObjectGuid.EMPTY;

    public MoveSplineSetFlag(ServerOpCode opcode) {
        super(opcode);
    }

    @Override
    public void write() {
        this.writeGuid(moverGUID);
    }
}
