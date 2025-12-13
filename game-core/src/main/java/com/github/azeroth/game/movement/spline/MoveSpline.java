package com.github.azeroth.game.movement.spline;


import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Vector4;
import com.github.azeroth.common.Assert;
import com.github.azeroth.common.Logs;
import com.github.azeroth.game.domain.unit.AnimTier;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.movement.FacingInfo;
import com.github.azeroth.game.movement.enums.MonsterMoveType;
import com.github.azeroth.game.movement.model.SpellEffectExtraData;
import com.github.azeroth.game.movement.Spline;
import com.github.azeroth.game.movement.model.AnimTierTransition;
import com.github.azeroth.game.movement.model.EvaluationMode;
import com.github.azeroth.utils.MathUtil;

import java.util.ArrayList;
import java.util.function.BiFunction;


public class MoveSpline {



    /// Velocity bounds that makes fall speed limited
    private static final float terminalVelocity = 60.148003f;
    private static final float terminalSafefallVelocity = 7.0f;
    private static final float gravity = 19.291103363037109375f;


    private static final float terminal_length = terminalVelocity * terminalVelocity / (2.0f * gravity);
    private static final float terminal_safeFall_length = (terminalSafefallVelocity * terminalSafefallVelocity) / (2.0f * gravity);
    private static final float terminal_fallTime = terminalVelocity / gravity; // the time that needed to reach terminalVelocity
    private static final float terminal_safeFall_fallTime = terminalSafefallVelocity / gravity; // the time that needed to reach terminalVelocity with safefall



    public MoveSplineInitArgs initArgs;
    public Spline spline = new Spline();
    public FacingInfo facing;
    public int id;
    public MoveSplineFlag splineFlags = new MoveSplineFlag();
    public int timePassed;
    // currently duration mods are unused, but its _currently_
    //float           duration_mod;
    //float           duration_mod_next;
    public float verticalAcceleration;
    public float initialOrientation;
    public int effectStartTime;
    public int pointIdx;
    public int pointIdxOffset;
    public float velocity;
    public SpellEffectExtraData spellEffectExtra;
    public AnimTierTransition animTierTransition;
    public boolean onTransport;
    public boolean splineIsFacingOnly;
    public Unit owner;

    public MoveSpline(Unit owner) {
        splineFlags.setFlag(SplineFlag.Done);
        this.owner = owner;
    }

    public static float computeFallElevation(float t_passed, boolean isSafeFall) {
        return computeFallElevation(t_passed, isSafeFall, 0.0f);
    }

    public static float computeFallElevation(float t_passed, boolean isSafeFall, float start_velocity) {
        float termVel;
        float result;

        if (isSafeFall) {
            termVel = terminalSafefallVelocity;
        } else {
            termVel = terminalVelocity;
        }

        if (start_velocity > termVel) {
            start_velocity = termVel;
        }

        var terminal_time = (float) ((isSafeFall ? terminal_safeFall_fallTime : terminal_fallTime) - start_velocity / gravity); // the time that needed to reach terminalVelocity

        if (t_passed > terminal_time) {
            result = termVel * (t_passed - terminal_time) + start_velocity * terminal_time + gravity * terminal_time * terminal_time * 0.5f;
        } else {
            result = t_passed * (start_velocity + t_passed * gravity * 0.5f);
        }

        return result;
    }

    public final void initialize(MoveSplineInitArgs args) {
        splineFlags = args.flags;
        facing = args.facing;
        id = args.splineId;
        pointIdxOffset = args.pathIdxOffset;
        initialOrientation = args.initialOrientation;

        timePassed = 0;
        verticalAcceleration = 0.0f;
        effectStartTime = 0;
        spellEffectExtra = args.spellEffectExtra;
        animTierTransition = args.animTierTransition;
        splineIsFacingOnly = args.path.size() == 2 && args.facing.type != MonsterMoveType.NORMAL && ((args.path.get(1).sub(args.path.get(0))).len() < 0.1f);

        velocity = args.velocity;

        // Check if its a stop spline
        if (args.flags.hasFlag(SplineFlag.Done)) {
            spline.clear();
            return;
        }

        initSpline(args);


        // init parabolic / animation
        // spline initialized, duration known and i able to compute parabolic acceleration
        if (args.flags.hasAnyFlag(SplineFlag.Parabolic, SplineFlag.Animation, SplineFlag.FadeObject)) {

            float spline_duration = duration();

            effectStartTime = (int)spline.length(spline.first() + (int)args.effectStartTime.toMillis());
            if (effectStartTime > spline_duration)
                effectStartTime = (int) spline_duration;


            if (args.flags.hasFlag(SplineFlag.Parabolic) && effectStartTime < duration()) {
                if (args.parabolicAmplitude != 0.0f) {
                    var f_duration = MSToSec((int) (duration() - effectStartTime));
                    verticalAcceleration = args.parabolicAmplitude * 8.0f / (f_duration * f_duration);
                } else if (args.verticalAcceleration != 0.0f) {
                    verticalAcceleration = args.verticalAcceleration;
                }
            }
        }
    }

    public final int currentPathIdx() {
        var point = pointIdxOffset + pointIdx - spline.first() + (finalized() ? 1 : 0);

        if (isCyclic()) {
            point %= (spline.last() - spline.first());
        }

        return point;
    }

    public final Vector3[] getPath() {
        return spline.getPoints().toArray(new Vector3[0]);
    }

    public final int timePassed() {
        return timePassed;
    }

    public final float duration() {
        return spline.length();
    }

    public final int currentSplineIdx() {
        return pointIdx;
    }

    public final int getId() {
        return id;
    }

    public final boolean finalized() {
        return splineFlags.hasFlag(SplineFlag.Done);
    }

    public final Vector4 computePosition(int timePoint, int pointIndex) {
        Assert.isTrue(initialized());

        var u = 1.0f;
        float seg_time = spline.length(pointIndex, pointIndex + 1);

        if (seg_time > 0) {
            u = Math.min((timePoint - spline.length(pointIndex)) / seg_time, 1.0f);
        }

        var orientation = initialOrientation;
        Vector3 c = new Vector3();
        spline.evaluate_percent(pointIndex, u, c);

        if (splineFlags.hasFlag(SplineFlag.Parabolic)) {
            c.z = computeParabolicElevation(timePoint, c.z);
        } else if (splineFlags.hasFlag(SplineFlag.Falling)) {
            c.z = computeFallElevation(timePoint);
        }

        if (splineFlags.hasFlag(SplineFlag.Done) && facing.type != MonsterMoveType.NORMAL) {
            if (facing.type == MonsterMoveType.FACING_ANGLE ) {
                orientation = facing.angle;
            } else if (facing.type == MonsterMoveType.FACING_SPOT) {
                orientation = (float) Math.atan2(facing.f.y - c.y, facing.f.x - c.x);
            }
            //nothing to do for MoveSplineFlag.Final_Target flag
        } else {
            if (!splineFlags.hasAnyFlag(SplineFlag.OrientationFixed, SplineFlag.Falling, SplineFlag.JumpOrientationFixed)) {
                Vector3 hermite = new Vector3();
                spline.evaluate_derivative(pointIdx, u, hermite);
                if (hermite.x != 0f || hermite.y != 0f) {
                    orientation = (float) Math.atan2(hermite.y, hermite.x);
                }
            }

            if (splineFlags.hasFlag(SplineFlag.Backward)) {
                orientation -= MathUtil.PI;
            }
        }

        return new Vector4(c.x, c.y, c.z, orientation);
    }

    public final Vector4 computePosition() {
        return computePosition(timePassed, pointIdx);
    }

    public final Vector4 computePosition(int time_offset) {

        var time_point = timePassed + time_offset;

        if (time_point >= duration()) {
            return computePosition((int) duration(), spline.last() - 1);
        }

        if (time_point <= 0) {
            return computePosition(0, spline.first());
        }

        // find point_index where spline.length(point_index) < time_point < spline.length(point_index + 1)
        var point_index = pointIdx;

        while (time_point >= spline.length(point_index + 1)) {
            ++point_index;
        }

        while (time_point < spline.length(point_index)) {
            --point_index;
        }

        return computePosition(time_point, point_index);
    }

    public final float computeParabolicElevation(int time_point, float el) {
        if (time_point > effectStartTime) {
            var t_passedf = MSToSec(time_point - effectStartTime);
            var t_durationf = MSToSec((int) (duration() - effectStartTime)); //client use not modified duration here

            if (spellEffectExtra != null && spellEffectExtra.parabolicCurveId != 0) {
                t_passedf *= owner.getWorldContext().getDbcObjectManager().getCurveValueAt(spellEffectExtra.parabolicCurveId, (float) time_point / duration());
            }
            // -a*x*x + bx + c:
            //(dur * v3->z_acceleration * dt)/2 - (v3->z_acceleration * dt * dt)/2 + Z;
            el += (t_durationf - t_passedf) * 0.5f * verticalAcceleration * t_passedf;
        }
        return el;
    }

    public final float computeFallElevation(int time_point) {

        var z_now = spline.getPoint(spline.first()).z - computeFallElevation(MSToSec(time_point), false);
        var final_z = finalDestination().z;
        return Math.max(z_now, final_z);
    }

    public final boolean hasStarted() {
        return timePassed > 0;
    }

    public final void interrupt() {
        splineFlags.setFlag(SplineFlag.Done);
    }


    ///#region Fields

    public final void updateState(int diffTime) {
        do {

            if (finalized()) {
                break;
            }

            var minimal_diff = Math.min(diffTime, segmentTimeElapsed());
            timePassed += minimal_diff;
            diffTime -= minimal_diff;

            if (timePassed >= nextTimestamp()) {
                ++pointIdx;

                if (spline.isCyclic()) {
                    pointIdx = spline.first();
                    timePassed %= (int) duration();
                    // Remove first point from the path after one full cycle.
                    // That point was the position of the unit prior to entering the cycle and it shouldn't be repeated with continuous cycles.
                    if (splineFlags.hasFlag(SplineFlag.Enter_Cycle)) {
                        splineFlags.setFlag(SplineFlag.Enter_Cycle, false);
                        reinit_spline_for_next_cycle();
                    }
                } else {
                    _Finalize();
                    diffTime = 0;
                }
            }
        } while (diffTime > 0);
    }

    public final boolean isCyclic() {
        return splineFlags.hasFlag(SplineFlag.Cyclic);
    }

    public final boolean isFalling() {
        return splineFlags.hasFlag(SplineFlag.Falling);
    }

    public final boolean initialized() {
        return !spline.empty();
    }

    public final Vector3 finalDestination() {
        return initialized() ? spline.getPoint(spline.last()) : Vector3.Zero;
    }

    public final Vector3 currentDestination() {
        return initialized() ? spline.getPoint(pointIdx + 1) : Vector3.Zero;
    }

    public final AnimTier getAnimation() {
        return animTierTransition != null ? animTierTransition.animTier : null;
    }

    private void initSpline(MoveSplineInitArgs args) {
        EvaluationMode mode = args.flags.isSmooth() ? EvaluationMode.Catmullrom : EvaluationMode.Linear;
        if (args.flags.hasFlag(SplineFlag.Cyclic)) {
            var cyclic_point = 0;

            if (splineFlags.hasFlag(SplineFlag.Enter_Cycle)) {
                cyclic_point = 1; // shouldn't be modified, came from client
            }

            spline.init_cyclic_spline(args.path.toArray(new Vector3[0]), args.path.size(), mode, cyclic_point, args.initialOrientation);
        } else {
            spline.init_spline(args.path.toArray(new Vector3[0]), args.path.size(), mode, args.initialOrientation);
        }

        // init spline timestamps
        if (splineFlags.hasFlag(SplineFlag.Falling)) {
            spline.initLengths(newFallInitializer(spline.getPoint(spline.first()).z));
        } else if (splineFlags.hasFlag(SplineFlag.Parabolic) && args.velocity < 0.01f) {
            spline.initLengths(newParabolicInPlaceInitializer(args.parabolicAmplitude));
        }else {
            spline.initLengths(newCommonInitializer(args.velocity));
        }

        // TODO: what to do in such cases? problem is in input data (all points are at same coords)
        if (spline.length() < 1) {
            Logs.MISC.error("MoveSpline::init_spline: zero length spline, wrong input data?");
            spline.setLength(spline.last(), spline.isCyclic() ? 1000 : 1);
        }

        pointIdx = spline.first();
    }

    private void _Finalize() {
        splineFlags.setFlag(SplineFlag.Done);
        pointIdx = spline.last() - 1;
        timePassed = (int) duration();
    }

    private float MSToSec(int ms) {
        return ms / 1000.0f;
    }

    private int nextTimestamp() {
        return (int) spline.length(pointIdx + 1);
    }

    private int segmentTimeElapsed() {
        return nextTimestamp() - timePassed;
    }


    private BiFunction<Spline, Integer, Float> newCommonInitializer(float velocity) {
        return (s, i) -> {
            float velocityInv = 1000f / velocity;
            float time = 1; //minimal_duration;
            return time + (s.SegLength(i) * velocityInv);
        };
    }

    private BiFunction<Spline, Integer, Float> newFallInitializer(float startElevation) {
        return (s, i) -> {
            float path_length = startElevation - s.getPoint(i + 1).z;
            return computeFallTime(path_length, false) * 1000.f;
        };
    }


    private BiFunction<Spline, Integer, Float> newParabolicInPlaceInitializer(float parabolic_amplitude) {
        float time = 1; //minimal_duration;
        return (s, i) -> time + computeFallTime(parabolic_amplitude, false) * 1000.f;

    }

    private static float computeFallTime(float path_length, boolean isSafeFall) {
        if (path_length < 0.0f) {
            return 0f;
        }
        float time;

        if (isSafeFall) {
            if (path_length >= terminal_safeFall_length) {
                time = (path_length - terminal_safeFall_length) / terminalSafefallVelocity + terminal_safeFall_fallTime;
            } else {
                time = (float) Math.sqrt(2.0f * path_length / gravity);
            }
        } else {
            if (path_length >= terminal_length) {
                time = (path_length - terminal_length) / terminalVelocity + terminal_fallTime;
            } else {
                time = (float) Math.sqrt(2.0f * path_length / gravity);
            }
        }
        return time;
    }


    private void reinit_spline_for_next_cycle() {
        MoveSplineInitArgs args = new MoveSplineInitArgs();
        args.path = new ArrayList<>(spline.getPoints().subList(spline.first() + 1, spline.last()));
        args.facing = facing;
        args.flags = splineFlags;
        args.pathIdxOffset = pointIdxOffset;
        args.splineId = id;
        args.initialOrientation = initialOrientation;
        args.velocity = 1.0f; // Calculated below
        args.hasVelocity = true;
        args.transformForTransport = onTransport;
        if (args.validate(null)) {
            // New cycle should preserve previous cycle's duration for some weird reason, even though
            // the path is really different now. Blizzard is weird. Or this was just a simple oversight.
            // Since our splines precalculate length with velocity in mind, if we want to find the desired
            // velocity, we have to make a fake spline, calculate its duration and then compare it to the
            // desired duration, thus finding out how much the velocity has to be increased for them to match.
            MoveSpline tempSpline = new MoveSpline(owner);
            tempSpline.initialize(args);
            args.velocity = tempSpline.duration() / duration();

            if (args.validate(null))
                initSpline(args);
        }
    }

    ///#endregion
}
