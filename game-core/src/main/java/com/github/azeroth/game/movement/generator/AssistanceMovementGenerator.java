package com.github.azeroth.game.movement.generator;


import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.movement.PointMovementGenerator;

public class AssistanceMovementGenerator extends PointMovementGenerator {
    public AssistanceMovementGenerator(int id, float x, float y, float z) {
        super(id, x, y, z, true);
    }

    @Override
    public void finalize(Unit owner, boolean active, boolean movementInform) {
        addFlag(MovementGeneratorFlags.Finalized);

        if (active) {
            owner.clearUnitState(UnitState.RoamingMove);
        }

        if (movementInform && hasFlag(MovementGeneratorFlags.InformEnabled)) {
            var ownerCreature = owner.toCreature();
            ownerCreature.setNoCallAssistance(false);
            ownerCreature.callAssistance();

            if (ownerCreature.isAlive()) {
                ownerCreature.getMotionMaster().moveSeekAssistanceDistract(WorldConfig.getUIntValue(WorldCfg.CreatureFamilyAssistanceDelay));
            }
        }
    }

    @Override
    public MovementGeneratorType getMovementGeneratorType() {
        return MovementGeneratorType.Assistance;
    }
}
