package com.github.azeroth.game.networking.packet.movement;


import com.github.azeroth.game.networking.ServerPacket;

public class MoveApplyMovementForce extends ServerPacket {
    public ObjectGuid moverGUID = ObjectGuid.EMPTY;
    public int sequenceIndex;
    public Movementforce force;

    public MoveApplyMovementForce() {
        super(ServerOpcode.MoveApplyMovementForce);
    }

    @Override
    public void write() {
        this.writeGuid(moverGUID);
        this.writeInt32(sequenceIndex);
        force.write(this);
    }
}
