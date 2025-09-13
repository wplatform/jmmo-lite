package com.github.azeroth.game.networking.packet.movement;


import com.github.azeroth.game.networking.ServerPacket;

public class MoveRemoveMovementForce extends ServerPacket {
    public ObjectGuid moverGUID = ObjectGuid.EMPTY;
    public int sequenceIndex;
    public ObjectGuid ID = ObjectGuid.EMPTY;

    public MoveRemoveMovementForce() {
        super(ServerOpcode.MoveRemoveMovementForce);
    }

    @Override
    public void write() {
        this.writeGuid(moverGUID);
        this.writeInt32(sequenceIndex);
        this.writeGuid(ID);
    }
}
