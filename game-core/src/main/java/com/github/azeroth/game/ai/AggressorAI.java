package com.github.azeroth.game.ai;

import com.github.azeroth.game.entity.creature.Creature;

public class AggressorAI extends CreatureAI {
    public AggressorAI(Creature c) {
        super(c);
    }


    @Override
    public void updateAI(int diff) {
        if (!updateVictim()) {
            return;
        }

        doMeleeAttackIfReady();
    }
}
