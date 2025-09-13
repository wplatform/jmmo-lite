package com.github.azeroth.game.networking.packet.movement;


public class MoveSetFlag extends ServerPacket {
    public ObjectGuid moverGUID = ObjectGuid.EMPTY;
    public int sequenceIndex; // Unit movement packet index, incremented each time

    public MoveSetFlag(ServerOpCode opcode) {
        super(opcode);
    }

    @Override
    public void write() {
        this.writeGuid(moverGUID);
        this.writeInt32(sequenceIndex);
    }
}
