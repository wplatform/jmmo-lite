package com.github.azeroth.game.movement.spline;


import com.badlogic.gdx.math.Vector3;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.movement.FacingInfo;
import com.github.azeroth.game.movement.SpellEffectExtraData;
import com.github.azeroth.game.movement.model.AnimTierTransition;

import java.util.ArrayList;


public class MoveSplineInitArgs {
    public ArrayList<Vector3> path = new ArrayList<>();
    public FacingInfo facing = new FacingInfo();
    public MoveSplineFlag flags = new MoveSplineFlag();
    public int path_Idx_offset;
    public float velocity;
    public float parabolic_amplitude;
    public float vertical_acceleration;
    public float time_perc;
    public int splineId;
    public float initialOrientation;
    public SpellEffectExtraData spellEffectExtra;
    public AnimTierTransition animTier;
    public boolean walk;
    public boolean hasVelocity;
    public boolean transformForTransport;


    public MoveSplineInitArgs() {
        this(16);
    }

    public MoveSplineInitArgs(int path_capacity) {
        path_Idx_offset = 0;
        velocity = 0.0f;
        parabolic_amplitude = 0.0f;
        time_perc = 0.0f;
        splineId = 0;
        initialOrientation = 0.0f;
        hasVelocity = false;
        transformForTransport = true;
    }

    // Returns true to show that the arguments were configured correctly and MoveSpline initialization will succeed.
    public final boolean validate(Unit unit) {

//		bool CHECK(bool exp, bool verbose)
//			{
//				if (!exp)
//				{
//					if (unit)
//						Log.outError(LogFilter.movement, string.format("MoveSplineInitArgs::Validate: expression '{0}' failed for {1}", exp, (verbose ? unit.getDebugInfo() : unit.GUID.toString())));
//					else
//						Log.outError(LogFilter.movement, string.format("MoveSplineInitArgs::Validate: expression '{0}' failed for cyclic spline continuation", exp));
//
//					return false;
//				}
//
//				return true;
//			}

        if (!CHECK(path.size() > 1, true)) {
            return false;
        }

        if (!CHECK(velocity >= 0.01f, true)) {
            return false;
        }

        if (!CHECK(time_perc >= 0.0f && time_perc <= 1.0f, true)) {
            return false;
        }

        if (!CHECK(checkPathLengths(), false)) {
            return false;
        }

        if (spellEffectExtra != null) {
            if (!CHECK(spellEffectExtra.progressCurveId == 0 || CliDB.CurveStorage.containsKey(spellEffectExtra.progressCurveId), false)) {
                return false;
            }

            if (!CHECK(spellEffectExtra.parabolicCurveId == 0 || CliDB.CurveStorage.containsKey(spellEffectExtra.parabolicCurveId), false)) {
                return false;
            }

            if (!CHECK(spellEffectExtra.progressCurveId == 0 || CliDB.CurveStorage.containsKey(spellEffectExtra.progressCurveId), true)) {
                return false;
            }

            if (!CHECK(spellEffectExtra.parabolicCurveId == 0 || CliDB.CurveStorage.containsKey(spellEffectExtra.parabolicCurveId), true)) {
                return false;
            }
        }

        return true;
    }

    private boolean checkPathLengths() {
        if (path.size() > 2 || facing.type == Framework.Constants.MonsterMoveType.NORMAL) {
            for (var i = 0; i < path.size() - 1; ++i) {
                if ((path.get(i + 1) - path.get(i)).length() < 0.1f) {
                    return false;
                }
            }
        }

        return true;
    }
}
