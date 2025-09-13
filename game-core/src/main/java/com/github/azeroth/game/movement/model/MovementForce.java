package com.github.azeroth.game.movement.model;


import com.badlogic.gdx.math.Vector3;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.WorldPacket;
import com.github.azeroth.game.networking.packet.movement.MovementIOUtil;
import lombok.Data;

@Data
public class MovementForce {
    public ObjectGuid id = ObjectGuid.EMPTY;
    public Vector3 origin;
    public Vector3 direction;
    public int transportID;
    public float magnitude;
    public MovementForceType type = MovementForceType.values()[0];
    public int unused910;

    public final void read(WorldPacket data) {
        id = data.readPackedGuid();
        origin = data.readVector3();
        direction = data.readVector3();
        transportID = data.readUInt32();
        magnitude = data.readFloat();
        unused910 = data.readInt32();
        int bit = data.readBit(2);
        type = bit == 0 ? MovementForceType.Gravity : MovementForceType.values()[bit - 1];
    }

    public final void write(WorldPacket data) {
        MovementIOUtil.writeMovementForceWithDirection(this, data);
    }
}

