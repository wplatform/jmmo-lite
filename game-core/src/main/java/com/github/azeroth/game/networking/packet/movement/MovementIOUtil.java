package com.github.azeroth.game.networking.packet.movement;


import com.badlogic.gdx.math.Vector3;
import com.github.azeroth.common.EnumFlag;
import com.github.azeroth.game.domain.object.Position;
import com.github.azeroth.game.domain.unit.MovementFlag;
import com.github.azeroth.game.domain.unit.MovementFlag2;
import com.github.azeroth.game.movement.Spline;
import com.github.azeroth.game.movement.model.MovementForce;
import com.github.azeroth.game.movement.model.MovementForceType;
import com.github.azeroth.game.movement.model.MovementInfo;
import com.github.azeroth.game.movement.model.TransportInfo;
import com.github.azeroth.game.movement.spline.MoveSpline;
import com.github.azeroth.game.networking.WorldPacket;

public final class MovementIOUtil {


    public static TransportInfo readTransportInfo(WorldPacket data) {

        TransportInfo transportInfo = new TransportInfo();

        transportInfo.setGuid(data.readPackedGuid());

        transportInfo.setGuid(data.readPackedGuid()); // Transport Guid
        transportInfo.setPos(new Position(data.readFloat(), data.readFloat(), data.readFloat(), data.readFloat()));
        transportInfo.setSeat(data.readByte()); // VehicleSeatIndex
        transportInfo.setTime(data.readUInt32()); // MoveTime

        var hasPrevTime = data.readBit();
        var hasVehicleId = data.readBit();

        if (hasPrevTime) {
            transportInfo.setPrevTime(data.readUInt32()); // PrevMoveTime
        }

        if (hasVehicleId) {
            transportInfo.setVehicleId(data.readUInt32()); // VehicleRecID
        }
        return transportInfo;
    }

    public static void writeTransportInfo(WorldPacket data, TransportInfo transportInfo) {
        var hasPrevTime = transportInfo.getPrevTime() != 0;
        var hasVehicleId = transportInfo.getVehicleId() != 0;

        data.writeGuid(transportInfo.getGuid()); // Transport Guid
        data.writeFloat(transportInfo.getPos().getX());
        data.writeFloat(transportInfo.getPos().getY());
        data.writeFloat(transportInfo.getPos().getZ());
        data.writeFloat(transportInfo.getPos().getO());
        data.writeInt8(transportInfo.getSeat()); // VehicleSeatIndex
        data.writeInt32(transportInfo.getTime()); // MoveTime

        data.writeBit(hasPrevTime);
        data.writeBit(hasVehicleId);
        data.flushBits();

        if (hasPrevTime) {
            data.writeInt32(transportInfo.getPrevTime()); // PrevMoveTime
        }

        if (hasVehicleId) {
            data.writeInt32(transportInfo.getVehicleId()); // VehicleRecID
        }
    }

    public static MovementInfo readMovementInfo(WorldPacket data) {
        var movementInfo = new MovementInfo();
        movementInfo.setGuid(data.readPackedGuid());
        movementInfo.setTime(data.readUInt32());
        movementInfo.setPos(new Position(data.readFloat(), data.readFloat(), data.readFloat(), data.readFloat()));
        movementInfo.setPitch(data.readFloat());
        movementInfo.setStepUpStartElevation(data.readFloat());

        var removeMovementForcesCount = data.readUInt32();
        var moveIndex = data.readUInt32();

        for (int i = 0; i < removeMovementForcesCount; ++i) {
            data.readPackedGuid();
        }

        movementInfo.setFlags(EnumFlag.of(MovementFlag.class, data.readBit(30)));
        movementInfo.setFlags2(EnumFlag.of(MovementFlag2.class, data.readBit(18)));

        var hasTransport = data.readBit();
        var hasFall = data.readBit();
        var hasSpline = data.readBit(); // todo 6.x read this infos

        data.readBit(); // HeightChangeFailed
        data.readBit(); // RemoteTimeValid

        if (hasTransport) {
            movementInfo.setTransport(readTransportInfo(data));
        }

        if (hasFall) {
            movementInfo.getJump().setFallTime(data.readUInt32());
            movementInfo.getJump().setZspeed(data.readFloat());

            // ResetBitReader

            var hasFallDirection = data.readBit();

            if (hasFallDirection) {
                movementInfo.getJump().setSinAngle(data.readFloat());
                movementInfo.getJump().setCosAngle(data.readFloat());
                movementInfo.getJump().setXyspeed(data.readFloat());
            }
        }

        return movementInfo;
    }

    public static void writeMovementInfo(WorldPacket data, MovementInfo movementInfo) {
        var hasTransportData = !movementInfo.getTransport().getGuid().isEmpty();
        var hasFallDirection = movementInfo.getFlags().hasFlag(MovementFlag.FALLING.addFlag(MovementFlag.FALLING_FAR));
        var hasFallData = hasFallDirection || movementInfo.getJump().getFallTime() != 0;
        var hasSpline = false; // todo 6.x send this infos

        data.writeGuid(movementInfo.getGuid());
        data.writeInt32(movementInfo.getTime());
        data.writeFloat(movementInfo.getPos().getX());
        data.writeFloat(movementInfo.getPos().getY());
        data.writeFloat(movementInfo.getPos().getZ());
        data.writeFloat(movementInfo.getPos().getO());
        data.writeFloat(movementInfo.getPitch());
        data.writeFloat(movementInfo.getStepUpStartElevation());

        //removeMovementForcesCount = 0;
        data.writeInt32(0);

        //moveIndex = 0;
        data.writeInt32(0);

        /*for (uint i = 0; i < removeMovementForcesCount; ++i)
		{
		    this << ObjectGuid;
		}*/

        data.writeBits(movementInfo.getMovementFlags(), 30);
        data.writeBits(movementInfo.getExtraMovementFlags(), 18);


        data.writeBit(hasTransportData);
        data.writeBit(hasFallData);
        data.writeBit(hasSpline);

        data.writeBit(false); // HeightChangeFailed
        data.writeBit(false); // RemoteTimeValid
        data.flushBits();

        if (hasTransportData) {
            writeTransportInfo(data, movementInfo.getTransport());
        }


        if (hasFallData) {
            data.writeInt32(movementInfo.getJump().getFallTime());
            data.writeFloat(movementInfo.getJump().getZspeed());

            data.writeBit(hasFallDirection);
            data.flushBits();

            if (hasFallDirection) {
                data.writeFloat(movementInfo.getJump().getSinAngle());
                data.writeFloat(movementInfo.getJump().getCosAngle());
                data.writeFloat(movementInfo.getJump().getXyspeed());
            }
        }
    }

    public static void writeCreateObjectSplineDataBlock(MoveSpline moveSpline, WorldPacket data) {
        data.writeInt32(moveSpline.getId()); // ID

        if (!moveSpline.isCyclic()) // Destination
        {
            data.writeVector3(moveSpline.finalDestination());
        } else {
            data.writeVector3(Vector3.Zero);
        }

        var hasSplineMove = data.writeBit(!moveSpline.finalized() && !moveSpline.splineIsFacingOnly);
        data.flushBits();

        if (hasSplineMove) {
            data.writeInt32((int) moveSpline.splineflags.flags.getValue()); // SplineFlags
            data.writeInt32(moveSpline.timePassed()); // Elapsed
            data.writeInt32(moveSpline.duration()); // Duration
            data.writeFloat(1.0f); // DurationModifier
            data.writeFloat(1.0f); // NextDurationModifier
            data.writeBits((byte) moveSpline.facing.type.getValue(), 2); // Face
            var hasFadeObjectTime = data.writeBit(moveSpline.splineflags.hasFlag(SplineFlag.FadeObject) && moveSpline.effect_start_time < moveSpline.duration());
            data.writeBits(moveSpline.getPath().length, 16);
            data.writeBit(false); // HasSplineFilter
            data.writeBit(moveSpline.spell_effect_extra != null); // HasSpellEffectExtraData
            var hasJumpExtraData = data.writeBit(moveSpline.splineflags.hasFlag(SplineFlag.Parabolic) && (moveSpline.spell_effect_extra == null || moveSpline.effect_start_time != 0));
            data.writeBit(moveSpline.anim_tier != null); // HasAnimTierTransition
            data.writeBit(false); // HasUnknown901
            data.flushBits();

            //if (HasSplineFilterKey)
            //{
            //    data << uint32(FilterKeysCount);
            //    for (var i = 0; i < FilterKeysCount; ++i)
            //    {
            //        data << float(In);
            //        data << float(Out);
            //    }

            //    data.writeBits(filterFlags, 2);
            //    data.flushBits();
            //}

            switch (moveSpline.facing.type) {
                case FacingSpot:
                    data.writeVector3(moveSpline.facing.f); // FaceSpot

                    break;
                case FacingTarget:
                    data.writeGuid(moveSpline.facing.target); // FaceGUID

                    break;
                case FacingAngle:
                    data.writeFloat(moveSpline.facing.angle); // FaceDirection

                    break;
            }

            if (hasFadeObjectTime) {
                data.writeInt32(moveSpline.effect_start_time); // FadeObjectTime
            }

            for (var vec : moveSpline.getPath()) {
                data.writeVector3(vec);
            }

            if (moveSpline.spell_effect_extra != null) {
                data.writeGuid(moveSpline.spell_effect_extra.target);
                data.writeInt32(moveSpline.spell_effect_extra.spellVisualId);
                data.writeInt32(moveSpline.spell_effect_extra.progressCurveId);
                data.writeInt32(moveSpline.spell_effect_extra.parabolicCurveId);
            }

            if (hasJumpExtraData) {
                data.writeFloat(moveSpline.vertical_acceleration);
                data.writeInt32(moveSpline.effect_start_time);
                data.writeInt32(0); // duration (override)
            }

            if (moveSpline.anim_tier != null) {
                data.writeInt32(moveSpline.anim_tier.tierTransitionId);
                data.writeInt32(moveSpline.effect_start_time);
                data.writeInt32(0);
                data.writeInt8(moveSpline.anim_tier.animTier);
            }

            //if (HasUnknown901)
            //{
            //    for (WorldPackets::Movement::MonsterSplineUnknown901::Inner const& unkInner : unk.data) size = 16
            //    {
            //        data << int32(unkInner.unknown_1);
            //        data << int32(unkInner.Unknown_2);
            //        data << int32(unkInner.Unknown_3);
            //        data << uint32(unkInner.unknown_4);
            //    }
            //}
        }
    }

    public static void writeCreateObjectAreaTriggerSpline(Spline spline, WorldPacket data) {
        data.writeBits(spline.getPoints().length, 16);

        for (var point : spline.getPoints()) {
            data.writeVector3(point);
        }
    }


    public static void writeMovementForceWithDirection(MovementForce movementForce, WorldPacket data) {
        writeMovementForceWithDirection(movementForce, data, null);
    }

    public static void writeMovementForceWithDirection(MovementForce movementForce, WorldPacket data, Position objectPosition) {
        data.writeGuid(movementForce.getId());
        data.writeVector3(movementForce.getOrigin());

        if (movementForce.getType() == MovementForceType.Gravity && objectPosition != null) // gravity
        {
            var direction = Vector3.Zero;

            if (movementForce.getMagnitude() != 0.0f) {
                Position tmp = new Position(movementForce.getOrigin().X - objectPosition.getX(), movementForce.getOrigin().Y - objectPosition.getY(), movementForce.getOrigin().Z - objectPosition.getZ());

                var lengthSquared = tmp.getExactDistSq(0.0f, 0.0f, 0.0f);

                if (lengthSquared > 0.0f) {
                    var mult = 1.0f / (float) Math.sqrt(lengthSquared) * movementForce.getMagnitude();
                    tmp.setX(tmp.getX() * mult);
                    tmp.setY(tmp.getY() * mult);
                    tmp.setZ(tmp.getZ() * mult);

                    var minLengthSquared = (tmp.getX() * tmp.getX() * 0.04f) + (tmp.getY() * tmp.getY() * 0.04f) + (tmp.getZ() * tmp.getZ() * 0.04f);

                    if (lengthSquared > minLengthSquared) {
                        direction = new Vector3(tmp.getX(), tmp.getY(), tmp.getZ());
                    }
                }
            }

            data.writeVector3(direction);
        } else {
            data.writeVector3(movementForce.getDirection());
        }

        data.writeInt32(movementForce.getTransportID());
        data.writeFloat(movementForce.getMagnitude());
        data.writeInt32(movementForce.getUnused910());
        data.writeBits((byte) movementForce.getType().getValue(), 2);
        data.flushBits();
    }
}

//Structs

