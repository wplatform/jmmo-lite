package com.github.azeroth.game.networking.packet.movement;

import com.github.azeroth.game.networking.ServerPacket;

public class MoveSetCollisionHeight extends ServerPacket {
    public float scale = 1.0f;
    public ObjectGuid moverGUID = ObjectGuid.EMPTY;
    public int mountDisplayID;
    public UpdateCollisionHeightreason reason = UpdateCollisionHeightReason.values()[0];
    public int sequenceIndex;
    public int scaleDuration;
    public float height = 1.0f;

    public MoveSetCollisionHeight() {
        super(ServerOpcode.MoveSetCollisionHeight);
    }

    @Override
    public void write() {
        this.writeGuid(moverGUID);
        this.writeInt32(sequenceIndex);
        this.writeFloat(height);
        this.writeFloat(scale);
        this.writeInt8((byte) reason.getValue());
        this.writeInt32(mountDisplayID);
        this.writeInt32(scaleDuration);
    }
}
