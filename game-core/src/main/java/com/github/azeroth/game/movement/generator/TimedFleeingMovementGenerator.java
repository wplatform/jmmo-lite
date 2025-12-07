package com.github.azeroth.game.movement.generator;


import com.github.azeroth.game.ai.CreatureAI;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.domain.unit.UnitFlag;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.movement.enums.MovementGeneratorFlag;
import com.github.azeroth.game.movement.enums.MovementGeneratorType;
import com.github.azeroth.time.TimeTracker;

public class TimedFleeingMovementGenerator extends FleeingMovementGenerator {
    private final TimeTracker totalFleeTime;


    public TimedFleeingMovementGenerator(ObjectGuid fright, int time) {
        super(fright);
        totalFleeTime = new TimeTracker(time);
    }


    @Override
    public boolean update(Unit owner, int diff) {
        if (owner == null || !owner.isAlive()) {
            return false;
        }

        totalFleeTime.update(diff);

        if (totalFleeTime.passed()) {
            return false;
        }

        return super.update(owner, diff);
    }

    @Override
    public void finalize(Unit owner, boolean active, boolean movementInform) {
        addFlag(MovementGeneratorFlag.FINALIZED);

        if (!active) {
            return;
        }

        owner.removeUnitFlag(UnitFlag.FLEEING);
        owner.stopMoving();

        var victim = owner.getVictim();

        if (victim != null) {
            if (owner.isAlive()) {
                owner.attackStop();
                owner.toCreature().getAi().attackStart(victim);
            }
        }

        if (movementInform) {

            if (owner.getAi() instanceof CreatureAI ai) {
                ai.movementInform(MovementGeneratorType.TIMED_FLEEING, 0);
            }
        }
    }

    @Override
    public MovementGeneratorType getMovementGeneratorType() {
        return MovementGeneratorType.TIMED_FLEEING;
    }
}
