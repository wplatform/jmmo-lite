package com.github.azeroth.game.movement.spline;


import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Vector4;
import com.github.azeroth.common.EnumFlag;
import com.github.azeroth.game.domain.object.Position;
import com.github.azeroth.game.domain.unit.AnimTier;
import com.github.azeroth.game.domain.unit.UnitMoveType;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.domain.unit.MovementFlag;
import com.github.azeroth.game.domain.unit.NPCFlags2;
import com.github.azeroth.game.movement.*;
import com.github.azeroth.game.movement.enums.MonsterMoveType;
import com.github.azeroth.game.movement.enums.PathType;
import com.github.azeroth.game.movement.model.AnimTierTransition;
import com.github.azeroth.game.movement.model.SpellEffectExtraData;
import com.github.azeroth.game.networking.packet.movement.MonsterMove;
import com.github.azeroth.utils.MathUtil;

import java.util.ArrayList;
import java.util.function.Supplier;

public class MoveSplineInit {
    private final Unit unit;
    public MoveSplineInitArgs args = new MoveSplineInitArgs();

    public MoveSplineInit(Unit m) {
        unit = m;
        args.splineId = MotionMaster.newSplineId();

        // Elevators also use MOVEMENTFLAG_ONTRANSPORT but we do not keep track of their position changes
        args.transformForTransport = !unit.getTransGUID().isEmpty();
        // mix existing state into new
        args.flags.setFlag(SplineFlag.CanSwim, unit.canSwim());
        args.walk = unit.hasUnitMovementFlag(MovementFlag.WALKING);
        args.flags.setFlag(SplineFlag.Flying, unit.hasUnitMovementFlag(MovementFlag.CAN_FLY, MovementFlag.DISABLE_GRAVITY));
        args.flags.setFlag(SplineFlag.SmoothGroundPath, true); // enabled by default, CatmullRom mode or client config "pathSmoothing" will disable this
        args.flags.setFlag(SplineFlag.Steering, unit.hasNpcFlag2(NPCFlags2.STEERING));
    }

    public final int launch() {
        var moveSpline = unit.getMoveSpline();

        var transport = !unit.getTransGUID().isEmpty();
        Vector4 realPosition = new Vector4();

        // there is a big chance that current position is unknown if current state is not finalized, need compute it
        // this also allows calculate spline position and update map position in much greater intervals
        // Don't compute for transport movement if the unit is in a motion between two transports
        if (!moveSpline.finalized() && moveSpline.onTransport == transport) {
            realPosition = moveSpline.computePosition();
        } else {
            Position pos;

            if (!transport) {
                pos = unit.getLocation();
            } else {
                pos = unit.getMovementInfo().transport.pos;
            }

            realPosition.set(pos.getX(), pos.getY(), pos.getZ(), pos.getO());
        }

        // should i do the things that user should do? - no.
        if (args.path.isEmpty()) {
            return 0;
        }

        // correct first vertex
        args.path.set(0, new Vector3(realPosition.x, realPosition.y, realPosition.z));
        args.initialOrientation = realPosition.w;
        args.flags.setFlag(SplineFlag.Enter_Cycle, args.flags.hasFlag(SplineFlag.Cyclic));
        moveSpline.onTransport = transport;

        var moveFlags = unit.getMovementInfo().getFlags();

        if (!args.flags.hasFlag(SplineFlag.Backward)) {
            moveFlags.removeFlag(MovementFlag.BACKWARD);
            moveFlags.addFlag(MovementFlag.FORWARD);
        } else {
            moveFlags.removeFlag(MovementFlag.FORWARD);
            moveFlags.addFlag(MovementFlag.BACKWARD);
        }

        if (moveFlags.hasFlag(MovementFlag.ROOT)) {
            moveFlags.removeFlag(MovementFlag.MASK_MOVING);
        }

        if (!args.hasVelocity) {
            // If spline is initialized with SetWalk method it only means we need to select
            // walk move speed for it but not add walk flag to unit

            if (args.walk) {
                moveFlags.addFlag(MovementFlag.WALKING);
            } else {
                moveFlags.removeFlag(MovementFlag.WALKING);
            }

            args.velocity = unit.getSpeed(selectSpeedType(moveFlags));
            var creature = unit.toCreature();

            if (creature != null) {
                if (creature.getHasSearchedAssistance()) {
                    args.velocity *= 0.66f;
                }
            }
        }

        // limit the speed in the same way the client does

        Supplier<Float> speedLimit = () -> {
            if (args.flags.hasFlag(SplineFlag.UnlimitedSpeed))
                return Float.MAX_VALUE;

            if (args.flags.hasFlag(SplineFlag.Falling) || args.flags.hasFlag(SplineFlag.Catmullrom) || args.flags.hasFlag(SplineFlag.Flying) || args.flags.hasFlag(SplineFlag.Parabolic))
                return 50.0f;

            return Math.max(28.0f, unit.getSpeed(UnitMoveType.RUN) * 4.0f);
        };

        args.velocity = Math.min(args.velocity, speedLimit.get());

        if (!args.validate(unit)) {
            return 0;
        }

        moveSpline.initialize(args);

        MonsterMove packet = new MonsterMove();
        packet.moverGUID = unit.getGUID();
        packet.pos = new Vector3(realPosition.x, realPosition.y, realPosition.z);
        packet.initializeSplineData(moveSpline);

        if (transport) {
            packet.splineData.move.transportGUID = unit.getTransGUID();
            packet.splineData.move.vehicleSeat = unit.getTransSeat();
        }

        unit.sendMessageToSet(packet, true);

        return (int)moveSpline.duration();
    }

    public final void stop() {
        var moveSpline = unit.getMoveSpline();

        // No need to stop if we are not moving
        if (moveSpline.finalized()) {
            return;
        }

        var transport = !unit.getTransGUID().isEmpty();
        Vector4 loc = new Vector4();

        if (moveSpline.onTransport == transport) {
            loc = moveSpline.computePosition();
        } else {
            Position pos;

            if (!transport) {
                pos = unit.getLocation();
            } else {
                pos = unit.getMovementInfo().transport.pos;
            }

            loc.set(pos.getX(), pos.getY(), pos.getZ(), pos.getO());
        }

        args.flags.setFlag(SplineFlag.Done);
        unit.removeUnitMovementFlag(MovementFlag.FORWARD);
        moveSpline.onTransport = transport;
        moveSpline.initialize(args);

        MonsterMove packet = new MonsterMove();
        packet.moverGUID = unit.getGUID();
        packet.pos = new Vector3(loc.x, loc.y, loc.z);
        packet.splineData.stopDistanceTolerance = 2;
        packet.splineData.id = moveSpline.getId();
        packet.splineData.move.face = args.facing.type;
        packet.splineData.move.faceDirection = args.facing.angle;


        if (transport) {
            packet.splineData.move.transportGUID = unit.getTransGUID();
            packet.splineData.move.vehicleSeat = unit.getTransSeat();
        }

        unit.sendMessageToSet(packet, true);
    }

    public final void moveTo(Vector3 dest, boolean generatePath) {
        moveTo(dest, generatePath, false);
    }

    public final void moveTo(Vector3 dest) {
        moveTo(dest, true, false);
    }

    public final void moveTo(Vector3 dest, boolean generatePath, boolean forceDestination) {
        if (generatePath) {
            PathGenerator path = new PathGenerator(unit);
            var result = path.calculatePath(new Position(dest.x, dest.y, dest.z), forceDestination);

            if (result && !path.getPathType().hasFlag(PathType.NOPATH)) {
                movebyPath(path.getPath());

                return;
            }
        }

        args.pathIdxOffset = 0;
        TransportPathTransform transform = new TransportPathTransform(unit, args.transformForTransport);
        args.path.set(1, transform.calc(dest));
    }

    public final void setFall() {
        args.flags.enableFalling();
        args.flags.setFlag(SplineFlag.FallingSlow, unit.hasUnitMovementFlag(MovementFlag.FALLING_SLOW));
    }

    public final void setFirstPointId(int pointId) {
        args.pathIdxOffset = pointId;
    }

    public final void setFly() {
        args.flags.enableFlying();
    }

    public final void setWalk(boolean enable) {
        args.walk = enable;
    }

    public final void setSmooth() {
        args.flags.enableCatmullRom();
    }

    public final void setUncompressed() {
        args.flags.setFlag(SplineFlag.UncompressedPath);
    }

    public final void setCyclic() {
        args.flags.setFlag(SplineFlag.Cyclic);
    }

    public final void setVelocity(float vel) {
        args.velocity = vel;
        args.hasVelocity = true;
    }

    public final void setTransportEnter() {
        args.flags.enableTransportEnter();
    }

    public final void setTransportExit() {
        args.flags.enableTransportExit();
    }

    public final void setOrientationFixed(boolean enable) {
        args.flags.setFlag(SplineFlag.OrientationFixed, enable);
    }

    public final void setUnlimitedSpeed() {
        args.flags.setFlag(SplineFlag.UnlimitedSpeed, true);
    }


    public final void movebyPath(Vector3[] controls) {
        movebyPath(controls, 0);
    }

    public final void movebyPath(Vector3[] controls, int path_offset) {
        args.pathIdxOffset = path_offset;
        TransportPathTransform transform = new TransportPathTransform(unit, args.transformForTransport);

        for (var i = 0; i < controls.length; i++) {
            args.path.add(transform.calc(controls[i]));
        }
    }


    public final void moveTo(float x, float y, float z, boolean generatePath) {
        moveTo(x, y, z, generatePath, false);
    }

    public final void moveTo(float x, float y, float z) {
        moveTo(x, y, z, true, false);
    }

    public final void moveTo(float x, float y, float z, boolean generatePath, boolean forceDest) {
        moveTo(new Vector3(x, y, z), generatePath, forceDest);
    }

    public final void setParabolic(float amplitude, int startPoint) {
        args.effectStartPoint = startPoint;
        args.parabolicAmplitude = amplitude;
        args.verticalAcceleration = 0.0f;
        args.flags.enableParabolic();
    }



    public final void setParabolicVerticalAcceleration(float vertical_acceleration, int startPoint) {
        args.effectStartPoint = startPoint;
        args.parabolicAmplitude = 0.0f;
        args.verticalAcceleration = vertical_acceleration;
        args.flags.enableParabolic();
    }

    public final void setAnimation(AnimTier anim, int tierTransitionId, int transitionStartPoint) {
        args.effectStartPoint = transitionStartPoint;
        args.animTierTransition = new AnimTierTransition();
        args.animTierTransition.animTier = anim;
        args.animTierTransition.tierTransitionId = tierTransitionId;
        args.flags.setFlag(SplineFlag.Animation, tierTransitionId == 0);

    }

    public final void setFacing(Vector3 spot) {
        TransportPathTransform transform = new TransportPathTransform(unit, args.transformForTransport);
        var finalSpot = transform.calc(spot);
        args.facing.f = new Vector3(finalSpot.x, finalSpot.y, finalSpot.z);
        args.facing.type = MonsterMoveType.FACING_SPOT;
    }

    public final void setFacing(float angle) {
        TransportPathTransform transform = new TransportPathTransform(unit, args.transformForTransport);
        args.facing.angle = Position.normalizeOrientation(transform.calc(angle));
        args.facing.type = MonsterMoveType.FACING_ANGLE;
    }


    public final void setFacing(Unit target) {
        args.facing.angle = unit.getLocation().getAbsoluteAngle(target.getLocation());
        args.facing.target = target.getGUID();
        args.facing.type = MonsterMoveType.FACING_TARGET;
    }


    public final void disableTransportPathTransformations() {
        args.transformForTransport = false;
    }

    public final void setSpellEffectExtraData(SpellEffectExtraData spellEffectExtraData) {
        args.spellEffectExtra = spellEffectExtraData;
    }

    public final ArrayList<Vector3> path() {
        return args.path;
    }

    private UnitMoveType selectSpeedType(EnumFlag<MovementFlag> moveFlags) {
        if (moveFlags.hasFlag(MovementFlag.FLYING)) {
            if (moveFlags.hasFlag(MovementFlag.BACKWARD)) {
                return UnitMoveType.FLIGHT_BACK;
            } else {
                return UnitMoveType.FLIGHT;
            }
        } else if (moveFlags.hasFlag(MovementFlag.SWIMMING)) {
            if (moveFlags.hasFlag(MovementFlag.BACKWARD)) {
                return UnitMoveType.SWIM_BACK;
            } else {
                return UnitMoveType.SWIM;
            }
        } else if (moveFlags.hasFlag(MovementFlag.WALKING)) {
            return UnitMoveType.WALK;
        } else if (moveFlags.hasFlag(MovementFlag.BACKWARD)) {
            return UnitMoveType.RUN_BACK;
        }

        // Flying creatures use MOVEMENTFLAG_CAN_FLY or MOVEMENTFLAG_DISABLE_GRAVITY
        // Run speed is their default flight speed.
        return UnitMoveType.RUN;
    }

    private void setBackward() {
        args.flags.setFlag(SplineFlag.Backward);
    }

}
