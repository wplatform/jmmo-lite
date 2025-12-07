package com.github.azeroth.game.networking.packet.movement;


import com.badlogic.gdx.math.Vector3;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.domain.unit.AnimTier;
import com.github.azeroth.game.movement.enums.MonsterMoveType;
import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;


public class MovementSpline {
    public int flags; // Spline flags
    public MonsterMoveType face; // Movement direction (see MonsterMoveType enum)
    public AnimTier animTier;
    public int tierTransStartTime;
    public int elapsed;
    public int moveTime;
    public float jumpGravity;
    public int specialTime;
    public ArrayList<Vector3> points = new ArrayList<>(); // Spline path
    public byte mode; // Spline mode - actually always 0 in this packet - Catmullrom mode appears only in SMSG_UPDATE_OBJECT. In this packet it is determined by flags
    public byte vehicleExitVoluntary;
    public ObjectGuid transportGUID;
    public byte vehicleSeat = -1;
    public ArrayList<Vector3> packedDeltas = new ArrayList<>();
    public MonsterSplineFilter splineFilter;
    public MonsterSplineSpellEffectExtraData spellEffectExtraData;
    public float faceDirection;
    public ObjectGuid faceGUID;
    public Vector3 faceSpot;

    public final void write(WorldPacket data) {
        data.writeInt32(flags);
        data.writeInt8(animTier);
        data.writeInt32(tierTransStartTime);
        data.writeInt32(elapsed);
        data.writeInt32(moveTime);
        data.writeFloat(jumpGravity);
        data.writeInt32(specialTime);
        data.writeInt8(mode);
        data.writeInt8(vehicleExitVoluntary);
        data.writeGuid(transportGUID);
        data.writeInt8(vehicleSeat);
        data.writeBits(face.ordinal(), 2);
        data.writeBits(points.size(), 16);
        data.writeBits(packedDeltas.size(), 16);
        data.writeBit(splineFilter != null);
        data.writeBit(spellEffectExtraData != null);
        data.flushBits();

        if (splineFilter != null) {
            splineFilter.write(data);
        }

        switch (face) {
            case FACING_SPOT:
                data.writeVector3(faceSpot);
                break;
            case FACING_TARGET:
                data.writeFloat(faceDirection);
                data.writeGuid(faceGUID);
                break;
            case FACING_ANGLE:
                data.writeFloat(faceDirection);
                break;
        }

        for (var pos : points) {
            data.writeVector3(pos);
        }

        for (var pos : packedDeltas) {
            data.writePackXYZ(pos);
        }

        if (spellEffectExtraData != null) {
            spellEffectExtraData.write(data);
        }

    }
}
