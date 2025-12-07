package com.github.azeroth.game.networking.packet.movement;


import com.badlogic.gdx.math.Vector3;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.movement.spline.MoveSpline;
import com.github.azeroth.game.movement.spline.SplineFlag;
import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

import java.util.Arrays;

public class MonsterMove extends ServerPacket {
    public MovementMonsterSpline splineData;
    public ObjectGuid moverGUID;
    public Vector3 pos;

    public MonsterMove() {
        super(ServerOpCode.SMSG_ON_MONSTER_MOVE);
        splineData = new MovementMonsterSpline();
    }

    public final void initializeSplineData(MoveSpline moveSpline) {
        splineData.id = moveSpline.getId();
        var movementSpline = splineData.move;

        var splineFlags = moveSpline.splineFlags;
        movementSpline.flags = (splineFlags.getFlag() & ~SplineFlag.Done.value);
        movementSpline.face = moveSpline.facing.type;
        movementSpline.faceDirection = moveSpline.facing.angle;
        movementSpline.faceGUID = moveSpline.facing.target;
        movementSpline.faceSpot = moveSpline.facing.f;

        if (splineFlags.hasFlag(SplineFlag.Animation)) {
            movementSpline.animTier = moveSpline.animTierTransition.animTier;
            movementSpline.tierTransStartTime = moveSpline.effectStartTime;
        }

        movementSpline.moveTime = moveSpline.duration();

        if (splineFlags.hasFlag(SplineFlag.Parabolic) && (moveSpline.spellEffectExtra == null || moveSpline.effectStartTime != 0)) {
            movementSpline.jumpGravity = moveSpline.verticalAcceleration;
            movementSpline.specialTime = moveSpline.effectStartTime;
        }

        if (splineFlags.hasFlag(SplineFlag.FadeObject)) {
            movementSpline.specialTime = moveSpline.effectStartTime;
        }

        if (moveSpline.spellEffectExtra != null) {
            MonsterSplineSpellEffectExtraData spellEffectExtraData = new MonsterSplineSpellEffectExtraData();
            spellEffectExtraData.targetGuid = moveSpline.spellEffectExtra.target;
            spellEffectExtraData.spellVisualID = moveSpline.spellEffectExtra.spellVisualId;
            spellEffectExtraData.progressCurveID = moveSpline.spellEffectExtra.progressCurveId;
            spellEffectExtraData.parabolicCurveID = moveSpline.spellEffectExtra.parabolicCurveId;
            movementSpline.spellEffectExtraData = spellEffectExtraData;
        }


        var spline = moveSpline.spline;
        var array = spline.getPoints();

        if (splineFlags.hasFlag(SplineFlag.UncompressedPath)) {
            int count = spline.getPointCount() - (splineFlags.hasFlag(SplineFlag.Cyclic) ? 4 : 3);
            movementSpline.points.addAll(Arrays.asList(array).subList(2, count + 2));
        } else {
            int lastIdx = spline.getPointCount() - (splineFlags.hasFlag(SplineFlag.Cyclic) ? 4 : 3);
            Vector3[] realPath = Arrays.copyOfRange(array, 1, array.length);
            movementSpline.points.add(realPath[lastIdx]);
            if (lastIdx > 1) {
                var middle = (realPath[0].add(realPath[lastIdx])).scl(0.5f);
                // first and last points already appended
                for (var i = 1; i < lastIdx; ++i) {
                    movementSpline.packedDeltas.add(middle.sub(realPath[i]));
                }
            }
        }
    }

    @Override
    public void write() {
        this.writeGuid(moverGUID);
        this.writeVector3(pos);
        splineData.write(this);
    }
}
