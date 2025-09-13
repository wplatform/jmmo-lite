package com.github.azeroth.game.networking.packet.movement;


import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class MoveSetCollisionHeightAck extends ClientPacket {
    public movementAck data = new movementAck();
    public UpdateCollisionHeightreason reason = UpdateCollisionHeightReason.values()[0];
    public int mountDisplayID;
    public float height = 1.0f;

    public MoveSetCollisionHeightAck(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        data.read(this);
        height = this.readFloat();
        mountDisplayID = this.readUInt32();
        reason = UpdateCollisionHeightReason.forValue(this.readUInt8());
    }
}
