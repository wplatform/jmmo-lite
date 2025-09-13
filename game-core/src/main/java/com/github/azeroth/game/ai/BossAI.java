package com.github.azeroth.game.ai;


import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.unit.Unit;

public class BossAI extends ScriptedAI {
    private final int bossId;
    public instanceScript instance;
    public SummonList summons;

    public BossAI(Creature creature, int bossId) {
        super(creature);
        instance = creature.getInstanceScript();
        summons = new SummonList(creature);
        bossId = bossId;

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
        events.reset();
        summons.despawnAll();
        getScheduler().CancelAll();

        if (instance != null && instance.getBossState(bossId) != EncounterState.Done) {
            instance.setBossState(bossId, EncounterState.NotStarted);
        }
    }

    public final void _JustDied() {
        events.reset();
        summons.despawnAll();
        getScheduler().CancelAll();

        if (instance != null) {
            instance.setBossState(bossId, EncounterState.Done);
        }
    }

    public final void _JustEngagedWith(Unit who) {
        if (instance != null) {
            // bosses do not respawn, check only on enter combat
            if (!instance.checkRequiredBosses(bossId, who.toPlayer())) {
                enterEvadeMode(EvadeReason.SequenceBreak);

                return;
            }

            instance.setBossState(bossId, EncounterState.inProgress);
        }

        me.setCombatPulseDelay(5);
        me.setActive(true);
        doZoneInCombat();
        scheduleTasks();
    }

    public final void teleportCheaters() {
        for (var pair : me.getCombatManager().getPvECombatRefs().entrySet()) {
            var target = pair.getValue().getOther(me);

            if (target.IsControlledByPlayer && !isInBoundary(target.location)) {
                target.nearTeleportTo(me.getLocation());
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

    public final void _DespawnAtEvade() {
        _DespawnAtEvade(duration.FromSeconds(30));
    }


    public final void _DespawnAtEvade(Duration delayToRespawn) {
        _DespawnAtEvade(delayToRespawn, null);
    }

    public final void _DespawnAtEvade(Duration delayToRespawn, Creature who) {
        if (delayToRespawn < duration.FromSeconds(2)) {
            Log.outError(LogFilter.ScriptsAi, String.format("BossAI::_DespawnAtEvade: called with delay of %1$s seconds, defaulting to 2 (me: %2$s)", delayToRespawn, me.getGUID()));
            delayToRespawn = duration.FromSeconds(2);
        }

        if (!who) {
            who = me;
        }

        var whoSummon = who.toTempSummon();

        if (whoSummon) {
            Log.outWarn(LogFilter.ScriptsAi, String.format("BossAI::_DespawnAtEvade: called on a temporary summon (who: %1$s)", who.getGUID()));
            whoSummon.unSummon();

            return;
        }

        who.despawnOrUnsummon(duration.Zero, delayToRespawn);

        if (instance != null && who == me) {
            instance.setBossState(bossId, EncounterState.Fail);
        }
    }

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
        _JustDied();
    }

    @Override
    public void justReachedHome() {
        _JustReachedHome();
    }

    @Override
    public boolean canAIAttack(Unit victim) {
        return isInBoundary(victim.getLocation());
    }

    public final void _JustReachedHome() {
        me.setActive(false);
    }

    public final int getBossId() {
        return bossId;
    }


    private void forceCombatStopForCreatureEntry(int entry, float maxSearchRange) {
        forceCombatStopForCreatureEntry(entry, maxSearchRange, true);
    }

    private void forceCombatStopForCreatureEntry(int entry) {
        forceCombatStopForCreatureEntry(entry, 250.0f, true);
    }

    private void forceCombatStopForCreatureEntry(int entry, float maxSearchRange, boolean reset) {
        Log.outDebug(LogFilter.ScriptsAi, String.format("BossAI::ForceStopCombatForCreature: called on %1$s. Debug info: %2$s", me.getGUID(), me.getDebugInfo()));

        var creatures = me.getCreatureListWithEntryInGrid(entry, maxSearchRange);

        for (var creature : creatures) {
            creature.combatStop(true);
            creature.doNotReacquireSpellFocusTarget();
            creature.getMotionMaster().clear(MovementGeneratorPriority.NORMAL);

            if (reset) {
                creature.loadCreaturesAddon();
                creature.setTappedBy(null);
                creature.resetPlayerDamageReq();
                creature.setLastDamagedTime(0);
                creature.setCannotReachTarget(false);
            }
        }
    }
}
