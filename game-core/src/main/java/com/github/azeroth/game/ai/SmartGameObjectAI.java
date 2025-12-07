package game.ai;

import Framework.Constants.*;
import game.entities.*;
import game.spells.*;
import game.*;

// Copyright (c) Forged WoW LLC <https://github.com/ForgedWoW/ForgedCore>
// Licensed under GPL-3.0 license. See <https://github.com/ForgedWoW/ForgedCore/blob/master/LICENSE> for full information.




public class SmartGameObjectAI extends GameObjectAI {
    private final SmartScript script = new SmartScript();

    // Gossip
    private boolean gossipReturn;

    public SmartGameObjectAI(GameObject go) {
        super(go);
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public override void UpdateAI(uint diff)
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

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public override bool OnGossipSelect(Player player, uint menuId, uint gossipListId)
    @Override
    public boolean onGossipSelect(Player player, int menuId, int gossipListId) {
        gossipReturn = false;
        getScript().processEventsFor(SmartEvents.GossipSelect, player, menuId, gossipListId, false, null, me);

        return gossipReturn;
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public override bool OnGossipSelectCode(Player player, uint menuId, uint gossipListId, string code)
    @Override
    public boolean onGossipSelectCode(Player player, int menuId, int gossipListId, String code) {
        return false;
    }

    @Override
    public void onQuestAccept(Player player, Quest quest) {
        getScript().processEventsFor(SmartEvents.AcceptedQuest, player, quest.id, 0, false, null, me);
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public override void OnQuestReward(Player player, Quest quest, LootItemType type, uint opt)
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

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public override void Destroyed(WorldObject attacker, uint eventId)
    @Override
    public void destroyed(WorldObject attacker, int eventId) {
        getScript().processEventsFor(SmartEvents.Death, attacker != null ? attacker.getAsUnit() : null, eventId, 0, false, null, me);
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public override void SetData(uint id, uint value)
    @Override
    public void setData(int id, int value) {
        setData(id, value, null);
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public void SetData(uint id, uint value, Unit invoker)
    public final void setData(int id, int value, Unit invoker) {
        getScript().processEventsFor(SmartEvents.DataSet, invoker, id, value);
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public void SetTimedActionList(SmartScriptHolder e, uint entry, Unit invoker)
    public final void setTimedActionList(SmartScriptHolder e, int entry, Unit invoker) {
        getScript().setTimedActionList(e, entry, invoker);
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public override void OnGameEvent(bool start, ushort eventId)
    @Override
    public void onGameEvent(boolean start, short eventId) {
        getScript().processEventsFor(start ? SmartEvents.GameEventStart : SmartEvents.GameEventEnd, null, eventId);
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public override void OnLootStateChanged(uint state, Unit unit)
    @Override
    public void onLootStateChanged(int state, Unit unit) {
        getScript().processEventsFor(SmartEvents.GoLootStateChanged, unit, state);
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public override void EventInform(uint eventId)
    @Override
    public void eventInform(int eventId) {
        getScript().processEventsFor(SmartEvents.GoEventInform, null, eventId);
    }

    @Override
    public void spellHit(WorldObject caster, SpellInfo spellInfo) {
        getScript().processEventsFor(SmartEvents.SpellHit, caster.getAsUnit(), 0, 0, false, spellInfo);
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