package com.github.azeroth.game.movement.generator;


import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.domain.unit.UnitState;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.movement.MovementGenerator;
import com.github.azeroth.game.movement.enums.MovementGeneratorFlag;
import com.github.azeroth.game.movement.enums.MovementGeneratorMode;
import com.github.azeroth.game.movement.enums.MovementGeneratorPriority;
import com.github.azeroth.game.movement.enums.MovementGeneratorType;
import com.github.azeroth.game.movement.spline.MoveSplineInit;

import java.time.Duration;
import java.util.function.Consumer;

class GenericMovementGenerator extends MovementGenerator {
    private final Consumer<MoveSplineInit> splineInit;
    private final MovementGeneratorType type;
    private final int pointId;
    private Duration duration;
    private final int arrivalSpellId;
    private final ObjectGuid arrivalSpellTargetGuid;


    public GenericMovementGenerator(Consumer<MoveSplineInit> initializer, MovementGeneratorType type, int id, int arrivalSpellId) {
        this(initializer, type, id, arrivalSpellId, null);
    }

    public GenericMovementGenerator(Consumer<MoveSplineInit> initializer, MovementGeneratorType type, int id) {
        this(initializer, type, id, 0, null);
    }
    public GenericMovementGenerator(Consumer<MoveSplineInit> initializer, MovementGeneratorType type, int id, int arrivalSpellId, ObjectGuid arrivalSpellTargetGuid) {
        this.splineInit = initializer;
        this.type = type;
        this.pointId = id;
        this.duration = Duration.ZERO;
        this.arrivalSpellId = arrivalSpellId;
        this.arrivalSpellTargetGuid = arrivalSpellTargetGuid;

        mode = MovementGeneratorMode.DEFAULT;
        priority = MovementGeneratorPriority.NORMAL;
        flags.addFlag(MovementGeneratorFlag.INITIALIZATION_PENDING);
        baseUnitState = UnitState.ROAMING;
    }

    @Override
    public void initialize(Unit owner) {
        if (hasFlag(MovementGeneratorFlag.DEACTIVATED) && !hasFlag(MovementGeneratorFlag.INITIALIZATION_PENDING)) // Resume spline is not supported
        {
            removeFlag(MovementGeneratorFlag.DEACTIVATED);
            addFlag(MovementGeneratorFlag.FINALIZED);

            return;
        }
        flags.removeFlag(MovementGeneratorFlag.INITIALIZATION_PENDING, MovementGeneratorFlag.DEACTIVATED);
        addFlag(MovementGeneratorFlag.INITIALIZED);

        MoveSplineInit init = new MoveSplineInit(owner);
        splineInit.accept(init);
        duration = Duration.ofMillis(init.launch());
    }

    @Override
    public void reset(Unit owner) {
        initialize(owner);
    }

    @Override
    public boolean update(Unit owner, int diff) {
        if (owner == null || hasFlag(MovementGeneratorFlag.FINALIZED)) {
            return false;
        }

        // Cyclic splines never expire, so update the duration only if it's not cyclic
        if (!owner.getMoveSpline().isCyclic()) {
            duration = duration.minusMillis(diff);
        }

        if (!duration.isPositive() || owner.getMoveSpline().finalized()) {
            addFlag(MovementGeneratorFlag.INFORM_ENABLED);

            return false;
        }

        return true;
    }

    @Override
    public void deactivate(Unit owner) {
        addFlag(MovementGeneratorFlag.DEACTIVATED);
    }

    @Override
    public void finalize(Unit owner, boolean active, boolean movementInform) {
        addFlag(MovementGeneratorFlag.FINALIZED);

        if (movementInform && hasFlag(MovementGeneratorFlag.INFORM_ENABLED)) {
            movementInform(owner);
        }
    }

    @Override
    public MovementGeneratorType getMovementGeneratorType() {
        return type;
    }

    private void movementInform(Unit owner) {
        if (arrivalSpellId != 0) {
            owner.castSpell(owner.getWorldContext().getUnit(owner, arrivalSpellTargetGuid), arrivalSpellId, true);
        }

        var creature = owner.toCreature();

        if (creature != null && creature.getAi() != null) {
            creature.getAi().movementInform(type, pointId);
        }
    }
}
