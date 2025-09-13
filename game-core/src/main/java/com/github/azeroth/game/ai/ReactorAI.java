package com.github.azeroth.game.ai;

import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.unit.Unit;

public class ReactorAI extends CreatureAI {
    public ReactorAI(Creature c) {
        super(c);
    }

    @Override
    public void moveInLineOfSight(Unit who) {
    }

    @Override
    public void updateAI(int diff) {
        if (!updateVictim()) {
            return;
        }

        doMeleeAttackIfReady();
    }
}
