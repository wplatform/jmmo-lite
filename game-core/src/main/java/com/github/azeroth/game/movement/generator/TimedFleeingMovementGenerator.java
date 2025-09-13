package com.github.azeroth.game.movement.generator;


import com.github.azeroth.game.entity.unit.Unit;

public class TimedFleeingMovementGenerator extends FleeingMovementGenerator<Creature> {
    private final TimeTracker totalFleeTime;


    public TimedFleeingMovementGenerator(ObjectGuid fright, int time) {
        super(fright);
        totalFleeTime = new timeTracker(time);
    }


    @Override
    public boolean update(Unit owner, int diff) {
        if (owner == null || !owner.isAlive()) {
            return false;
        }

        totalFleeTime.update(diff);

        if (totalFleeTime.Passed) {
            return false;
        }

        return doUpdate(owner.toCreature(), diff);
    }

    @Override
    public void finalize(Unit owner, boolean active, boolean movementInform) {
        addFlag(MovementGeneratorFlags.Finalized);

        if (!active) {
            return;
        }

        owner.removeUnitFlag(UnitFlag.Fleeing);
        var victim = owner.getVictim();

        if (victim != null) {
            if (owner.isAlive()) {
                owner.attackStop();
                owner.toCreature().getAI().attackStart(victim);
            }
        }

        if (movementInform) {
            var ownerCreature = owner.toCreature();
            var ai = ownerCreature != null ? ownerCreature.getAI() : null;

            if (ai != null) {
                ai.movementInform(MovementGeneratorType.TimedFleeing, 0);
            }
        }
    }

    @Override
    public MovementGeneratorType getMovementGeneratorType() {
        return MovementGeneratorType.TimedFleeing;
    }
}
