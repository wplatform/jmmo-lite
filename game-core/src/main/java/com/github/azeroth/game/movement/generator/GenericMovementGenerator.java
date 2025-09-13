package com.github.azeroth.game.movement.generator;


import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.movement.MovementGenerator;
import com.github.azeroth.game.movement.spline.MoveSplineInit;

class GenericMovementGenerator extends MovementGenerator {
    private final tangible.Action1Param<MoveSplineInit> splineInit;
    private final MovementGeneratorType type;
    private final int pointId;
    private final TimeTracker duration;
    private final int arrivalSpellId;
    private final ObjectGuid arrivalSpellTargetGuid;


    public GenericMovementGenerator(action<MoveSplineInit> initializer, MovementGeneratorType type, int id, int arrivalSpellId) {
        this(initializer, type, id, arrivalSpellId, null);
    }

    public GenericMovementGenerator(action<MoveSplineInit> initializer, MovementGeneratorType type, int id) {
        this(initializer, type, id, 0, null);
    }

    public GenericMovementGenerator(tangible.Action1Param<MoveSplineInit> initializer, MovementGeneratorType type, int id, int arrivalSpellId, ObjectGuid arrivalSpellTargetGuid) {
        splineInit = initializer;
        type = type;
        pointId = id;
        duration = new timeTracker();
        arrivalSpellId = arrivalSpellId;
        arrivalSpellTargetGuid = arrivalSpellTargetGuid;

        mode = MovementGeneratorMode.Default;
        priority = MovementGeneratorPriority.NORMAL;
        flags = MovementGeneratorFlags.InitializationPending;
        baseUnitState = UnitState.Roaming;
    }

    @Override
    public void initialize(Unit owner) {
        if (hasFlag(MovementGeneratorFlags.Deactivated) && !hasFlag(MovementGeneratorFlags.InitializationPending)) // Resume spline is not supported
        {
            removeFlag(MovementGeneratorFlags.Deactivated);
            addFlag(MovementGeneratorFlags.Finalized);

            return;
        }

        removeFlag(MovementGeneratorFlags.InitializationPending.getValue() | MovementGeneratorFlags.Deactivated.getValue());
        addFlag(MovementGeneratorFlags.initialized);

        MoveSplineInit init = new MoveSplineInit(owner);
        splineInit.invoke(init);
        duration.reset((int) init.launch());
    }

    @Override
    public void reset(Unit owner) {
        initialize(owner);
    }

    @Override
    public boolean update(Unit owner, int diff) {
        if (!owner || hasFlag(MovementGeneratorFlags.Finalized)) {
            return false;
        }

        // Cyclic splines never expire, so update the duration only if it's not cyclic
        if (!owner.getMoveSpline().isCyclic()) {
            duration.update(diff);
        }

        if (duration.Passed || owner.getMoveSpline().finalized()) {
            addFlag(MovementGeneratorFlags.InformEnabled);

            return false;
        }

        return true;
    }

    @Override
    public void deactivate(Unit owner) {
        addFlag(MovementGeneratorFlags.Deactivated);
    }

    @Override
    public void finalize(Unit owner, boolean active, boolean movementInform) {
        addFlag(MovementGeneratorFlags.Finalized);

        if (movementInform && hasFlag(MovementGeneratorFlags.InformEnabled)) {
            movementInform(owner);
        }
    }

    @Override
    public MovementGeneratorType getMovementGeneratorType() {
        return type;
    }

    private void movementInform(Unit owner) {
        if (arrivalSpellId != 0) {
            owner.castSpell(global.getObjAccessor().GetUnit(owner, arrivalSpellTargetGuid), arrivalSpellId, true);
        }

        var creature = owner.toCreature();

        if (creature != null && creature.getAI() != null) {
            creature.getAI().movementInform(type, pointId);
        }
    }
}
