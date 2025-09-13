package com.github.azeroth.game.ai;


import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.unit.Unit;

public class WorldBossAI extends ScriptedAI {
    private final SummonList summons;

    public WorldBossAI(Creature creature) {
        super(creature);
        summons = new SummonList(creature);
    }

    @Override
    public void justSummoned(Creature summon) {
        summons.summon(summon);
        var target = selectTarget(SelectTargetMethod.random, 0, 0.0f, true);

        if (target) {
            summon.getAI().attackStart(target);
        }
    }

    @Override
    public void summonedCreatureDespawn(Creature summon) {
        summons.despawn(summon);
    }

    @Override
    public void updateAI(int diff) {
        if (!updateVictim()) {
            return;
        }

        events.update(diff);

        if (me.hasUnitState(UnitState.Casting)) {
            return;
        }

        events.ExecuteEvents(eventId ->
        {
            executeEvent(eventId);

            if (me.hasUnitState(UnitState.Casting)) {
                return;
            }
        });

        doMeleeAttackIfReady();
    }

    // Hook used to execute events scheduled into EventMap without the need
    // to override UpdateAI
    // note: You must re-schedule the event within this method if the event
    // is supposed to run more than once
    public void executeEvent(int eventId) {
    }

    @Override
    public void reset() {
        _Reset();
    }

    @Override
    public void justEngagedWith(Unit who) {
        _JustEngagedWith();
    }

    @Override
    public void justDied(Unit killer) {
        _JustDied();
    }

    private void _Reset() {
        if (!me.isAlive()) {
            return;
        }

        events.reset();
        summons.despawnAll();
    }

    private void _JustDied() {
        events.reset();
        summons.despawnAll();
    }

    private void _JustEngagedWith() {
        var target = selectTarget(SelectTargetMethod.random, 0, 0.0f, true);

        if (target) {
            attackStart(target);
        }
    }
}
