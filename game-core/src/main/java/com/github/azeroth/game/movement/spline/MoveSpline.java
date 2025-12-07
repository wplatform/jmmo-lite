package com.github.azeroth.game.movement.spline;


import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Vector4;
import com.github.azeroth.common.Assert;
import com.github.azeroth.game.domain.unit.AnimTier;
import com.github.azeroth.game.movement.FacingInfo;
import com.github.azeroth.game.movement.IInitializer;
import com.github.azeroth.game.movement.model.SpellEffectExtraData;
import com.github.azeroth.game.movement.Spline;
import com.github.azeroth.game.movement.model.AnimTierTransition;
import com.github.azeroth.game.movement.model.EvaluationMode;

import java.util.Arrays;
import java.util.HashMap;


public class MoveSpline {
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

    public MoveSpline() {
        id = 0;
        timePassed = 0;
        verticalAcceleration = 0.0f;
        initialOrientation = 0.0f;
        effectStartTime = 0;
        pointIdx = 0;
        pointIdxOffset = 0;
        onTransport = false;
        splineIsFacingOnly = false;
        splineFlags.setFlag(SplineFlag.Done);
    }

    public static float computeFallElevation(float t_passed, boolean isSafeFall) {
        return computeFallElevation(t_passed, isSafeFall, 0.0f);
    }

    public static float computeFallElevation(float t_passed, boolean isSafeFall, float start_velocity) {
        float termVel;
        float result;

        if (isSafeFall) {
            termVel = SharedConst.terminalSafefallVelocity;
        } else {
            termVel = SharedConst.terminalVelocity;
        }

        if (start_velocity > termVel) {
            start_velocity = termVel;
        }

        var terminal_time = (float) ((isSafeFall ? SharedConst.terminal_safeFall_fallTime : SharedConst.terminal_fallTime) - start_velocity / SharedConst.gravity); // the time that needed to reach terminalVelocity

        if (t_passed > terminal_time) {
            result = termVel * (t_passed - terminal_time) + start_velocity * terminal_time + (float) SharedConst.gravity * terminal_time * terminal_time * 0.5f;
        } else {
            result = t_passed * (float) (start_velocity + t_passed * SharedConst.gravity * 0.5f);
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
        turn = args.turnData;
        spellEffectExtra = args.spellEffectExtra;
        animTierTransition = args.animTierTransition;
        splineIsFacingOnly = args.path.size() == 2 && args.facing.type != MonsterMoveType.NORMAL && ((args.path.get(1) - args.path.get(0)).length() < 0.1f);

        velocity = args.velocity;

        // Check if its a stop spline
        if (args.flags.hasFlag(SplineFlag.Done)) {
            synchronized (spline) {
                spline.clear();
            }

            return;
        }


        synchronized (spline) {
            initSpline(args);
        }

        // init parabolic / animation
        // spline initialized, duration known and i able to compute parabolic acceleration
        if (args.flags.hasFlag(SplineFlag.Parabolic.getValue() | SplineFlag.Animation.getValue().getValue() | SplineFlag.FadeObject.getValue().getValue())) {
            effectStartTime = (int) (duration() * args.time_perc);

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
        synchronized (spline) {
            var point = pointIdxOffset + pointIdx - spline.first() + (finalized() ? 1 : 0);

            if (isCyclic()) {
                point %= (spline.last() - spline.first());
            }

            return point;
        }
    }

    public final Vector3[] getPath() {
        synchronized (spline) {
            return spline.getPoints();
        }
    }

    public final int timePassed() {
        return timePassed;
    }

    public final int duration() {
        synchronized (spline) {
            return spline.length();
        }
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
        synchronized (spline) {
            var u = 1.0f;
            int seg_time = spline.length(pointIndex, pointIndex + 1);

            if (seg_time > 0) {
                u = (timePoint - spline.length(pointIndex)) / (float) seg_time;
            }

            var orientation = initialOrientation;
            Vector3 c;
            tangible.OutObject<Vector3> tempOut_c = new tangible.OutObject<Vector3>();
            spline.evaluate_Percent(pointIndex, u, tempOut_c);
            c = tempOut_c.outArgValue;

            if (splineFlags.hasFlag(SplineFlag.Parabolic)) {
                tangible.RefObject<Float> tempRef_Z = new tangible.RefObject<Float>(c.Z);
                computeParabolicElevation(timePoint, tempRef_Z);
                c.Z = tempRef_Z.refArgValue;
            } else if (splineFlags.hasFlag(SplineFlag.Falling)) {

                computeFallElevation(timePoint, ref c.Z);
            }

            if (splineFlags.hasFlag(SplineFlag.Done) && facing.type != MonsterMoveType.NORMAL) {
                if (facing.type == MonsterMoveType.FacingAngle) {
                    orientation = facing.angle;
                } else if (facing.type == MonsterMoveType.FacingSpot) {
                    orientation = (float) Math.atan2(facing.f.Y - c.Y, facing.f.X - c.X);
                }
                //nothing to do for MoveSplineFlag.Final_Target flag
            } else {
                if (!splineFlags.hasFlag(SplineFlag.OrientationFixed.getValue() | SplineFlag.Falling.getValue().getValue() | SplineFlag.JumpOrientationFixed.getValue().getValue())) {
                    Vector3 hermite;
                    tangible.OutObject<Vector3> tempOut_hermite = new tangible.OutObject<Vector3>();
                    spline.evaluate_Derivative(pointIdx, u, tempOut_hermite);
                    hermite = tempOut_hermite.outArgValue;

                    if (hermite.X != 0f || hermite.Y != 0f) {
                        orientation = (float) Math.atan2(hermite.Y, hermite.X);
                    }
                }

                if (splineFlags.hasFlag(SplineFlag.Backward)) {
                    orientation -= (float) Math.PI;
                }
            }

            return new Vector4(c.X, c.Y, c.Z, orientation);
        }
    }

    public final Vector4 computePosition() {
        return computePosition(timePassed, pointIdx);
    }

    public final Vector4 computePosition(int time_offset) {
        synchronized (spline) {
            var time_point = timePassed + time_offset;

            if (time_point >= duration()) {
                return computePosition(duration(), spline.last() - 1);
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
    }

    public final void computeParabolicElevation(int time_point, tangible.RefObject<Float> el) {
        if (time_point > effectStartTime) {
            var t_passedf = MSToSec((int) (time_point - effectStartTime));
            var t_durationf = MSToSec((int) (duration() - effectStartTime)); //client use not modified duration here

            if (spellEffectExtra != null && spellEffectExtra.parabolicCurveId != 0) {
                t_passedf *= global.getDB2Mgr().GetCurveValueAt(spellEffectExtra.parabolicCurveId, (float) time_point / duration());
            }

            el.refArgValue += (t_durationf - t_passedf) * 0.5f * verticalAcceleration * t_passedf;
        }
    }

    public final void computeFallElevation(int time_point, tangible.RefObject<Float> el) {

        synchronized (spline) {
            var z_now = spline.getPoint(spline.first()).Z - computeFallElevation(MSToSec((int) time_point), false);
            var final_z = finalDestination().Z;
            el.refArgValue = Math.max(z_now, final_z);
        }
    }

    public final boolean hasStarted() {
        return timePassed > 0;
    }

    public final void interrupt() {
        splineFlags.setUnsetFlag(SplineFlag.Done);
    }


    ///#region Fields

    public final void updateState(int difftime) {
        do {
            tangible.RefObject<Integer> tempRef_difftime = new tangible.RefObject<Integer>(difftime);
            updateState(tempRef_difftime);
            difftime = tempRef_difftime.refArgValue;
        } while (difftime > 0);
    }

    public final boolean isCyclic() {
        return splineFlags.hasFlag(SplineFlag.Cyclic);
    }

    public final boolean isFalling() {
        return splineFlags.hasFlag(SplineFlag.Falling);
    }

    public final boolean initialized() {
        synchronized (spline) {
            return !spline.isEmpty();
        }
    }

    public final Vector3 finalDestination() {
        synchronized (spline) {
            return initialized() ? spline.getPoint(spline.last()) : Vector3.Zero;
        }
    }

    public final Vector3 currentDestination() {
        synchronized (spline) {
            return initialized() ? spline.getPoint(pointIdx + 1) : Vector3.Zero;
        }
    }

    public final AnimTier getAnimation() {
        return animTierTransition != null ? animTier.forValue(animTierTransition.animTier) : null;
    }

    private void initSpline(MoveSplineInitArgs args) {
        var modes = new EvaluationMode[]{EvaluationMode.Linear, EvaluationMode.Catmullrom};

        if (args.flags.hasFlag(SplineFlag.Cyclic)) {
            var cyclic_point = 0;

            if (splineFlags.hasFlag(SplineFlag.EnterCycle)) {
                cyclic_point = 1; // shouldn't be modified, came from client
            }

            spline.initCyclicSpline(args.path.toArray(new Vector3[0]), args.path.size(), modes[(int) args.flags.isSmooth()], cyclic_point, args.initialOrientation);
        } else {
            spline.initSpline(args.path.toArray(new Vector3[0]), args.path.size(), modes[(int) args.flags.isSmooth()], args.initialOrientation);
        }

        // init spline timestamps
        if (splineFlags.hasFlag(SplineFlag.Falling)) {
            FallInitializer init = new FallInitializer(spline.getPoint(spline.first()).Z);
            spline.initLengths(init);
        } else {
            CommonInitializer init = new CommonInitializer(args.velocity);
            spline.initLengths(init);
        }

        // TODO: what to do in such cases? problem is in input data (all points are at same coords)
        if (spline.length() < 1) {
            Log.outError(LogFilter.unit, "MoveSpline.init_spline: zero length spline, wrong input data?");
            spline.set_length(spline.last(), spline.isCyclic() ? 1000 : 1);
        }

        pointIdx = spline.first();
    }

    private void _Finalize() {
        splineFlags.setUnsetFlag(SplineFlag.Done);
        pointIdx = spline.last() - 1;
        timePassed = duration();
    }

    private float MSToSec(int ms) {
        return ms / 1000.0f;
    }

    private UpdateResult updateState(tangible.RefObject<Integer> ms_time_diff) {
        synchronized (spline) {
            if (finalized()) {
                ms_time_diff.refArgValue = 0;

                return UpdateResult.Arrived;
            }

            var result = UpdateResult.NONE;
            var minimal_diff = Math.min(ms_time_diff.refArgValue, segmentTimeElapsed());
            timePassed += minimal_diff;
            ms_time_diff.refArgValue -= minimal_diff;

            if (timePassed >= nextTimestamp()) {
                ++pointIdx;

                if (pointIdx < spline.last()) {
                    result = UpdateResult.NextSegment;
                } else {
                    if (spline.isCyclic()) {
                        pointIdx = spline.first();
                        timePassed %= duration();
                        result = UpdateResult.NextCycle;

                        // Remove first point from the path after one full cycle.
                        // That point was the position of the unit prior to entering the cycle and it shouldn't be repeated with continuous cycles.
                        if (splineFlags.hasFlag(SplineFlag.EnterCycle)) {
                            splineFlags.setUnsetFlag(SplineFlag.EnterCycle, false);

                            MoveSplineInitArgs args = new moveSplineInitArgs(spline.getPointCount());
                            args.path.addAll(Arrays.asList(spline.getPoints().AsSpan().Slice(spline.first() + 1, spline.last()).ToArray()));
                            args.facing = facing;
                            args.flags = splineFlags;
                            args.pathIdxOffset = pointIdxOffset;
                            // MoveSplineFlag::Parabolic | MoveSplineFlag::Animation not supported currently
                            //args.parabolic_amplitude = ?;
                            //args.time_perc = ?;
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
                                MoveSpline tempSpline = new moveSpline();
                                tempSpline.initialize(args);
                                args.velocity = (float) tempSpline.duration() / duration();

                                if (args.validate(null)) {
                                    initSpline(args);
                                }
                            }
                        }
                    } else {
                        _Finalize();
                        ms_time_diff.refArgValue = 0;
                        result = UpdateResult.Arrived;
                    }
                }
            }

            return result;
        }
    }

    private int nextTimestamp() {
        return spline.length(pointIdx + 1);
    }

    private int segmentTimeElapsed() {
        return nextTimestamp() - timePassed;
    }
    public enum UpdateResult {
        NONE(0x01),
        Arrived(0x02),
        NextCycle(0x04),
        NextSegment(0x08);

        public static final int SIZE = Integer.SIZE;
        private static HashMap<Integer, UpdateResult> mappings;
        private int intValue;

        private UpdateResult(int value) {
            intValue = value;
            getMappings().put(value, this);
        }

        private static HashMap<Integer, UpdateResult> getMappings() {
            if (mappings == null) {
                synchronized (UpdateResult.class) {
                    if (mappings == null) {
                        mappings = new HashMap<Integer, UpdateResult>();
                    }
                }
            }
            return mappings;
        }

        public static UpdateResult forValue(int value) {
            return getMappings().get(value);
        }

        public int getValue() {
            return intValue;
        }
    }

    public static class CommonInitializer implements IInitializer<Integer> {
        public float velocityInv;
        public int time;

        public CommonInitializer(float _velocity) {
            velocityInv = 1000f / _velocity;
            time = 1;
        }

        public final int invoke(spline<Integer> s, int i) {
            time += (int) (s.segLength(i) * velocityInv);

            return time;
        }
    }

    public static class FallInitializer implements IInitializer<Integer> {
        private final float startElevation;

        public FallInitializer(float startelevation) {
            startElevation = startelevation;
        }

        public final int invoke(spline<Integer> s, int i) {
            return (int) (computeFallTime(startElevation - s.getPoint(i + 1).Z, false) * 1000.0f);
        }

        private float computeFallTime(float path_length, boolean isSafeFall) {
            if (path_length < 0.0f) {
                return 0.0f;
            }

            float time;

            if (isSafeFall) {
                if (path_length >= SharedConst.terminal_safeFall_length) {
                    time = (path_length - SharedConst.terminal_safeFall_length) / SharedConst.terminalSafefallVelocity + SharedConst.terminal_safeFall_fallTime;
                } else {
                    time = (float) Math.sqrt(2.0f * path_length / SharedConst.gravity);
                }
            } else {
                if (path_length >= SharedConst.terminal_length) {
                    time = (path_length - SharedConst.terminal_length) / SharedConst.terminalVelocity + SharedConst.terminal_fallTime;
                } else {
                    time = (float) Math.sqrt(2.0f * path_length / SharedConst.gravity);
                }
            }

            return time;
        }
    }


    ///#endregion
}
