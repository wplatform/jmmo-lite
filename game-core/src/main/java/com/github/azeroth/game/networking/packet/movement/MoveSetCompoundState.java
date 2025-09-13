package com.github.azeroth.game.networking.packet.movement;


import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;

public class MoveSetCompoundState extends ServerPacket {
    public ObjectGuid moverGUID = ObjectGuid.EMPTY;
    public ArrayList<MoveStateChange> stateChanges = new ArrayList<>();

    public MoveSetCompoundState() {
        super(ServerOpcode.MoveSetCompoundState);
    }

    @Override
    public void write() {
        this.writeGuid(moverGUID);
        this.writeInt32(stateChanges.size());

        for (var stateChange : stateChanges) {
            stateChange.write(this);
        }
    }

    public final static class CollisionHeightInfo {
        public float height;
        public float scale;
        public UpdateCollisionHeightreason reason = UpdateCollisionHeightReason.values()[0];

        public CollisionHeightInfo clone() {
            CollisionHeightInfo varCopy = new CollisionHeightInfo();

            varCopy.height = this.height;
            varCopy.scale = this.scale;
            varCopy.reason = this.reason;

            return varCopy;
        }
    }

    public final static class KnockBackInfo {
        public float horzSpeed;
        public Vector2 direction;
        public float initVertSpeed;

        public KnockBackInfo clone() {
            KnockBackInfo varCopy = new KnockBackInfo();

            varCopy.horzSpeed = this.horzSpeed;
            varCopy.direction = this.direction;
            varCopy.initVertSpeed = this.initVertSpeed;

            return varCopy;
        }
    }

    public static class SpeedRange {
        public float min;
        public float max;
    }

    public static class MoveStateChange {
        public ServerOpcode messageID = ServerOpcode.values()[0];
        public int sequenceIndex;
        public Float speed = null;
        public speedRange speedRange;
        public knockBackInfo knockBack = null;
        public Integer vehicleRecID = null;
        public collisionHeightInfo collisionHeight = null;
        public com.github.azeroth.game.movement.movementForce movementForce;
        public ObjectGuid movementForceGUID = null;
        public Integer movementInertiaID = null;
        public Integer movementInertiaLifetimeMs = null;

        public MoveStateChange(ServerOpcode messageId, int sequenceIndex) {
            messageID = messageId;
            sequenceIndex = sequenceIndex;
        }

        public final void write(WorldPacket data) {
            data.writeInt16((short) messageID.getValue());
            data.writeInt32(sequenceIndex);
            data.writeBit(speed != null);
            data.writeBit(speedRange != null);
            data.writeBit(knockBack != null);
            data.writeBit(vehicleRecID != null);
            data.writeBit(collisionHeight != null);
            data.writeBit(movementForce != null);
            data.writeBit(movementForceGUID != null);
            data.writeBit(movementInertiaID != null);
            data.writeBit(movementInertiaLifetimeMs != null);
            data.flushBits();

            if (movementForce != null) {
                movementForce.write(data);
            }

            if (speed != null) {
                data.writeFloat(speed.floatValue());
            }

            if (speedRange != null) {
                data.writeFloat(speedRange.min);
                data.writeFloat(speedRange.max);
            }

            if (knockBack != null) {
                data.writeFloat(knockBack.getValue().horzSpeed);
                data.writeVector2(knockBack.getValue().direction);
                data.writeFloat(knockBack.getValue().initVertSpeed);
            }

            if (vehicleRecID != null) {
                data.writeInt32(vehicleRecID.intValue());
            }

            if (collisionHeight != null) {
                data.writeFloat(collisionHeight.getValue().height);
                data.writeFloat(collisionHeight.getValue().scale);
                data.writeBits(collisionHeight.getValue().reason, 2);
                data.flushBits();
            }

            if (movementForceGUID != null) {
                data.writeGuid(movementForceGUID.getValue());
            }

            if (movementInertiaID != null) {
                data.writeInt32(movementInertiaID.intValue());
            }

            if (movementInertiaLifetimeMs != null) {
                data.writeInt32(movementInertiaLifetimeMs.intValue());
            }
        }
    }
}
