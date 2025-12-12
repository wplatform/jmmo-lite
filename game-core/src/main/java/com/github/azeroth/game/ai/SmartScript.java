package game.ai;

import Framework.Constants.*;
import game.chat.*;
import game.datastorage.*;
import game.entities.*;
import game.maps.*;
import game.misc.*;
import game.movement.*;
import game.scripting.interfaces.iareatrigger.*;
import game.spells.*;
import game.*;
import java.util.*;

// Copyright (c) Forged WoW LLC <https://github.com/ForgedWoW/ForgedCore>
// Licensed under GPL-3.0 license. See <https://github.com/ForgedWoW/ForgedCore/blob/master/LICENSE> for full information.




public class SmartScript {
    public ObjectGuid lastInvoker = new ObjectGuid();

    // Max number of nested ProcessEventsFor() calls to avoid infinite loops
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: const uint MaxNestedEvents = 10;
    private static final int MAX_NESTED_EVENTS = 10;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: readonly Dictionary<uint, uint> _counterList = new();
    private final HashMap<Integer, Integer> counterList = new HashMap<Integer, Integer>();
    private final ArrayList<SmartScriptHolder> events = new ArrayList<SmartScriptHolder>();
    private final ArrayList<SmartScriptHolder> installEvents = new ArrayList<SmartScriptHolder>();
    private final ArrayList<SmartScriptHolder> storedEvents = new ArrayList<SmartScriptHolder>();
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: readonly List<uint> _remIDs = new();
    private final ArrayList<Integer> remIDs = new ArrayList<Integer>();
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: readonly Dictionary<uint, ObjectGuidList> _storedTargets = new();
    private final HashMap<Integer, ObjectGuidList> storedTargets = new HashMap<Integer, ObjectGuidList>();
    private ArrayList<SmartScriptHolder> timedActionList = new ArrayList<SmartScriptHolder>();
    private ObjectGuid mTimedActionListInvoker = new ObjectGuid();
    private Creature me;
    private ObjectGuid meOrigGUID = new ObjectGuid();
    private GameObject go;
    private ObjectGuid goOrigGUID = new ObjectGuid();
    private Player player;
    private AreaTriggerRecord trigger;
    private AreaTrigger areaTrigger;
    private SceneTemplate sceneTemplate;
    private Quest quest;
    private SmartScriptType scriptType = SmartScriptType.values()[0];
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: uint _eventPhase;
    private int eventPhase;

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: uint _pathId;
    private int pathId;

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: uint _textTimer;
    private int textTimer;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: uint _lastTextID;
    private int lastTextID;
    private ObjectGuid textGUID = new ObjectGuid();
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: uint _talkerEntry;
    private int talkerEntry;
    private boolean useTextTimer;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: uint _currentPriority;
    private int currentPriority;
    private boolean eventSortingRequired;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: uint _nestedEventsCounter;
    private int nestedEventsCounter;
    private SmartEventFlags allEventFlags = SmartEventFlags.values()[0];

    public SmartScript() {
        go = null;
        me = null;
        trigger = null;
        eventPhase = 0;
        pathId = 0;
        textTimer = 0;
        lastTextID = 0;
        textGUID = ObjectGuid.empty;
        useTextTimer = false;
        talkerEntry = 0;
        meOrigGUID = ObjectGuid.empty;
        goOrigGUID = ObjectGuid.empty;
        lastInvoker = ObjectGuid.empty;
        scriptType = SmartScriptType.Creature;
    }

    public final void onReset() {
        resetBaseObject();

        synchronized (events) {
            for (var holder : events) {
                if (!holder.event.eventFlags.HasAnyFlag(SmartEventFlags.DontReset)) {
                    initTimer(holder);
                    holder.runOnce = false;
                }

                if (holder.priority != SmartScriptHolder.DEFAULT_PRIORITY) {
                    holder.priority = SmartScriptHolder.DEFAULT_PRIORITY;
                    eventSortingRequired = true;
                }
            }
        }

        processEventsFor(SmartEvents.Reset);
        lastInvoker.clear();
    }


    public final void processEventsFor(SmartEvents e, Unit unit, int var0, int var1, boolean bvar, SpellInfo spell, GameObject gob) {
        processEventsFor(e, unit, var0, var1, bvar, spell, gob, "");
    }

    public final void processEventsFor(SmartEvents e, Unit unit, int var0, int var1, boolean bvar, SpellInfo spell) {
        processEventsFor(e, unit, var0, var1, bvar, spell, null, "");
    }

    public final void processEventsFor(SmartEvents e, Unit unit, int var0, int var1, boolean bvar) {
        processEventsFor(e, unit, var0, var1, bvar, null, null, "");
    }

    public final void processEventsFor(SmartEvents e, Unit unit, int var0, int var1) {
        processEventsFor(e, unit, var0, var1, false, null, null, "");
    }

    public final void processEventsFor(SmartEvents e, Unit unit, int var0) {
        processEventsFor(e, unit, var0, 0, false, null, null, "");
    }

    public final void processEventsFor(SmartEvents e, Unit unit) {
        processEventsFor(e, unit, 0, 0, false, null, null, "");
    }

    public final void processEventsFor(SmartEvents e) {
        processEventsFor(e, null, 0, 0, false, null, null, "");
    }

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public void ProcessEventsFor(SmartEvents e, Unit unit = null, uint var0 = 0, uint var1 = 0, bool bvar = false, SpellInfo spell = null, GameObject gob = null, string varString = "")
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
    public final void processEventsFor(SmartEvents e, Unit unit, int var0, int var1, boolean bvar, SpellInfo spell, GameObject gob, String varString) {
        nestedEventsCounter++;

        // Allow only a fixed number of nested ProcessEventsFor calls
        if (nestedEventsCounter > MAX_NESTED_EVENTS) {
            Log.outWarn(LogFilter.ScriptsAi, String.format("SmartScript::ProcessEventsFor: reached the limit of max allowed nested ProcessEventsFor() calls with event %1$s, skipping!\n%2$s", e, getBaseObject().getDebugInfo()));
        } else if (nestedEventsCounter == 1) {
            synchronized (events) { // only lock on the first event to prevent deadlock.
                Process(e, unit, var0, var1, bvar, spell, gob, varString);
            }
        } else {
            Process(e, unit, var0, var1, bvar, spell, gob, varString);
        }
        --nestedEventsCounter;

//C# TO JAVA CONVERTER TODO TASK: Local functions are not converted by C# to Java Converter:
//        void Process(SmartEvents e, Unit unit, uint var0, uint var1, bool bvar, SpellInfo spell, GameObject gob, string varString)
//            {
//                foreach (var Event in _events)
//                {
//                    var eventType = Event.GetEventType();
//
//                    if (eventType == SmartEvents.Link) //special handling
//                        continue;
//
//                    if (eventType == e)
//                        if (Global.ConditionMgr.IsObjectMeetingSmartEventConditions(Event.EntryOrGuid, Event.EventId, Event.SourceType, unit, GetBaseObject()))
//                            ProcessEvent(Event, unit, var0, var1, bvar, spell, gob, varString);
//                }
//            }
    }

    public final boolean checkTimer(SmartScriptHolder e) {
        return e.active;
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public void OnUpdate(uint diff)
    public final void onUpdate(int diff) {
        if ((scriptType == SmartScriptType.Creature || scriptType == SmartScriptType.GameObject || scriptType == SmartScriptType.AreaTriggerEntity || scriptType == SmartScriptType.AreaTriggerEntityServerside) && !getBaseObject()) {
            return;
        }

        if (me != null && me.isInEvadeMode()) {
            // Check if the timed action list finished and clear it if so.
            // This is required by SMART_ACTION_CALL_TIMED_ACTIONLIST failing if mTimedActionList is not empty.
            if (!timedActionList.Empty()) {
                var needCleanup1 = true;

                for (var scriptholder : timedActionList) {
                    if (scriptholder.enableTimed) {
                        needCleanup1 = false;
                    }
                }

                if (needCleanup1) {
                    timedActionList.clear();
                }
            }

            return;
        }

        installEvents(); //before UpdateTimers

        if (eventSortingRequired) {
            synchronized (events) {
                sortEvents(events);
            }

            eventSortingRequired = false;
        }

        synchronized (events) {
            for (var holder : events) {
                updateTimer(holder, diff);
            }
        }

        if (!storedEvents.Empty()) {
            for (var holder : storedEvents) {
                updateTimer(holder, diff);
            }
        }

        var needCleanup = true;

        if (!timedActionList.Empty()) {
            for (var i = 0; i < timedActionList.size(); ++i) {
                var scriptHolder = timedActionList.get(i);

                if (scriptHolder.enableTimed) {
                    updateTimer(scriptHolder, diff);
                    needCleanup = false;
                }
            }
        }

        if (needCleanup) {
            timedActionList.clear();
        }

        if (!remIDs.Empty()) {
            for (var id : remIDs) {
                removeStoredEvent(id);
            }

            remIDs.clear();
        }

        if (useTextTimer && me != null) {
            if (textTimer < diff) {
                var textID = lastTextID;
                lastTextID = 0;
                var entry = talkerEntry;
                talkerEntry = 0;
                textTimer = 0;
                useTextTimer = false;
                processEventsFor(SmartEvents.TextOver, null, textID, entry);
            } else {
                textTimer -= diff;
            }
        }
    }


    public final void onInitialize(WorldObject obj, AreaTriggerRecord at, SceneTemplate scene) {
        onInitialize(obj, at, scene, null);
    }

    public final void onInitialize(WorldObject obj, AreaTriggerRecord at) {
        onInitialize(obj, at, null, null);
    }

    public final void onInitialize(WorldObject obj) {
        onInitialize(obj, null, null, null);
    }

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public void OnInitialize(WorldObject obj, AreaTriggerRecord at = null, SceneTemplate scene = null, Quest qst = null)
    public final void onInitialize(WorldObject obj, AreaTriggerRecord at, SceneTemplate scene, Quest qst) {
        if (at != null) {
            scriptType = SmartScriptType.AreaTrigger;
            trigger = at;
            player = obj.getAsPlayer();

            if (player == null) {
                Log.outError(LogFilter.Misc, String.format("SmartScript::OnInitialize: source is AreaTrigger with id %1$s, missing trigger player", trigger.id));

                return;
            }

            Log.outDebug(LogFilter.ScriptsAi, String.format("SmartScript::OnInitialize: source is AreaTrigger with id %1$s, triggered by player %2$s", trigger.id, player.getGUID().clone()));
        } else if (scene != null) {
            scriptType = SmartScriptType.Scene;
            sceneTemplate = scene;
            player = obj.getAsPlayer();

            if (player == null) {
                Log.outError(LogFilter.Misc, String.format("SmartScript::OnInitialize: source is Scene with id %1$s, missing trigger player", sceneTemplate.sceneId));

                return;
            }

            Log.outDebug(LogFilter.ScriptsAi, String.format("SmartScript::OnInitialize: source is Scene with id %1$s, triggered by player %2$s", sceneTemplate.sceneId, player.getGUID().clone()));
        } else if (qst != null) {
            scriptType = SmartScriptType.Quest;
            quest = qst;
            player = obj.getAsPlayer();

            if (player == null) {
                Log.outError(LogFilter.Misc, String.format("SmartScript::OnInitialize: source is Quest with id %1$s, missing trigger player", qst.id));

                return;
            }

            Log.outDebug(LogFilter.ScriptsAi, String.format("SmartScript::OnInitialize: source is Quest with id %1$s, triggered by player %2$s", qst.id, player.getGUID().clone()));
        } else if (obj != null) { // Handle object based scripts
            switch (obj.getTypeId()) {
                case Unit:
                    scriptType = SmartScriptType.Creature;
                    me = obj.getAsCreature();
                    Log.outDebug(LogFilter.Scripts, String.format("SmartScript.OnInitialize: source is Creature %1$s", me.getEntry()));

                    break;
                case GameObject:
                    scriptType = SmartScriptType.GameObject;
                    go = obj.getAsGameObject();
                    Log.outDebug(LogFilter.Scripts, String.format("SmartScript.OnInitialize: source is GameObject %1$s", go.getEntry()));

                    break;
                case AreaTrigger:
                    areaTrigger = obj.getAsAreaTrigger();
                    scriptType = areaTrigger.isServerSide() ? SmartScriptType.AreaTriggerEntityServerside : SmartScriptType.AreaTriggerEntity;
                    Log.outDebug(LogFilter.ScriptsAi, String.format("SmartScript.OnInitialize: source is AreaTrigger %1$s, IsServerSide %2$s", areaTrigger.getEntry(), areaTrigger.isServerSide()));

                    break;
                default:
                    Log.outError(LogFilter.Scripts, "SmartScript.OnInitialize: Unhandled TypeID !WARNING!");

                    return;
            }
        } else {
            Log.outError(LogFilter.ScriptsAi, "SmartScript.OnInitialize: !WARNING! Initialized WorldObject is Null.");

            return;
        }

        getScript(); //load copy of script

        synchronized (events) {
            for (var holder : events) {
                initTimer(holder); //calculate timers for first Time use
            }
        }

        processEventsFor(SmartEvents.AiInit);
        installEvents();
        processEventsFor(SmartEvents.JustCreated);
        counterList.clear();
    }

    public final void onMoveInLineOfSight(Unit who) {
        if (me == null) {
            return;
        }

        processEventsFor(me.isEngaged() ? SmartEvents.IcLos : SmartEvents.OocLos, who);
    }


    public final Unit doSelectBelowHpPctFriendlyWithEntry(int entry, float range, byte minHPDiff) {
        return doSelectBelowHpPctFriendlyWithEntry(entry, range, minHPDiff, true);
    }

    public final Unit doSelectBelowHpPctFriendlyWithEntry(int entry, float range) {
        return doSelectBelowHpPctFriendlyWithEntry(entry, range, 1, true);
    }

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public Unit DoSelectBelowHpPctFriendlyWithEntry(uint entry, float range, byte minHPDiff = 1, bool excludeSelf = true)
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
    public final Unit doSelectBelowHpPctFriendlyWithEntry(int entry, float range, byte minHPDiff, boolean excludeSelf) {
        FriendlyBelowHpPctEntryInRange uCheck = new FriendlyBelowHpPctEntryInRange(me, entry, range, minHPDiff, excludeSelf);
        UnitLastSearcher searcher = new UnitLastSearcher(me, uCheck, GridType.All);
        Cell.visitGrid(me, searcher, range);

        return searcher.getTarget();
    }


    public final void setTimedActionList(SmartScriptHolder e, int entry, Unit invoker) {
        setTimedActionList(e, entry, invoker, 0);
    }

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public void SetTimedActionList(SmartScriptHolder e, uint entry, Unit invoker, uint startFromEventId = 0)
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
    public final void setTimedActionList(SmartScriptHolder e, int entry, Unit invoker, int startFromEventId) {
        // Do NOT allow to start a new actionlist if a previous one is already running, unless explicitly allowed. We need to always finish the current actionlist
        if (e.getActionType() == SmartActions.CallTimedActionlist && e.action.timedActionList.allowOverride == 0 && !timedActionList.Empty()) {
            return;
        }

        timedActionList.clear();
        timedActionList = Global.getSmartAIMgr().getScript((int)entry, SmartScriptType.TimedActionlist);

        if (timedActionList.Empty()) {
            return;
        }

        tangible.ListHelper.removeAll(timedActionList, script -> {
                return script.EventId < startFromEventId;
        });

        mTimedActionListInvoker = invoker != null ? invoker.getGUID() : ObjectGuid.empty;

        for (var i = 0; i < timedActionList.size(); ++i) {
            var scriptHolder = timedActionList.get(i);
            scriptHolder.enableTimed = i == 0; //enable processing only for the first action

            if (e.action.timedActionList.timerType == 0) {
                scriptHolder.event.type = SmartEvents.UpdateOoc;
            } else if (e.action.timedActionList.timerType == 1) {
                scriptHolder.event.type = SmartEvents.UpdateIc;
            } else if (e.action.timedActionList.timerType > 1) {
                scriptHolder.event.type = SmartEvents.Update;
            }

            initTimer(scriptHolder);
        }
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public void SetPathId(uint id)
    public final void setPathId(int id) {
        pathId = id;
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint GetPathId()
    public final int getPathId() {
        return pathId;
    }

    public final boolean hasAnyEventWithFlag(SmartEventFlags flag) {
        return allEventFlags.HasAnyFlag(flag);
    }

    public final boolean isUnit(WorldObject obj) {
        return obj != null && (obj.isTypeId(TypeId.Unit) || obj.isTypeId(TypeId.Player));
    }

    public final boolean isPlayer(WorldObject obj) {
        return obj != null && obj.isTypeId(TypeId.Player);
    }

    public final boolean isCreature(WorldObject obj) {
        return obj != null && obj.isTypeId(TypeId.Unit);
    }

    public final boolean isCharmedCreature(WorldObject obj) {
        if (!obj) {
            return false;
        }

        var creatureObj = obj.getAsCreature();

        if (creatureObj) {
            return creatureObj.isCharmed();
        }

        return false;
    }

    public final boolean isGameObject(WorldObject obj) {
        return obj != null && obj.isTypeId(TypeId.GameObject);
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public List<WorldObject> GetStoredTargetList(uint id, WorldObject obj)
    public final ArrayList<WorldObject> getStoredTargetList(int id, WorldObject obj) {
        var list = storedTargets.LookupByKey(id);

        if (list != null) {
            return list.GetObjectList(obj);
        }

        return null;
    }


    private void processAction(SmartScriptHolder e, Unit unit, int var0, int var1, boolean bvar, SpellInfo spell, GameObject gob) {
        processAction(e, unit, var0, var1, bvar, spell, gob, "");
    }

    private void processAction(SmartScriptHolder e, Unit unit, int var0, int var1, boolean bvar, SpellInfo spell) {
        processAction(e, unit, var0, var1, bvar, spell, null, "");
    }

    private void processAction(SmartScriptHolder e, Unit unit, int var0, int var1, boolean bvar) {
        processAction(e, unit, var0, var1, bvar, null, null, "");
    }

    private void processAction(SmartScriptHolder e, Unit unit, int var0, int var1) {
        processAction(e, unit, var0, var1, false, null, null, "");
    }

    private void processAction(SmartScriptHolder e, Unit unit, int var0) {
        processAction(e, unit, var0, 0, false, null, null, "");
    }

    private void processAction(SmartScriptHolder e, Unit unit) {
        processAction(e, unit, 0, 0, false, null, null, "");
    }

    private void processAction(SmartScriptHolder e) {
        processAction(e, null, 0, 0, false, null, null, "");
    }

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: void ProcessAction(SmartScriptHolder e, Unit unit = null, uint var0 = 0, uint var1 = 0, bool bvar = false, SpellInfo spell = null, GameObject gob = null, string varString = "")
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
    private void processAction(SmartScriptHolder e, Unit unit, int var0, int var1, boolean bvar, SpellInfo spell, GameObject gob, String varString) {
        e.runOnce = true; //used for repeat check

        //calc random
        if (e.getEventType() != SmartEvents.Link && e.event.eventChance < 100 && e.event.eventChance != 0 && !e.event.eventFlags.HasFlag(SmartEventFlags.TempIgnoreChanceRoll)) {
            if (RandomHelper.randChance(e.event.eventChance)) {
                return;
            }
        }

        // Remove SMART_EVENT_FLAG_TEMP_IGNORE_CHANCE_ROLL flag after processing roll chances as it's not needed anymore
        e.event.eventFlags = SmartEventFlags.forValue(e.event.eventFlags.getValue() & ~SmartEventFlags.TempIgnoreChanceRoll.getValue());

        if (unit != null) {
            lastInvoker = unit.getGUID().clone();
        }

        var tempInvoker = getLastInvoker();

        if (tempInvoker != null) {
            Log.outDebug(LogFilter.ScriptsAi, "SmartScript.ProcessAction: Invoker: {0} (guidlow: {1})", tempInvoker.getName(), tempInvoker.getGUID().toString());
        }

        var targets = getTargets(e, unit != null ? unit : gob);

        switch (e.getActionType()) {
            case Talk: {
                var talker = e.target.type == 0 ? me : null;
                Unit talkTarget = null;

                for (var target : targets) {
                    if (isCreature(target) && !target.getAsCreature().isPet()) { // Prevented sending text to pets.
                        if (e.action.talk.useTalkTarget != 0) {
                            talker = me;
                            talkTarget = target.getAsCreature();
                        } else {
                            talker = target.getAsCreature();
                        }

                        break;
                    } else if (isPlayer(target)) {
                        talker = me;
                        talkTarget = target.getAsPlayer();

                        break;
                    }
                }

                if (talkTarget == null) {
                    talkTarget = getLastInvoker();
                }

                if (talker == null) {
                    break;
                }

                talkerEntry = talker.getEntry();
                lastTextID = e.action.talk.textGroupId;
                textTimer = e.action.talk.duration;

                useTextTimer = true;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: Global.CreatureTextMgr.SendChat(talker, (byte)e.Action.talk.textGroupId, talkTarget);
                Global.getCreatureTextMgr().sendChat(talker, (byte)e.action.talk.textGroupId, talkTarget);

                Log.outDebug(LogFilter.ScriptsAi, "SmartScript.ProcessAction: SMART_ACTION_TALK: talker: {0} (Guid: {1}), textGuid: {2}", talker.getName(), talker.getGUID().toString(), textGUID.toString());

                break;
            }
            case SimpleTalk: {
                for (var target : targets) {
                    if (isCreature(target)) {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: Global.CreatureTextMgr.SendChat(target.AsCreature, (byte)e.Action.simpleTalk.textGroupId, IsPlayer(GetLastInvoker()) ? GetLastInvoker() : null);
                        Global.getCreatureTextMgr().sendChat(target.getAsCreature(), (byte)e.action.simpleTalk.textGroupId, isPlayer(getLastInvoker()) ? getLastInvoker() : null);
                    } else if (isPlayer(target) && me != null) {
                        var templastInvoker = getLastInvoker();
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: Global.CreatureTextMgr.SendChat(_me, (byte)e.Action.simpleTalk.textGroupId, IsPlayer(templastInvoker) ? templastInvoker : null, ChatMsg.Addon, Language.Addon, CreatureTextRange.Normal, 0, SoundKitPlayType.Normal, TeamFaction.Other, false, target.AsPlayer);
                        Global.getCreatureTextMgr().sendChat(me, (byte)e.action.simpleTalk.textGroupId, isPlayer(templastInvoker) ? templastInvoker : null, ChatMsg.Addon, Language.Addon, CreatureTextRange.Normal, 0, SoundKitPlayType.Normal, TeamFaction.Other, false, target.getAsPlayer());
                    }

                    Log.outDebug(LogFilter.ScriptsAi, "SmartScript.ProcessAction. SMART_ACTION_SIMPLE_TALK: talker: {0} (GuidLow: {1}), textGroupId: {2}", target.getName(), target.getGUID().toString(), e.action.simpleTalk.textGroupId);
                }

                break;
            }
            case PlayEmote: {
                for (var target : targets) {
                    if (isUnit(target)) {
                        target.getAsUnit().handleEmoteCommand(Emote.forValue(e.action.emote.emoteId));

                        Log.outDebug(LogFilter.ScriptsAi, "SmartScript.ProcessAction. SMART_ACTION_PLAY_EMOTE: target: {0} (GuidLow: {1}), emote: {2}", target.getName(), target.getGUID().toString(), e.action.emote.emoteId);
                    }
                }

                break;
            }
            case Sound: {
                for (var target : targets) {
                    if (isUnit(target)) {
                        if (e.action.sound.distance == 1) {
                            target.playDistanceSound(e.action.sound.soundId, e.action.sound.onlySelf != 0 ? target.getAsPlayer() : null);
                        } else {
                            target.playDirectSound(e.action.sound.soundId, e.action.sound.onlySelf != 0 ? target.getAsPlayer() : null, e.action.sound.keyBroadcastTextId);
                        }

                        Log.outDebug(LogFilter.ScriptsAi, "SmartScript.ProcessAction. SMART_ACTION_SOUND: target: {0} (GuidLow: {1}), sound: {2}, onlyself: {3}", target.getName(), target.getGUID().toString(), e.action.sound.soundId, e.action.sound.onlySelf);
                    }
                }

                break;
            }
            case SetFaction: {
                for (var target : targets) {
                    if (isCreature(target)) {
                        if (e.action.faction.factionId != 0) {
                            target.getAsCreature().setFaction(e.action.faction.factionId);

                            Log.outDebug(LogFilter.ScriptsAi, "SmartScript.ProcessAction. SMART_ACTION_SET_FACTION: Creature entry {0}, GuidLow {1} set faction to {2}", target.getEntry(), target.getGUID().toString(), e.action.faction.factionId);
                        } else {
                            var ci = Global.getObjectMgr().getCreatureTemplate(target.getAsCreature().getEntry());

                            if (ci != null) {
                                if (target.getAsCreature().getFaction() != ci.faction) {
                                    target.getAsCreature().setFaction(ci.faction);

                                    Log.outDebug(LogFilter.ScriptsAi, "SmartScript.ProcessAction. SMART_ACTION_SET_FACTION: Creature entry {0}, GuidLow {1} set faction to {2}", target.getEntry(), target.getGUID().toString(), ci.faction);
                                }
                            }
                        }
                    }
                }

                break;
            }
            case MorphToEntryOrModel: {
                for (var target : targets) {
                    if (!isCreature(target)) {
                        continue;
                    }

                    if (e.action.morphOrMount.creature != 0 || e.action.morphOrMount.model != 0) {
                        //set model based on entry from creature_template
                        if (e.action.morphOrMount.creature != 0) {
                            var ci = Global.getObjectMgr().getCreatureTemplate(e.action.morphOrMount.creature);

                            if (ci != null) {
                                var model = ObjectManager.chooseDisplayId(ci);
                                target.getAsCreature().setDisplayId(model.creatureDisplayId, model.displayScale);

                                Log.outDebug(LogFilter.ScriptsAi, "SmartScript.ProcessAction. SMART_ACTION_MORPH_TO_ENTRY_OR_MODEL: Creature entry {0}, GuidLow {1} set displayid to {2}", target.getEntry(), target.getGUID().toString(), model.creatureDisplayId);
                            }
                        }
                        //if no param1, then use value from param2 (modelId)
                        else {
                            target.getAsCreature().setDisplayId(e.action.morphOrMount.model);

                            Log.outDebug(LogFilter.ScriptsAi, "SmartScript.ProcessAction. SMART_ACTION_MORPH_TO_ENTRY_OR_MODEL: Creature entry {0}, GuidLow {1} set displayid to {2}", target.getEntry(), target.getGUID().toString(), e.action.morphOrMount.model);
                        }
                    } else {
                        target.getAsCreature().deMorph();

                        Log.outDebug(LogFilter.ScriptsAi, "SmartScript.ProcessAction. SMART_ACTION_MORPH_TO_ENTRY_OR_MODEL: Creature entry {0}, GuidLow {1} demorphs.", target.getEntry(), target.getGUID().toString());
                    }
                }

                break;
            }
            case FailQuest: {
                for (var target : targets) {
                    if (isPlayer(target)) {
                        target.getAsPlayer().failQuest(e.action.quest.questId);

                        Log.outDebug(LogFilter.ScriptsAi, "SmartScript.ProcessAction. SMART_ACTION_FAIL_QUEST: Player guidLow {0} fails quest {1}", target.getGUID().toString(), e.action.quest.questId);
                    }
                }

                break;
            }
            case OfferQuest: {
                for (var target : targets) {
                    var player = target.getAsPlayer();

                    if (player) {
                        var quest = Global.getObjectMgr().getQuestTemplate(e.action.questOffer.questId);

                        if (quest != null) {
                            if (me && e.action.questOffer.directAdd == 0) {
                                if (player.canTakeQuest(quest, true)) {
                                    var session = player.getSession();

                                    if (session) {
                                        PlayerMenu menu = new PlayerMenu(session);
                                        menu.sendQuestGiverQuestDetails(quest, me.getGUID().clone(), true, false);
                                        Log.outDebug(LogFilter.ScriptsAi, "SmartScript.ProcessAction:: SMART_ACTION_OFFER_QUEST: Player {0} - offering quest {1}", player.getGUID().toString(), e.action.questOffer.questId);
                                    }
                                }
                            } else {
                                player.addQuestAndCheckCompletion(quest, null);
                                Log.outDebug(LogFilter.ScriptsAi, "SmartScript.ProcessAction: SMART_ACTION_ADD_QUEST: Player {0} add quest {1}", player.getGUID().toString(), e.action.questOffer.questId);
                            }
                        }
                    }
                }

                break;
            }
            case SetReactState: {
                for (var target : targets) {
                    if (!isCreature(target)) {
                        continue;
                    }

                    target.getAsCreature().reactState = ReactStates.forValue(e.action.react.state);
                }

                break;
            }
            case RandomEmote: {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: List<uint> emotes = new();
                ArrayList<Integer> emotes = new ArrayList<Integer>();
                var randomEmote = e.action.randomEmote.clone();

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: foreach (var id in new[] { randomEmote.emote1, randomEmote.emote2, randomEmote.emote3, randomEmote.emote4, randomEmote.emote5, randomEmote.emote6})
                for (var id : new int[] {randomEmote.emote1, randomEmote.emote2, randomEmote.emote3, randomEmote.emote4, randomEmote.emote5, randomEmote.emote6}) {
                    if (id != 0) {
                        emotes.add(id);
                    }
                }

                for (var target : targets) {
                    if (isUnit(target)) {
                        var emote = emotes.SelectRandom();
                        target.getAsUnit().handleEmoteCommand(Emote.forValue(emote));

                        Log.outDebug(LogFilter.ScriptsAi, "SmartScript.ProcessAction. SMART_ACTION_RANDOM_EMOTE: Creature guidLow {0} handle random emote {1}", target.getGUID().toString(), emote);
                    }
                }

                break;
            }
            case ThreatAllPct: {
                if (me == null) {
                    break;
                }

                for (var refe : me.getThreatManager().getModifiableThreatList()) {
                    refe.modifyThreatByPercent(Math.max(-100, (int)(e.action.threatPCT.threatINC - e.action.threatPCT.threatDEC)));
                    Log.outDebug(LogFilter.ScriptsAi, String.format("SmartScript.ProcessAction: SMART_ACTION_THREAT_ALL_PCT: Creature %1$s modify threat for %2$s, value %3$s", me.getGUID().clone(), refe.getVictim().getGUID().clone(), e.action.threatPCT.threatINC - e.action.threatPCT.threatDEC));
                }

                break;
            }
            case ThreatSinglePct: {
                if (me == null) {
                    break;
                }

                for (var target : targets) {
                    if (isUnit(target)) {
                        me.getThreatManager().modifyThreatByPercent(target.getAsUnit(), Math.max(-100, (int)(e.action.threatPCT.threatINC - e.action.threatPCT.threatDEC)));
                        Log.outDebug(LogFilter.ScriptsAi, String.format("SmartScript.ProcessAction: SMART_ACTION_THREAT_SINGLE_PCT: Creature %1$s modify threat for %2$s, value %3$s", me.getGUID().clone(), target.getGUID().clone(), e.action.threatPCT.threatINC - e.action.threatPCT.threatDEC));
                    }
                }

                break;
            }
            case CallAreaexploredoreventhappens: {
                for (var target : targets) {
                    // Special handling for vehicles
                    if (isUnit(target)) {
                        var vehicle = target.getAsUnit().getVehicleKit();

                        if (vehicle != null) {
                            for (var seat : vehicle.seats.entrySet()) {
                                var player = Global.getObjAccessor().GetPlayer(target, seat.getValue().Passenger.Guid);

                                if (player != null) {
                                    player.AreaExploredOrEventHappens(e.action.quest.questId);
                                }
                            }
                        }
                    }

                    if (isPlayer(target)) {
                        target.getAsPlayer().areaExploredOrEventHappens(e.action.quest.questId);

                        Log.outDebug(LogFilter.ScriptsAi, "SmartScript.ProcessAction. SMART_ACTION_CALL_AREAEXPLOREDOREVENTHAPPENS: {0} credited quest {1}", target.getGUID().toString(), e.action.quest.questId);
                    }
                }

                break;
            }
            case Cast: {
                if (e.action.cast.targetsLimit > 0 && targets.size() > e.action.cast.targetsLimit) {
                    targets.RandomResize(e.action.cast.targetsLimit);
                }

                var failedSpellCast = false;
                var successfulSpellCast = false;

                for (var target : targets) {
                    if (go != null) {
                        go.castSpell(target.getAsUnit(), e.action.cast.spell);
                    }

                    if (!isUnit(target)) {
                        continue;
                    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: if (!e.Action.cast.castFlags.HasAnyFlag((uint)SmartCastFlags.AuraNotPresent) || !target.AsUnit.HasAura(e.Action.cast.spell))
                    if (!e.action.cast.castFlags.HasAnyFlag((int)SmartCastFlags.AuraNotPresent.getValue()) || !target.getAsUnit().hasAura(e.action.cast.spell)) {
                        var triggerFlag = TriggerCastFlags.None;

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: if (e.Action.cast.castFlags.HasAnyFlag((uint)SmartCastFlags.Triggered))
                        if (e.action.cast.castFlags.HasAnyFlag((int)SmartCastFlags.Triggered.getValue())) {
                            if (e.action.cast.triggerFlags != 0) {
                                triggerFlag = TriggerCastFlags.forValue(e.action.cast.triggerFlags);
                            } else {
                                triggerFlag = TriggerCastFlags.FullMask;
                            }
                        }

                        if (me) {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: if (e.Action.cast.castFlags.HasAnyFlag((uint)SmartCastFlags.InterruptPrevious))
                            if (e.action.cast.castFlags.HasAnyFlag((int)SmartCastFlags.InterruptPrevious.getValue())) {
                                me.interruptNonMeleeSpells(false);
                            }

                            var result = me.castSpell(target.getAsUnit(), e.action.cast.spell, new CastSpellExtraArgs(triggerFlag));
                            var spellCastFailed = (result != SpellCastResult.SpellCastOk && result != SpellCastResult.SpellInProgress);

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: if (e.Action.cast.castFlags.HasAnyFlag((uint)SmartCastFlags.CombatMove))
                            if (e.action.cast.castFlags.HasAnyFlag((int)SmartCastFlags.CombatMove.getValue())) {
                                ((SmartAI)me.getAI()).setCombatMove(spellCastFailed, true);
                            }

                            if (spellCastFailed) {
                                failedSpellCast = true;
                            } else {
                                successfulSpellCast = true;
                            }
                        } else if (go) {
                            go.castSpell(target.getAsUnit(), e.action.cast.spell, new CastSpellExtraArgs(triggerFlag));
                        }
                    } else {
                        Log.outDebug(LogFilter.ScriptsAi, "Spell {0} not casted because it has flag SMARTCAST_AURA_NOT_PRESENT and the target (Guid: {1} Entry: {2} Type: {3}) already has the aura", e.action.cast.spell, target.getGUID().clone(), target.getEntry(), target.getTypeId());
                    }
                }

                // If there is at least 1 failed cast and no successful casts at all, retry again on next loop
                if (failedSpellCast && !successfulSpellCast) {
                    retryLater(e, true);

                    // Don't execute linked events
                    return;
                }

                break;
            }
            case SelfCast: {
                if (targets.Empty()) {
                    break;
                }

                if (e.action.cast.targetsLimit != 0) {
                    targets.RandomResize(e.action.cast.targetsLimit);
                }

                var triggerFlags = TriggerCastFlags.None;

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: if (e.Action.cast.castFlags.HasAnyFlag((uint)SmartCastFlags.Triggered))
                if (e.action.cast.castFlags.HasAnyFlag((int)SmartCastFlags.Triggered.getValue())) {
                    if (e.action.cast.triggerFlags != 0) {
                        triggerFlags = TriggerCastFlags.forValue(e.action.cast.triggerFlags);
                    } else {
                        triggerFlags = TriggerCastFlags.FullMask;
                    }
                }

                for (var target : targets) {
                    var uTarget = target.getAsUnit();

                    if (uTarget == null) {
                        continue;
                    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: if (!e.Action.cast.castFlags.HasAnyFlag((uint)SmartCastFlags.AuraNotPresent) || !uTarget.HasAura(e.Action.cast.spell))
                    if (!e.action.cast.castFlags.HasAnyFlag((int)SmartCastFlags.AuraNotPresent.getValue()) || !uTarget.hasAura(e.action.cast.spell)) {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: if (e.Action.cast.castFlags.HasAnyFlag((uint)SmartCastFlags.InterruptPrevious))
                        if (e.action.cast.castFlags.HasAnyFlag((int)SmartCastFlags.InterruptPrevious.getValue())) {
                            uTarget.interruptNonMeleeSpells(false);
                        }

                        uTarget.castSpell(uTarget, e.action.cast.spell, new CastSpellExtraArgs(triggerFlags));
                    }
                }

                break;
            }
            case InvokerCast: {
                var tempLastInvoker = getLastInvoker(unit);

                if (tempLastInvoker == null) {
                    break;
                }

                if (targets.Empty()) {
                    break;
                }

                if (e.action.cast.targetsLimit != 0) {
                    targets.RandomResize(e.action.cast.targetsLimit);
                }

                for (var target : targets) {
                    if (!isUnit(target)) {
                        continue;
                    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: if (!e.Action.cast.castFlags.HasAnyFlag((uint)SmartCastFlags.AuraNotPresent) || !target.AsUnit.HasAura(e.Action.cast.spell))
                    if (!e.action.cast.castFlags.HasAnyFlag((int)SmartCastFlags.AuraNotPresent.getValue()) || !target.getAsUnit().hasAura(e.action.cast.spell)) {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: if (e.Action.cast.castFlags.HasAnyFlag((uint)SmartCastFlags.InterruptPrevious))
                        if (e.action.cast.castFlags.HasAnyFlag((int)SmartCastFlags.InterruptPrevious.getValue())) {
                            tempLastInvoker.interruptNonMeleeSpells(false);
                        }

                        var triggerFlag = TriggerCastFlags.None;

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: if (e.Action.cast.castFlags.HasAnyFlag((uint)SmartCastFlags.Triggered))
                        if (e.action.cast.castFlags.HasAnyFlag((int)SmartCastFlags.Triggered.getValue())) {
                            if (e.action.cast.triggerFlags != 0) {
                                triggerFlag = TriggerCastFlags.forValue(e.action.cast.triggerFlags);
                            } else {
                                triggerFlag = TriggerCastFlags.FullMask;
                            }
                        }

                        tempLastInvoker.castSpell(target.getAsUnit(), e.action.cast.spell, new CastSpellExtraArgs(triggerFlag));

                        Log.outDebug(LogFilter.ScriptsAi, "SmartScript.ProcessAction. SMART_ACTION_INVOKER_CAST: Invoker {0} casts spell {1} on target {2} with castflags {3}", tempLastInvoker.getGUID().toString(), e.action.cast.spell, target.getGUID().toString(), e.action.cast.castFlags);
                    } else {
                        Log.outDebug(LogFilter.ScriptsAi, "Spell {0} not cast because it has flag SMARTCAST_AURA_NOT_PRESENT and the target ({1}) already has the aura", e.action.cast.spell, target.getGUID().toString());
                    }
                }

                break;
            }
            case ActivateGobject: {
                for (var target : targets) {
                    if (isGameObject(target)) {
                        // Activate
                        target.getAsGameObject().setLootState(LootState.Ready);

                        target.getAsGameObject().useDoorOrButton(0, false, unit);

                        Log.outDebug(LogFilter.ScriptsAi, "SmartScript.ProcessAction. SMART_ACTION_ACTIVATE_GOBJECT. Gameobject {0} (entry: {1}) activated", target.getGUID().toString(), target.getEntry());
                    }
                }

                break;
            }
            case ResetGobject: {
                for (var target : targets) {
                    if (isGameObject(target)) {
                        target.getAsGameObject().resetDoorOrButton();

                        Log.outDebug(LogFilter.ScriptsAi, "SmartScript.ProcessAction. SMART_ACTION_RESET_GOBJECT. Gameobject {0} (entry: {1}) reset", target.getGUID().toString(), target.getEntry());
                    }
                }

                break;
            }
            case SetEmoteState: {
                for (var target : targets) {
                    if (isUnit(target)) {
                        target.getAsUnit().setEmoteState(Emote.forValue(e.action.emote.emoteId));

                        Log.outDebug(LogFilter.ScriptsAi, "SmartScript.ProcessAction. SMART_ACTION_SET_EMOTE_STATE. Unit {0} set emotestate to {1}", target.getGUID().toString(), e.action.emote.emoteId);
                    }
                }

                break;
            }
            case AutoAttack: {
                me.setCanMelee(e.action.autoAttack.attack != 0);

                Log.outDebug(LogFilter.ScriptsAi, "SmartScript.ProcessAction. SMART_ACTION_AUTO_ATTACK: Creature: {0} bool on = {1}", me.getGUID().toString(), e.action.autoAttack.attack);

                break;
            }
            case AllowCombatMovement: {
                if (!isSmart()) {
                    break;
                }

                var move = e.action.combatMove.move != 0;
                ((SmartAI)me.getAI()).setCombatMove(move);

                Log.outDebug(LogFilter.ScriptsAi, "SmartScript.ProcessAction. SMART_ACTION_ALLOW_COMBAT_MOVEMENT: Creature {0} bool on = {1}", me.getGUID().toString(), e.action.combatMove.move);

                break;
            }
            case SetEventPhase: {
                if (getBaseObject() == null) {
                    break;
                }

                setPhase(e.action.setEventPhase.phase);

                Log.outDebug(LogFilter.ScriptsAi, "SmartScript.ProcessAction. SMART_ACTION_SET_EVENT_PHASE: Creature {0} set event phase {1}", getBaseObject().getGUID().toString(), e.action.setEventPhase.phase);

                break;
            }
            case IncEventPhase: {
                if (getBaseObject() == null) {
                    break;
                }

                incPhase(e.action.incEventPhase.inc);
                decPhase(e.action.incEventPhase.dec);

                Log.outDebug(LogFilter.ScriptsAi, "SmartScript.ProcessAction. SMART_ACTION_INC_EVENT_PHASE: Creature {0} inc event phase by {1}, " + "decrease by {2}", getBaseObject().getGUID().toString(), e.action.incEventPhase.inc, e.action.incEventPhase.dec);

                break;
            }
            case Evade: {
                if (me == null) {
                    break;
                }

                // Reset home position to respawn position if specified in the parameters
                if (e.action.evade.toRespawnPosition == 0) {
                    me.setHomePosition(me.getRespawnPosition());
                }

                me.getAI().enterEvadeMode();
                Log.outDebug(LogFilter.ScriptsAi, "SmartScript.ProcessAction. SMART_ACTION_EVADE: Creature {0} EnterEvadeMode", me.getGUID().toString());

                break;
            }
            case FleeForAssist: {
                if (!me) {
                    break;
                }

                me.doFleeToGetAssistance();

                if (e.action.fleeAssist.withEmote != 0) {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: var builder = new BroadcastTextBuilder(_me, ChatMsg.MonsterEmote, (uint)BroadcastTextIds.FleeForAssist, _me.Gender);
                    var builder = new BroadcastTextBuilder(me, ChatMsg.MonsterEmote, (int)BroadcastTextIds.FleeForAssist.getValue(), me.getGender());
                    Global.getCreatureTextMgr().sendChatPacket(me, builder, ChatMsg.Emote);
                }

                Log.outDebug(LogFilter.ScriptsAi, "SmartScript.ProcessAction. SMART_ACTION_FLEE_FOR_ASSIST: Creature {0} DoFleeToGetAssistance", me.getGUID().toString());

                break;
            }
            case CallGroupeventhappens: {
                if (unit == null) {
                    break;
                }

                // If invoker was pet or charm
                var playerCharmed = unit.getCharmerOrOwnerPlayerOrPlayerItself();

                if (playerCharmed && getBaseObject() != null) {
                    playerCharmed.groupEventHappens(e.action.quest.questId, getBaseObject());

                    Log.outDebug(LogFilter.ScriptsAi, "SmartScript.ProcessAction: SMART_ACTION_CALL_GROUPEVENTHAPPENS: Player {0}, group credit for quest {1}", unit.getGUID().toString(), e.action.quest.questId);
                }

                // Special handling for vehicles
                var vehicle = unit.getVehicleKit();

                if (vehicle != null) {
                    for (var seat : vehicle.seats.entrySet()) {
                        var passenger = Global.getObjAccessor().GetPlayer(unit, seat.getValue().Passenger.Guid);

                        if (passenger != null) {
                            passenger.GroupEventHappens(e.action.quest.questId, getBaseObject());
                        }
                    }
                }

                break;
            }
            case CombatStop: {
                if (!me) {
                    break;
                }

                me.combatStop(true);
                Log.outDebug(LogFilter.ScriptsAi, "SmartScript.ProcessAction: SMART_ACTION_COMBAT_STOP: {0} CombatStop", me.getGUID().toString());

                break;
            }
            case RemoveAurasFromSpell: {
                for (var target : targets) {
                    if (!isUnit(target)) {
                        continue;
                    }

                    if (e.action.removeAura.spell != 0) {
                        ObjectGuid casterGUID = null;

                        if (e.action.removeAura.onlyOwnedAuras != 0) {
                            if (me == null) {
                                break;
                            }

                            casterGUID = me.getGUID().clone();
                        }

                        if (e.action.removeAura.charges != 0) {
                            var aur = target.getAsUnit().getAura(e.action.removeAura.spell, casterGUID.clone());

                            if (aur != null) {
                                aur.modCharges(-(int)e.action.removeAura.charges, AuraRemoveMode.Expire);
                            }
                        }

                        target.getAsUnit().removeAura(e.action.removeAura.spell);
                    } else {
                        target.getAsUnit().removeAllAuras();
                    }

                    Log.outDebug(LogFilter.ScriptsAi, "SmartScript.ProcessAction: SMART_ACTION_REMOVEAURASFROMSPELL: Unit {0}, spell {1}", target.getGUID().toString(), e.action.removeAura.spell);
                }

                break;
            }
            case Follow: {
                if (!isSmart()) {
                    break;
                }

                if (targets.Empty()) {
                    ((SmartAI)me.getAI()).stopFollow(false);

                    break;
                }

                for (var target : targets) {
                    if (isUnit(target)) {
                        var angle = e.action.follow.angle > 6 ? (e.action.follow.angle * (float)Math.PI / 180.0f) : e.action.follow.angle;
                        ((SmartAI)me.getAI()).setFollow(target.getAsUnit(), e.action.follow.dist + 0.1f, angle, e.action.follow.credit, e.action.follow.entry, e.action.follow.creditType);

                        Log.outDebug(LogFilter.ScriptsAi, "SmartScript.ProcessAction: SMART_ACTION_FOLLOW: Creature {0} following target {1}", me.getGUID().toString(), target.getGUID().toString());

                        break;
                    }
                }

                break;
            }
            case RandomPhase: {
                if (getBaseObject() == null) {
                    break;
                }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: List<uint> phases = new();
                ArrayList<Integer> phases = new ArrayList<Integer>();
                var randomPhase = e.action.randomPhase.clone();

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: foreach (var id in new[] { randomPhase.phase1, randomPhase.phase2, randomPhase.phase3, randomPhase.phase4, randomPhase.phase5, randomPhase.phase6 })
                for (var id : new int[] {randomPhase.phase1, randomPhase.phase2, randomPhase.phase3, randomPhase.phase4, randomPhase.phase5, randomPhase.phase6}) {
                    if (id != 0) {
                        phases.add(id);
                    }
                }

                var phase = phases.SelectRandom();
                setPhase(phase);

                Log.outDebug(LogFilter.ScriptsAi, "SmartScript.ProcessAction: SMART_ACTION_RANDOM_PHASE: Creature {0} sets event phase to {1}", getBaseObject().getGUID().toString(), phase);

                break;
            }
            case RandomPhaseRange: {
                if (getBaseObject() == null) {
                    break;
                }

                var phase = RandomHelper.URand(e.action.randomPhaseRange.phaseMin, e.action.randomPhaseRange.phaseMax);
                setPhase(phase);

                Log.outDebug(LogFilter.ScriptsAi, "SmartScript.ProcessAction: SMART_ACTION_RANDOM_PHASE_RANGE: Creature {0} sets event phase to {1}", getBaseObject().getGUID().toString(), phase);

                break;
            }
            case CallKilledmonster: {
                if (e.target.type == SmartTargets.None || e.target.type == SmartTargets.Self) { // Loot recipient and his group members
                    if (me == null) {
                        break;
                    }

                    for (var tapperGuid : me.getTapList()) {
                        var tapper = Global.getObjAccessor().GetPlayer(me, tapperGuid);

                        if (tapper != null) {
                            tapper.KilledMonsterCredit(e.action.killedMonster.creature, me.getGUID().clone());
                            Log.outDebug(LogFilter.ScriptsAi, String.format("SmartScript::ProcessAction: SMART_ACTION_CALL_KILLEDMONSTER: Player %1$s, Killcredit: %2$s", tapper.GUID, e.action.killedMonster.creature));
                        }
                    }
                }
                else { // Specific target type
                    for (var target : targets) {
                        if (isPlayer(target)) {
                            target.getAsPlayer().killedMonsterCredit(e.action.killedMonster.creature);

                            Log.outDebug(LogFilter.ScriptsAi, "SmartScript.ProcessAction: SMART_ACTION_CALL_KILLEDMONSTER: Player {0}, Killcredit: {1}", target.getGUID().toString(), e.action.killedMonster.creature);
                        } else if (isUnit(target)) { // Special handling for vehicles
                            var vehicle = target.getAsUnit().getVehicleKit();

                            if (vehicle != null) {
                                for (var seat : vehicle.seats.entrySet()) {
                                    var player = Global.getObjAccessor().GetPlayer(target, seat.getValue().Passenger.Guid);

                                    if (player != null) {
                                        player.KilledMonsterCredit(e.action.killedMonster.creature);
                                    }
                                }
                            }
                        }
                    }
                }

                break;
            }
            case SetInstData: {
                var obj = getBaseObject();

                if (obj == null) {
                    obj = unit;
                }

                if (obj == null) {
                    break;
                }

                var instance = obj.getInstanceScript();

                if (instance == null) {
                    Log.outError(LogFilter.Sql, "SmartScript: Event {0} attempt to set instance data without instance script. EntryOrGuid {1}", e.getEventType(), e.entryOrGuid);

                    break;
                }

                switch (e.action.setInstanceData.type) {
                    case 0:
                        instance.setData(e.action.setInstanceData.field, e.action.setInstanceData.data);

                        Log.outDebug(LogFilter.ScriptsAi, "SmartScript.ProcessAction: SMART_ACTION_SET_INST_DATA: SetData Field: {0}, data: {1}", e.action.setInstanceData.field, e.action.setInstanceData.data);

                        break;
                    case 1:
                        instance.setBossState(e.action.setInstanceData.field, EncounterState.forValue(e.action.setInstanceData.data));

                        Log.outDebug(LogFilter.ScriptsAi, "SmartScript.ProcessAction: SMART_ACTION_SET_INST_DATA: SetBossState BossId: {0}, State: {1} ({2})", e.action.setInstanceData.field, e.action.setInstanceData.data, EncounterState.forValue(e.action.setInstanceData.data));

                        break;
                    default: // Static analysis
                        break;
                }

                break;
            }
            case SetInstData64: {
                var obj = getBaseObject();

                if (obj == null) {
                    obj = unit;
                }

                if (obj == null) {
                    break;
                }

                var instance = obj.getInstanceScript();

                if (instance == null) {
                    Log.outError(LogFilter.Sql, "SmartScript: Event {0} attempt to set instance data without instance script. EntryOrGuid {1}", e.getEventType(), e.entryOrGuid);

                    break;
                }

                if (targets.Empty()) {
                    break;
                }

                instance.setGuidData(e.action.setInstanceData64.field, targets.get(0).GUID);

                Log.outDebug(LogFilter.ScriptsAi, "SmartScript.ProcessAction: SMART_ACTION_SET_INST_DATA64: Field: {0}, data: {1}", e.action.setInstanceData64.field, targets.get(0).GUID);

                break;
            }
            case UpdateTemplate: {
                for (var target : targets) {
                    if (isCreature(target)) {
                        target.getAsCreature().updateEntry(e.action.updateTemplate.creature, null, e.action.updateTemplate.updateLevel != 0);
                    }
                }

                break;
            }
            case Die: {
                if (me != null && !me.isDead()) {
                    me.killSelf();
                    Log.outDebug(LogFilter.ScriptsAi, "SmartScript.ProcessAction: SMART_ACTION_DIE: Creature {0}", me.getGUID().toString());
                }

                break;
            }
            case SetInCombatWithZone: {
                if (me != null && me.isAIEnabled()) {
                    me.getAI().doZoneInCombat();
                    Log.outDebug(LogFilter.ScriptsAi, String.format("SmartScript.ProcessAction: SMART_ACTION_SET_IN_COMBAT_WITH_ZONE: Creature: %1$s", me.getGUID().clone()));
                }

                break;
            }
            case CallForHelp: {
                if (me != null) {
                    me.callForHelp(e.action.callHelp.range);

                    if (e.action.callHelp.withEmote != 0) {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: var builder = new BroadcastTextBuilder(_me, ChatMsg.Emote, (uint)BroadcastTextIds.CallForHelp, _me.Gender);
                        var builder = new BroadcastTextBuilder(me, ChatMsg.Emote, (int)BroadcastTextIds.CallForHelp.getValue(), me.getGender());
                        Global.getCreatureTextMgr().sendChatPacket(me, builder, ChatMsg.MonsterEmote);
                    }

                    Log.outDebug(LogFilter.ScriptsAi, String.format("SmartScript.ProcessAction: SMART_ACTION_CALL_FOR_HELP: Creature: %1$s", me.getGUID().clone()));
                }

                break;
            }
            case SetSheath: {
                if (me != null) {
                    me.setSheath(SheathState.forValue(e.action.setSheath.sheath));

                    Log.outDebug(LogFilter.ScriptsAi, "SmartScript.ProcessAction: SMART_ACTION_SET_SHEATH: Creature {0}, State: {1}", me.getGUID().toString(), e.action.setSheath.sheath);
                }

                break;
            }
            case ForceDespawn: {
                // there should be at least a world update tick before despawn, to avoid breaking linked actions
                var despawnDelay = TimeSpan.FromMilliseconds(e.action.forceDespawn.delay);

                if (despawnDelay <= TimeSpan.Zero) {
                    despawnDelay = TimeSpan.FromMilliseconds(1);
                }

                var forceRespawnTimer = TimeSpan.FromSeconds(e.action.forceDespawn.forceRespawnTimer);

                for (var target : targets) {
                    var creature = target.getAsCreature();

                    if (creature != null) {
                        creature.despawnOrUnsummon(despawnDelay, forceRespawnTimer);
                    } else {
                        var go = target.getAsGameObject();

                        if (go != null) {
                            go.despawnOrUnsummon(despawnDelay, forceRespawnTimer);
                        }
                    }
                }

                break;
            }
            case SetIngamePhaseId: {
                for (var target : targets) {
                    if (e.action.ingamePhaseId.apply == 1) {
                        PhasingHandler.addPhase(target, e.action.ingamePhaseId.id, true);
                    } else {
                        PhasingHandler.removePhase(target, e.action.ingamePhaseId.id, true);
                    }
                }

                break;
            }
            case SetIngamePhaseGroup: {
                for (var target : targets) {
                    if (e.action.ingamePhaseGroup.apply == 1) {
                        PhasingHandler.addPhaseGroup(target, e.action.ingamePhaseGroup.groupId, true);
                    } else {
                        PhasingHandler.removePhaseGroup(target, e.action.ingamePhaseGroup.groupId, true);
                    }
                }

                break;
            }
            case MountToEntryOrModel: {
                for (var target : targets) {
                    if (!isUnit(target)) {
                        continue;
                    }

                    if (e.action.morphOrMount.creature != 0 || e.action.morphOrMount.model != 0) {
                        if (e.action.morphOrMount.creature > 0) {
                            var cInfo = Global.getObjectMgr().getCreatureTemplate(e.action.morphOrMount.creature);

                            if (cInfo != null) {
                                target.getAsUnit().mount(ObjectManager.chooseDisplayId(cInfo).creatureDisplayId);
                            }
                        } else {
                            target.getAsUnit().mount(e.action.morphOrMount.model);
                        }
                    } else {
                        target.getAsUnit().dismount();
                    }
                }

                break;
            }
            case SetInvincibilityHpLevel: {
                for (var target : targets) {
                    if (isCreature(target)) {
                        var ai = (SmartAI)me.getAI();

                        if (ai == null) {
                            continue;
                        }

                        if (e.action.invincHP.percent != 0) {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: ai.SetInvincibilityHpLevel((uint)target.AsCreature.CountPctFromMaxHealth((int)e.Action.invincHP.percent));
                            ai.setInvincibilityHpLevel((int)target.getAsCreature().countPctFromMaxHealth((int)e.action.invincHP.percent));
                        } else {
                            ai.setInvincibilityHpLevel(e.action.invincHP.minHP);
                        }
                    }
                }

                break;
            }
            case SetData: {
                for (var target : targets) {
                    var cTarget = target.getAsCreature();

                    if (cTarget != null) {
                        var ai = cTarget.getAI();

                        if (isSmart(cTarget, true)) {
                            ((SmartAI)ai).setData(e.action.setData.field, e.action.setData.data, me);
                        } else {
                            ai.setData(e.action.setData.field, e.action.setData.data);
                        }
                    } else {
                        var oTarget = target.getAsGameObject();

                        if (oTarget != null) {
                            var ai = oTarget.getAI();

                            if (isSmart(oTarget, true)) {
                                ((SmartGameObjectAI)ai).setData(e.action.setData.field, e.action.setData.data, me);
                            } else {
                                ai.setData(e.action.setData.field, e.action.setData.data);
                            }
                        }
                    }
                }

                break;
            }
            case AttackStop: {
                for (var target : targets) {
                    var unitTarget = target.getAsUnit();

                    if (unitTarget != null) {
                        unitTarget.attackStop();
                    }
                }

                break;
            }
            case MoveOffset: {
                for (var target : targets) {
                    if (!isCreature(target)) {
                        continue;
                    }

                    if (!e.event.eventFlags.HasAnyFlag(SmartEventFlags.WhileCharmed) && isCharmedCreature(target)) {
                        continue;
                    }

                    Position pos = target.location;

                    // Use forward/backward/left/right cartesian plane movement
                    var o = pos.getOrientation();
                    var x = (float)(pos.x + (Math.cos(o - (Math.PI / 2)) * e.target.x) + (Math.cos(o) * e.target.y));
                    var y = (float)(pos.y + (Math.sin(o - (Math.PI / 2)) * e.target.x) + (Math.sin(o) * e.target.y));
                    var z = pos.z + e.target.z;
                    target.getAsCreature().getMotionMaster().movePoint(e.action.moveOffset.pointId, x, y, z);
                }

                break;
            }
            case SetVisibility: {
                for (var target : targets) {
                    if (isUnit(target)) {
                        target.getAsUnit().setVisible(e.action.visibility.state != 0);
                    }
                }

                break;
            }
            case SetActive: {
                for (var target : targets) {
                    target.setActive(e.action.active.state != 0);
                }

                break;
            }
            case AttackStart: {
                if (me == null) {
                    break;
                }

                if (targets.Empty()) {
                    break;
                }

                var target = targets.SelectRandom().AsUnit;

                if (target != null) {
                    me.getAI().attackStart(target);
                }

                break;
            }
            case SummonCreature: {
                var flags = SmartActionSummonCreatureFlags.forValue(e.action.summonCreature.flags);
                var preferUnit = flags.HasAnyFlag(SmartActionSummonCreatureFlags.PreferUnit);
                var summoner = preferUnit ? unit : getBaseObjectOrUnitInvoker(unit);

                if (summoner == null) {
                    break;
                }

                var privateObjectOwner = ObjectGuid.empty;

                if (flags.HasAnyFlag(SmartActionSummonCreatureFlags.PersonalSpawn)) {
                    privateObjectOwner = summoner.isPrivateObject() ? summoner.getPrivateObjectOwner() : summoner.getGUID();
                }

                var spawnsCount = Math.max(e.action.summonCreature.count, 1);

                for (var target : targets) {
                    var pos = target.location.copy();
                    pos.x += e.target.x;
                    pos.y += e.target.y;
                    pos.z += e.target.z;
                    pos.setOrientation(pos.getOrientation() + e.target.o);

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: for (uint counter = 0; counter < spawnsCount; counter++)
                    for (int counter = 0; counter < spawnsCount; counter++) {
                        Creature summon = summoner.SummonCreature(e.action.summonCreature.creature, pos, TempSummonType.forValue(e.action.summonCreature.type), TimeSpan.FromMilliseconds(e.action.summonCreature.duration), 0, 0, privateObjectOwner.clone());

                        if (summon != null) {
                            if (e.action.summonCreature.attackInvoker != 0) {
                                summon.getAI().attackStart(target.getAsUnit());
                            }
                        }
                    }
                }

                if (e.getTargetType() != SmartTargets.Position) {
                    break;
                }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: for (uint counter = 0; counter < spawnsCount; counter++)
                for (int counter = 0; counter < spawnsCount; counter++) {
                    Creature summon = summoner.SummonCreature(e.action.summonCreature.creature, new Position(e.target.x, e.target.y, e.target.z, e.target.o), TempSummonType.forValue(e.action.summonCreature.type), TimeSpan.FromMilliseconds(e.action.summonCreature.duration), 0, 0, privateObjectOwner.clone());

                    if (summon != null) {
                        if (unit != null && e.action.summonCreature.attackInvoker != 0) {
                            summon.getAI().attackStart(unit);
                        }
                    }
                }

                break;
            }
            case SummonGo: {
                var summoner = getBaseObjectOrUnitInvoker(unit);

                if (!summoner) {
                    break;
                }

                for (var target : targets) {
                    var pos = target.location.getPositionWithOffset(new Position(e.target.x, e.target.y, e.target.z, e.target.o));
                    var rot = Quaternion.CreateFromRotationMatrix(Extensions.fromEulerAnglesZYX(pos.getOrientation(), 0f, 0f));
                    summoner.summonGameObject(e.action.summonGO.entry, pos, rot, TimeSpan.FromSeconds(e.action.summonGO.despawnTime), GameObjectSummonType.forValue(e.action.summonGO.summonType));
                }

                if (e.getTargetType() != SmartTargets.Position) {
                    break;
                }

                var rot = Quaternion.CreateFromRotationMatrix(Extensions.fromEulerAnglesZYX(e.target.o, 0f, 0f));
                summoner.summonGameObject(e.action.summonGO.entry, new Position(e.target.x, e.target.y, e.target.z, e.target.o), rot, TimeSpan.FromSeconds(e.action.summonGO.despawnTime), GameObjectSummonType.forValue(e.action.summonGO.summonType));

                break;
            }
            case KillUnit: {
                for (var target : targets) {
                    if (!isUnit(target)) {
                        continue;
                    }

                    target.getAsUnit().killSelf();
                }

                break;
            }
            case AddItem: {
                for (var target : targets) {
                    if (!isPlayer(target)) {
                        continue;
                    }

                    target.getAsPlayer().addItem(e.action.item.entry, e.action.item.count);
                }

                break;
            }
            case RemoveItem: {
                for (var target : targets) {
                    if (!isPlayer(target)) {
                        continue;
                    }

                    target.getAsPlayer().destroyItemCount(e.action.item.entry, e.action.item.count, true);
                }

                break;
            }
            case StoreTargetList: {
                storeTargetList(targets, e.action.storeTargets.id);

                break;
            }
            case Teleport: {
                for (var target : targets) {
                    if (isPlayer(target)) {
                        target.getAsPlayer().teleportTo(e.action.teleport.mapID, e.target.x, e.target.y, e.target.z, e.target.o);
                    } else if (isCreature(target)) {
                        target.getAsCreature().nearTeleportTo(e.target.x, e.target.y, e.target.z, e.target.o);
                    }
                }

                break;
            }
            case SetDisableGravity: {
                if (!isSmart()) {
                    break;
                }

                ((SmartAI)me.getAI()).setDisableGravity(e.action.setDisableGravity.disable != 0);

                break;
            }
            case SetRun: {
                if (!isSmart()) {
                    break;
                }

                ((SmartAI)me.getAI()).setRun(e.action.setRun.run != 0);

                break;
            }
            case SetCounter: {
                if (!targets.Empty()) {
                    for (var target : targets) {
                        if (isCreature(target)) {
                            var ai = (SmartAI)target.getAsCreature().getAI();

                            if (ai != null) {
                                ai.getScript().storeCounter(e.action.setCounter.counterId, e.action.setCounter.value, e.action.setCounter.reset);
                            } else {
                                Log.outError(LogFilter.Sql, "SmartScript: Action target for SMART_ACTION_SET_COUNTER is not using SmartAI, skipping");
                            }
                        } else if (isGameObject(target)) {
                            var ai = (SmartGameObjectAI)target.getAsGameObject().getAI();

                            if (ai != null) {
                                ai.getScript().storeCounter(e.action.setCounter.counterId, e.action.setCounter.value, e.action.setCounter.reset);
                            } else {
                                Log.outError(LogFilter.Sql, "SmartScript: Action target for SMART_ACTION_SET_COUNTER is not using SmartGameObjectAI, skipping");
                            }
                        }
                    }
                } else {
                    storeCounter(e.action.setCounter.counterId, e.action.setCounter.value, e.action.setCounter.reset);
                }

                break;
            }
            case WpStart: {
                if (!isSmart()) {
                    break;
                }

                var run = e.action.wpStart.run != 0;
                var entry = e.action.wpStart.pathID;
                var repeat = e.action.wpStart.repeat != 0;

                for (var target : targets) {
                    if (isPlayer(target)) {
                        storeTargetList(targets, SharedConst.SmartEscortTargets);

                        break;
                    }
                }

                me.<SmartAI>getAI().StartPath(run, entry, repeat, unit);

                var quest = e.action.wpStart.quest;
                var despawnTime = e.action.wpStart.despawnTime;
                me.<SmartAI>getAI().EscortQuestID = quest;
                me.<SmartAI>getAI().SetDespawnTime(despawnTime);

                break;
            }
            case WpPause: {
                if (!isSmart()) {
                    break;
                }

                var delay = e.action.wpPause.delay;
                ((SmartAI)me.getAI()).pausePath(delay, true);

                break;
            }
            case WpStop: {
                if (!isSmart()) {
                    break;
                }

                var despawnTime = e.action.wpStop.despawnTime;
                var quest = e.action.wpStop.quest;
                var fail = e.action.wpStop.fail != 0;
                ((SmartAI)me.getAI()).stopPath(despawnTime, quest, fail);

                break;
            }
            case WpResume: {
                if (!isSmart()) {
                    break;
                }

                // Set the timer to 1 ms so the path will be resumed on next update loop
                if (me.<SmartAI>getAI().CanResumePath()) {
                    me.<SmartAI>getAI().SetWPPauseTimer(1);
                }

                break;
            }
            case SetOrientation: {
                if (me == null) {
                    break;
                }

                if (e.getTargetType() == SmartTargets.Self) {
                    me.setFacingTo((me.getTransport() != null ? me.getTransportHomePosition() : me.getHomePosition()).getOrientation());
                } else if (e.getTargetType() == SmartTargets.Position) {
                    me.setFacingTo(e.target.o);
                } else if (!targets.Empty()) {
                    me.setFacingToObject(targets.get(0));
                }

                break;
            }
            case Playmovie: {
                for (var target : targets) {
                    if (!isPlayer(target)) {
                        continue;
                    }

                    target.getAsPlayer().sendMovieStart(e.action.movie.entry);
                }

                break;
            }
            case MoveToPos: {
                if (!isSmart()) {
                    break;
                }

                WorldObject target = null;

                /*if (e.GetTargetType() == SmartTargets.CreatureRange || e.GetTargetType() == SmartTargets.CreatureGuid ||
                	e.GetTargetType() == SmartTargets.CreatureDistance || e.GetTargetType() == SmartTargets.GameobjectRange ||
                	e.GetTargetType() == SmartTargets.GameobjectGuid || e.GetTargetType() == SmartTargets.GameobjectDistance ||
                	e.GetTargetType() == SmartTargets.ClosestCreature || e.GetTargetType() == SmartTargets.ClosestGameobject ||
                	e.GetTargetType() == SmartTargets.OwnerOrSummoner || e.GetTargetType() == SmartTargets.ActionInvoker ||
                	e.GetTargetType() == SmartTargets.ClosestEnemy || e.GetTargetType() == SmartTargets.ClosestFriendly)*/ {
                    // we want to move to random element
                    if (!targets.Empty()) {
                        target = targets.SelectRandom();
                    }
                	}

                if (target == null) {
                    Position dest = new Position(e.target.x, e.target.y, e.target.z);

                    if (e.action.moveToPos.transport != 0) {
                        var trans = me.getDirectTransport();

                        if (trans != null) {
                            trans.calculatePassengerPosition(dest);
                        }
                    }

                    me.getMotionMaster().movePoint(e.action.moveToPos.pointId, dest, e.action.moveToPos.disablePathfinding == 0);
                } else {
                    var pos = target.location.copy();

                    if (e.action.moveToPos.contactDistance > 0) {
                        target.getContactPoint(me, pos, e.action.moveToPos.contactDistance);
                    }

                    me.getMotionMaster().movePoint(e.action.moveToPos.pointId, pos.x + e.target.x, pos.y + e.target.y, pos.z + e.target.z, e.action.moveToPos.disablePathfinding == 0);
                }

                break;
            }
            case EnableTempGobj: {
                for (var target : targets) {
                    if (isCreature(target)) {
                        Log.outWarn(LogFilter.Sql, String.format("Invalid creature target '%1$s' (entry %2$s, spawnId %3$s) specified for SMART_ACTION_ENABLE_TEMP_GOBJ", target.getName(), target.getEntry(), target.getAsCreature().spawnId));
                    } else if (isGameObject(target)) {
                        if (target.getAsGameObject().isSpawnedByDefault()) {
                            Log.outWarn(LogFilter.Sql, String.format("Invalid gameobject target '%1$s' (entry %2$s, spawnId %3$s) for SMART_ACTION_ENABLE_TEMP_GOBJ - the object is spawned by default", target.getName(), target.getEntry(), target.getAsGameObject().getSpawnId()));
                        } else {
                            target.getAsGameObject().setRespawnTime((int)e.action.enableTempGO.duration);
                        }
                    }
                }

                break;
            }
            case CloseGossip: {
                for (var target : targets) {
                    if (isPlayer(target)) {
                        target.getAsPlayer().playerTalkClass.sendCloseGossip();
                    }
                }

                break;
            }
            case Equip: {
                for (var target : targets) {
                    var npc = target.getAsCreature();

                    if (npc != null) {
                        var slot = new EquipmentItem[SharedConst.MaxEquipmentItems];
                        var equipId = (byte)e.action.equip.entry;

                        if (equipId != 0) {
                            var eInfo = Global.getObjectMgr().getEquipmentInfo(npc.getEntry(), equipId);

                            if (eInfo == null) {
                                Log.outError(LogFilter.Sql, "SmartScript: SMART_ACTION_EQUIP uses non-existent equipment info id {0} for creature {1}", equipId, npc.getEntry());

                                break;
                            }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: npc.CurrentEquipmentId = (byte)equipId;
                            npc.currentEquipmentId = (byte)equipId;
                            system.arraycopy(eInfo.items, 0, slot, 0, SharedConst.MaxEquipmentItems);
                        } else {
                            slot[0].itemId = e.action.equip.slot1;
                            slot[1].itemId = e.action.equip.slot2;
                            slot[2].itemId = e.action.equip.slot3;
                        }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: for (uint i = 0; i < SharedConst.MaxEquipmentItems; ++i)
                        for (int i = 0; i < SharedConst.MaxEquipmentItems; ++i) {
                            if (e.action.equip.mask == 0 || (e.action.equip.mask & (1 << (int)i)) != 0) {
                                npc.setVirtualItem(i, slot[i].itemId, slot[i].appearanceModId, slot[i].itemVisual);
                            }
                        }
                    }
                }

                break;
            }
            case CreateTimedEvent: {
                SmartEvent ne = new SmartEvent();
                ne.type = SmartEvents.Update;
                ne.eventChance = e.action.timeEvent.chance;

                if (ne.eventChance == 0) {
                    ne.eventChance = 100;
                }

                ne.minMaxRepeat.min = e.action.timeEvent.min;
                ne.minMaxRepeat.max = e.action.timeEvent.max;
                ne.minMaxRepeat.repeatMin = e.action.timeEvent.repeatMin;
                ne.minMaxRepeat.repeatMax = e.action.timeEvent.repeatMax;

                ne.eventFlags = SmartEventFlags.forValue(0);

                if (ne.minMaxRepeat.repeatMin == 0 && ne.minMaxRepeat.repeatMax == 0) {
                    ne.eventFlags = SmartEventFlags.forValue(ne.eventFlags.getValue() | SmartEventFlags.NotRepeatable.getValue());
                }

                SmartAction ac = new SmartAction();
                ac.type = SmartActions.TriggerTimedEvent;
                ac.timeEvent.id = e.action.timeEvent.id;

                SmartScriptHolder ev = new SmartScriptHolder();
                ev.event = ne.clone();
                ev.eventId = e.action.timeEvent.id;
                ev.target = e.target.clone();
                ev.action = ac.clone();
                initTimer(ev);
                storedEvents.add(ev);

                break;
            }
            case TriggerTimedEvent:
                processEventsFor(SmartEvents.TimedEventTriggered, null, e.action.timeEvent.id);

                // remove this event if not repeatable
                if (e.event.eventFlags.HasAnyFlag(SmartEventFlags.NotRepeatable)) {
                    remIDs.add(e.action.timeEvent.id);
                }

                break;
            case RemoveTimedEvent:
                remIDs.add(e.action.timeEvent.id);

                break;
            case CallScriptReset:
                setPhase(0);
                onReset();

                break;
            case SetRangedMovement: {
                if (!isSmart()) {
                    break;
                }

                float attackDistance = e.action.setRangedMovement.distance;
                var attackAngle = e.action.setRangedMovement.angle / 180.0f * MathFunctions.PI;

                for (var target : targets) {
                    var creature = target.getAsCreature();

                    if (creature != null) {
                        if (isSmart(creature) && creature.getVictim() != null) {
                            if (((SmartAI)creature.getAI()).canCombatMove()) {
                                creature.getMotionMaster().moveChase(creature.getVictim(), attackDistance, attackAngle);
                            }
                        }
                    }
                }

                break;
            }
            case CallTimedActionlist: {
                if (e.getTargetType() == SmartTargets.None) {
                    Log.outError(LogFilter.Sql, "SmartScript: Entry {0} SourceType {1} Event {2} Action {3} is using TARGET_NONE(0) for Script9 target. Please correct target_type in database.", e.entryOrGuid, e.getScriptType(), e.getEventType(), e.getActionType());

                    break;
                }

                for (var target : targets) {
                    var creature = target.getAsCreature();

                    if (creature != null) {
                        if (isSmart(creature)) {
                            creature.<SmartAI>getAI().SetTimedActionList(e, e.action.timedActionList.id, getLastInvoker());
                        }
                    } else {
                        var go = target.getAsGameObject();

                        if (go != null) {
                            if (isSmart(go)) {
                                go.<SmartGameObjectAI>getAI().SetTimedActionList(e, e.action.timedActionList.id, getLastInvoker());
                            }
                        } else {
                            var areaTriggerTarget = target.getAsAreaTrigger();

                            if (areaTriggerTarget != null) {
                                areaTriggerTarget.<IAreaTriggerSmartScript>forEachAreaTriggerScript(a -> a.SetTimedActionList(e, e.action.timedActionList.id, getLastInvoker()));
                            }
                        }
                    }
                }

                break;
            }
            case SetNpcFlag: {
                for (var target : targets) {
                    if (isUnit(target)) {
                        target.getAsUnit().replaceAllNpcFlags(NPCFlags.forValue(e.action.flag.flag));
                    }
                }

                break;
            }
            case AddNpcFlag: {
                for (var target : targets) {
                    if (isUnit(target)) {
                        target.getAsUnit().setNpcFlag(NPCFlags.forValue(e.action.flag.flag));
                    }
                }

                break;
            }
            case RemoveNpcFlag: {
                for (var target : targets) {
                    if (isUnit(target)) {
                        target.getAsUnit().removeNpcFlag(NPCFlags.forValue(e.action.flag.flag));
                    }
                }

                break;
            }
            case CrossCast: {
                if (targets.Empty()) {
                    break;
                }

                var casters = getTargets(createSmartEvent(SmartEvents.UpdateIc, 0, 0, 0, 0, 0, 0, SmartActions.None, 0, 0, 0, 0, 0, 0, 0, SmartTargets.forValue(e.action.crossCast.targetType), e.action.crossCast.targetParam1, e.action.crossCast.targetParam2, e.action.crossCast.targetParam3, 0, 0), unit);

                for (var caster : casters) {
                    if (!isUnit(caster)) {
                        continue;
                    }

                    var casterUnit = caster.getAsUnit();
                    var interruptedSpell = false;

                    for (var target : targets) {
                        if (!isUnit(target)) {
                            continue;
                        }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: if (!(e.Action.crossCast.castFlags.HasAnyFlag((uint)SmartCastFlags.AuraNotPresent)) || !target.AsUnit.HasAura(e.Action.crossCast.spell))
                        if (!(e.action.crossCast.castFlags.HasAnyFlag((int)SmartCastFlags.AuraNotPresent.getValue())) || !target.getAsUnit().hasAura(e.action.crossCast.spell)) {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: if (!interruptedSpell && e.Action.crossCast.castFlags.HasAnyFlag((uint)SmartCastFlags.InterruptPrevious))
                            if (!interruptedSpell && e.action.crossCast.castFlags.HasAnyFlag((int)SmartCastFlags.InterruptPrevious.getValue())) {
                                casterUnit.interruptNonMeleeSpells(false);
                                interruptedSpell = true;
                            }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: casterUnit.CastSpell(target.AsUnit, e.Action.crossCast.spell, e.Action.crossCast.castFlags.HasAnyFlag((uint)SmartCastFlags.Triggered));
                            casterUnit.CastSpell(target.getAsUnit(), e.action.crossCast.spell, e.action.crossCast.castFlags.HasAnyFlag((int)SmartCastFlags.Triggered.getValue()));
                        } else {
                            Log.outDebug(LogFilter.ScriptsAi, "Spell {0} not cast because it has flag SMARTCAST_AURA_NOT_PRESENT and the target ({1}) already has the aura", e.action.crossCast.spell, target.getGUID().toString());
                        }
                    }
                }

                break;
            }
            case CallRandomTimedActionlist: {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: List<uint> actionLists = new();
                ArrayList<Integer> actionLists = new ArrayList<Integer>();
                var randTimedActionList = e.action.randTimedActionList.clone();

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: foreach (var id in new[] { randTimedActionList.actionList1, randTimedActionList.actionList2, randTimedActionList.actionList3, randTimedActionList.actionList4, randTimedActionList.actionList5, randTimedActionList.actionList6 })
                for (var id : new int[] {randTimedActionList.actionList1, randTimedActionList.actionList2, randTimedActionList.actionList3, randTimedActionList.actionList4, randTimedActionList.actionList5, randTimedActionList.actionList6}) {
                    if (id != 0) {
                        actionLists.add(id);
                    }
                }

                if (e.getTargetType() == SmartTargets.None) {
                    Log.outError(LogFilter.Sql, "SmartScript: Entry {0} SourceType {1} Event {2} Action {3} is using TARGET_NONE(0) for Script9 target. Please correct target_type in database.", e.entryOrGuid, e.getScriptType(), e.getEventType(), e.getActionType());

                    break;
                }

                var randomId = actionLists.SelectRandom();

                for (var target : targets) {
                    var creature = target.getAsCreature();

                    if (creature != null) {
                        if (isSmart(creature)) {
                            creature.<SmartAI>getAI().SetTimedActionList(e, randomId, getLastInvoker());
                        }
                    } else {
                        var go = target.getAsGameObject();

                        if (go != null) {
                            if (isSmart(go)) {
                                go.<SmartGameObjectAI>getAI().SetTimedActionList(e, randomId, getLastInvoker());
                            }
                        } else {
                            var areaTriggerTarget = target.getAsAreaTrigger();

                            if (areaTriggerTarget != null) {
                                areaTriggerTarget.<IAreaTriggerSmartScript>forEachAreaTriggerScript(a -> a.SetTimedActionList(e, randomId, getLastInvoker()));
                            }
                        }
                    }
                }

                break;
            }
            case CallRandomRangeTimedActionlist: {
                var id = RandomHelper.URand(e.action.randRangeTimedActionList.idMin, e.action.randRangeTimedActionList.idMax);

                if (e.getTargetType() == SmartTargets.None) {
                    Log.outError(LogFilter.Sql, "SmartScript: Entry {0} SourceType {1} Event {2} Action {3} is using TARGET_NONE(0) for Script9 target. Please correct target_type in database.", e.entryOrGuid, e.getScriptType(), e.getEventType(), e.getActionType());

                    break;
                }

                for (var target : targets) {
                    var creature = target.getAsCreature();

                    if (creature != null) {
                        if (isSmart(creature)) {
                            creature.<SmartAI>getAI().SetTimedActionList(e, id, getLastInvoker());
                        }
                    } else {
                        var go = target.getAsGameObject();

                        if (go != null) {
                            if (isSmart(go)) {
                                go.<SmartGameObjectAI>getAI().SetTimedActionList(e, id, getLastInvoker());
                            }
                        } else {
                            var areaTriggerTarget = target.getAsAreaTrigger();

                            if (areaTriggerTarget != null) {
                                areaTriggerTarget.<IAreaTriggerSmartScript>forEachAreaTriggerScript(a -> a.SetTimedActionList(e, id, getLastInvoker()));
                            }
                        }
                    }
                }

                break;
            }
            case ActivateTaxi: {
                for (var target : targets) {
                    if (isPlayer(target)) {
                        target.getAsPlayer().activateTaxiPathTo(e.action.taxi.id);
                    }
                }

                break;
            }
            case RandomMove: {
                var foundTarget = false;

                for (var obj : targets) {
                    if (isCreature(obj)) {
                        if (e.action.moveRandom.distance != 0) {
                            obj.getAsCreature().getMotionMaster().moveRandom(e.action.moveRandom.distance);
                        } else {
                            obj.getAsCreature().getMotionMaster().moveIdle();
                        }
                    }
                }

                if (!foundTarget && me != null && isCreature(me)) {
                    if (e.action.moveRandom.distance != 0) {
                        me.getMotionMaster().moveRandom(e.action.moveRandom.distance);
                    } else {
                        me.getMotionMaster().moveIdle();
                    }
                }

                break;
            }
            case SetUnitFieldBytes1: {
                for (var target : targets) {
                    if (isUnit(target)) {
                        switch (e.action.setunitByte.type) {
                            case 0:
                                target.getAsUnit().setStandState(UnitStandStateType.forValue((byte)e.action.setunitByte.byte1));

                                break;
                            case 1:
                                // pet talent points
                                break;
                            case 2:
                                target.getAsUnit().setVisFlag(UnitVisFlags.forValue(e.action.setunitByte.byte1));

                                break;
                            case 3:
                                target.getAsUnit().setAnimTier(AnimTier.forValue(e.action.setunitByte.byte1));

                                break;
                        }
                    }
                }

                break;
            }
            case RemoveUnitFieldBytes1: {
                for (var target : targets) {
                    if (isUnit(target)) {
                        switch (e.action.setunitByte.type) {
                            case 0:
                                target.getAsUnit().setStandState(UnitStandStateType.Stand);

                                break;
                            case 1:
                                // pet talent points
                                break;
                            case 2:
                                target.getAsUnit().removeVisFlag(UnitVisFlags.forValue(e.action.setunitByte.byte1));

                                break;
                            case 3:
                                target.getAsUnit().setAnimTier(AnimTier.Ground);

                                break;
                        }
                    }
                }

                break;
            }
            case InterruptSpell: {
                for (var target : targets) {
                    if (isUnit(target)) {
                        target.getAsUnit().interruptNonMeleeSpells(e.action.interruptSpellCasting.withDelayed != 0, e.action.interruptSpellCasting.spellId, e.action.interruptSpellCasting.withInstant != 0);
                    }
                }

                break;
            }
            case AddDynamicFlag: {
                for (var target : targets) {
                    if (isUnit(target)) {
                        target.getAsUnit().setDynamicFlag(UnitDynFlags.forValue(e.action.flag.flag));
                    }
                }

                break;
            }
            case RemoveDynamicFlag: {
                for (var target : targets) {
                    if (isUnit(target)) {
                        target.getAsUnit().removeDynamicFlag(UnitDynFlags.forValue(e.action.flag.flag));
                    }
                }

                break;
            }
            case JumpToPos: {
                WorldObject target = null;

                if (!targets.Empty()) {
                    target = targets.SelectRandom();
                }

                Position pos = new Position(e.target.x, e.target.y, e.target.z);

                if (target) {
                    var tpos = target.location.copy();

                    if (e.action.jump.contactDistance > 0) {
                        target.getContactPoint(me, tpos, e.action.jump.contactDistance);
                    }

                    pos = new Position(tpos.x + e.target.x, tpos.y + e.target.y, tpos.z + e.target.z);
                }

                if (e.action.jump.gravity != 0 || e.action.jump.useDefaultGravity != 0) {
                    var gravity = e.action.jump.useDefaultGravity != 0 ? (float)MotionMaster.GRAVITY : e.action.jump.gravity;
                    me.getMotionMaster().moveJumpWithGravity(pos, e.action.jump.speedXY, gravity, e.action.jump.pointId);
                } else {
                    me.getMotionMaster().moveJump(pos, e.action.jump.speedXY, e.action.jump.speedZ, e.action.jump.pointId);
                }

                break;
            }
            case GoSetLootState: {
                for (var target : targets) {
                    if (isGameObject(target)) {
                        target.getAsGameObject().setLootState(LootState.forValue(e.action.setGoLootState.state));
                    }
                }

                break;
            }
            case GoSetGoState: {
                for (var target : targets) {
                    if (isGameObject(target)) {
                        target.getAsGameObject().setGoState(GameObjectState.forValue(e.action.goState.state));
                    }
                }

                break;
            }
            case SendTargetToTarget: {
                var baseObject = getBaseObject();

                if (baseObject == null) {
                    baseObject = unit;
                }

                if (baseObject == null) {
                    break;
                }

                var storedTargets = getStoredTargetList(e.action.sendTargetToTarget.id, baseObject);

                if (storedTargets == null) {
                    break;
                }

                for (var target : targets) {
                    if (isCreature(target)) {
                        var ai = (SmartAI)target.getAsCreature().getAI();

                        if (ai != null) {
                            ai.getScript().storeTargetList(new ArrayList<WorldObject>(storedTargets), e.action.sendTargetToTarget.id); // store a copy of target list
                        } else {
                            Log.outError(LogFilter.Sql, "SmartScript: Action target for SMART_ACTION_SEND_TARGET_TO_TARGET is not using SmartAI, skipping");
                        }
                    } else if (isGameObject(target)) {
                        var ai = (SmartGameObjectAI)target.getAsGameObject().getAI();

                        if (ai != null) {
                            ai.getScript().storeTargetList(new ArrayList<WorldObject>(storedTargets), e.action.sendTargetToTarget.id); // store a copy of target list
                        } else {
                            Log.outError(LogFilter.Sql, "SmartScript: Action target for SMART_ACTION_SEND_TARGET_TO_TARGET is not using SmartGameObjectAI, skipping");
                        }
                    }
                }

                break;
            }
            case SendGossipMenu: {
                if (getBaseObject() == null || !isSmart()) {
                    break;
                }

                Log.outDebug(LogFilter.ScriptsAi, "SmartScript.ProcessAction. SMART_ACTION_SEND_GOSSIP_MENU: gossipMenuId {0}, gossipNpcTextId {1}", e.action.sendGossipMenu.gossipMenuId, e.action.sendGossipMenu.gossipNpcTextId);

                // override default gossip
                if (me) {
                    ((SmartAI)me.getAI()).setGossipReturn(true);
                } else if (go) {
                    ((SmartGameObjectAI)go.getAI()).setGossipReturn(true);
                }

                for (var target : targets) {
                    var player = target.getAsPlayer();

                    if (player != null) {
                        if (e.action.sendGossipMenu.gossipMenuId != 0) {
                            player.prepareGossipMenu(getBaseObject(), e.action.sendGossipMenu.gossipMenuId, true);
                        } else {
                            player.playerTalkClass.clearMenus();
                        }

                        var gossipNpcTextId = e.action.sendGossipMenu.gossipNpcTextId;

                        if (gossipNpcTextId == 0) {
                            gossipNpcTextId = player.getGossipTextId(e.action.sendGossipMenu.gossipMenuId, getBaseObject());
                        }

                        player.playerTalkClass.sendGossipMenu(gossipNpcTextId, getBaseObject().getGUID().clone());
                    }
                }

                break;
            }
            case SetHomePos: {
                for (var target : targets) {
                    if (isCreature(target)) {
                        if (e.getTargetType() == SmartTargets.Self) {
                            target.getAsCreature().setHomePosition(me.location.x, me.location.y, me.location.z, me.location.getOrientation());
                        } else if (e.getTargetType() == SmartTargets.Position) {
                            target.getAsCreature().setHomePosition(e.target.x, e.target.y, e.target.z, e.target.o);
                        } else if (e.getTargetType() == SmartTargets.CreatureRange || e.getTargetType() == SmartTargets.CreatureGuid || e.getTargetType() == SmartTargets.CreatureDistance || e.getTargetType() == SmartTargets.GameobjectRange || e.getTargetType() == SmartTargets.GameobjectGuid || e.getTargetType() == SmartTargets.GameobjectDistance || e.getTargetType() == SmartTargets.ClosestCreature || e.getTargetType() == SmartTargets.ClosestGameobject || e.getTargetType() == SmartTargets.OwnerOrSummoner || e.getTargetType() == SmartTargets.ActionInvoker || e.getTargetType() == SmartTargets.ClosestEnemy || e.getTargetType() == SmartTargets.ClosestFriendly || e.getTargetType() == SmartTargets.ClosestUnspawnedGameobject) {
                            target.getAsCreature().setHomePosition(target.location.x, target.location.y, target.location.z, target.location.getOrientation());
                        } else {
                            Log.outError(LogFilter.Sql, "SmartScript: Action target for SMART_ACTION_SET_HOME_POS is invalid, skipping");
                        }
                    }
                }

                break;
            }
            case SetHealthRegen: {
                for (var target : targets) {
                    if (isCreature(target)) {
                        target.getAsCreature().setRegenerateHealth(e.action.setHealthRegen.regenHealth != 0);
                    }
                }

                break;
            }
            case SetRoot: {
                for (var target : targets) {
                    if (isCreature(target)) {
                        target.getAsCreature().setControlled(e.action.setRoot.root != 0, UnitState.Root);
                    }
                }

                break;
            }
            case SummonCreatureGroup: {
                ArrayList<TempSummon> summonList;
                tangible.OutObject<ArrayList<TempSummon>> tempOutSummonList = new tangible.OutObject<ArrayList<TempSummon>>();
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: GetBaseObject().SummonCreatureGroup((byte)e.Action.creatureGroup.group, out var summonList);
                getBaseObject().summonCreatureGroup((byte)e.action.creatureGroup.group, tempOutSummonList);
            summonList = tempOutSummonList.outArgValue;

                for (var summon : summonList) {
                    if (unit == null && e.action.creatureGroup.attackInvoker != 0) {
                        summon.getAI().attackStart(unit);
                    }
                }

                break;
            }
            case SetPower: {
                for (var target : targets) {
                    if (isUnit(target)) {
                        target.getAsUnit().SetPower(PowerType.forValue((byte)e.action.power.powerType), (int)e.action.power.newPower);
                    }
                }

                break;
            }
            case AddPower: {
                for (var target : targets) {
                    if (isUnit(target)) {
                        target.getAsUnit().SetPower(PowerType.forValue((byte)e.action.power.powerType), target.getAsUnit().getPower(PowerType.forValue((byte)e.action.power.powerType)) + (int)e.action.power.newPower);
                    }
                }

                break;
            }
            case RemovePower: {
                for (var target : targets) {
                    if (isUnit(target)) {
                        target.getAsUnit().SetPower(PowerType.forValue((byte)e.action.power.powerType), target.getAsUnit().getPower(PowerType.forValue((byte)e.action.power.powerType)) - (int)e.action.power.newPower);
                    }
                }

                break;
            }
            case GameEventStop: {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: var eventId = (ushort)e.Action.gameEventStop.id;
                var eventId = (short)e.action.gameEventStop.id;

                if (!Global.getGameEventMgr().isActiveEvent(eventId)) {
                    Log.outError(LogFilter.Sql, "SmartScript.ProcessAction: At case SMART_ACTION_GAME_EVENT_STOP, inactive event (id: {0})", eventId);

                    break;
                }

                Global.getGameEventMgr().stopEvent(eventId, true);

                break;
            }
            case GameEventStart: {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: var eventId = (ushort)e.Action.gameEventStart.id;
                var eventId = (short)e.action.gameEventStart.id;

                if (Global.getGameEventMgr().isActiveEvent(eventId)) {
                    Log.outError(LogFilter.Sql, "SmartScript.ProcessAction: At case SMART_ACTION_GAME_EVENT_START, already activated event (id: {0})", eventId);

                    break;
                }

                Global.getGameEventMgr().startEvent(eventId, true);

                break;
            }
            case StartClosestWaypoint: {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: List<uint> waypoints = new();
                ArrayList<Integer> waypoints = new ArrayList<Integer>();
                var closestWaypointFromList = e.action.closestWaypointFromList.clone();

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: foreach (var id in new[] { closestWaypointFromList.wp1, closestWaypointFromList.wp2, closestWaypointFromList.wp3, closestWaypointFromList.wp4, closestWaypointFromList.wp5, closestWaypointFromList.wp6 })
                for (var id : new int[] {closestWaypointFromList.wp1, closestWaypointFromList.wp2, closestWaypointFromList.wp3, closestWaypointFromList.wp4, closestWaypointFromList.wp5, closestWaypointFromList.wp6}) {
                    if (id != 0) {
                        waypoints.add(id);
                    }
                }

                var distanceToClosest = Float.MAX_VALUE;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: uint closestPathId = 0;
                int closestPathId = 0;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: uint closestWaypointId = 0;
                int closestWaypointId = 0;

                for (var target : targets) {
                    var creature = target.getAsCreature();

                    if (creature != null) {
                        if (isSmart(creature)) {
                            for (var pathId : waypoints) {
                                var path = Global.getSmartAIMgr().getPath(pathId);

                                if (path == null || path.nodes.Empty()) {
                                    continue;
                                }

                                for (var waypoint : path.nodes) {
                                    var distToThisPath = creature.getDistance(waypoint.x, waypoint.y, waypoint.z);

                                    if (distToThisPath < distanceToClosest) {
                                        distanceToClosest = distToThisPath;
                                        closestPathId = pathId;
                                        closestWaypointId = waypoint.id;
                                    }
                                }
                            }

                            if (closestPathId != 0) {
                                ((SmartAI)creature.getAI()).startPath(false, closestPathId, true, null, closestWaypointId);
                            }
                        }
                    }
                }

                break;
            }
            case RandomSound: {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: List<uint> sounds = new();
                ArrayList<Integer> sounds = new ArrayList<Integer>();
                var randomSound = e.action.randomSound.clone();

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: foreach (var id in new[] { randomSound.sound1, randomSound.sound2, randomSound.sound3, randomSound.sound4 })
                for (var id : new int[] {randomSound.sound1, randomSound.sound2, randomSound.sound3, randomSound.sound4}) {
                    if (id != 0) {
                        sounds.add(id);
                    }
                }

                var onlySelf = e.action.randomSound.onlySelf != 0;

                for (var target : targets) {
                    if (isUnit(target)) {
                        var sound = sounds.SelectRandom();

                        if (e.action.randomSound.distance == 1) {
                            target.playDistanceSound(sound, onlySelf ? target.getAsPlayer() : null);
                        } else {
                            target.playDirectSound(sound, onlySelf ? target.getAsPlayer() : null);
                        }

                        Log.outDebug(LogFilter.ScriptsAi, "SmartScript.ProcessAction:: SMART_ACTION_RANDOM_SOUND: target: {0} ({1}), sound: {2}, onlyself: {3}", target.getName(), target.getGUID().toString(), sound, onlySelf);
                    }
                }

                break;
            }
            case SetCorpseDelay: {
                for (var target : targets) {
                    if (isCreature(target)) {
                        target.getAsCreature().setCorpseDelay(e.action.corpseDelay.timer, e.action.corpseDelay.includeDecayRatio == 0);
                    }
                }

                break;
            }
            case SpawnSpawngroup: {
                if (e.action.groupSpawn.minDelay == 0 && e.action.groupSpawn.maxDelay == 0) {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: var ignoreRespawn = ((e.Action.groupSpawn.spawnflags & (uint)SmartAiSpawnFlags.IgnoreRespawn) != 0);
                    var ignoreRespawn = ((e.action.groupSpawn.spawnflags & (int)SmartAiSpawnFlags.IgnoreRespawn.getValue()) != 0);
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: var force = ((e.Action.groupSpawn.spawnflags & (uint)SmartAiSpawnFlags.ForceSpawn) != 0);
                    var force = ((e.action.groupSpawn.spawnflags & (int)SmartAiSpawnFlags.ForceSpawn.getValue()) != 0);

                    // Instant spawn
                    getBaseObject().getMap().spawnGroupSpawn(e.action.groupSpawn.groupId, ignoreRespawn, force);
                } else {
                    // Delayed spawn (use values from parameter to schedule event to call us back
                    SmartEvent ne = new SmartEvent();
                    ne.type = SmartEvents.Update;
                    ne.eventChance = 100;

                    ne.minMaxRepeat.min = e.action.groupSpawn.minDelay;
                    ne.minMaxRepeat.max = e.action.groupSpawn.maxDelay;
                    ne.minMaxRepeat.repeatMin = 0;
                    ne.minMaxRepeat.repeatMax = 0;

                    ne.eventFlags = SmartEventFlags.forValue(0);
                    ne.eventFlags = SmartEventFlags.forValue(ne.eventFlags.getValue() | SmartEventFlags.NotRepeatable.getValue());

                    SmartAction ac = new SmartAction();
                    ac.type = SmartActions.SpawnSpawngroup;
                    ac.groupSpawn.groupId = e.action.groupSpawn.groupId;
                    ac.groupSpawn.minDelay = 0;
                    ac.groupSpawn.maxDelay = 0;
                    ac.groupSpawn.spawnflags = e.action.groupSpawn.spawnflags;
                    ac.timeEvent.id = e.action.timeEvent.id;

                    SmartScriptHolder ev = new SmartScriptHolder();
                    ev.event = ne.clone();
                    ev.eventId = e.eventId;
                    ev.target = e.target.clone();
                    ev.action = ac.clone();
                    initTimer(ev);
                    storedEvents.add(ev);
                }

                break;
            }
            case DespawnSpawngroup: {
                if (e.action.groupSpawn.minDelay == 0 && e.action.groupSpawn.maxDelay == 0) {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: var deleteRespawnTimes = ((e.Action.groupSpawn.spawnflags & (uint)SmartAiSpawnFlags.NosaveRespawn) != 0);
                    var deleteRespawnTimes = ((e.action.groupSpawn.spawnflags & (int)SmartAiSpawnFlags.NosaveRespawn.getValue()) != 0);

                    // Instant spawn
                    getBaseObject().getMap().spawnGroupSpawn(e.action.groupSpawn.groupId, deleteRespawnTimes);
                } else {
                    // Delayed spawn (use values from parameter to schedule event to call us back
                    SmartEvent ne = new SmartEvent();
                    ne.type = SmartEvents.Update;
                    ne.eventChance = 100;

                    ne.minMaxRepeat.min = e.action.groupSpawn.minDelay;
                    ne.minMaxRepeat.max = e.action.groupSpawn.maxDelay;
                    ne.minMaxRepeat.repeatMin = 0;
                    ne.minMaxRepeat.repeatMax = 0;

                    ne.eventFlags = SmartEventFlags.forValue(0);
                    ne.eventFlags = SmartEventFlags.forValue(ne.eventFlags.getValue() | SmartEventFlags.NotRepeatable.getValue());

                    SmartAction ac = new SmartAction();
                    ac.type = SmartActions.DespawnSpawngroup;
                    ac.groupSpawn.groupId = e.action.groupSpawn.groupId;
                    ac.groupSpawn.minDelay = 0;
                    ac.groupSpawn.maxDelay = 0;
                    ac.groupSpawn.spawnflags = e.action.groupSpawn.spawnflags;
                    ac.timeEvent.id = e.action.timeEvent.id;

                    SmartScriptHolder ev = new SmartScriptHolder();
                    ev.event = ne.clone();
                    ev.eventId = e.eventId;
                    ev.target = e.target.clone();
                    ev.action = ac.clone();
                    initTimer(ev);
                    storedEvents.add(ev);
                }

                break;
            }
            case DisableEvade: {
                if (!isSmart()) {
                    break;
                }

                ((SmartAI)me.getAI()).setEvadeDisabled(e.action.disableEvade.disable != 0);

                break;
            }
            case AddThreat: {
                if (!me.getCanHaveThreatList()) {
                    break;
                }

                for (var target : targets) {
                    if (isUnit(target)) {
                        me.getThreatManager().addThreat(target.getAsUnit(), (float)(e.action.threat.threatINC - (float)e.action.threat.threatDEC), null, true, true);
                    }
                }

                break;
            }
            case LoadEquipment: {
                for (var target : targets) {
                    if (isCreature(target)) {
                        target.getAsCreature().loadEquipment((int)e.action.loadEquipment.id, e.action.loadEquipment.force != 0);
                    }
                }

                break;
            }
            case TriggerRandomTimedEvent: {
                var eventId = RandomHelper.URand(e.action.randomTimedEvent.minId, e.action.randomTimedEvent.maxId);
                processEventsFor(SmartEvents.TimedEventTriggered, null, eventId);

                break;
            }
            case PauseMovement: {
                for (var target : targets) {
                    if (isUnit(target)) {
                        target.getAsUnit().pauseMovement(e.action.pauseMovement.pauseTimer, MovementSlot.forValue(e.action.pauseMovement.movementSlot), e.action.pauseMovement.force != 0);
                    }
                }

                break;
            }
            case RespawnBySpawnId: {
                Map map = null;
                var obj = getBaseObject();

                if (obj != null) {
                    map = obj.getMap();
                } else if (!targets.Empty()) {
                    map = targets.get(0).Map;
                }

                if (map) {
                    map.Respawn(SpawnObjectType.forValue(e.action.respawnData.spawnType), e.action.respawnData.spawnId);
                } else {
                    Log.outError(LogFilter.Sql, String.format("SmartScript.ProcessAction: Entry %1$s SourceType %2$s, Event %3$s - tries to respawn by spawnId but does not provide a map", e.entryOrGuid, e.getScriptType(), e.eventId));
                }

                break;
            }
            case PlayAnimkit: {
                for (var target : targets) {
                    if (isCreature(target)) {
                        if (e.action.animKit.type == 0) {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: target.AsCreature.PlayOneShotAnimKitId((ushort)e.Action.animKit.animKit);
                            target.getAsCreature().playOneShotAnimKitId((short)e.action.animKit.animKit);
                        } else if (e.action.animKit.type == 1) {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: target.AsCreature.SetAIAnimKitId((ushort)e.Action.animKit.animKit);
                            target.getAsCreature().setAIAnimKitId((short)e.action.animKit.animKit);
                        } else if (e.action.animKit.type == 2) {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: target.AsCreature.SetMeleeAnimKitId((ushort)e.Action.animKit.animKit);
                            target.getAsCreature().setMeleeAnimKitId((short)e.action.animKit.animKit);
                        } else if (e.action.animKit.type == 3) {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: target.AsCreature.SetMovementAnimKitId((ushort)e.Action.animKit.animKit);
                            target.getAsCreature().setMovementAnimKitId((short)e.action.animKit.animKit);
                        }

                        Log.outDebug(LogFilter.ScriptsAi, String.format("SmartScript::ProcessAction:: SMART_ACTION_PLAY_ANIMKIT: target: %1$s (%2$s), AnimKit: %3$s, Type: %4$s", target.getName(), target.getGUID().clone(), e.action.animKit.animKit, e.action.animKit.type));
                    } else if (isGameObject(target)) {
                        switch (e.action.animKit.type) {
                            case 0:
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: target.AsGameObject.SetAnimKitId((ushort)e.Action.animKit.animKit, true);
                                target.getAsGameObject().setAnimKitId((short)e.action.animKit.animKit, true);

                                break;
                            case 1:
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: target.AsGameObject.SetAnimKitId((ushort)e.Action.animKit.animKit, false);
                                target.getAsGameObject().setAnimKitId((short)e.action.animKit.animKit, false);

                                break;
                            default:
                                break;
                        }

                        Log.outDebug(LogFilter.ScriptsAi, "SmartScript.ProcessAction:: SMART_ACTION_PLAY_ANIMKIT: target: {0} ({1}), AnimKit: {2}, Type: {3}", target.getName(), target.getGUID().toString(), e.action.animKit.animKit, e.action.animKit.type);
                    }
                }

                break;
            }
            case ScenePlay: {
                for (var target : targets) {
                    var playerTarget = target.getAsPlayer();

                    if (playerTarget) {
                        playerTarget.getSceneMgr().playScene(e.action.scene.sceneId);
                    }
                }

                break;
            }
            case SceneCancel: {
                for (var target : targets) {
                    var playerTarget = target.getAsPlayer();

                    if (playerTarget) {
                        playerTarget.getSceneMgr().cancelSceneBySceneId(e.action.scene.sceneId);
                    }
                }

                break;
            }
            case PlayCinematic: {
                for (var target : targets) {
                    if (!isPlayer(target)) {
                        continue;
                    }

                    target.getAsPlayer().sendCinematicStart(e.action.cinematic.entry);
                }

                break;
            }
            case SetMovementSpeed: {
                var speedInteger = e.action.movementSpeed.speedInteger;
                var speedFraction = e.action.movementSpeed.speedFraction;
                var speed = (float)((float)speedInteger + (float)speedFraction / Math.pow(10, Math.floor(Math.log10((float)(speedFraction != 0 ? speedFraction : 1)) + 1)));

                for (var target : targets) {
                    if (isCreature(target)) {
                        target.getAsCreature().setSpeed(UnitMoveType.forValue(e.action.movementSpeed.movementType), speed);
                    }
                }

                break;
            }
            case PlaySpellVisualKit: {
                for (var target : targets) {
                    if (isUnit(target)) {
                        target.getAsUnit().sendPlaySpellVisualKit(e.action.spellVisualKit.spellVisualKitId, e.action.spellVisualKit.kitType, e.action.spellVisualKit.duration);

                        Log.outDebug(LogFilter.ScriptsAi, String.format("SmartScript::ProcessAction:: SMART_ACTION_PLAY_SPELL_VISUAL_KIT: target: %1$s (%2$s), SpellVisualKit: %3$s", target.getName(), target.getGUID().clone(), e.action.spellVisualKit.spellVisualKitId));
                    }
                }

                break;
            }
            case OverrideLight: {
                var obj = getBaseObject();

                if (obj != null) {
                    obj.getMap().setZoneOverrideLight(e.action.overrideLight.zoneId, e.action.overrideLight.areaLightId, e.action.overrideLight.overrideLightId, TimeSpan.FromMilliseconds(e.action.overrideLight.transitionMilliseconds));

                    Log.outDebug(LogFilter.ScriptsAi, String.format("SmartScript::ProcessAction: SMART_ACTION_OVERRIDE_LIGHT: %1$s sets zone override light (zoneId: %2$s, ", obj.getGUID().clone(), e.action.overrideLight.zoneId) + String.format("areaLightId: %1$s, overrideLightId: %2$s, transitionMilliseconds: %3$s)", e.action.overrideLight.areaLightId, e.action.overrideLight.overrideLightId, e.action.overrideLight.transitionMilliseconds));
                }

                break;
            }
            case OverrideWeather: {
                var obj = getBaseObject();

                if (obj != null) {
                    obj.getMap().setZoneWeather(e.action.overrideWeather.zoneId, WeatherState.forValue(e.action.overrideWeather.weatherId), e.action.overrideWeather.intensity);

                    Log.outDebug(LogFilter.ScriptsAi, String.format("SmartScript::ProcessAction: SMART_ACTION_OVERRIDE_WEATHER: %1$s sets zone weather (zoneId: %2$s, ", obj.getGUID().clone(), e.action.overrideWeather.zoneId) + String.format("weatherId: %1$s, intensity: %2$s)", e.action.overrideWeather.weatherId, e.action.overrideWeather.intensity));
                }

                break;
            }
            case SetHover: {
                for (var target : targets) {
                    if (isUnit(target)) {
                        target.getAsUnit().setHover(e.action.setHover.enable != 0);
                    }
                }

                break;
            }
            case SetHealthPct: {
                for (var target : targets) {
                    var targetUnit = target.getAsUnit();

                    if (targetUnit != null) {
                        targetUnit.setHealth(targetUnit.countPctFromMaxHealth((int)e.action.setHealthPct.percent));
                    }
                }

                break;
            }
            case CreateConversation: {
                var baseObject = getBaseObject();

                for (var target : targets) {
                    var playerTarget = target.getAsPlayer();

                    if (playerTarget != null) {
                        var conversation = Conversation.createConversation(e.action.conversation.id, playerTarget, playerTarget.location, playerTarget.getGUID().clone(), null);

                        if (!conversation) {
                            Log.outWarn(LogFilter.ScriptsAi, String.format("SmartScript.ProcessAction: SMART_ACTION_CREATE_CONVERSATION: id %1$s, baseObject %2$s, target %3$s - failed to create", e.action.conversation.id, baseObject == null ? null : baseObject.getName(), playerTarget.getName()));
                        }
                    }
                }

                break;
            }
            case SetImmunePC: {
                for (var target : targets) {
                    if (isUnit(target)) {
                        if (e.action.setImmunePC.immunePC != 0) {
                            target.getAsUnit().setUnitFlag(UnitFlags.ImmuneToPc);
                        } else {
                            target.getAsUnit().removeUnitFlag(UnitFlags.ImmuneToPc);
                        }
                    }
                }

                break;
            }
            case SetImmuneNPC: {
                for (var target : targets) {
                    if (isUnit(target)) {
                        if (e.action.setImmuneNPC.immuneNPC != 0) {
                            target.getAsUnit().setUnitFlag(UnitFlags.ImmuneToNpc);
                        } else {
                            target.getAsUnit().removeUnitFlag(UnitFlags.ImmuneToNpc);
                        }
                    }
                }

                break;
            }
            case SetUninteractible: {
                for (var target : targets) {
                    if (isUnit(target)) {
                        if (e.action.setUninteractible.uninteractible != 0) {
                            target.getAsUnit().setUnitFlag(UnitFlags.Uninteractible);
                        } else {
                            target.getAsUnit().removeUnitFlag(UnitFlags.Uninteractible);
                        }
                    }
                }

                break;
            }
            case ActivateGameobject: {
                for (var target : targets) {
                    var targetGo = target.getAsGameObject();

                    if (targetGo != null) {
                        targetGo.activateObject(GameObjectActions.forValue(e.action.activateGameObject.gameObjectAction), (int)e.action.activateGameObject.param, getBaseObject());
                    }
                }

                break;
            }
            case AddToStoredTargetList: {
                if (!targets.Empty()) {
                    addToStoredTargetList(targets, e.action.addToStoredTargets.id);
                } else {
                    var baseObject = getBaseObject();
                    Log.outWarn(LogFilter.ScriptsAi, String.format("SmartScript::ProcessAction:: SMART_ACTION_ADD_TO_STORED_TARGET_LIST: var %1$s, baseObject %2$s, event %3$s - tried to add no targets to stored target list", e.action.addToStoredTargets.id, (baseObject == null ? "" : baseObject.getName()), e.eventId));
                }

                break;
            }
            case BecomePersonalCloneForPlayer: {
                var baseObject = getBaseObject();

                void doCreatePersonalClone(Position position, Player privateObjectOwner) {
                    Creature summon = getBaseObject().summonPersonalClone(position, TempSummonType.forValue(e.action.becomePersonalClone.type), TimeSpan.FromMilliseconds(e.action.becomePersonalClone.duration), 0, 0, privateObjectOwner);

                    if (summon != null) {
                        if (isSmart(summon)) {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: ((SmartAI)summon.AI).SetTimedActionList(e, (uint)e.EntryOrGuid, privateObjectOwner, e.EventId + 1);
                            ((SmartAI)summon.getAI()).setTimedActionList(e, (int)e.entryOrGuid, privateObjectOwner, e.eventId + 1);
                        }
                    }
                }

                // if target is position then targets container was empty
                if (e.getTargetType() != SmartTargets.Position) {
                    for (var target : targets) {
                        var playerTarget = target == null ? null : target.getAsPlayer();

                        if (playerTarget != null) {
                            doCreatePersonalClone(baseObject.location, playerTarget);
                        }
                    }
                } else {
                    var invoker = getLastInvoker() == null ? null : getLastInvoker().getAsPlayer();

                    if (invoker != null) {
                        doCreatePersonalClone(new Position(e.target.x, e.target.y, e.target.z, e.target.o), invoker);
                    }
                }

                // action list will continue on personal clones
                tangible.ListHelper.removeAll(timedActionList, script -> {
                        return script.EventId > e.eventId;
                });

                break;
            }
            case TriggerGameEvent: {
                var sourceObject = getBaseObjectOrUnitInvoker(unit);

                for (var target : targets) {
                    if (e.action.triggerGameEvent.useSaiTargetAsGameEventSource != 0) {
                        GameEvents.trigger(e.action.triggerGameEvent.eventId, target, sourceObject);
                    } else {
                        GameEvents.trigger(e.action.triggerGameEvent.eventId, sourceObject, target);
                    }
                }

                break;
            }
            case DoAction: {
                for (var target : targets) {
                    var unitTarget = target == null ? null : target.getAsUnit();

                    if (unitTarget != null) {
                        if (unitTarget.getAI() != null) {
                            unitTarget.getAI().doAction((int)e.action.doAction.actionId);
                        }
                    } else {
                        var goTarget = target == null ? null : target.getAsGameObject();

                        if (goTarget != null) {
                            if (goTarget.getAI() != null) {
                                goTarget.getAI().doAction((int)e.action.doAction.actionId);
                            }
                        }
                    }
                }

                break;
            }
            default:
                Log.outError(LogFilter.Sql, "SmartScript.ProcessAction: Entry {0} SourceType {1}, Event {2}, Unhandled Action type {3}", e.entryOrGuid, e.getScriptType(), e.eventId, e.getActionType());

                break;
        }

        if (e.link != 0 && e.link != e.eventId) {
            var linked = Global.getSmartAIMgr().findLinkedEvent(events, e.link);

            if (linked != null) {
                processEvent(linked, unit, var0, var1, bvar, spell, gob, varString);
            } else {
                Log.outError(LogFilter.Sql, "SmartScript.ProcessAction: Entry {0} SourceType {1}, Event {2}, Link Event {3} not found or invalid, skipped.", e.entryOrGuid, e.getScriptType(), e.eventId, e.link);
            }
        }
    }


    private void processTimedAction(SmartScriptHolder e, int min, int max, Unit unit, int var0, int var1, boolean bvar, SpellInfo spell, GameObject gob) {
        processTimedAction(e, min, max, unit, var0, var1, bvar, spell, gob, "");
    }

    private void processTimedAction(SmartScriptHolder e, int min, int max, Unit unit, int var0, int var1, boolean bvar, SpellInfo spell) {
        processTimedAction(e, min, max, unit, var0, var1, bvar, spell, null, "");
    }

    private void processTimedAction(SmartScriptHolder e, int min, int max, Unit unit, int var0, int var1, boolean bvar) {
        processTimedAction(e, min, max, unit, var0, var1, bvar, null, null, "");
    }

    private void processTimedAction(SmartScriptHolder e, int min, int max, Unit unit, int var0, int var1) {
        processTimedAction(e, min, max, unit, var0, var1, false, null, null, "");
    }

    private void processTimedAction(SmartScriptHolder e, int min, int max, Unit unit, int var0) {
        processTimedAction(e, min, max, unit, var0, 0, false, null, null, "");
    }

    private void processTimedAction(SmartScriptHolder e, int min, int max, Unit unit) {
        processTimedAction(e, min, max, unit, 0, 0, false, null, null, "");
    }

    private void processTimedAction(SmartScriptHolder e, int min, int max) {
        processTimedAction(e, min, max, null, 0, 0, false, null, null, "");
    }

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: void ProcessTimedAction(SmartScriptHolder e, uint min, uint max, Unit unit = null, uint var0 = 0, uint var1 = 0, bool bvar = false, SpellInfo spell = null, GameObject gob = null, string varString = "")
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
    private void processTimedAction(SmartScriptHolder e, int min, int max, Unit unit, int var0, int var1, boolean bvar, SpellInfo spell, GameObject gob, String varString) {
        // We may want to execute action rarely and because of this if condition is not fulfilled the action will be rechecked in a long time
        if (Global.getConditionMgr().isObjectMeetingSmartEventConditions(e.entryOrGuid, e.eventId, e.sourceType, unit, getBaseObject())) {
            recalcTimer(e, min, max);
            processAction(e, unit, var0, var1, bvar, spell, gob, varString);
        } else {
            recalcTimer(e, Math.min(min, 5000), Math.min(min, 5000));
        }
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: SmartScriptHolder CreateSmartEvent(SmartEvents e, SmartEventFlags event_flags, uint event_param1, uint event_param2, uint event_param3, uint event_param4, uint event_param5, SmartActions action, uint action_param1, uint action_param2, uint action_param3, uint action_param4, uint action_param5, uint action_param6, uint action_param7, SmartTargets t, uint target_param1, uint target_param2, uint target_param3, uint target_param4, uint phaseMask)
    private SmartScriptHolder createSmartEvent(SmartEvents e, SmartEventFlags eventFlags, int eventParam1, int eventParam2, int eventParam3, int eventParam4, int eventParam5, SmartActions action, int actionParam1, int actionParam2, int actionParam3, int actionParam4, int actionParam5, int actionParam6, int actionParam7, SmartTargets t, int targetParam1, int targetParam2, int targetParam3, int targetParam4, int phaseMask) {
        SmartScriptHolder script = new SmartScriptHolder();
        script.event.type = e;
        script.event.raw.param1 = eventParam1;
        script.event.raw.param2 = eventParam2;
        script.event.raw.param3 = eventParam3;
        script.event.raw.param4 = eventParam4;
        script.event.raw.param5 = eventParam5;
        script.event.eventPhaseMask = phaseMask;
        script.event.eventFlags = eventFlags;
        script.event.eventChance = 100;

        script.action.type = action;
        script.action.raw.param1 = actionParam1;
        script.action.raw.param2 = actionParam2;
        script.action.raw.param3 = actionParam3;
        script.action.raw.param4 = actionParam4;
        script.action.raw.param5 = actionParam5;
        script.action.raw.param6 = actionParam6;
        script.action.raw.param7 = actionParam7;

        script.target.type = t;
        script.target.raw.param1 = targetParam1;
        script.target.raw.param2 = targetParam2;
        script.target.raw.param3 = targetParam3;
        script.target.raw.param4 = targetParam4;

        script.sourceType = SmartScriptType.Creature;
        initTimer(script);

        return script;
    }


    private java.util.ArrayList<WorldObject> getTargets(SmartScriptHolder e) {
        return getTargets(e, null);
    }

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: List<WorldObject> GetTargets(SmartScriptHolder e, WorldObject invoker = null)
    private ArrayList<WorldObject> getTargets(SmartScriptHolder e, WorldObject invoker) {
        WorldObject scriptTrigger = null;

        if (invoker != null) {
            scriptTrigger = invoker;
        } else {
            var tempLastInvoker = getLastInvoker();

            if (tempLastInvoker != null) {
                scriptTrigger = tempLastInvoker;
            }
        }

        var baseObject = getBaseObject();

        ArrayList<WorldObject> targets = new ArrayList<WorldObject>();

        switch (e.getTargetType()) {
            case Self:
                if (baseObject != null) {
                    targets.add(baseObject);
                }

                break;
            case Victim:
                if (me != null && me.getVictim() != null) {
                    targets.add(me.getVictim());
                }

                break;
            case HostileSecondAggro:
                if (me != null) {
                    if (e.target.hostilRandom.powerType != 0) {
                        var u = me.getAI().SelectTarget(SelectTargetMethod.MaxThreat, 1, new PowerUsersSelector(me, PowerType.forValue((byte)(e.target.hostilRandom.powerType - 1)), (float)e.target.hostilRandom.maxDist, e.target.hostilRandom.playerOnly != 0));

                        if (u != null) {
                            targets.add(u);
                        }
                    } else {
                        var u = me.getAI().SelectTarget(SelectTargetMethod.MaxThreat, 1, (float)e.target.hostilRandom.maxDist, e.target.hostilRandom.playerOnly != 0);

                        if (u != null) {
                            targets.add(u);
                        }
                    }
                }

                break;
            case HostileLastAggro:
                if (me != null) {
                    if (e.target.hostilRandom.powerType != 0) {
                        var u = me.getAI().SelectTarget(SelectTargetMethod.MinThreat, 1, new PowerUsersSelector(me, PowerType.forValue((byte)(e.target.hostilRandom.powerType - 1)), (float)e.target.hostilRandom.maxDist, e.target.hostilRandom.playerOnly != 0));

                        if (u != null) {
                            targets.add(u);
                        }
                    } else {
                        var u = me.getAI().SelectTarget(SelectTargetMethod.MinThreat, 1, (float)e.target.hostilRandom.maxDist, e.target.hostilRandom.playerOnly != 0);

                        if (u != null) {
                            targets.add(u);
                        }
                    }
                }

                break;
            case HostileRandom:
                if (me != null) {
                    if (e.target.hostilRandom.powerType != 0) {
                        var u = me.getAI().SelectTarget(SelectTargetMethod.Random, 1, new PowerUsersSelector(me, PowerType.forValue((byte)(e.target.hostilRandom.powerType - 1)), (float)e.target.hostilRandom.maxDist, e.target.hostilRandom.playerOnly != 0));

                        if (u != null) {
                            targets.add(u);
                        }
                    } else {
                        var u = me.getAI().SelectTarget(SelectTargetMethod.Random, 1, (float)e.target.hostilRandom.maxDist, e.target.hostilRandom.playerOnly != 0);

                        if (u != null) {
                            targets.add(u);
                        }
                    }
                }

                break;
            case HostileRandomNotTop:
                if (me != null) {
                    if (e.target.hostilRandom.powerType != 0) {
                        var u = me.getAI().SelectTarget(SelectTargetMethod.Random, 1, new PowerUsersSelector(me, PowerType.forValue((byte)(e.target.hostilRandom.powerType - 1)), (float)e.target.hostilRandom.maxDist, e.target.hostilRandom.playerOnly != 0));

                        if (u != null) {
                            targets.add(u);
                        }
                    } else {
                        var u = me.getAI().SelectTarget(SelectTargetMethod.Random, 1, (float)e.target.hostilRandom.maxDist, e.target.hostilRandom.playerOnly != 0);

                        if (u != null) {
                            targets.add(u);
                        }
                    }
                }

                break;
            case Farthest:
                if (me) {
                    var u = me.getAI().SelectTarget(SelectTargetMethod.MaxDistance, 0, new FarthestTargetSelector(me, (float)e.target.farthest.maxDist, e.target.farthest.playerOnly != 0, e.target.farthest.isInLos != 0));

                    if (u != null) {
                        targets.add(u);
                    }
                }

                break;
            case ActionInvoker:
                if (scriptTrigger != null) {
                    targets.add(scriptTrigger);
                }

                break;
            case ActionInvokerVehicle:
                if (scriptTrigger != null && scriptTrigger.getAsUnit() == null ? null : scriptTrigger.getAsUnit().getVehicle1() != null && scriptTrigger.getAsUnit().getVehicle1().getBase() != null) {
                    targets.add(scriptTrigger.getAsUnit().getVehicle1().getBase());
                }

                break;
            case InvokerParty:
                if (scriptTrigger != null) {
                    var player = scriptTrigger.getAsPlayer();

                    if (player != null) {
                        var group = player.getGroup();

                        if (group) {
                            for (var groupRef = group.getFirstMember(); groupRef != null; groupRef = groupRef.Next()) {
                                var member = groupRef.getSource();

                                if (member) {
                                    if (member.isInMap(player)) {
                                        targets.add(member);
                                    }
                                }
                            }
                        }
                        // We still add the player to the list if there is no group. If we do
                        // this even if there is a group (thus the else-check), it will add the
                        // same player to the list twice. We don't want that to happen.
                        else {
                            targets.add(scriptTrigger);
                        }
                    }
                }

                break;
            case CreatureRange: {
                var refObj = baseObject;

                if (refObj == null) {
                    refObj = scriptTrigger;
                }

                if (refObj == null) {
                    Log.outError(LogFilter.Sql, String.format("SMART_TARGET_CREATURE_RANGE: %1$s is missing base object or invoker.", e));

                    break;
                }

                var units = getWorldObjectsInDist(e.target.unitRange.maxDist);

                for (var obj : units) {
                    if (!isCreature(obj)) {
                        continue;
                    }

                    if (me != null && me == obj) {
                        continue;
                    }

                    if ((e.target.unitRange.creature == 0 || obj.getAsCreature().getEntry() == e.target.unitRange.creature) && refObj.isInRange(obj, e.target.unitRange.minDist, e.target.unitRange.maxDist)) {
                        targets.add(obj);
                    }
                }

                if (e.target.unitRange.maxSize != 0) {
                    targets.RandomResize(e.target.unitRange.maxSize);
                }

                break;
            }
            case CreatureDistance: {
                var units = getWorldObjectsInDist(e.target.unitDistance.dist);

                for (var obj : units) {
                    if (!isCreature(obj)) {
                        continue;
                    }

                    if (me != null && me == obj) {
                        continue;
                    }

                    if (e.target.unitDistance.creature == 0 || obj.getAsCreature().getEntry() == e.target.unitDistance.creature) {
                        targets.add(obj);
                    }
                }

                if (e.target.unitDistance.maxSize != 0) {
                    targets.RandomResize(e.target.unitDistance.maxSize);
                }

                break;
            }
            case GameobjectDistance: {
                var units = getWorldObjectsInDist(e.target.goDistance.dist);

                for (var obj : units) {
                    if (!isGameObject(obj)) {
                        continue;
                    }

                    if (go != null && go == obj) {
                        continue;
                    }

                    if (e.target.goDistance.entry == 0 || obj.getAsGameObject().getEntry() == e.target.goDistance.entry) {
                        targets.add(obj);
                    }
                }

                if (e.target.goDistance.maxSize != 0) {
                    targets.RandomResize(e.target.goDistance.maxSize);
                }

                break;
            }
            case GameobjectRange: {
                var refObj = baseObject;

                if (refObj == null) {
                    refObj = scriptTrigger;
                }

                if (refObj == null) {
                    Log.outError(LogFilter.Sql, String.format("SMART_TARGET_GAMEOBJECT_RANGE: %1$s is missing base object or invoker.", e));

                    break;
                }

                var units = getWorldObjectsInDist(e.target.goRange.maxDist);

                for (var obj : units) {
                    if (!isGameObject(obj)) {
                        continue;
                    }

                    if (go != null && go == obj) {
                        continue;
                    }

                    if ((e.target.goRange.entry == 0 || obj.getAsGameObject().getEntry() == e.target.goRange.entry) && refObj.isInRange(obj, e.target.goRange.minDist, e.target.goRange.maxDist)) {
                        targets.add(obj);
                    }
                }

                if (e.target.goRange.maxSize != 0) {
                    targets.RandomResize(e.target.goRange.maxSize);
                }

                break;
            }
            case CreatureGuid: {
                if (scriptTrigger == null && baseObject == null) {
                    Log.outError(LogFilter.Sql, String.format("SMART_TARGET_CREATURE_GUID %1$s can not be used without invoker", e));

                    break;
                }

                var target = findCreatureNear(scriptTrigger != null ? scriptTrigger : baseObject, e.target.unitGUID.dbGuid);

                if (target) {
                    if (target != null && (e.target.unitGUID.entry == 0 || target.getEntry() == e.target.unitGUID.entry)) {
                        targets.add(target);
                    }
                }

                break;
            }
            case GameobjectGuid: {
                if (scriptTrigger == null && baseObject == null) {
                    Log.outError(LogFilter.Sql, String.format("SMART_TARGET_GAMEOBJECT_GUID %1$s can not be used without invoker", e));

                    break;
                }

                var target = findGameObjectNear(scriptTrigger != null ? scriptTrigger : baseObject, e.target.goGUID.dbGuid);

                if (target) {
                    if (target != null && (e.target.goGUID.entry == 0 || target.getEntry() == e.target.goGUID.entry)) {
                        targets.add(target);
                    }
                }

                break;
            }
            case PlayerRange: {
                var units = getWorldObjectsInDist(e.target.playerRange.maxDist);

                if (!units.Empty() && baseObject != null) {
                    for (var obj : units) {
                        if (isPlayer(obj) && baseObject.isInRange(obj, e.target.playerRange.minDist, e.target.playerRange.maxDist)) {
                            targets.add(obj);
                        }
                    }
                }

                break;
            }
            case PlayerDistance: {
                var units = getWorldObjectsInDist(e.target.playerDistance.dist);

                for (var obj : units) {
                    if (isPlayer(obj)) {
                        targets.add(obj);
                    }
                }

                break;
            }
            case Stored: {
                var refObj = baseObject;

                if (refObj == null) {
                    refObj = scriptTrigger;
                }

                if (refObj == null) {
                    Log.outError(LogFilter.Sql, String.format("SMART_TARGET_STORED: %1$s is missing base object or invoker.", e));

                    break;
                }

                var stored = getStoredTargetList(e.target.stored.id, refObj);

                if (stored != null) {
                    targets.addAll(stored);
                }

                break;
            }
            case ClosestCreature: {
                var refObj = baseObject;

                if (refObj == null) {
                    refObj = scriptTrigger;
                }

                if (refObj == null) {
                    Log.outError(LogFilter.Sql, String.format("SMART_TARGET_CLOSEST_CREATURE: %1$s is missing base object or invoker.", e));

                    break;
                }

                var target = refObj.findNearestCreature(e.target.unitClosest.entry, e.target.unitClosest.dist != 0 ? e.target.unitClosest.dist : 100, e.target.unitClosest.dead == 0);

                if (target) {
                    targets.add(target);
                }

                break;
            }
            case ClosestGameobject: {
                var refObj = baseObject;

                if (refObj == null) {
                    refObj = scriptTrigger;
                }

                if (refObj == null) {
                    Log.outError(LogFilter.Sql, String.format("SMART_TARGET_CLOSEST_GAMEOBJECT: %1$s is missing base object or invoker.", e));

                    break;
                }

                var target = refObj.findNearestGameObject(e.target.goClosest.entry, e.target.goClosest.dist != 0 ? e.target.goClosest.dist : 100);

                if (target) {
                    targets.add(target);
                }

                break;
            }
            case ClosestPlayer: {
                var refObj = baseObject;

                if (refObj == null) {
                    refObj = scriptTrigger;
                }

                if (refObj == null) {
                    Log.outError(LogFilter.Sql, String.format("SMART_TARGET_CLOSEST_PLAYER: %1$s is missing base object or invoker.", e));

                    break;
                }

                var target = refObj.selectNearestPlayer(e.target.playerDistance.dist);

                if (target) {
                    targets.add(target);
                }

                break;
            }
            case OwnerOrSummoner: {
                if (me != null) {
                    var charmerOrOwnerGuid = me.getCharmerOrOwnerGUID().clone();

                    if (charmerOrOwnerGuid.isEmpty()) {
                        var tempSummon = me.toTempSummon();

                        if (tempSummon) {
                            var summoner = tempSummon.getSummoner();

                            if (summoner) {
                                charmerOrOwnerGuid = summoner.getGUID().clone();
                            }
                        }
                    }

                    if (charmerOrOwnerGuid.isEmpty()) {
                        charmerOrOwnerGuid = me.getCreatorGUID().clone();
                    }

                    var owner = Global.getObjAccessor().getWorldObject(me, charmerOrOwnerGuid.clone());

                    if (owner != null) {
                        targets.add(owner);
                    }
                } else if (go != null) {
                    var owner = Global.getObjAccessor().getUnit(go, go.getOwnerGUID().clone());

                    if (owner) {
                        targets.add(owner);
                    }
                }

                // Get owner of owner
                if (e.target.owner.useCharmerOrOwner != 0 && !targets.Empty()) {
                    var owner = targets.get(0);
                    targets.clear();

                    var unitBase = Global.getObjAccessor().getUnit(owner, owner.CharmerOrOwnerGUID);

                    if (unitBase != null) {
                        targets.add(unitBase);
                    }
                }

                break;
            }
            case ThreatList: {
                if (me != null && me.getCanHaveThreatList()) {
                    for (var refe : me.getThreatManager().getSortedThreatList()) {
                        if (e.target.threatList.maxDist == 0 || me.isWithinCombatRange(refe.getVictim(), e.target.threatList.maxDist)) {
                            targets.add(refe.getVictim());
                        }
                    }
                }

                break;
            }
            case ClosestEnemy: {
                if (me != null) {
                    var target = me.selectNearestTarget(e.target.closestAttackable.maxDist);

                    if (target != null) {
                        targets.add(target);
                    }
                }

                break;
            }
            case ClosestFriendly: {
                if (me != null) {
                    var target = doFindClosestFriendlyInRange(e.target.closestFriendly.maxDist);

                    if (target != null) {
                        targets.add(target);
                    }
                }

                break;
            }
            case LootRecipients: {
                if (me) {
                    for (var tapperGuid : me.getTapList()) {
                        var tapper = Global.getObjAccessor().GetPlayer(me, tapperGuid);

                        if (tapper != null) {
                            targets.add(tapper);
                        }
                    }
                }

                break;
            }
            case VehiclePassenger: {
                if (me && me.isVehicle()) {
                    for (var pair : me.getVehicleKit().seats.entrySet()) {
                        if (e.target.vehicle.seatMask == 0 || (e.target.vehicle.seatMask & (1 << pair.getKey())) != 0) {
                            var u = Global.getObjAccessor().getUnit(me, pair.getValue().Passenger.Guid);

                            if (u != null) {
                                targets.add(u);
                            }
                        }
                    }
                }

                break;
            }
            case ClosestUnspawnedGameobject: {
                var target = baseObject.findNearestUnspawnedGameObject(e.target.goClosest.entry, (float)(e.target.goClosest.dist != 0 ? e.target.goClosest.dist : 100));

                if (target != null) {
                    targets.add(target);
                }

                break;
            }
            case Position:
            default:
                break;
        }

        return targets;
    }

    private ArrayList<WorldObject> getWorldObjectsInDist(float dist) {
        ArrayList<WorldObject> targets = new ArrayList<WorldObject>();
        var obj = getBaseObject();

        if (obj == null) {
            return targets;
        }

        var uCheck = new AllWorldObjectsInRange(obj, dist);
        var searcher = new WorldObjectListSearcher(obj, targets, uCheck);
        Cell.visitGrid(obj, searcher, dist);

        return targets;
    }


    private void processEvent(SmartScriptHolder e, Unit unit, int var0, int var1, boolean bvar, SpellInfo spell, GameObject gob) {
        processEvent(e, unit, var0, var1, bvar, spell, gob, "");
    }

    private void processEvent(SmartScriptHolder e, Unit unit, int var0, int var1, boolean bvar, SpellInfo spell) {
        processEvent(e, unit, var0, var1, bvar, spell, null, "");
    }

    private void processEvent(SmartScriptHolder e, Unit unit, int var0, int var1, boolean bvar) {
        processEvent(e, unit, var0, var1, bvar, null, null, "");
    }

    private void processEvent(SmartScriptHolder e, Unit unit, int var0, int var1) {
        processEvent(e, unit, var0, var1, false, null, null, "");
    }

    private void processEvent(SmartScriptHolder e, Unit unit, int var0) {
        processEvent(e, unit, var0, 0, false, null, null, "");
    }

    private void processEvent(SmartScriptHolder e, Unit unit) {
        processEvent(e, unit, 0, 0, false, null, null, "");
    }

    private void processEvent(SmartScriptHolder e) {
        processEvent(e, null, 0, 0, false, null, null, "");
    }

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: void ProcessEvent(SmartScriptHolder e, Unit unit = null, uint var0 = 0, uint var1 = 0, bool bvar = false, SpellInfo spell = null, GameObject gob = null, string varString = "")
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
    private void processEvent(SmartScriptHolder e, Unit unit, int var0, int var1, boolean bvar, SpellInfo spell, GameObject gob, String varString) {
        if (!e.active && e.getEventType() != SmartEvents.Link) {
            return;
        }

        if ((e.event.eventPhaseMask != 0 && !isInPhase(e.event.eventPhaseMask)) || (e.event.eventFlags.HasAnyFlag(SmartEventFlags.NotRepeatable) && e.runOnce)) {
            return;
        }

        if (!e.event.eventFlags.HasAnyFlag(SmartEventFlags.WhileCharmed) && isCharmedCreature(me)) {
            return;
        }

        switch (e.getEventType()) {
            case Link: //special handling
                processAction(e, unit, var0, var1, bvar, spell, gob);

                break;
            //called from Update tick
            case Update:
                processTimedAction(e, e.event.minMaxRepeat.repeatMin, e.event.minMaxRepeat.repeatMax);

                break;
            case UpdateOoc:
                if (me != null && me.isEngaged()) {
                    return;
                }

                processTimedAction(e, e.event.minMaxRepeat.repeatMin, e.event.minMaxRepeat.repeatMax);

                break;
            case UpdateIc:
                if (me == null || !me.isEngaged()) {
                    return;
                }

                processTimedAction(e, e.event.minMaxRepeat.repeatMin, e.event.minMaxRepeat.repeatMax);

                break;
            case HealthPct: {
                if (me == null || !me.isEngaged() || me.getMaxHealth() == 0) {
                    return;
                }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: var perc = (uint)_me.HealthPct;
                var perc = (int)me.getHealthPct();

                if (perc > e.event.minMaxRepeat.max || perc < e.event.minMaxRepeat.min) {
                    return;
                }

                processTimedAction(e, e.event.minMaxRepeat.repeatMin, e.event.minMaxRepeat.repeatMax);

                break;
            }
            case ManaPct: {
                if (me == null || !me.isEngaged() || me.getMaxPower(PowerType.Mana) == 0) {
                    return;
                }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: var perc = (uint)_me.GetPowerPct(PowerType.Mana);
                var perc = (int)me.getPowerPct(PowerType.Mana);

                if (perc > e.event.minMaxRepeat.max || perc < e.event.minMaxRepeat.min) {
                    return;
                }

                processTimedAction(e, e.event.minMaxRepeat.repeatMin, e.event.minMaxRepeat.repeatMax);

                break;
            }
            case Range: {
                if (me == null || !me.isEngaged() || me.getVictim() == null) {
                    return;
                }

                if (me.isInRange(me.getVictim(), e.event.minMaxRepeat.min, e.event.minMaxRepeat.max)) {
                    processTimedAction(e, e.event.minMaxRepeat.repeatMin, e.event.minMaxRepeat.repeatMax, me.getVictim());
                }
                else { // make it predictable
                    recalcTimer(e, 500, 500);
                }

                break;
            }
            case VictimCasting: {
                if (me == null || !me.isEngaged()) {
                    return;
                }

                var victim = me.getVictim();

                if (victim == null || !victim.isNonMeleeSpellCast(false, false, true)) {
                    return;
                }

                if (e.event.targetCasting.spellId > 0) {
                    var currSpell = victim.getCurrentSpell(CurrentSpellTypes.Generic);

                    if (currSpell != null) {
                        if (currSpell.spellInfo.id != e.event.targetCasting.spellId) {
                            return;
                        }
                    }
                }

                processTimedAction(e, e.event.targetCasting.repeatMin, e.event.targetCasting.repeatMax, me.getVictim());

                break;
            }
            case FriendlyIsCc: {
                if (me == null || !me.isEngaged()) {
                    return;
                }

                ArrayList<Creature> creatures = new ArrayList<Creature>();
                doFindFriendlyCC(creatures, e.event.friendlyCC.radius);

                if (creatures.Empty()) {
                    // if there are at least two same npcs, they will perform the same action immediately even if this is useless...
                    recalcTimer(e, 1000, 3000);

                    return;
                }

                processTimedAction(e, e.event.friendlyCC.repeatMin, e.event.friendlyCC.repeatMax, creatures.get(0));

                break;
            }
            case FriendlyMissingBuff: {
                ArrayList<Creature> creatures = new ArrayList<Creature>();
                doFindFriendlyMissingBuff(creatures, e.event.missingBuff.radius, e.event.missingBuff.spell);

                if (creatures.Empty()) {
                    return;
                }

                processTimedAction(e, e.event.missingBuff.repeatMin, e.event.missingBuff.repeatMax, creatures.SelectRandom());

                break;
            }
            case HasAura: {
                if (me == null) {
                    return;
                }

                var count = me.getAuraCount(e.event.aura.spell);

                if ((e.event.aura.count == 0 && count == 0) || (e.event.aura.count != 0 && count >= e.event.aura.count)) {
                    processTimedAction(e, e.event.aura.repeatMin, e.event.aura.repeatMax);
                }

                break;
            }
            case TargetBuffed: {
                if (me == null || me.getVictim() == null) {
                    return;
                }

                var count = me.getVictim().getAuraCount(e.event.aura.spell);

                if (count < e.event.aura.count) {
                    return;
                }

                processTimedAction(e, e.event.aura.repeatMin, e.event.aura.repeatMax, me.getVictim());

                break;
            }
            case Charmed: {
                if (bvar == (e.event.charm.onRemove != 1)) {
                    processAction(e, unit, var0, var1, bvar, spell, gob);
                }

                break;
            }
            case QuestAccepted:
            case QuestCompletion:
            case QuestFail:
            case QuestRewarded: {
                processAction(e, unit);

                break;
            }
            case QuestObjCompletion: {
                if (var0 == (e.event.questObjective.id)) {
                    processAction(e, unit);
                }

                break;
            }
            //no params
            case Aggro:
            case Death:
            case Evade:
            case ReachedHome:
            case CorpseRemoved:
            case AiInit:
            case TransportAddplayer:
            case TransportRemovePlayer:
            case JustSummoned:
            case Reset:
            case JustCreated:
            case FollowCompleted:
            case OnSpellclick:
            case OnDespawn:
                processAction(e, unit, var0, var1, bvar, spell, gob);

                break;
            case GossipHello: {
                switch (e.event.gossipHello.filter) {
                    case 0:
                        // no filter set, always execute action
                        break;
                    case 1:
                        // OnGossipHello only filter set, skip action if OnReportUse
                        if (var0 != 0) {
                            return;
                        }

                        break;
                    case 2:
                        // OnReportUse only filter set, skip action if OnGossipHello
                        if (var0 == 0) {
                            return;
                        }

                        break;
                    default:
                        // Ignore any other value
                        break;
                }

                processAction(e, unit, var0, var1, bvar, spell, gob);

                break;
            }
            case ReceiveEmote:
                if (e.event.emote.emoteId == var0) {
                    recalcTimer(e, e.event.emote.cooldownMin, e.event.emote.cooldownMax);
                    processAction(e, unit);
                }

                break;
            case Kill: {
                if (me == null || unit == null) {
                    return;
                }

                if (e.event.kill.playerOnly != 0 && !unit.isTypeId(TypeId.Player)) {
                    return;
                }

                if (e.event.kill.creature != 0 && unit.getEntry() != e.event.kill.creature) {
                    return;
                }

                recalcTimer(e, e.event.kill.cooldownMin, e.event.kill.cooldownMax);
                processAction(e, unit);

                break;
            }
            case SpellHitTarget:
            case SpellHit: {
                if (spell == null) {
                    return;
                }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: if ((e.Event.spellHit.spell == 0 || spell.Id == e.Event.spellHit.spell) && (e.Event.spellHit.school == 0 || Convert.ToBoolean((uint)spell.SchoolMask & e.Event.spellHit.school)))
                if ((e.event.spellHit.spell == 0 || spell.id == e.event.spellHit.spell) && (e.event.spellHit.school == 0 || (boolean)((int)spell.schoolMask.getValue() & e.event.spellHit.school))) {
                    recalcTimer(e, e.event.spellHit.cooldownMin, e.event.spellHit.cooldownMax);
                    processAction(e, unit, 0, 0, bvar, spell, gob);
                }

                break;
            }
            case OnSpellCast:
            case OnSpellFailed:
            case OnSpellStart: {
                if (spell == null) {
                    return;
                }

                if (spell.id != e.event.spellCast.spell) {
                    return;
                }

                recalcTimer(e, e.event.spellCast.cooldownMin, e.event.spellCast.cooldownMax);
                processAction(e, null, 0, 0, bvar, spell);

                break;
            }
            case OocLos: {
                if (me == null || me.isEngaged()) {
                    return;
                }

                //can trigger if closer than fMaxAllowedRange
                float range = e.event.los.maxDist;

                //if range is ok and we are actually in LOS
                if (me.isWithinDistInMap(unit, range) && me.isWithinLOSInMap(unit)) {
                    var hostilityMode = LOSHostilityMode.forValue(e.event.los.hostilityMode);

                    //if friendly event&&who is not hostile OR hostile event&&who is hostile
                    if ((hostilityMode == LOSHostilityMode.Any) || (hostilityMode == LOSHostilityMode.NotHostile && !me.isHostileTo(unit)) || (hostilityMode == LOSHostilityMode.Hostile && me.isHostileTo(unit))) {
                        if (e.event.los.playerOnly != 0 && !unit.isTypeId(TypeId.Player)) {
                            return;
                        }

                        recalcTimer(e, e.event.los.cooldownMin, e.event.los.cooldownMax);
                        processAction(e, unit);
                    }
                }

                break;
            }
            case IcLos: {
                if (me == null || !me.isEngaged()) {
                    return;
                }

                //can trigger if closer than fMaxAllowedRange
                float range = e.event.los.maxDist;

                //if range is ok and we are actually in LOS
                if (me.isWithinDistInMap(unit, range) && me.isWithinLOSInMap(unit)) {
                    var hostilityMode = LOSHostilityMode.forValue(e.event.los.hostilityMode);

                    //if friendly event&&who is not hostile OR hostile event&&who is hostile
                    if ((hostilityMode == LOSHostilityMode.Any) || (hostilityMode == LOSHostilityMode.NotHostile && !me.isHostileTo(unit)) || (hostilityMode == LOSHostilityMode.Hostile && me.isHostileTo(unit))) {
                        if (e.event.los.playerOnly != 0 && !unit.isTypeId(TypeId.Player)) {
                            return;
                        }

                        recalcTimer(e, e.event.los.cooldownMin, e.event.los.cooldownMax);
                        processAction(e, unit);
                    }
                }

                break;
            }
            case Respawn: {
                if (getBaseObject() == null) {
                    return;
                }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: if (e.Event.respawn.type == (uint)SmartRespawnCondition.Map && GetBaseObject().Location.MapId != e.Event.respawn.map)
                if (e.event.respawn.type == (int)SmartRespawnCondition.Map.getValue() && getBaseObject().location.mapId != e.event.respawn.map) {
                    return;
                }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: if (e.Event.respawn.type == (uint)SmartRespawnCondition.Area && GetBaseObject().Zone != e.Event.respawn.area)
                if (e.event.respawn.type == (int)SmartRespawnCondition.Area.getValue() && getBaseObject().getZone() != e.event.respawn.area) {
                    return;
                }

                processAction(e);

                break;
            }
            case SummonedUnit:
            case SummonedUnitDies: {
                if (!isCreature(unit)) {
                    return;
                }

                if (e.event.summoned.creature != 0 && unit.getEntry() != e.event.summoned.creature) {
                    return;
                }

                recalcTimer(e, e.event.summoned.cooldownMin, e.event.summoned.cooldownMax);
                processAction(e, unit);

                break;
            }
            case ReceiveHeal:
            case Damaged:
            case DamagedTarget: {
                if (var0 > e.event.minMaxRepeat.max || var0 < e.event.minMaxRepeat.min) {
                    return;
                }

                recalcTimer(e, e.event.minMaxRepeat.repeatMin, e.event.minMaxRepeat.repeatMax);
                processAction(e, unit);

                break;
            }
            case Movementinform: {
                if ((e.event.movementInform.type != 0 && var0 != e.event.movementInform.type) || (e.event.movementInform.id != 0xFFFFFFFF && var1 != e.event.movementInform.id)) {
                    return;
                }

                processAction(e, unit, var0, var1);

                break;
            }
            case TransportRelocate: {
                if (e.event.transportRelocate.pointID != 0 && var0 != e.event.transportRelocate.pointID) {
                    return;
                }

                processAction(e, unit, var0);

                break;
            }
            case WaypointReached:
            case WaypointResumed:
            case WaypointPaused:
            case WaypointStopped:
            case WaypointEnded: {
                if (me == null || (e.event.waypoint.pointID != 0 && var0 != e.event.waypoint.pointID) || (e.event.waypoint.pathID != 0 && var1 != e.event.waypoint.pathID)) {
                    return;
                }

                processAction(e, unit);

                break;
            }
            case SummonDespawned: {
                if (e.event.summoned.creature != 0 && e.event.summoned.creature != var0) {
                    return;
                }

                recalcTimer(e, e.event.summoned.cooldownMin, e.event.summoned.cooldownMax);
                processAction(e, unit, var0);

                break;
            }
            case InstancePlayerEnter: {
                if (e.event.instancePlayerEnter.team != 0 && var0 != e.event.instancePlayerEnter.team) {
                    return;
                }

                recalcTimer(e, e.event.instancePlayerEnter.cooldownMin, e.event.instancePlayerEnter.cooldownMax);
                processAction(e, unit, var0);

                break;
            }
            case AcceptedQuest:
            case RewardQuest: {
                if (e.event.quest.questId != 0 && var0 != e.event.quest.questId) {
                    return;
                }

                recalcTimer(e, e.event.quest.cooldownMin, e.event.quest.cooldownMax);
                processAction(e, unit, var0);

                break;
            }
            case TransportAddcreature: {
                if (e.event.transportAddCreature.creature != 0 && var0 != e.event.transportAddCreature.creature) {
                    return;
                }

                processAction(e, unit, var0);

                break;
            }
            case AreatriggerOntrigger: {
                if (e.event.areatrigger.id != 0 && var0 != e.event.areatrigger.id) {
                    return;
                }

                processAction(e, unit, var0);

                break;
            }
            case TextOver: {
                if (var0 != e.event.textOver.textGroupID || (e.event.textOver.creatureEntry != 0 && e.event.textOver.creatureEntry != var1)) {
                    return;
                }

                processAction(e, unit, var0);

                break;
            }
            case DataSet: {
                if (e.event.dataSet.id != var0 || e.event.dataSet.value != var1) {
                    return;
                }

                recalcTimer(e, e.event.dataSet.cooldownMin, e.event.dataSet.cooldownMax);
                processAction(e, unit, var0, var1);

                break;
            }
            case PassengerRemoved:
            case PassengerBoarded: {
                if (unit == null) {
                    return;
                }

                recalcTimer(e, e.event.minMax.repeatMin, e.event.minMax.repeatMax);
                processAction(e, unit);

                break;
            }
            case TimedEventTriggered: {
                if (e.event.timedEvent.id == var0) {
                    processAction(e, unit);
                }

                break;
            }
            case GossipSelect: {
                Log.outDebug(LogFilter.ScriptsAi, "SmartScript: Gossip Select:  menu {0} action {1}", var0, var1); //little help for scripters

                if (e.event.gossip.sender != var0 || e.event.gossip.action != var1) {
                    return;
                }

                processAction(e, unit, var0, var1);

                break;
            }
            case GameEventStart:
            case GameEventEnd: {
                if (e.event.gameEvent.gameEventId != var0) {
                    return;
                }

                processAction(e, null, var0);

                break;
            }
            case GoLootStateChanged: {
                if (e.event.goLootStateChanged.lootState != var0) {
                    return;
                }

                processAction(e, unit, var0, var1);

                break;
            }
            case GoEventInform: {
                if (e.event.eventInform.eventId != var0) {
                    return;
                }

                processAction(e, null, var0);

                break;
            }
            case ActionDone: {
                if (e.event.doAction.eventId != var0) {
                    return;
                }

                processAction(e, unit, var0);

                break;
            }
            case FriendlyHealthPCT: {
                if (me == null || !me.isEngaged()) {
                    return;
                }

                Unit unitTarget = null;

                switch (e.getTargetType()) {
                    case CreatureRange:
                    case CreatureGuid:
                    case CreatureDistance:
                    case ClosestCreature:
                    case ClosestPlayer:
                    case PlayerRange:
                    case PlayerDistance: {
                        var targets = getTargets(e);

                        for (var target : targets) {
                            if (isUnit(target) && me.isFriendlyTo(target.getAsUnit()) && target.getAsUnit().isAlive() && target.getAsUnit().isInCombat()) {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: var healthPct = (uint)target.AsUnit.HealthPct;
                                var healthPct = (int)target.getAsUnit().getHealthPct();

                                if (healthPct > e.event.friendlyHealthPct.maxHpPct || healthPct < e.event.friendlyHealthPct.minHpPct) {
                                    continue;
                                }

                                unitTarget = target.getAsUnit();

                                break;
                            }
                        }
                    }

                        break;
                    case ActionInvoker:
                        unitTarget = doSelectLowestHpPercentFriendly((float)e.event.friendlyHealthPct.radius, e.event.friendlyHealthPct.minHpPct, e.event.friendlyHealthPct.maxHpPct);

                        break;
                    default:
                        return;
                }

                if (unitTarget == null) {
                    return;
                }

                processTimedAction(e, e.event.friendlyHealthPct.repeatMin, e.event.friendlyHealthPct.repeatMax, unitTarget);

                break;
            }
            case DistanceCreature: {
                if (!me) {
                    return;
                }

                Creature creature = null;

                if (e.event.distance.guid != 0) {
                    creature = findCreatureNear(me, e.event.distance.guid);

                    if (!creature) {
                        return;
                    }

                    if (!me.isInRange(creature, 0, e.event.distance.dist)) {
                        return;
                    }
                } else if (e.event.distance.entry != 0) {
                    var list = me.getCreatureListWithEntryInGrid(e.event.distance.entry, e.event.distance.dist);

                    if (!list.Empty()) {
                        creature = list.FirstOrDefault();
                    }
                }

                if (creature) {
                    processTimedAction(e, e.event.distance.repeat, e.event.distance.repeat, creature);
                }

                break;
            }
            case DistanceGameobject: {
                if (!me) {
                    return;
                }

                GameObject gameobject = null;

                if (e.event.distance.guid != 0) {
                    gameobject = findGameObjectNear(me, e.event.distance.guid);

                    if (!gameobject) {
                        return;
                    }

                    if (!me.isInRange(gameobject, 0, e.event.distance.dist)) {
                        return;
                    }
                } else if (e.event.distance.entry != 0) {
                    var list = me.getGameObjectListWithEntryInGrid(e.event.distance.entry, e.event.distance.dist);

                    if (!list.Empty()) {
                        gameobject = list.FirstOrDefault();
                    }
                }

                if (gameobject) {
                    processTimedAction(e, e.event.distance.repeat, e.event.distance.repeat, null, 0, 0, false, null, gameobject);
                }

                break;
            }
            case CounterSet:
                if (e.event.counter.id != var0 || getCounterValue(e.event.counter.id) != e.event.counter.value) {
                    return;
                }

                processTimedAction(e, e.event.counter.cooldownMin, e.event.counter.cooldownMax);

                break;
            case SceneStart:
            case SceneCancel:
            case SceneComplete: {
                processAction(e, unit);

                break;
            }
            case SceneTrigger: {
                if (!e.event.paramString.equals(varString)) {
                    return;
                }

                processAction(e, unit, var0, 0, false, null, null, varString);

                break;
            }
            default:
                Log.outError(LogFilter.Sql, "SmartScript.ProcessEvent: Unhandled Event type {0}", e.getEventType());

                break;
        }
    }

    private void initTimer(SmartScriptHolder e) {
        switch (e.getEventType()) {
            //set only events which have initial timers
            case Update:
            case UpdateIc:
            case UpdateOoc:
                recalcTimer(e, e.event.minMaxRepeat.min, e.event.minMaxRepeat.max);

                break;
            case DistanceCreature:
            case DistanceGameobject:
                recalcTimer(e, e.event.distance.repeat, e.event.distance.repeat);

                break;
            default:
                e.active = true;

                break;
        }
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: void RecalcTimer(SmartScriptHolder e, uint min, uint max)
    private void recalcTimer(SmartScriptHolder e, int min, int max) {
        if (e.entryOrGuid == 15294 && e.timer != 0) {
            Log.outError(LogFilter.Server, "Called RecalcTimer");
        }

        // min/max was checked at loading!
        e.timer = RandomHelper.URand(min, max);
        e.active = e.timer == 0;
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: void UpdateTimer(SmartScriptHolder e, uint diff)
    private void updateTimer(SmartScriptHolder e, int diff) {
        if (e.getEventType() == SmartEvents.Link) {
            return;
        }

        if (e.event.eventPhaseMask != 0 && !isInPhase(e.event.eventPhaseMask)) {
            return;
        }

        if (e.getEventType() == SmartEvents.UpdateIc && (me == null || !me.isEngaged())) {
            return;
        }

        if (e.getEventType() == SmartEvents.UpdateOoc && (me != null && me.isEngaged())) { //can be used with me=NULL (go script)
            return;
        }

        if (e.timer < diff) {
            // delay spell cast event if another spell is being casted
            if (e.getActionType() == SmartActions.Cast) {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: if (!Convert.ToBoolean(e.Action.cast.castFlags & (uint)SmartCastFlags.InterruptPrevious))
                if (!(boolean)(e.action.cast.castFlags & (int)SmartCastFlags.InterruptPrevious.getValue())) {
                    if (me != null && me.hasUnitState(UnitState.Casting)) {
                        raisePriority(e);

                        return;
                    }
                }
            }

            // Delay flee for assist event if stunned or rooted
            if (e.getActionType() == SmartActions.FleeForAssist) {
                if (me && me.hasUnitState(UnitState.Root.getValue() | UnitState.LostControl.getValue())) {
                    e.timer = 1;

                    return;
                }
            }

            e.active = true; //activate events with cooldown

            switch (e.getEventType()) { //process ONLY timed events
                case Update:
                case UpdateIc:
                case UpdateOoc:
                case HealthPct:
                case ManaPct:
                case Range:
                case VictimCasting:
                case FriendlyIsCc:
                case FriendlyMissingBuff:
                case HasAura:
                case TargetBuffed:
                case FriendlyHealthPCT:
                case DistanceCreature:
                case DistanceGameobject: {
                    if (e.getScriptType() == SmartScriptType.TimedActionlist) {
                        Unit invoker = null;

                        if (me != null && !mTimedActionListInvoker.isEmpty()) {
                            invoker = Global.getObjAccessor().getUnit(me, mTimedActionListInvoker.clone());
                        }

                        processEvent(e, invoker);
                        e.enableTimed = false; //disable event if it is in an ActionList and was processed once

                        for (var holder : timedActionList) {
                            //find the first event which is not the current one and enable it
                            if (holder.eventId > e.eventId) {
                                holder.enableTimed = true;

                                break;
                            }
                        }
                    } else {
                        processEvent(e);
                    }

                    break;
                }
            }

            if (e.priority != SmartScriptHolder.DEFAULT_PRIORITY) {
                // Reset priority to default one only if the event hasn't been rescheduled again to next loop
                if (e.timer > 1) {
                    // Re-sort events if this was moved to the top of the queue
                    eventSortingRequired = true;
                    // Reset priority to default one
                    e.priority = SmartScriptHolder.DEFAULT_PRIORITY;
                }
            }
        } else {
            e.timer -= diff;

            if (e.entryOrGuid == 15294 && me.getGUID().getCounter() == 55039 && e.timer != 0) {
                Log.outError(LogFilter.Server, "Called UpdateTimer: reduce timer: e.timer: {0}, diff: {1}  current time: {2}", e.timer, diff, Time.getMSTime());
            }
        }
    }

    private void installEvents() {
        if (!installEvents.Empty()) {
            synchronized (events) {
                for (var holder : installEvents) {
                    events.add(holder); //must be before UpdateTimers
                }
            }

            installEvents.clear();
        }
    }

    private void sortEvents(ArrayList<SmartScriptHolder> events) {
        Collections.sort(events);
    }

    private void raisePriority(SmartScriptHolder e) {
        e.timer = 1;

        // Change priority only if it's set to default, otherwise keep the current order of events
        if (e.priority == SmartScriptHolder.DEFAULT_PRIORITY) {
            e.priority = currentPriority++;
            eventSortingRequired = true;
        }
    }


    private void retryLater(SmartScriptHolder e) {
        retryLater(e, false);
    }

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: void RetryLater(SmartScriptHolder e, bool ignoreChanceRoll = false)
    private void retryLater(SmartScriptHolder e, boolean ignoreChanceRoll) {
        raisePriority(e);

        // This allows to retry the action later without rolling again the chance roll (which might fail and end up not executing the action)
        if (ignoreChanceRoll) {
            e.event.eventFlags = SmartEventFlags.forValue(e.event.eventFlags.getValue() | SmartEventFlags.TempIgnoreChanceRoll.getValue());
        }

        e.runOnce = false;
    }

    private void fillScript(ArrayList<SmartScriptHolder> e, WorldObject obj, AreaTriggerRecord at, SceneTemplate scene, Quest quest) {
        if (e.Empty()) {
            if (obj != null) {
                Log.outDebug(LogFilter.ScriptsAi, String.format("SmartScript: EventMap for Entry %1$s is empty but is using SmartScript.", obj.getEntry()));
            }

            if (at != null) {
                Log.outDebug(LogFilter.ScriptsAi, String.format("SmartScript: EventMap for AreaTrigger %1$s is empty but is using SmartScript.", at.id));
            }

            if (scene != null) {
                Log.outDebug(LogFilter.ScriptsAi, String.format("SmartScript: EventMap for SceneId %1$s is empty but is using SmartScript.", scene.sceneId));
            }

            if (quest != null) {
                Log.outDebug(LogFilter.ScriptsAi, String.format("SmartScript: EventMap for Quest %1$s is empty but is using SmartScript.", quest.id));
            }

            return;
        }

        for (var holder : e) {
            if (holder.event.eventFlags.HasAnyFlag(SmartEventFlags.DifficultyAll)) { //if has instance flag add only if in it
                if (!(obj != null && obj.getMap().isDungeon())) {
                    continue;
                }

                // TODO: fix it for new maps and difficulties
                switch (obj.getMap().getDifficultyID()) {
                    case Normal:
                    case Raid10N:
                        if (holder.event.eventFlags.HasAnyFlag(SmartEventFlags.Difficulty0)) {
                            synchronized (events) {
                                events.add(holder);
                            }
                        }

                        break;
                    case Heroic:
                    case Raid25N:
                        if (holder.event.eventFlags.HasAnyFlag(SmartEventFlags.Difficulty1)) {
                            synchronized (events) {
                                events.add(holder);
                            }
                        }

                        break;
                    case Raid10HC:
                        if (holder.event.eventFlags.HasAnyFlag(SmartEventFlags.Difficulty2)) {
                            synchronized (events) {
                                events.add(holder);
                            }
                        }

                        break;
                    case Raid25HC:
                        if (holder.event.eventFlags.HasAnyFlag(SmartEventFlags.Difficulty3)) {
                            synchronized (events) {
                                events.add(holder);
                            }
                        }

                        break;
                    default:
                        break;
                }
            }

            allEventFlags = SmartEventFlags.forValue(allEventFlags.getValue() | holder.event.eventFlags.getValue());

            synchronized (events) {
                events.add(holder); //NOTE: 'world(0)' events still get processed in ANY instance mode
            }
        }
    }

    private void getScript() {
        ArrayList<SmartScriptHolder> e;

        if (me != null) {
            e = Global.getSmartAIMgr().getScript(-((int)me.spawnId), scriptType);

            if (e.Empty()) {
                e = Global.getSmartAIMgr().getScript((int)me.getEntry(), scriptType);
            }

            fillScript(e, me, null, null, null);
        } else if (go != null) {
            e = Global.getSmartAIMgr().getScript(-((int)go.getSpawnId()), scriptType);

            if (e.Empty()) {
                e = Global.getSmartAIMgr().getScript((int)go.getEntry(), scriptType);
            }

            fillScript(e, go, null, null, null);
        } else if (trigger != null) {
            e = Global.getSmartAIMgr().getScript((int)trigger.id, scriptType);
            fillScript(e, null, trigger, null, null);
        } else if (areaTrigger != null) {
            e = Global.getSmartAIMgr().getScript((int)areaTrigger.getEntry(), scriptType);
            fillScript(e, areaTrigger, null, null, null);
        } else if (sceneTemplate != null) {
            e = Global.getSmartAIMgr().getScript((int)sceneTemplate.sceneId, scriptType);
            fillScript(e, null, null, sceneTemplate, null);
        } else if (quest != null) {
            e = Global.getSmartAIMgr().getScript((int)quest.id, scriptType);
            fillScript(e, null, null, null, quest);
        }
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: Unit DoSelectLowestHpFriendly(float range, uint MinHPDiff)
    private Unit doSelectLowestHpFriendly(float range, int minHPDiff) {
        if (!me) {
            return null;
        }

        var uCheck = new MostHPMissingInRange<Unit>(me, range, minHPDiff);
        var searcher = new UnitLastSearcher(me, uCheck, GridType.Grid);
        Cell.visitGrid(me, searcher, range);

        return searcher.getTarget();
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: Unit DoSelectLowestHpPercentFriendly(float range, uint minHpPct, uint maxHpPct)
    private Unit doSelectLowestHpPercentFriendly(float range, int minHpPct, int maxHpPct) {
        if (me == null) {
            return null;
        }

        MostHPPercentMissingInRange uCheck = new MostHPPercentMissingInRange(me, range, minHpPct, maxHpPct);
        UnitLastSearcher searcher = new UnitLastSearcher(me, uCheck, GridType.Grid);
        Cell.visitGrid(me, searcher, range);

        return searcher.getTarget();
    }

    private void doFindFriendlyCC(ArrayList<Creature> creatures, float range) {
        if (me == null) {
            return;
        }

        var uCheck = new FriendlyCCedInRange(me, range);
        var searcher = new CreatureListSearcher(me, creatures, uCheck, GridType.Grid);
        Cell.visitGrid(me, searcher, range);
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: void DoFindFriendlyMissingBuff(List<Creature> creatures, float range, uint spellid)
    private void doFindFriendlyMissingBuff(ArrayList<Creature> creatures, float range, int spellid) {
        if (me == null) {
            return;
        }

        var uCheck = new FriendlyMissingBuffInRange(me, range, spellid);
        var searcher = new CreatureListSearcher(me, creatures, uCheck, GridType.Grid);
        Cell.visitGrid(me, searcher, range);
    }

    private Unit doFindClosestFriendlyInRange(float range) {
        if (!me) {
            return null;
        }

        var uCheck = new AnyFriendlyUnitInObjectRangeCheck(me, me, range);
        var searcher = new UnitLastSearcher(me, uCheck, GridType.All);
        Cell.visitGrid(me, searcher, range);

        return searcher.getTarget();
    }


    private Unit getLastInvoker() {
        return getLastInvoker(null);
    }

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: Unit GetLastInvoker(Unit invoker = null)
    private Unit getLastInvoker(Unit invoker) {
        // Look for invoker only on map of base object... Prevents multithreaded crashes
        var baseObject = getBaseObject();

        if (baseObject != null) {
            return Global.getObjAccessor().getUnit(baseObject, lastInvoker.clone());
        }
        // used for area triggers invoker cast
        else if (invoker != null) {
            return Global.getObjAccessor().getUnit(invoker, lastInvoker.clone());
        }

        return null;
    }

    private WorldObject getBaseObject() {
        WorldObject obj = null;

        if (me != null) {
            obj = me;
        } else if (go != null) {
            obj = go;
        } else if (areaTrigger != null) {
            obj = areaTrigger;
        } else if (player != null) {
            obj = player;
        }

        return obj;
    }

    private WorldObject getBaseObjectOrUnitInvoker(Unit invoker) {
        return getBaseObject() != null ? getBaseObject() : invoker;
    }


    private boolean isSmart(Creature creature) {
        return isSmart(creature, false);
    }

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: bool IsSmart(Creature creature, bool silent = false)
    private boolean isSmart(Creature creature, boolean silent) {
        if (creature == null) {
            return false;
        }

        var smart = true;

        if (creature.<SmartAI>getAI() == null) {
            smart = false;
        }

        if (!smart && !silent) {
            Log.outError(LogFilter.Sql, "SmartScript: Action target Creature (GUID: {0} Entry: {1}) is not using SmartAI, action skipped to prevent crash.", creature != null ? creature.spawnId : (me != null ? me.spawnId : 0), creature != null ? creature.getEntry() : (me != null ? me.getEntry() : 0));
        }

        return smart;
    }


    private boolean isSmart(GameObject gameObject) {
        return isSmart(gameObject, false);
    }

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: bool IsSmart(GameObject gameObject, bool silent = false)
    private boolean isSmart(GameObject gameObject, boolean silent) {
        if (gameObject == null) {
            return false;
        }

        var smart = true;

        if (gameObject.<SmartGameObjectAI>getAI() == null) {
            smart = false;
        }

        if (!smart && !silent) {
            Log.outError(LogFilter.Sql, "SmartScript: Action target GameObject (GUID: {0} Entry: {1}) is not using SmartGameObjectAI, action skipped to prevent crash.", gameObject != null ? gameObject.getSpawnId() : (go != null ? go.getSpawnId() : 0), gameObject != null ? gameObject.getEntry() : (go != null ? go.getEntry() : 0));
        }

        return smart;
    }


    private boolean isSmart() {
        return isSmart(false);
    }

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: bool IsSmart(bool silent = false)
    private boolean isSmart(boolean silent) {
        if (me != null) {
            return isSmart(me, silent);
        }

        if (go != null) {
            return isSmart(go, silent);
        }

        return false;
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: void StoreTargetList(List<WorldObject> targets, uint id)
    private void storeTargetList(ArrayList<WorldObject> targets, int id) {
        // insert or replace
        storedTargets.remove(id);
        storedTargets.put(id, new ObjectGuidList(targets));
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: void AddToStoredTargetList(List<WorldObject> targets, uint id)
    private void addToStoredTargetList(ArrayList<WorldObject> targets, int id) {
        var inserted = storedTargets.TryAdd(id, new ObjectGuidList(targets));

        if (!inserted) {
            for (var obj : targets) {
                storedTargets.get(id).addGuid(obj.getGUID().clone());
            }
        }
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: void StoreCounter(uint id, uint value, uint reset)
    private void storeCounter(int id, int value, int reset) {
        if (counterList.containsKey(id)) {
            if (reset == 0) {
                counterList.put(id, counterList.get(id) + value);
            } else {
                counterList.put(id, value);
            }
        } else {
            counterList.put(id, value);
        }

        processEventsFor(SmartEvents.CounterSet, null, id);
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: uint GetCounterValue(uint id)
    private int getCounterValue(int id) {
        if (counterList.containsKey(id)) {
            return counterList.get(id);
        }

        return 0;
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: GameObject FindGameObjectNear(WorldObject searchObject, ulong guid)
    private GameObject findGameObjectNear(WorldObject searchObject, long guid) {
        var bounds = searchObject.getMap().getGameObjectBySpawnIdStore().LookupByKey(guid);

        if (bounds.Empty()) {
            return null;
        }

        return bounds[0];
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: Creature FindCreatureNear(WorldObject searchObject, ulong guid)
    private Creature findCreatureNear(WorldObject searchObject, long guid) {
        var bounds = searchObject.getMap().getCreatureBySpawnIdStore().LookupByKey(guid);

        if (bounds.Empty()) {
            return null;
        }

        var foundCreature = tangible.ListHelper.find(bounds, creature -> creature.IsAlive);

        return foundCreature != null ? foundCreature : bounds[0];
    }

    private void resetBaseObject() {
        WorldObject lookupRoot = me;

        if (!lookupRoot) {
            lookupRoot = go;
        }

        if (lookupRoot) {
            if (!meOrigGUID.isEmpty()) {
                var m = ObjectAccessor.getCreature(lookupRoot, meOrigGUID.clone());

                if (m != null) {
                    me = m;
                    go = null;
                    areaTrigger = null;
                }
            }

            if (!goOrigGUID.isEmpty()) {
                var o = ObjectAccessor.getGameObject(lookupRoot, goOrigGUID.clone());

                if (o != null) {
                    me = null;
                    go = o;
                    areaTrigger = null;
                }
            }
        }

        goOrigGUID.clear();
        meOrigGUID.clear();
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: void IncPhase(uint p)
    private void incPhase(int p) {
        // protect phase from overflowing
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: SetPhase(Math.Min((uint)SmartPhase.Phase12, _eventPhase + p));
        setPhase(Math.min((int)SmartPhase.Phase12.getValue(), eventPhase + p));
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: void DecPhase(uint p)
    private void decPhase(int p) {
        if (p >= eventPhase) {
            setPhase(0);
        } else {
            setPhase(eventPhase - p);
        }
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: void SetPhase(uint p)
    private void setPhase(int p) {
        eventPhase = p;
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: bool IsInPhase(uint p)
    private boolean isInPhase(int p) {
        if (eventPhase == 0) {
            return false;
        }

        return ((1 << (int)(eventPhase - 1)) & p) != 0;
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: void RemoveStoredEvent(uint id)
    private void removeStoredEvent(int id) {
        if (!storedEvents.Empty()) {
            for (var holder : storedEvents) {
                if (holder.eventId == id) {
                    storedEvents.remove(holder);

                    return;
                }
            }
        }
    }
}