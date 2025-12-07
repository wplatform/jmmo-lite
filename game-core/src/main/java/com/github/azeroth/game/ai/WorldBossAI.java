package game.ai;

import Framework.Constants.*;
import game.entities.*;
import game.*;

// Copyright (c) Forged WoW LLC <https://github.com/ForgedWoW/ForgedCore>
// Licensed under GPL-3.0 license. See <https://github.com/ForgedWoW/ForgedCore/blob/master/LICENSE> for full information.




public class WorldBossAI extends ScriptedAI {
    private final SummonList summons;

    public WorldBossAI(Creature creature) {
        super(creature);
        summons = new SummonList(creature);
    }

    @Override
    public void justSummoned(Creature summon) {
        summons.summon(summon);
        var target = SelectTarget(SelectTargetMethod.Random, 0, 0.0f, true);

        if (target) {
            summon.getAI().attackStart(target);
        }
    }

    @Override
    public void summonedCreatureDespawn(Creature summon) {
        summons.despawn(summon);
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public override void UpdateAI(uint diff)
    @Override
    public void updateAI(int diff) {
        if (!updateVictim()) {
            return;
        }

        events.Update(diff);

        if (me.hasUnitState(UnitState.Casting)) {
            return;
        }

        events.ExecuteEvents(eventId -> {
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
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public virtual void ExecuteEvent(uint eventId)
    public void executeEvent(int eventId) {
    }

    @Override
    public void reset() {
        _Reset();
    }

    @Override
    public void justEngagedWith(Unit who) {
        justEngagedWith();
    }

    @Override
    public void justDied(Unit killer) {
        justDied();
    }

    private void _Reset() {
        if (!me.isAlive()) {
            return;
        }

        events.Reset();
        summons.despawnAll();
    }

    private void justDied() {
        events.Reset();
        summons.despawnAll();
    }

    private void justEngagedWith() {
        var target = SelectTarget(SelectTargetMethod.Random, 0, 0.0f, true);

        if (target) {
            attackStart(target);
        }
    }
}