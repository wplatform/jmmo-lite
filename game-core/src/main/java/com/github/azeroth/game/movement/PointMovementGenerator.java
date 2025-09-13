package com.github.azeroth.game.movement;


import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.movement.enums.*;
import com.github.azeroth.game.movement.spline.MoveSplineInit;

public class PointMovementGenerator extends MovementGeneratorMedium<unit> {
    private final int movementId;
    private final Position destination;
    private final Float speed;
    private final boolean generatePath;

    //! if set then unit will turn to specified _orient in provided _pos
    private final Float finalOrient;
    private final Unit faceTarget;
    private final SpellEffectExtraData spellEffectExtra;
    private final MovementWalkRunSpeedSelectionMode speedSelectionMode;
    private final Float closeEnoughDistance;


    public PointMovementGenerator(int id, float x, float y, float z, boolean generatePath, float speed, Float finalOrient, Unit faceTarget, SpellEffectExtraData spellEffectExtraData, MovementWalkRunSpeedSelectionMode speedSelectionMode) {
        this(id, x, y, z, generatePath, speed, finalOrient, faceTarget, spellEffectExtraData, speedSelectionMode, 0);
    }

    public PointMovementGenerator(int id, float x, float y, float z, boolean generatePath, float speed, Float finalOrient, Unit faceTarget, SpellEffectExtraData spellEffectExtraData) {
        this(id, x, y, z, generatePath, speed, finalOrient, faceTarget, spellEffectExtraData, MovementWalkRunSpeedSelectionMode.Default, 0);
    }

    public PointMovementGenerator(int id, float x, float y, float z, boolean generatePath, float speed, Float finalOrient, Unit faceTarget) {
        this(id, x, y, z, generatePath, speed, finalOrient, faceTarget, null, MovementWalkRunSpeedSelectionMode.Default, 0);
    }

    public PointMovementGenerator(int id, float x, float y, float z, boolean generatePath, float speed, Float finalOrient) {
        this(id, x, y, z, generatePath, speed, finalOrient, null, null, MovementWalkRunSpeedSelectionMode.Default, 0);
    }

    public PointMovementGenerator(int id, float x, float y, float z, boolean generatePath, float speed) {
        this(id, x, y, z, generatePath, speed, null, null, null, MovementWalkRunSpeedSelectionMode.Default, 0);
    }

    public PointMovementGenerator(int id, float x, float y, float z, boolean generatePath) {
        this(id, x, y, z, generatePath, 0.0f, null, null, null, MovementWalkRunSpeedSelectionMode.Default, 0);
    }

    public PointMovementGenerator(int id, float x, float y, float z, boolean generatePath, float speed, Float finalOrient, Unit faceTarget, SpellEffectExtraData spellEffectExtraData, MovementWalkRunSpeedSelectionMode speedSelectionMode, float closeEnoughDistance) {
        movementId = id;
        destination = new Position(x, y, z);
        speed = speed == 0.0f ? null : speed;
        generatePath = generatePath;
        finalOrient = finalOrient;
        faceTarget = faceTarget;
        spellEffectExtra = spellEffectExtraData;
        closeEnoughDistance = closeEnoughDistance == 0 ? null : closeEnoughDistance;
        speedSelectionMode = speedSelectionMode;

        mode = MovementGeneratorMode.Default;
        priority = MovementGeneratorPriority.NORMAL;
        flags = MovementGeneratorFlags.InitializationPending;
        baseUnitState = UnitState.Roaming;
    }


    public final int getId() {
        return movementId;
    }

    @Override
    public void doInitialize(Unit owner) {
        removeFlag(MovementGeneratorFlags.InitializationPending.getValue() | MovementGeneratorFlags.Deactivated.getValue());
        addFlag(MovementGeneratorFlags.initialized);

        if (movementId == eventId.ChargePrepath) {
            owner.addUnitState(UnitState.RoamingMove);

            return;
        }

        if (owner.hasUnitState(UnitState.NotMove) || owner.isMovementPreventedByCasting()) {
            addFlag(MovementGeneratorFlags.Interrupted);
            owner.stopMoving();

            return;
        }

        owner.addUnitState(UnitState.RoamingMove);

        MoveSplineInit init = new MoveSplineInit(owner);

        if (generatePath) {
            var path = new PathGenerator(owner);

            var result = path.calculatePath(destination, false);

            if (result && (path.getPathType().getValue() & PathType.NOPATH.getValue()) == 0) {
                if (closeEnoughDistance != null) {
                    path.shortenPathUntilDist(destination, closeEnoughDistance.floatValue());
                }

                init.movebyPath(path.getPath());

                return;
            }
        }

        if (closeEnoughDistance != null) {
            owner.movePosition(destination, Math.min(closeEnoughDistance.floatValue(), destination.getExactDist(owner.getLocation())), (float) Math.PI + owner.getLocation().getRelativeAngle(destination));
        }

        init.moveTo(destination.getX(), destination.getY(), destination.getZ(), false);


        if (speed != null) {
            init.setVelocity(speed.floatValue());
        }

        if (faceTarget) {
            init.setFacing(faceTarget);
        }

        if (spellEffectExtra != null) {
            init.setSpellEffectExtraData(spellEffectExtra);
        }

        if (finalOrient != null) {
            init.setFacing(finalOrient.floatValue());
        }

        switch (speedSelectionMode) {
            case Default:
                break;
            case ForceRun:
                init.setWalk(false);

                break;
            case ForceWalk:
                init.setWalk(true);

                break;
            default:
                break;
        }

        init.launch();

        // Call for creature group update
        var creature = owner.toCreature();

        if (creature != null) {
            creature.signalFormationMovement();
        }
    }

    @Override
    public void doReset(Unit owner) {
        removeFlag(MovementGeneratorFlags.Transitory.getValue() | MovementGeneratorFlags.Deactivated.getValue());

        doInitialize(owner);
    }

    @Override
    public boolean doUpdate(Unit owner, int diff) {
        if (owner == null) {
            return false;
        }

        if (movementId == eventId.ChargePrepath) {
            if (owner.getMoveSpline().finalized()) {
                addFlag(MovementGeneratorFlags.InformEnabled);

                return false;
            }

            return true;
        }

        if (owner.hasUnitState(UnitState.NotMove) || owner.isMovementPreventedByCasting()) {
            addFlag(MovementGeneratorFlags.Interrupted);
            owner.stopMoving();

            return true;
        }

        if ((hasFlag(MovementGeneratorFlags.Interrupted) && owner.getMoveSpline().finalized()) || (hasFlag(MovementGeneratorFlags.SpeedUpdatePending) && !owner.getMoveSpline().finalized())) {
            removeFlag(MovementGeneratorFlags.Interrupted.getValue() | MovementGeneratorFlags.SpeedUpdatePending.getValue());

            owner.addUnitState(UnitState.RoamingMove);

            MoveSplineInit init = new MoveSplineInit(owner);
            init.moveTo(destination.getX(), destination.getY(), destination.getZ(), generatePath);

            if (speed != null) // Default second for point motion type is 0.0, if 0.0 spline will use GetSpeed on unit
            {
                init.setVelocity(speed.floatValue());
            }

            init.launch();

            // Call for creature group update
            var creature = owner.toCreature();

            if (creature != null) {
                creature.signalFormationMovement();
            }
        }

        if (owner.getMoveSpline().finalized()) {
            removeFlag(MovementGeneratorFlags.Transitory);
            addFlag(MovementGeneratorFlags.InformEnabled);

            return false;
        }

        return true;
    }

    @Override
    public void doDeactivate(Unit owner) {
        addFlag(MovementGeneratorFlags.Deactivated);
        owner.clearUnitState(UnitState.RoamingMove);
    }

    @Override
    public void doFinalize(Unit owner, boolean active, boolean movementInform) {
        addFlag(MovementGeneratorFlags.Finalized);

        if (active) {
            owner.clearUnitState(UnitState.RoamingMove);
        }

        if (movementInform && hasFlag(MovementGeneratorFlags.InformEnabled) && owner.isCreature()) {
            movementInform(owner);
        }
    }

    public final void movementInform(Unit owner) {
        if (owner.isTypeId(TypeId.UNIT)) {
            if (owner.toCreature().getAI() != null) {
                owner.toCreature().getAI().movementInform(MovementGeneratorType.Point, movementId);
            }
        }
    }

    @Override
    public MovementGeneratorType getMovementGeneratorType() {
        return MovementGeneratorType.Point;
    }

    @Override
    public void unitSpeedChanged() {
        addFlag(MovementGeneratorFlags.SpeedUpdatePending);
    }
}
