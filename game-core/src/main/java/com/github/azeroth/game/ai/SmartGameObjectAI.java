package com.github.azeroth.game.ai;


import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.gobject.GameObject;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.spell.SpellInfo;

public class SmartGameObjectAI extends GameObjectAI {
    private final smartScript script = new smartScript();

    // Gossip
    private boolean gossipReturn;

    public SmartGameObjectAI(GameObject go) {
        super(go);
    }

    @Override
    public void updateAI(int diff) {
        getScript().onUpdate(diff);
    }

    @Override
    public void initializeAI() {
        getScript().onInitialize(me);

        // do not call respawn event if go is not spawned
        if (me.isSpawned()) {
            getScript().processEventsFor(SmartEvents.Respawn);
        }
    }

    @Override
    public void reset() {
        getScript().onReset();
    }

    @Override
    public boolean onGossipHello(Player player) {
        gossipReturn = false;
        getScript().processEventsFor(SmartEvents.GossipHello, player, 0, 0, false, null, me);

        return gossipReturn;
    }

    @Override
    public boolean onGossipSelect(Player player, int menuId, int gossipListId) {
        gossipReturn = false;
        getScript().processEventsFor(SmartEvents.GossipSelect, player, menuId, gossipListId, false, null, me);

        return gossipReturn;
    }

    @Override
    public boolean onGossipSelectCode(Player player, int menuId, int gossipListId, String code) {
        return false;
    }

    @Override
    public void onQuestAccept(Player player, Quest quest) {
        getScript().processEventsFor(SmartEvents.AcceptedQuest, player, quest.id, 0, false, null, me);
    }

    @Override
    public void onQuestReward(Player player, Quest quest, LootItemType type, int opt) {
        getScript().processEventsFor(SmartEvents.RewardQuest, player, quest.id, opt, false, null, me);
    }

    @Override
    public boolean onReportUse(Player player) {
        gossipReturn = false;
        getScript().processEventsFor(SmartEvents.GossipHello, player, 1, 0, false, null, me);

        return gossipReturn;
    }

    @Override
    public void destroyed(WorldObject attacker, int eventId) {
        getScript().processEventsFor(SmartEvents.Death, attacker != null ? attacker.toUnit() : null, eventId, 0, false, null, me);
    }

    @Override
    public void setData(int id, int value) {
        setData(id, value, null);
    }

    public final void setData(int id, int value, Unit invoker) {
        getScript().processEventsFor(SmartEvents.DataSet, invoker, id, value);
    }

    public final void setTimedActionList(SmartScriptHolder e, int entry, Unit invoker) {
        getScript().setTimedActionList(e, entry, invoker);
    }

    @Override
    public void onGameEvent(boolean start, short eventId) {
        getScript().processEventsFor(start ? SmartEvents.GameEventStart : SmartEvents.GameEventEnd, null, eventId);
    }

    @Override
    public void onLootStateChanged(int state, Unit unit) {
        getScript().processEventsFor(SmartEvents.GoLootStateChanged, unit, state);
    }

    @Override
    public void eventInform(int eventId) {
        getScript().processEventsFor(SmartEvents.GoEventInform, null, eventId);
    }

    @Override
    public void spellHit(WorldObject caster, SpellInfo spellInfo) {
        getScript().processEventsFor(SmartEvents.SpellHit, caster.toUnit(), 0, 0, false, spellInfo);
    }

    @Override
    public void justSummoned(Creature creature) {
        getScript().processEventsFor(SmartEvents.SummonedUnit, creature);
    }

    @Override
    public void summonedCreatureDies(Creature summon, Unit killer) {
        getScript().processEventsFor(SmartEvents.SummonedUnitDies, summon);
    }

    @Override
    public void summonedCreatureDespawn(Creature unit) {
        getScript().processEventsFor(SmartEvents.SummonDespawned, unit, unit.getEntry());
    }

    public final void setGossipReturn(boolean val) {
        gossipReturn = val;
    }

    public final SmartScript getScript() {
        return script;
    }
}
