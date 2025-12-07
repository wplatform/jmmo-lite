package com.github.azeroth.game.movement.spline;


import com.badlogic.gdx.math.Vector3;
import com.github.azeroth.common.Logs;
import com.github.azeroth.dbc.DbcObjectManager;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.movement.FacingInfo;
import com.github.azeroth.game.movement.model.AnimTierTransition;
import com.github.azeroth.game.movement.model.SpellEffectExtraData;

import java.time.Duration;
import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.function.Function;


public class MoveSplineInitArgs {

    private static final float MAX_XY_OFFSET = (1 << 10) / 4.0f;
    private static final float MAX_Z_OFFSET = (1 << 9) / 4.0f;

    public ArrayList<Vector3> path = new ArrayList<>();
    public FacingInfo facing = new FacingInfo();
    public MoveSplineFlag flags = new MoveSplineFlag();
    public int pathIdxOffset;
    public float velocity;
    public float parabolicAmplitude;
    public float verticalAcceleration;
    public int effectStartPoint; // fraction of total spline duration
    public Duration effectStartTime;  // absolute value
    public int splineId;
    public float initialOrientation;
    public SpellEffectExtraData spellEffectExtra;
    public AnimTierTransition animTierTransition;
    public boolean walk;
    public boolean hasVelocity;
    public boolean transformForTransport;



    // Returns true to show that the arguments were configured correctly and MoveSpline initialization will succeed.
    public final boolean validate(Unit unit) {

        BiFunction<Boolean, String, Boolean> check = (exp, verbose) -> {
            if (!exp) {
                Logs.MISC_MOVE_SPLINE_INIT_ARGS.error("MoveSplineInitArgs::Validate: expression '{}' failed for {}", verbose, unit);
                return false;
            }
            return true;
        };


        if (!check.apply(path.size() > 1, "path.size() > 1")) {
            return false;
        }
        if (!check.apply(velocity >= 0.01f, "velocity >= 0.01f")) {
            return false;
        }

        if (!check.apply(velocity >= 0.01f || (flags.hasFlag(SplineFlag.Parabolic) && parabolicAmplitude != 0.0f),
                "velocity >= 0.01f || (flags.hasFlag(SplineFlag.Parabolic) && parabolicAmplitude != 0.0f)")) {
            return false;
        }

        if (!check.apply(effectStartPoint < path.size(), "effectStartPoint < path.size()")) {
            return false;
        }

        if (!check.apply(checkPathLengths(), "checkPathLengths()")) {
            return false;
        }

        DbcObjectManager dbcObjectManager = unit.getWorldContext().getDbcObjectManager();
        if (spellEffectExtra != null) {
            if (!check.apply(spellEffectExtra.progressCurveId == 0 || dbcObjectManager.curve().contains(spellEffectExtra.progressCurveId),
                    "spellEffectExtra.progressCurveId == 0 || dbcObjectManager.curve().contains(spellEffectExtra.progressCurveId)")) {
                return false;
            }

            if (!check.apply(spellEffectExtra.parabolicCurveId == 0 || dbcObjectManager.curve().contains(spellEffectExtra.parabolicCurveId),
                    "spellEffectExtra.parabolicCurveId == 0 || dbcObjectManager.curve().contains(spellEffectExtra.parabolicCurveId)")) {
                return false;
            }
        }

        return true;
    }

    private boolean checkPathLengths() {

        Function<Float, Boolean> isValidPackedXYOffset = (coord) -> coord > -MAX_XY_OFFSET && coord < MAX_XY_OFFSET;
        Function<Float, Boolean> isValidPackedZOffset = (coord) -> coord > -MAX_Z_OFFSET && coord < MAX_Z_OFFSET;

        Vector3 middle = new Vector3(path.getFirst()).add(path.getLast()).scl(0.5f);
        for (int i = 1; i < path.size() - 1; ++i)
        {
            if (new Vector3(path.get(i)).sub(middle).len2() < 0.01f)
                return false;

            // when compression is enabled, each point coord is packed into 11 bits (10 for Z)
            if (!flags.hasFlag(SplineFlag.UncompressedPath))
                if (!isValidPackedXYOffset.apply(middle.x - path.get(i).x)
                        || !isValidPackedXYOffset.apply(middle.y - path.get(i).y)
                        || !isValidPackedZOffset.apply(middle.z - path.get(i).z))
                    flags.setFlag(SplineFlag.UncompressedPath);
        }

        return true;
    }
}
