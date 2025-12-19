package com.github.azeroth.game.ai;









public class ReactorAI extends CreatureAI {
    public ReactorAI(Creature c) {
        super(c);
    }

    @Override
    public void moveInLineOfSight(Unit who) {
    }


//ORIGINAL LINE: public override void UpdateAI(uint diff)
    @Override
    public void updateAI(int diff) {
        if (!updateVictim()) {
            return;
        }

        doMeleeAttackIfReady();
    }
}