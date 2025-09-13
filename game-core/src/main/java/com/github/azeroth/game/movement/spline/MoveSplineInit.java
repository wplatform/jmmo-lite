package com.github.azeroth.game.movement.spline;


import com.badlogic.gdx.math.Vector4;
import com.github.azeroth.game.domain.object.Position;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.domain.unit.MovementFlag;
import com.github.azeroth.game.domain.unit.NPCFlags2;
import com.github.azeroth.game.movement.*;
import com.github.azeroth.game.movement.enums.PathType;

import java.util.ArrayList;

public class MoveSplineInit {
    private final Unit unit;
    public MoveSplineInitArgs args = new MoveSplineInitArgs();

    public MoveSplineInit(Unit m) {
        unit = m;
        args.splineId = MotionMaster.getSplineId();

        // Elevators also use MOVEMENTFLAG_ONTRANSPORT but we do not keep track of their position changes
        args.transformForTransport = !unit.getTransGUID().isEmpty();
        // mix existing state into new
        args.flags.setUnsetFlag(SplineFlag.CanSwim, unit.canSwim());
        args.walk = unit.hasUnitMovementFlag(MovementFlag.WALKING);
        args.flags.setUnsetFlag(SplineFlag.Flying, unit.hasUnitMovementFlag(MovementFlag.CAN_FLY.addFlag(MovementFlag.DISABLE_GRAVITY)));
        args.flags.setUnsetFlag(SplineFlag.SmoothGroundPath, true); // enabled by default, CatmullRom mode or client config "pathSmoothing" will disable this
        args.flags.setUnsetFlag(SplineFlag.Steering, unit.hasNpcFlag2(NPCFlags2.STEERING));
    }

    public final int launch() {
        var move_spline = unit.getMoveSpline();

        var transport = !unit.getTransGUID().isEmpty();
        Vector4 real_position = new Vector4();

        // there is a big chance that current position is unknown if current state is not finalized, need compute it
        // this also allows calculate spline position and update map position in much greater intervals
        // Don't compute for transport movement if the unit is in a motion between two transports
        if (!move_spline.finalized() && move_spline.onTransport == transport) {
            real_position = move_spline.computePosition();
        } else {
            Position pos;

            if (!transport) {
                pos = unit.getLocation();
            } else {
                pos = unit.getMovementInfo().transport.pos;
            }

            real_position.X = pos.getX();
            real_position.Y = pos.getY();
            real_position.Z = pos.getZ();
            real_position.W = unit.getLocation().getO();
        }

        // should i do the things that user should do? - no.
        if (args.path.isEmpty()) {
            return 0;
        }

        // correct first vertex
        args.path.set(0, new Vector3(real_position.X, real_position.Y, real_position.Z));
        args.initialOrientation = real_position.W;
        args.flags.setUnsetFlag(SplineFlag.EnterCycle, args.flags.hasFlag(SplineFlag.Cyclic));
        move_spline.onTransport = transport;

        var moveFlags = unit.getMovementInfo().getMovementFlags();

        if (!args.flags.hasFlag(SplineFlag.Backward)) {
            moveFlags = MovementFlag.forValue((moveFlags.getValue() & ~MovementFlag.Backward.getValue()).getValue() | MovementFlag.Forward.getValue());
        } else {
            moveFlags = MovementFlag.forValue((moveFlags.getValue() & ~MovementFlag.Forward.getValue()).getValue() | MovementFlag.Backward.getValue());
        }

        if ((boolean) (moveFlags.getValue() & MovementFlag.Root.getValue())) {
            moveFlags = MovementFlag.forValue(moveFlags.getValue() & ~MovementFlag.MaskMoving.getValue());
        }

        if (!args.hasVelocity) {
            // If spline is initialized with SetWalk method it only means we need to select
            // walk move speed for it but not add walk flag to unit
            var moveFlagsForSpeed = moveFlags;

            if (args.walk) {
                moveFlagsForSpeed = MovementFlag.forValue(moveFlagsForSpeed.getValue() | MovementFlag.Walking.getValue());
            } else {
                moveFlagsForSpeed = MovementFlag.forValue(moveFlagsForSpeed.getValue() & ~MovementFlag.Walking.getValue());
            }

            args.velocity = unit.getSpeed(selectSpeedType(moveFlagsForSpeed));
            var creature = unit.toCreature();

            if (creature != null) {
                if (creature.getHasSearchedAssistance()) {
                    args.velocity *= 0.66f;
                }
            }
        }

        // limit the speed in the same way the client does

//		float speedLimit()
//			{
//				if (args.flags.hasFlag(SplineFlag.UnlimitedSpeed))
//					return float.maxValue;
//
//				if (args.flags.hasFlag(SplineFlag.Falling) || args.flags.hasFlag(SplineFlag.Catmullrom) || args.flags.hasFlag(SplineFlag.Flying) || args.flags.hasFlag(SplineFlag.Parabolic))
//					return 50.0f;
//
//				return Math.max(28.0f, unit.getSpeed(UnitMoveType.run) * 4.0f);
//			}

        ;

        args.velocity = Math.min(args.velocity, speedLimit());

        if (!args.validate(unit)) {
            return 0;
        }

        unit.getMovementInfo().setMovementFlags(moveFlags);
        move_spline.initialize(args);

        MonsterMove packet = new MonsterMove();
        packet.moverGUID = unit.getGUID();
        packet.pos = new Vector3(real_position.X, real_position.Y, real_position.Z);
        packet.initializeSplineData(move_spline);

        if (transport) {
            packet.splineData.move.transportGUID = unit.getTransGUID();
            packet.splineData.move.vehicleSeat = unit.getTransSeat();
        }

        unit.sendMessageToSet(packet, true);

        return move_spline.duration();
    }

    public final void stop() {
        var move_spline = unit.getMoveSpline();

        // No need to stop if we are not moving
        if (move_spline.finalized()) {
            return;
        }

        var transport = !unit.getTransGUID().isEmpty();
        Vector4 loc = new Vector4();

        if (move_spline.onTransport == transport) {
            loc = move_spline.computePosition();
        } else {
            Position pos;

            if (!transport) {
                pos = unit.getLocation();
            } else {
                pos = unit.getMovementInfo().transport.pos;
            }

            loc.X = pos.getX();
            loc.Y = pos.getY();
            loc.Z = pos.getZ();
            loc.W = unit.getLocation().getO();
        }

        args.flags.flags = SplineFlag.Done;
        unit.getMovementInfo().removeMovementFlag(MovementFlag.Forward);
        move_spline.onTransport = transport;
        move_spline.initialize(args);

        MonsterMove packet = new MonsterMove();
        packet.moverGUID = unit.getGUID();
        packet.pos = new Vector3(loc.X, loc.Y, loc.Z);
        packet.splineData.stopDistanceTolerance = 2;
        packet.splineData.id = move_spline.getId();

        if (transport) {
            packet.splineData.move.transportGUID = unit.getTransGUID();
            packet.splineData.move.vehicleSeat = unit.getTransSeat();
        }

        unit.sendMessageToSet(packet, true);
    }

    public final void setFacing(Unit target) {
        args.facing.angle = unit.getLocation().getAbsoluteAngle(target.getLocation());
        args.facing.target = target.getGUID();
        args.facing.type = MonsterMoveType.FacingTarget;
    }

    public final void setFacing(float angle) {
        if (args.transformForTransport) {
            var vehicle = unit.getVehicleBase();

            if (vehicle != null) {
                angle -= vehicle.getLocation().getO();
            } else {
                var transport = unit.getTransport();

                if (transport != null) {
                    angle -= transport.getTransportOrientation();
                }
            }
        }

        args.facing.angle = MathUtil.wrap(angle, 0.0f, MathUtil.TwoPi);
        args.facing.type = MonsterMoveType.FacingAngle;
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
            var result = path.calculatePath(new Position(dest), forceDestination);

            if (result && !(boolean) (path.getPathType().getValue() & PathType.NOPATH.getValue())) {
                movebyPath(path.getPath());

                return;
            }
        }

        args.path_Idx_offset = 0;
        args.path.add(null);
        TransportPathTransform transform = new TransportPathTransform(unit, args.transformForTransport);
        args.path.add(transform.calc(dest));
    }

    public final void setFall() {
        args.flags.enableFalling();
        args.flags.setUnsetFlag(SplineFlag.FallingSlow, unit.hasUnitMovementFlag(MovementFlag.FallingSlow));
    }

    public final void setFirstPointId(int pointId) {
        args.path_Idx_offset = pointId;
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
        args.flags.setUnsetFlag(SplineFlag.UncompressedPath);
    }

    public final void setCyclic() {
        args.flags.setUnsetFlag(SplineFlag.Cyclic);
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
        args.flags.setUnsetFlag(SplineFlag.OrientationFixed, enable);
    }

    public final void setUnlimitedSpeed() {
        args.flags.setUnsetFlag(SplineFlag.UnlimitedSpeed, true);
    }


    public final void movebyPath(Vector3[] controls) {
        movebyPath(controls, 0);
    }

    public final void movebyPath(Vector3[] controls, int path_offset) {
        args.path_Idx_offset = path_offset;
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

    public final void setParabolic(float amplitude, float time_shift) {
        args.time_perc = time_shift;
        args.parabolic_amplitude = amplitude;
        args.vertical_acceleration = 0.0f;
        args.flags.enableParabolic();
    }

    public final void setParabolicVerticalAcceleration(float vertical_acceleration, float time_shift) {
        args.time_perc = time_shift;
        args.parabolic_amplitude = 0.0f;
        args.vertical_acceleration = vertical_acceleration;
        args.flags.enableParabolic();
    }

    public final void setAnimation(AnimTier anim) {
        args.time_perc = 0.0f;
        args.animTier = new animTierTransition();
        args.animTier.animTier = (byte) anim.getValue();
        args.flags.enableAnimation();
    }

    public final void setFacing(Vector3 spot) {
        TransportPathTransform transform = new TransportPathTransform(unit, args.transformForTransport);
        var finalSpot = transform.calc(spot);
        args.facing.f = new Vector3(finalSpot.X, finalSpot.Y, finalSpot.Z);
        args.facing.type = MonsterMoveType.FacingSpot;
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

    private UnitMoveType selectSpeedType(MovementFlag moveFlags) {
        if (moveFlags.hasFlag(MovementFlag.Flying)) {
            if (moveFlags.hasFlag(MovementFlag.Backward)) {
                return UnitMoveType.FlightBack;
            } else {
                return UnitMoveType.flight;
            }
        } else if (moveFlags.hasFlag(MovementFlag.Swimming)) {
            if (moveFlags.hasFlag(MovementFlag.Backward)) {
                return UnitMoveType.SwimBack;
            } else {
                return UnitMoveType.swim;
            }
        } else if (moveFlags.hasFlag(MovementFlag.Walking)) {
            return UnitMoveType.Walk;
        } else if (moveFlags.hasFlag(MovementFlag.Backward)) {
            return UnitMoveType.RunBack;
        }

        // Flying creatures use MOVEMENTFLAG_CAN_FLY or MOVEMENTFLAG_DISABLE_GRAVITY
        // Run speed is their default flight speed.
        return UnitMoveType.run;
    }

    private void setBackward() {
        args.flags.setUnsetFlag(SplineFlag.Backward);
    }
}
