package com.github.azeroth.game.ai;


import Framework.Dynamic.*;

import game.spells.*;







public class GameObjectAI {
    public final GameObject me;
    protected TaskScheduler scheduler;
    protected EventMap events;

    public GameObjectAI(GameObject go) {
        me = go;
        scheduler = new TaskScheduler();
        events = new EventMap();
    }


//ORIGINAL LINE: public virtual void UpdateAI(uint diff)
    public void updateAI(int diff) {
    }

    public void initializeAI() {
        reset();
    }

    public void reset() {
    }

    // Pass parameters between AI

    public void doAction() {
        doAction(0);
    }


//ORIGINAL LINE: public virtual void DoAction(int param = 0)
    public void doAction(int param) {
    }

    public void setGUID(ObjectGuid guid) {
        setGUID(guid, 0);
    }


//ORIGINAL LINE: public virtual void SetGUID(ObjectGuid guid, int id = 0)
    public void setGUID(ObjectGuid guid, int id) {
    }


    public ObjectGuid getGUID() {
        return getGUID(0);
    }


//ORIGINAL LINE: public virtual ObjectGuid GetGUID(int id = 0)
    public ObjectGuid getGUID(int id) {
        return ObjectGuid.empty;
    }

    /** 
      Called when the dialog status between a player and the gameobject is requested.
    */
    public QuestGiverStatus getDialogStatus(Player player) {
        return null;
    }

    /** 
      Called when a player opens a gossip dialog with the gameobject.
    */
    public boolean onGossipHello(Player player) {
        return false;
    }

    /** 
      Called when a player selects a gossip item in the gameobject's gossip menu.
    */

//ORIGINAL LINE: public virtual bool OnGossipSelect(Player player, uint menuId, uint gossipListId)
    public boolean onGossipSelect(Player player, int menuId, int gossipListId) {
        return false;
    }

    /** 
      Called when a player selects a gossip with a code in the gameobject's gossip menu.
    */

//ORIGINAL LINE: public virtual bool OnGossipSelectCode(Player player, uint sender, uint action, string code)
    public boolean onGossipSelectCode(Player player, int sender, int action, String code) {
        return false;
    }

    /** 
      Called when a player accepts a quest from the gameobject.
    */
    public void onQuestAccept(Player player, Quest quest) {
    }

    /** 
      Called when a player completes a quest and is rewarded, opt is the selected item's index or 0
    */

//ORIGINAL LINE: public virtual void OnQuestReward(Player player, Quest quest, LootItemType type, uint opt)
    public void onQuestReward(Player player, Quest quest, LootItemType type, int opt) {
    }

    // Called when a Player clicks a GameObject, before GossipHello
    // prevents achievement tracking if returning true
    public boolean onReportUse(Player player) {
        return false;
    }


//ORIGINAL LINE: public virtual void Destroyed(WorldObject attacker, uint eventId)
    public void destroyed(WorldObject attacker, int eventId) {
    }

//ORIGINAL LINE: public virtual void Damaged(WorldObject attacker, uint eventId)
    public void damaged(WorldObject attacker, int eventId) {
    }


//ORIGINAL LINE: public virtual void SetData64(uint id, ulong value)
    public void setData64(int id, long value) {
    }


//ORIGINAL LINE: public virtual ulong GetData64(uint id)
    public long getData64(int id) {
        return 0;
    }


//ORIGINAL LINE: public virtual uint GetData(uint id)
    public int getData(int id) {
        return 0;
    }



    public void setData(int id, int value) {
    }


//ORIGINAL LINE: public virtual void OnGameEvent(bool start, ushort eventId)
    public void onGameEvent(boolean start, short eventId) {
    }

//ORIGINAL LINE: public virtual void OnLootStateChanged(uint state, Unit unit)
    public void onLootStateChanged(int state, Unit unit) {
    }
    public void onStateChanged(GameObjectState state) {
    }

//ORIGINAL LINE: public virtual void EventInform(uint eventId)
    public void eventInform(int eventId) {
    }

    // Called when hit by a spell
    public void spellHit(WorldObject caster, SpellInfo spellInfo) {
    }

    // Called when spell hits a target
    public void spellHitTarget(WorldObject target, SpellInfo spellInfo) {
    }

    // Called when the gameobject summon successfully other creature
    public void justSummoned(Creature summon) {
    }

    public void summonedCreatureDespawn(Creature summon) {
    }
    public void summonedCreatureDies(Creature summon, Unit killer) {
    }

    // Called when the capture point gets assaulted by a player. Return true to disable default behaviour.
    public boolean onCapturePointAssaulted(Player player) {
        return false;
    }

    // Called when the capture point state gets updated. Return true to disable default behaviour.
    public boolean onCapturePointUpdated(BattlegroundCapturePointState state) {
        return false;
    }
}