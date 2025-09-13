package com.github.azeroth.game.networking.packet.movement;


import com.github.azeroth.game.movement.spline.MoveSpline;

public class MonsterMove extends ServerPacket {
    public MovementMonsterSpline splineData;
    public ObjectGuid moverGUID = ObjectGuid.EMPTY;
    public Vector3 pos;

    public MonsterMove() {
        super(ServerOpcode.OnMonsterMove);
        splineData = new MovementMonsterSpline();
    }

    public final void initializeSplineData(MoveSpline moveSpline) {
        splineData.id = moveSpline.getId();
        var movementSpline = splineData.move;

        var splineFlags = moveSpline.splineflags;
        splineFlags.setUnsetFlag(SplineFlag.Cyclic, moveSpline.isCyclic());
        movementSpline.flags = (int) (splineFlags.flags.getValue() & ~SplineFlag.MaskNoMonsterMove.getValue().getValue());
        movementSpline.face = moveSpline.facing.type;
        movementSpline.faceDirection = moveSpline.facing.angle;
        movementSpline.faceGUID = moveSpline.facing.target;
        movementSpline.faceSpot = moveSpline.facing.f;

        if (splineFlags.hasFlag(SplineFlag.Animation)) {
            MonsterSplineAnimTierTransition animTierTransition = new MonsterSplineAnimTierTransition();
            animTierTransition.tierTransitionID = (int) moveSpline.anim_tier.tierTransitionId;
            animTierTransition.startTime = (int) moveSpline.effect_start_time;
            animTierTransition.animTier = moveSpline.anim_tier.animTier;
            movementSpline.animTierTransition = animTierTransition;
        }

        movementSpline.moveTime = (int) moveSpline.duration();

        if (splineFlags.hasFlag(SplineFlag.Parabolic) && (moveSpline.spell_effect_extra == null || moveSpline.effect_start_time != 0)) {
            MonsterSplineJumpExtraData jumpExtraData = new MonsterSplineJumpExtraData();
            jumpExtraData.jumpGravity = moveSpline.vertical_acceleration;
            jumpExtraData.startTime = (int) moveSpline.effect_start_time;
            movementSpline.jumpExtraData = jumpExtraData;
        }

        if (splineFlags.hasFlag(SplineFlag.FadeObject)) {
            movementSpline.fadeObjectTime = (int) moveSpline.effect_start_time;
        }

        if (moveSpline.spell_effect_extra != null) {
            MonsterSplineSpellEffectExtraData spellEffectExtraData = new MonsterSplineSpellEffectExtraData();
            spellEffectExtraData.targetGuid = moveSpline.spell_effect_extra.target;
            spellEffectExtraData.spellVisualID = moveSpline.spell_effect_extra.spellVisualId;
            spellEffectExtraData.progressCurveID = moveSpline.spell_effect_extra.progressCurveId;
            spellEffectExtraData.parabolicCurveID = moveSpline.spell_effect_extra.parabolicCurveId;
            spellEffectExtraData.jumpGravity = moveSpline.vertical_acceleration;
            movementSpline.spellEffectExtraData = spellEffectExtraData;
        }

        synchronized (moveSpline.spline) {
            var spline = moveSpline.spline;
            var array = spline.getPoints();

            if (splineFlags.hasFlag(SplineFlag.UncompressedPath)) {
                if (!splineFlags.hasFlag(SplineFlag.Cyclic)) {
                    var count = spline.getPointCount() - 3;

                    for (int i = 0; i < count; ++i) {
                        movementSpline.points.add(array[i + 2]);
                    }
                } else {
                    var count = spline.getPointCount() - 3;
                    movementSpline.points.add(array[1]);

                    for (int i = 0; i < count; ++i) {
                        movementSpline.points.add(array[i + 1]);
                    }
                }
            } else {
                var lastIdx = spline.getPointCount() - 3;
                var realPath = (new Span<Vector3>(spline.getPoints())).Slice(1);

                movementSpline.points.add(realPath[lastIdx]);

                if (lastIdx > 1) {
                    var middle = (realPath[0] + realPath[lastIdx]) / 2.0f;

                    // first and last points already appended
                    for (var i = 1; i < lastIdx; ++i) {
                        movementSpline.packedDeltas.add(middle - realPath[i]);
                    }
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
