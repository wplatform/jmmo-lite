package com.github.azeroth.game.ai;










public class PossessedAI extends CreatureAI {
    public PossessedAI(Creature creature) {
        super(creature);
        creature.reactState = ReactStates.Passive;
    }

    @Override
    public void attackStart(Unit target) {
        me.attack(target, true);
    }


//ORIGINAL LINE: public override void UpdateAI(uint diff)
    @Override
    public void updateAI(int diff) {
        if (me.getVictim() != null) {
            if (!me.isValidAttackTarget(me.getVictim())) {
                me.attackStop();
            } else {
                doMeleeAttackIfReady();
            }
        }
    }
    @Override
    public void justDied(Unit unit) {
        // We died while possessed, disable our loot
        me.removeDynamicFlag(UnitDynFlags.Lootable);
    }

    @Override
    public void moveInLineOfSight(Unit who) {
    }

    @Override
    public void justEnteredCombat(Unit who) {
        engagementStart(who);
    }

    @Override
    public void justExitedCombat() {
        engagementOver();
    }

    @Override
    public void justStartedThreateningMe(Unit who) {
    }

    @Override
    public void enterEvadeMode(EvadeReason why) {
    }
}