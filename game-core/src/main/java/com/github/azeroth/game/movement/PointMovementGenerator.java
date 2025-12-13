package com.github.azeroth.game.movement;


import com.github.azeroth.defines.EventId;
import com.github.azeroth.game.ai.CreatureAI;
import com.github.azeroth.game.domain.object.Position;
import com.github.azeroth.game.domain.object.enums.TypeId;
import com.github.azeroth.game.domain.unit.UnitState;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.movement.enums.*;
import com.github.azeroth.game.movement.model.SpellEffectExtraData;
import com.github.azeroth.game.movement.spline.MoveSplineInit;

public class PointMovementGenerator extends MovementGenerator {
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
        this.movementId = id;
        this.destination = new Position(x, y, z);
        this.speed = speed == 0.0f ? null : speed;
        this.generatePath = generatePath;
        this.finalOrient = finalOrient;
        this.faceTarget = faceTarget;
        this.spellEffectExtra = spellEffectExtraData;
        this.closeEnoughDistance = closeEnoughDistance == 0 ? null : closeEnoughDistance;
        this.speedSelectionMode = speedSelectionMode;

        this.mode = MovementGeneratorMode.DEFAULT;
        this.priority = MovementGeneratorPriority.NORMAL;
        this.flags.set(MovementGeneratorFlag.INITIALIZATION_PENDING);
        this.baseUnitState = UnitState.ROAMING;
    }


    public final int getId() {
        return movementId;
    }

    @Override
    public void initialize(Unit owner) {
        flags.removeFlag(MovementGeneratorFlag.INITIALIZATION_PENDING, MovementGeneratorFlag.DEACTIVATED);
        flags.addFlag(MovementGeneratorFlag.INITIALIZED);

        if (movementId == EventId.CHARGE_PREPATH.value) {
            owner.addUnitState(UnitState.ROAMING_MOVE);

            return;
        }

        if (owner.hasUnitState(UnitState.NOT_MOVE) || owner.isMovementPreventedByCasting()) {
            flags.addFlag(MovementGeneratorFlag.INTERRUPTED);
            owner.stopMoving();

            return;
        }



        owner.addUnitState(UnitState.ROAMING_MOVE);

        MoveSplineInit init = new MoveSplineInit(owner);

        if (generatePath) {
            var path = new PathGenerator(owner);

            var result = path.calculatePath(destination, false);

            if (result && (!path.getPathType().hasFlag(PathType.NOPATH))) {
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
            init.setVelocity(speed);
        }

        if (faceTarget != null) {
            init.setFacing(faceTarget);
        }

        if (spellEffectExtra != null) {
            init.setSpellEffectExtraData(spellEffectExtra);
        }

        if (finalOrient != null) {
            init.setFacing(finalOrient);
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
    public void reset(Unit owner) {
        flags.removeFlag(MovementGeneratorFlag.TRANSITORY, MovementGeneratorFlag.DEACTIVATED);
        initialize(owner);
    }

    @Override
    public boolean update(Unit owner, int diff) {
        if (owner == null) {
            return false;
        }

        if (movementId == EventId.CHARGE_PREPATH.value) {
            if (owner.getMoveSpline().finalized()) {
                flags.addFlag(MovementGeneratorFlag.INFORM_ENABLED);

                return false;
            }

            return true;
        }

        if (owner.hasUnitState(UnitState.NOT_MOVE) || owner.isMovementPreventedByCasting()) {
            flags.addFlag(MovementGeneratorFlag.INTERRUPTED);
            owner.stopMoving();

            return true;
        }

        if ((flags.hasFlag(MovementGeneratorFlag.INTERRUPTED) && owner.getMoveSpline().finalized()) || (flags.hasFlag(MovementGeneratorFlag.SPEED_UPDATE_PENDING) && !owner.getMoveSpline().finalized())) {
            flags.removeFlag(MovementGeneratorFlag.INTERRUPTED, MovementGeneratorFlag.SPEED_UPDATE_PENDING);

            owner.addUnitState(UnitState.ROAMING_MOVE);

            MoveSplineInit init = new MoveSplineInit(owner);
            init.moveTo(destination.getX(), destination.getY(), destination.getZ(), generatePath);

            if (speed != null) // Default second for point motion type is 0.0, if 0.0 spline will use GetSpeed on unit
            {
                init.setVelocity(speed);
            }

            init.launch();

            // Call for creature group update
            var creature = owner.toCreature();

            if (creature != null) {
                creature.signalFormationMovement();
            }
        }

        if (owner.getMoveSpline().finalized()) {
            flags.removeFlag(MovementGeneratorFlag.TRANSITORY);
            flags.addFlag(MovementGeneratorFlag.INFORM_ENABLED);

            return false;
        }

        return true;
    }

    @Override
    public void deactivate(Unit owner) {
        flags.addFlag(MovementGeneratorFlag.DEACTIVATED);
        owner.clearUnitState(UnitState.ROAMING_MOVE);
    }

    @Override
    public void finalize(Unit owner, boolean active, boolean movementInform) {
        flags.addFlag(MovementGeneratorFlag.FINALIZED);

        if (active) {
            owner.clearUnitState(UnitState.ROAMING_MOVE);
        }

        if (movementInform && flags.hasFlag(MovementGeneratorFlag.INFORM_ENABLED) && owner.isCreature()) {
            movementInform(owner);
        }
    }

    public final void movementInform(Unit owner) {
        if (owner.isTypeId(TypeId.UNIT)) {
            if (owner.getAi() instanceof CreatureAI ai) {
                ai.movementInform(MovementGeneratorType.POINT, movementId);
            }
        }
    }

    @Override
    public MovementGeneratorType getMovementGeneratorType() {
        return MovementGeneratorType.POINT;
    }

    @Override
    public void unitSpeedChanged() {
        flags.addFlag(MovementGeneratorFlag.SPEED_UPDATE_PENDING);
    }
}
