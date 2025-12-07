package com.github.azeroth.game.movement.generator;


import com.github.azeroth.game.domain.unit.UnitState;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.movement.PointMovementGenerator;
import com.github.azeroth.game.movement.enums.MovementGeneratorFlag;
import com.github.azeroth.game.movement.enums.MovementGeneratorType;

public class AssistanceMovementGenerator extends PointMovementGenerator {
    public AssistanceMovementGenerator(int id, float x, float y, float z) {
        super(id, x, y, z, true);
    }

    @Override
    public void finalize(Unit owner, boolean active, boolean movementInform) {
        addFlag(MovementGeneratorFlag.FINALIZED);

        if (active) {
            owner.clearUnitState(UnitState.ROAMING_MOVE);
        }

        if (movementInform && hasFlag(MovementGeneratorFlag.INFORM_ENABLED)) {
            var ownerCreature = owner.toCreature();
            ownerCreature.setNoCallAssistance(false);
            ownerCreature.callAssistance();

            if (ownerCreature.isAlive()) {
                ownerCreature.getMotionMaster().moveSeekAssistanceDistract(ownerCreature.getWorldContext().getWorldSettings().creatureFamilyAssistanceDelay);
            }
        }
    }

    @Override
    public MovementGeneratorType getMovementGeneratorType() {
        return MovementGeneratorType.ASSISTANCE;
    }
}
