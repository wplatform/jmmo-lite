package game.ai;

import Framework.Constants.*;
import game.entities.*;
import game.maps.*;
import game.*;

// Copyright (c) Forged WoW LLC <https://github.com/ForgedWoW/ForgedCore>
// Licensed under GPL-3.0 license. See <https://github.com/ForgedWoW/ForgedCore/blob/master/LICENSE> for full information.




public class BossAI extends ScriptedAI {
    public InstanceScript instance;
    public SummonList summons;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: readonly uint _bossId;
    private final int bossId;

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public BossAI(Creature creature, uint bossId)
    public BossAI(Creature creature, int bossId) {
        super(creature);
        instance = creature.getInstanceScript();
        summons = new SummonList(creature);
        this.bossId = bossId;

        if (instance != null) {
            setBoundary(instance.getBossBoundary(bossId));
        }

        getScheduler().SetValidator(() -> !me.hasUnitState(UnitState.Casting));
    }

    public final void _Reset() {
        if (!me.isAlive()) {
            return;
        }

        me.setCombatPulseDelay(0);
        me.resetLootMode();
        events.Reset();
        summons.despawnAll();
        getScheduler().CancelAll();

        if (instance != null && instance.getBossState(bossId) != EncounterState.Done) {
            instance.setBossState(bossId, EncounterState.NotStarted);
        }
    }

    public final void justDied() {
        events.Reset();
        summons.despawnAll();
        getScheduler().CancelAll();

        if (instance != null) {
            instance.setBossState(bossId, EncounterState.Done);
        }
    }

    public final void _JustEngagedWith(Unit who) {
        if (instance != null) {
            // bosses do not respawn, check only on enter combat
            if (!instance.checkRequiredBosses(bossId, who.getAsPlayer())) {
                enterEvadeMode(EvadeReason.SequenceBreak);

                return;
            }

            instance.setBossState(bossId, EncounterState.InProgress);
        }

        me.setCombatPulseDelay(5);
        me.setActive(true);
        doZoneInCombat();
        scheduleTasks();
    }

    public final void teleportCheaters() {
        for (var pair : me.getCombatManager().getPvECombatRefs().entrySet()) {
            var target = pair.getValue().GetOther(me);

            if (target.IsControlledByPlayer && !isInBoundary(target.Location)) {
                target.NearTeleportTo(me.location);
            }
        }
    }

    @Override
    public void justSummoned(Creature summon) {
        summons.summon(summon);

        if (me.isEngaged()) {
            doZoneInCombat(summon);
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

    public final void despawnAtEvade() {
        despawnAtEvade(TimeSpan.FromSeconds(30));
    }


    public final void despawnAtEvade(TimeSpan delayToRespawn) {
        despawnAtEvade(delayToRespawn, null);
    }

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public void _DespawnAtEvade(TimeSpan delayToRespawn, Creature who = null)
    public final void despawnAtEvade(TimeSpan delayToRespawn, Creature who) {
        if (delayToRespawn < TimeSpan.FromSeconds(2)) {
            Log.outError(LogFilter.ScriptsAi, String.format("BossAI::_DespawnAtEvade: called with delay of %1$s seconds, defaulting to 2 (me: %2$s)", delayToRespawn, me.getGUID().clone()));
            delayToRespawn = TimeSpan.FromSeconds(2);
        }

        if (!who) {
            who = me;
        }

        var whoSummon = who.toTempSummon();

        if (whoSummon) {
            Log.outWarn(LogFilter.ScriptsAi, String.format("BossAI::_DespawnAtEvade: called on a temporary summon (who: %1$s)", who.getGUID().clone()));
            whoSummon.unSummon();

            return;
        }

        who.despawnOrUnsummon(TimeSpan.Zero, delayToRespawn);

        if (instance != null && who == me) {
            instance.setBossState(bossId, EncounterState.Fail);
        }
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public virtual void ExecuteEvent(uint eventId)
    public void executeEvent(int eventId) {
    }

    public void scheduleTasks() {
    }

    @Override
    public void reset() {
        _Reset();
    }

    @Override
    public void justEngagedWith(Unit who) {
        _JustEngagedWith(who);
    }

    @Override
    public void justDied(Unit killer) {
        justDied();
    }

    @Override
    public void justReachedHome() {
        _JustReachedHome();
    }

    @Override
    public boolean canAIAttack(Unit victim) {
        return isInBoundary(victim.location);
    }

    public final void _JustReachedHome() {
        me.setActive(false);
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint GetBossId()
    public final int getBossId() {
        return bossId;
    }


    private void forceCombatStopForCreatureEntry(int entry, float maxSearchRange) {
        forceCombatStopForCreatureEntry(entry, maxSearchRange, true);
    }

    private void forceCombatStopForCreatureEntry(int entry) {
        forceCombatStopForCreatureEntry(entry, 250.0f, true);
    }

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: void ForceCombatStopForCreatureEntry(uint entry, float maxSearchRange = 250.0f, bool reset = true)
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
    private void forceCombatStopForCreatureEntry(int entry, float maxSearchRange, boolean reset) {
        Log.outDebug(LogFilter.ScriptsAi, String.format("BossAI::ForceStopCombatForCreature: called on %1$s. Debug info: %2$s", me.getGUID().clone(), me.getDebugInfo()));

        var creatures = me.getCreatureListWithEntryInGrid(entry, maxSearchRange);

        for (var creature : creatures) {
            creature.combatStop(true);
            creature.doNotReacquireSpellFocusTarget();
            creature.getMotionMaster().clear(MovementGeneratorPriority.Normal);

            if (reset) {
                creature.loadCreaturesAddon();
                creature.setTappedBy(null);
                creature.resetPlayerDamageReq();
                creature.lastDamagedTime = 0;
                creature.setCannotReachTarget(false);
            }
        }
    }
}