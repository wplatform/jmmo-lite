package com.github.azeroth.game.networking.packet.movement;


import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;


public class MovementSpline {
    public int flags; // Spline flags
    public MonsterMoveType face = MonsterMoveType.values()[0]; // Movement direction (see MonsterMoveType enum)
    public int elapsed;
    public int moveTime;
    public int fadeObjectTime;
    public ArrayList<Vector3> points = new ArrayList<>(); // Spline path
    public byte mode; // Spline mode - actually always 0 in this packet - Catmullrom mode appears only in SMSG_UPDATE_OBJECT. In this packet it is determined by flags
    public boolean vehicleExitVoluntary;
    public boolean interpolate;
    public ObjectGuid transportGUID = ObjectGuid.EMPTY;
    public byte vehicleSeat = -1;
    public ArrayList<Vector3> packedDeltas = new ArrayList<>();
    public MonstersplineFilter splineFilter;
    public MonsterSplinespellEffectExtraData spellEffectExtraData = null;
    public MonsterSplinejumpExtraData jumpExtraData = null;
    public MonsterSplineanimTierTransition animTierTransition = null;
    public MonsterSplineunknown901 unknown901;
    public float faceDirection;
    public ObjectGuid faceGUID = ObjectGuid.EMPTY;
    public Vector3 faceSpot;

    public final void write(WorldPacket data) {
        data.writeInt32(flags);
        data.writeInt32(elapsed);
        data.writeInt32(moveTime);
        data.writeInt32(fadeObjectTime);
        data.writeInt8(mode);
        data.writeGuid(transportGUID);
        data.writeInt8(vehicleSeat);
        data.writeBits((byte) face.getValue(), 2);
        data.writeBits(points.size(), 16);
        data.writeBit(vehicleExitVoluntary);
        data.writeBit(interpolate);
        data.writeBits(packedDeltas.size(), 16);
        data.writeBit(splineFilter != null);
        data.writeBit(spellEffectExtraData != null);
        data.writeBit(jumpExtraData != null);
        data.writeBit(animTierTransition != null);
        data.writeBit(unknown901 != null);
        data.flushBits();

        if (splineFilter != null) {
            splineFilter.write(data);
        }

        switch (face) {
            case FacingSpot:
                data.writeVector3(faceSpot);

                break;
            case FacingTarget:
                data.writeFloat(faceDirection);
                data.writeGuid(faceGUID);

                break;
            case FacingAngle:
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
            spellEffectExtraData.getValue().write(data);
        }

        if (jumpExtraData != null) {
            jumpExtraData.getValue().write(data);
        }

        if (animTierTransition != null) {
            animTierTransition.getValue().write(data);
        }

        if (unknown901 != null) {
            unknown901.write(data);
        }
    }
}
