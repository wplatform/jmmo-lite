package game.ai;

import Framework.Configuration.*;
import Framework.Constants.*;
import Framework.Database.*;
import game.datastorage.*;
import game.entities.*;
import game.movement.*;
import game.*;
import java.util.*;

// Copyright (c) Forged WoW LLC <https://github.com/ForgedWoW/ForgedCore>
// Licensed under GPL-3.0 license. See <https://github.com/ForgedWoW/ForgedCore/blob/master/LICENSE> for full information.




public class SmartAIManager extends Singleton<SmartAIManager> {
    private final MultiMap<Integer, SmartScriptHolder>[] eventMap = new MultiMap<Integer, SmartScriptHolder>[SmartScriptType.Max.getValue()];
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: readonly Dictionary<uint, WaypointPath> _waypointStore = new();
    private final HashMap<Integer, WaypointPath> waypointStore = new HashMap<Integer, WaypointPath>();

    private SmartAIManager() {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: for (byte i = 0; i < (int)SmartScriptType.Max; i++)
        for (byte i = 0; i < SmartScriptType.Max.getValue(); i++) {
            eventMap[i] = new MultiMap<Integer, SmartScriptHolder>();
        }
    }

    public final void loadFromDB() {
        var oldMSTime = Time.getMSTime();

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: for (byte i = 0; i < (int)SmartScriptType.Max; i++)
        for (byte i = 0; i < SmartScriptType.Max.getValue(); i++) {
            eventMap[i].Clear(); //Drop Existing SmartAI List
        }

        var stmt = DB.World.GetPreparedStatement(WorldStatements.SelSmartScripts);
        var result = DB.World.Query(stmt);

        if (result.IsEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 SmartAI scripts. DB table `smartai_scripts` is empty.");

            return;
        }

        var count = 0;

        do {
            SmartScriptHolder temp = new SmartScriptHolder();

            temp.entryOrGuid = result.<Integer>Read(0);

            if (temp.entryOrGuid == 0) {
                if (ConfigMgr.GetDefaultValue("load.autoclean", false)) {
                    DB.World.Execute(String.format("DELETE FROM smart_scripts WHERE entryorguid = %1$s", temp.entryOrGuid));
                } else {
                    Log.outError(LogFilter.Sql, "SmartAIMgr.LoadFromDB: invalid entryorguid (0), skipped loading.");
                }

                continue;
            }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: var source_type = (SmartScriptType)result.Read<byte>(1);
            var sourceType = SmartScriptType.forValue(result.<Byte>Read(1));

            if (sourceType.getValue() >= SmartScriptType.Max.getValue()) {
                if (ConfigMgr.GetDefaultValue("load.autoclean", false)) {
                    DB.World.Execute(String.format("DELETE FROM smart_scripts WHERE entryorguid = %1$s", temp.entryOrGuid));
                } else {
                    Log.outError(LogFilter.Sql, "SmartAIMgr.LoadSmartAI: invalid source_type ({0}), skipped loading.", sourceType);
                }

                continue;
            }

            if (temp.entryOrGuid >= 0) {
                switch (sourceType) {
                    case Creature:
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: if (Global.ObjectMgr.GetCreatureTemplate((uint)temp.EntryOrGuid) == null)
                        if (Global.getObjectMgr().getCreatureTemplate((int)temp.entryOrGuid) == null) {
                            if (ConfigMgr.GetDefaultValue("load.autoclean", false)) {
                                DB.World.Execute(String.format("DELETE FROM smart_scripts WHERE entryorguid = %1$s", temp.entryOrGuid));
                            } else {
                                Log.outError(LogFilter.Sql, "SmartAIMgr.LoadSmartAI: Creature entry ({0}) does not exist, skipped loading.", temp.entryOrGuid);
                            }

                            continue;
                        }

                        break;

                    case GameObject: {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: if (Global.ObjectMgr.GetGameObjectTemplate((uint)temp.EntryOrGuid) == null)
                        if (Global.getObjectMgr().getGameObjectTemplate((int)temp.entryOrGuid) == null) {
                            if (ConfigMgr.GetDefaultValue("load.autoclean", false)) {
                                DB.World.Execute(String.format("DELETE FROM smart_scripts WHERE entryorguid = %1$s", temp.entryOrGuid));
                            } else {
                                Log.outError(LogFilter.Sql, "SmartAIMgr.LoadSmartAI: GameObject entry ({0}) does not exist, skipped loading.", temp.entryOrGuid);
                            }

                            continue;
                        }

                        break;
                    }
                    case AreaTrigger: {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: if (CliDB.AreaTableStorage.LookupByKey((uint)temp.EntryOrGuid) == null)
                        if (CliDB.areaTableStorage.LookupByKey((int)temp.entryOrGuid) == null) {
                            if (ConfigMgr.GetDefaultValue("load.autoclean", false)) {
                                DB.World.Execute(String.format("DELETE FROM smart_scripts WHERE entryorguid = %1$s", temp.entryOrGuid));
                            } else {
                                Log.outError(LogFilter.Sql, "SmartAIMgr.LoadSmartAI: AreaTrigger entry ({0}) does not exist, skipped loading.", temp.entryOrGuid);
                            }

                            continue;
                        }

                        break;
                    }
                    case Scene: {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: if (Global.ObjectMgr.GetSceneTemplate((uint)temp.EntryOrGuid) == null)
                        if (Global.getObjectMgr().getSceneTemplate((int)temp.entryOrGuid) == null) {
                            if (ConfigMgr.GetDefaultValue("load.autoclean", false)) {
                                DB.World.Execute(String.format("DELETE FROM smart_scripts WHERE entryorguid = %1$s", temp.entryOrGuid));
                            } else {
                                Log.outError(LogFilter.Sql, "SmartAIMgr.LoadFromDB: Scene id ({0}) does not exist, skipped loading.", temp.entryOrGuid);
                            }

                            continue;
                        }

                        break;
                    }
                    case Quest: {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: if (Global.ObjectMgr.GetQuestTemplate((uint)temp.EntryOrGuid) == null)
                        if (Global.getObjectMgr().getQuestTemplate((int)temp.entryOrGuid) == null) {
                            if (ConfigMgr.GetDefaultValue("load.autoclean", false)) {
                                DB.World.Execute(String.format("DELETE FROM smart_scripts WHERE entryorguid = %1$s", temp.entryOrGuid));
                            } else {
                                Log.outError(LogFilter.Sql, String.format("SmartAIMgr.LoadFromDB: Quest id (%1$s) does not exist, skipped loading.", temp.entryOrGuid));
                            }

                            continue;
                        }

                        break;
                    }
                    case TimedActionlist:
                        break; //nothing to check, really
                    case Max:
                    case AreaTriggerEntity: {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: if (Global.AreaTriggerDataStorage.GetAreaTriggerTemplate(new AreaTriggerId((uint)temp.EntryOrGuid, false)) == null)
                        if (Global.getAreaTriggerDataStorage().getAreaTriggerTemplate(new AreaTriggerId((int)temp.entryOrGuid, false)) == null) {
                            if (ConfigMgr.GetDefaultValue("load.autoclean", false)) {
                                DB.World.Execute(String.format("DELETE FROM smart_scripts WHERE entryorguid = %1$s", temp.entryOrGuid));
                            } else {
                                Log.outError(LogFilter.Sql, String.format("SmartAIMgr.LoadFromDB: AreaTrigger entry (%1$s IsServerSide false) does not exist, skipped loading.", temp.entryOrGuid));
                            }

                            continue;
                        }

                        break;
                    }
                    case AreaTriggerEntityServerside: {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: if (Global.AreaTriggerDataStorage.GetAreaTriggerTemplate(new AreaTriggerId((uint)temp.EntryOrGuid, true)) == null)
                        if (Global.getAreaTriggerDataStorage().getAreaTriggerTemplate(new AreaTriggerId((int)temp.entryOrGuid, true)) == null) {
                            if (ConfigMgr.GetDefaultValue("load.autoclean", false)) {
                                DB.World.Execute(String.format("DELETE FROM smart_scripts WHERE entryorguid = %1$s", temp.entryOrGuid));
                            } else {
                                Log.outError(LogFilter.Sql, String.format("SmartAIMgr.LoadFromDB: AreaTrigger entry (%1$s IsServerSide true) does not exist, skipped loading.", temp.entryOrGuid));
                            }

                            continue;
                        }

                        break;
                    }
                    default:
                        if (ConfigMgr.GetDefaultValue("load.autoclean", false)) {
                            DB.World.Execute(String.format("DELETE FROM smart_scripts WHERE entryorguid = %1$s", temp.entryOrGuid));
                        } else {
                            Log.outError(LogFilter.Sql, "SmartAIMgr.LoadFromDB: not yet implemented source_type {0}", sourceType);
                        }

                        continue;
                }
            } else {
                switch (sourceType) {
                    case Creature: {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: var creature = Global.ObjectMgr.GetCreatureData((ulong)-temp.EntryOrGuid);
                        var creature = Global.getObjectMgr().getCreatureData((long)-temp.entryOrGuid);

                        if (creature == null) {
                            if (ConfigMgr.GetDefaultValue("load.autoclean", false)) {
                                DB.World.Execute(String.format("DELETE FROM smart_scripts WHERE entryorguid = %1$s", temp.entryOrGuid));
                            } else {
                                Log.outError(LogFilter.Sql, String.format("SmartAIMgr.LoadFromDB: Creature guid (%1$s) does not exist, skipped loading.", -temp.entryOrGuid));
                            }

                            continue;
                        }

                        var creatureInfo = Global.getObjectMgr().getCreatureTemplate(creature.id);

                        if (creatureInfo == null) {
                            if (ConfigMgr.GetDefaultValue("load.autoclean", false)) {
                                DB.World.Execute(String.format("DELETE FROM smart_scripts WHERE entryorguid = %1$s", temp.entryOrGuid));
                            } else {
                                Log.outError(LogFilter.Sql, String.format("SmartAIMgr.LoadFromDB: Creature entry (%1$s) guid (%2$s) does not exist, skipped loading.", creature.id, -temp.entryOrGuid));
                            }

                            continue;
                        }

                        if (!creatureInfo.aIName.equals("SmartAI")) {
                            if (ConfigMgr.GetDefaultValue("load.autoclean", false)) {
                                DB.World.Execute(String.format("DELETE FROM smart_scripts WHERE entryorguid = %1$s", temp.entryOrGuid));
                            } else {
                                Log.outError(LogFilter.Sql, String.format("SmartAIMgr.LoadFromDB: Creature entry (%1$s) guid (%2$s) is not using SmartAI, skipped loading.", creature.id, -temp.entryOrGuid));
                            }

                            continue;
                        }

                        break;
                    }
                    case GameObject: {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: var gameObject = Global.ObjectMgr.GetGameObjectData((ulong)-temp.EntryOrGuid);
                        var gameObject = Global.getObjectMgr().getGameObjectData((long)-temp.entryOrGuid);

                        if (gameObject == null) {
                            if (ConfigMgr.GetDefaultValue("load.autoclean", false)) {
                                DB.World.Execute(String.format("DELETE FROM smart_scripts WHERE entryorguid = %1$s", temp.entryOrGuid));
                            } else {
                                Log.outError(LogFilter.Sql, String.format("SmartAIMgr.LoadFromDB: GameObject guid (%1$s) does not exist, skipped loading.", -temp.entryOrGuid));
                            }

                            continue;
                        }

                        var gameObjectInfo = Global.getObjectMgr().getGameObjectTemplate(gameObject.id);

                        if (gameObjectInfo == null) {
                            if (ConfigMgr.GetDefaultValue("load.autoclean", false)) {
                                DB.World.Execute(String.format("DELETE FROM smart_scripts WHERE entryorguid = %1$s", temp.entryOrGuid));
                            } else {
                                Log.outError(LogFilter.Sql, String.format("SmartAIMgr.LoadFromDB: GameObject entry (%1$s) guid (%2$s) does not exist, skipped loading.", gameObject.id, -temp.entryOrGuid));
                            }

                            continue;
                        }

                        if (!gameObjectInfo.aIName.equals("SmartGameObjectAI")) {
                            if (ConfigMgr.GetDefaultValue("load.autoclean", false)) {
                                DB.World.Execute(String.format("DELETE FROM smart_scripts WHERE entryorguid = %1$s", temp.entryOrGuid));
                            } else {
                                Log.outError(LogFilter.Sql, String.format("SmartAIMgr.LoadFromDB: GameObject entry (%1$s) guid (%2$s) is not using SmartGameObjectAI, skipped loading.", gameObject.id, -temp.entryOrGuid));
                            }

                            continue;
                        }

                        break;
                    }
                    default:
                        if (ConfigMgr.GetDefaultValue("load.autoclean", false)) {
                            DB.World.Execute(String.format("DELETE FROM smart_scripts WHERE entryorguid = %1$s", temp.entryOrGuid));
                        } else {
                            Log.outError(LogFilter.Sql, String.format("SmartAIMgr.LoadFromDB: GUID-specific scripting not yet implemented for source_type %1$s", sourceType));
                        }

                        continue;
                }
            }

            temp.sourceType = sourceType;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: temp.EventId = result.Read<ushort>(2);
            temp.eventId = result.<Short>Read(2);
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: temp.Link = result.Read<ushort>(3);
            temp.link = result.<Short>Read(3);
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: temp.Event.type = (SmartEvents)result.Read<byte>(4);
            temp.event.type = SmartEvents.forValue(result.<Byte>Read(4));
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: temp.Event.event_phase_mask = result.Read<ushort>(5);
            temp.event.eventPhaseMask = result.<Short>Read(5);
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: temp.Event.event_chance = result.Read<byte>(6);
            temp.event.eventChance = result.<Byte>Read(6);
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: temp.Event.event_flags = (SmartEventFlags)result.Read<ushort>(7);
            temp.event.eventFlags = SmartEventFlags.forValue(result.<Short>Read(7));

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: temp.Event.raw.param1 = result.Read<uint>(8);
            temp.event.raw.param1 = result.<Integer>Read(8);
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: temp.Event.raw.param2 = result.Read<uint>(9);
            temp.event.raw.param2 = result.<Integer>Read(9);
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: temp.Event.raw.param3 = result.Read<uint>(10);
            temp.event.raw.param3 = result.<Integer>Read(10);
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: temp.Event.raw.param4 = result.Read<uint>(11);
            temp.event.raw.param4 = result.<Integer>Read(11);
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: temp.Event.raw.param5 = result.Read<uint>(12);
            temp.event.raw.param5 = result.<Integer>Read(12);
            temp.event.paramString = result.<String>Read(13);

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: temp.Action.type = (SmartActions)result.Read<byte>(14);
            temp.action.type = SmartActions.forValue(result.<Byte>Read(14));
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: temp.Action.raw.param1 = result.Read<uint>(15);
            temp.action.raw.param1 = result.<Integer>Read(15);
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: temp.Action.raw.param2 = result.Read<uint>(16);
            temp.action.raw.param2 = result.<Integer>Read(16);
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: temp.Action.raw.param3 = result.Read<uint>(17);
            temp.action.raw.param3 = result.<Integer>Read(17);
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: temp.Action.raw.param4 = result.Read<uint>(18);
            temp.action.raw.param4 = result.<Integer>Read(18);
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: temp.Action.raw.param5 = result.Read<uint>(19);
            temp.action.raw.param5 = result.<Integer>Read(19);
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: temp.Action.raw.param6 = result.Read<uint>(20);
            temp.action.raw.param6 = result.<Integer>Read(20);
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: temp.Action.raw.param7 = result.Read<uint>(21);
            temp.action.raw.param7 = result.<Integer>Read(21);

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: temp.Target.type = (SmartTargets)result.Read<byte>(22);
            temp.target.type = SmartTargets.forValue(result.<Byte>Read(22));
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: temp.Target.raw.param1 = result.Read<uint>(23);
            temp.target.raw.param1 = result.<Integer>Read(23);
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: temp.Target.raw.param2 = result.Read<uint>(24);
            temp.target.raw.param2 = result.<Integer>Read(24);
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: temp.Target.raw.param3 = result.Read<uint>(25);
            temp.target.raw.param3 = result.<Integer>Read(25);
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: temp.Target.raw.param4 = result.Read<uint>(26);
            temp.target.raw.param4 = result.<Integer>Read(26);
            temp.target.x = result.<Float>Read(27);
            temp.target.y = result.<Float>Read(28);
            temp.target.z = result.<Float>Read(29);
            temp.target.o = result.<Float>Read(30);

            //check target
            if (!isTargetValid(temp)) {
                continue;
            }

            // check all event and action params
            if (!isEventValid(temp)) {
                continue;
            }

            // specific check for timed events
            switch (temp.event.type) {
                case Update:
                case UpdateOoc:
                case UpdateIc:
                case HealthPct:
                case ManaPct:
                case Range:
                case FriendlyHealthPCT:
                case FriendlyMissingBuff:
                case HasAura:
                case TargetBuffed:
                    if (temp.event.minMaxRepeat.repeatMin == 0 && temp.event.minMaxRepeat.repeatMax == 0 && !temp.event.eventFlags.HasAnyFlag(SmartEventFlags.NotRepeatable) && temp.sourceType != SmartScriptType.TimedActionlist) {
                        temp.event.eventFlags = SmartEventFlags.forValue(temp.event.eventFlags.getValue() | SmartEventFlags.NotRepeatable.getValue());
                        Log.outError(LogFilter.Sql, String.format("SmartAIMgr.LoadFromDB: Entry %1$s SourceType %2$s, Event %3$s, Missing Repeat flag.", temp.entryOrGuid, temp.getScriptType(), temp.eventId));
                    }

                    break;
                case VictimCasting:
                case IsBehindTarget:
                    if (temp.event.minMaxRepeat.min == 0 && temp.event.minMaxRepeat.max == 0 && !temp.event.eventFlags.HasAnyFlag(SmartEventFlags.NotRepeatable) && temp.sourceType != SmartScriptType.TimedActionlist) {
                        temp.event.eventFlags = SmartEventFlags.forValue(temp.event.eventFlags.getValue() | SmartEventFlags.NotRepeatable.getValue());
                        Log.outError(LogFilter.Sql, String.format("SmartAIMgr.LoadFromDB: Entry %1$s SourceType %2$s, Event %3$s, Missing Repeat flag.", temp.entryOrGuid, temp.getScriptType(), temp.eventId));
                    }

                    break;
                case FriendlyIsCc:
                    if (temp.event.friendlyCC.repeatMin == 0 && temp.event.friendlyCC.repeatMax == 0 && !temp.event.eventFlags.HasAnyFlag(SmartEventFlags.NotRepeatable) && temp.sourceType != SmartScriptType.TimedActionlist) {
                        temp.event.eventFlags = SmartEventFlags.forValue(temp.event.eventFlags.getValue() | SmartEventFlags.NotRepeatable.getValue());
                        Log.outError(LogFilter.Sql, String.format("SmartAIMgr.LoadFromDB: Entry %1$s SourceType %2$s, Event %3$s, Missing Repeat flag.", temp.entryOrGuid, temp.getScriptType(), temp.eventId));
                    }

                    break;
                default:
                    break;
            }

            // creature entry / guid not found in storage, create empty event list for it and increase counters
            if (!eventMap[sourceType.getValue()].ContainsKey(temp.entryOrGuid)) {
                ++count;
            }

            // store the new event
            eventMap[sourceType.getValue()].Add(temp.entryOrGuid, temp);
        } while (result.NextRow());

        // Post Loading Validation
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: for (byte i = 0; i < (int)SmartScriptType.Max; ++i)
        for (byte i = 0; i < SmartScriptType.Max.getValue(); ++i) {
            if (eventMap[i] == null) {
                continue;
            }

            for (var key : eventMap[i].getKeys()) {
                var list = eventMap[i].LookupByKey(key);

                for (var e : list) {
                    if (e.link != 0) {
                        if (findLinkedEvent(list, e.link) == null) {
                            Log.outError(LogFilter.Sql, "SmartAIMgr.LoadFromDB: Entry {0} SourceType {1}, Event {2}, Link Event {3} not found or invalid.", e.entryOrGuid, e.getScriptType(), e.eventId, e.link);
                        }
                    }

                    if (e.getEventType() == SmartEvents.Link) {
                        if (findLinkedSourceEvent(list, e.eventId) == null) {
                            Log.outError(LogFilter.Sql, "SmartAIMgr.LoadFromDB: Entry {0} SourceType {1}, Event {2}, Link Source Event not found or invalid. Event will never trigger.", e.entryOrGuid, e.getScriptType(), e.eventId);
                        }
                    }
                }
            }
        }

        Log.outInfo(LogFilter.ServerLoading, "Loaded {0} SmartAI scripts in {1} ms", count, Time.GetMSTimeDiffToNow(oldMSTime));
    }

    public final void loadWaypointFromDB() {
        var oldMSTime = Time.getMSTime();

        waypointStore.clear();

        var stmt = DB.World.GetPreparedStatement(WorldStatements.SelSmartaiWp);
        var result = DB.World.Query(stmt);

        if (result.IsEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 SmartAI Waypoint Paths. DB table `waypoints` is empty.");

            return;
        }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: uint count = 0;
        int count = 0;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: uint total = 0;
        int total = 0;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: uint lastEntry = 0;
        int lastEntry = 0;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: uint lastId = 1;
        int lastId = 1;

        do {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: var entry = result.Read<uint>(0);
            var entry = result.<Integer>Read(0);
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: var id = result.Read<uint>(1);
            var id = result.<Integer>Read(1);
            var x = result.<Float>Read(2);
            var y = result.<Float>Read(3);
            var z = result.<Float>Read(4);
            Float o = null;

            if (!result.IsNull(5)) {
                o = result.<Float>Read(5);
            }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: var delay = result.Read<uint>(6);
            var delay = result.<Integer>Read(6);

            if (lastEntry != entry) {
                lastId = 1;
                ++count;
            }

            if (lastId != id) {
                Log.outError(LogFilter.Sql, String.format("SmartWaypointMgr.LoadFromDB: Path entry %1$s, unexpected point id %2$s, expected %3$s.", entry, id, lastId));
            }

            ++lastId;

            if (!waypointStore.containsKey(entry)) {
                waypointStore.put(entry, new WaypointPath());
            }

            var path = waypointStore.get(entry);
            path.id = entry;
            path.nodes.add(new WaypointNode(id, x, y, z, o, delay));

            lastEntry = entry;
            ++total;
        } while (result.NextRow());

        Log.outInfo(LogFilter.ServerLoading, String.format("Loaded %1$s SmartAI waypoint paths (total %2$s waypoints) in %3$s ms", count, total, Time.GetMSTimeDiffToNow(oldMSTime)));
    }

    public final ArrayList<SmartScriptHolder> getScript(int entry, SmartScriptType type) {
        ArrayList<SmartScriptHolder> temp = new ArrayList<SmartScriptHolder>();

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: if (_eventMap[(uint)type].ContainsKey(entry))
        if (eventMap[(int)type.getValue()].ContainsKey(entry)) {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: foreach (var holder in _eventMap[(uint)type][entry])
            for (var holder : eventMap[(int)type.getValue()].get(entry)) {
                temp.add(new SmartScriptHolder(holder));
            }
        } else {
            if (entry > 0) { //first search is for guid (negative), do not drop error if not found
                Log.outDebug(LogFilter.ScriptsAi, "SmartAIMgr.GetScript: Could not load Script for Entry {0} ScriptType {1}.", entry, type);
            }
        }

        return temp;
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public WaypointPath GetPath(uint id)
    public final WaypointPath getPath(int id) {
        return waypointStore.LookupByKey(id);
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public static SmartScriptHolder FindLinkedSourceEvent(List<SmartScriptHolder> list, uint eventId)
    public static SmartScriptHolder findLinkedSourceEvent(ArrayList<SmartScriptHolder> list, int eventId) {
        var sch = tangible.ListHelper.find(list, p -> p.Link == eventId);

        if (sch != null) {
            return sch;
        }

        return null;
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public SmartScriptHolder FindLinkedEvent(List<SmartScriptHolder> list, uint link)
    public final SmartScriptHolder findLinkedEvent(ArrayList<SmartScriptHolder> list, int link) {
        var sch = tangible.ListHelper.find(list, p -> p.EventId == link && p.GetEventType() == SmartEvents.Link);

        if (sch != null) {
            return sch;
        }

        return null;
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public static uint GetTypeMask(SmartScriptType smartScriptType)
    public static int getTypeMask(SmartScriptType smartScriptType) {
        return switch (smartScriptType) {
            case Creature -> SmartScriptTypeMaskId.Creature;
            case GameObject -> SmartScriptTypeMaskId.Gameobject;
            case AreaTrigger -> SmartScriptTypeMaskId.Areatrigger;
            case Event -> SmartScriptTypeMaskId.Event;
            case Gossip -> SmartScriptTypeMaskId.Gossip;
            case Quest -> SmartScriptTypeMaskId.Quest;
            case Spell -> SmartScriptTypeMaskId.Spell;
            case Transport -> SmartScriptTypeMaskId.Transport;
            case getInstance() -> SmartScriptTypeMaskId.Instance;
            case TimedActionlist -> SmartScriptTypeMaskId.TimedActionlist;
            case Scene -> SmartScriptTypeMaskId.Scene;
            case AreaTriggerEntity -> SmartScriptTypeMaskId.AreatrigggerEntity;
            case AreaTriggerEntityServerside -> SmartScriptTypeMaskId.AreatrigggerEntity;
            default -> 0;
        };
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public static uint GetEventMask(SmartEvents smartEvent)
    public static int getEventMask(SmartEvents smartEvent) {
        return switch (smartEvent) {
            case UpdateIc -> SmartScriptTypeMaskId.Creature + SmartScriptTypeMaskId.TimedActionlist;
            case UpdateOoc -> SmartScriptTypeMaskId.Creature + SmartScriptTypeMaskId.Gameobject + SmartScriptTypeMaskId.Instance + SmartScriptTypeMaskId.AreatrigggerEntity;
            case HealthPct -> SmartScriptTypeMaskId.Creature;
            case ManaPct -> SmartScriptTypeMaskId.Creature;
            case Aggro -> SmartScriptTypeMaskId.Creature;
            case Kill -> SmartScriptTypeMaskId.Creature;
            case Death -> SmartScriptTypeMaskId.Creature;
            case Evade -> SmartScriptTypeMaskId.Creature;
            case SpellHit -> SmartScriptTypeMaskId.Creature + SmartScriptTypeMaskId.Gameobject;
            case Range -> SmartScriptTypeMaskId.Creature;
            case OocLos -> SmartScriptTypeMaskId.Creature;
            case Respawn -> SmartScriptTypeMaskId.Creature + SmartScriptTypeMaskId.Gameobject;
            case TargetHealthPct -> SmartScriptTypeMaskId.Creature;
            case VictimCasting -> SmartScriptTypeMaskId.Creature;
            case FriendlyHealth -> SmartScriptTypeMaskId.Creature;
            case FriendlyIsCc -> SmartScriptTypeMaskId.Creature;
            case FriendlyMissingBuff -> SmartScriptTypeMaskId.Creature;
            case SummonedUnit -> SmartScriptTypeMaskId.Creature + SmartScriptTypeMaskId.Gameobject;
            case TargetManaPct -> SmartScriptTypeMaskId.Creature;
            case AcceptedQuest -> SmartScriptTypeMaskId.Creature + SmartScriptTypeMaskId.Gameobject;
            case RewardQuest -> SmartScriptTypeMaskId.Creature + SmartScriptTypeMaskId.Gameobject;
            case ReachedHome -> SmartScriptTypeMaskId.Creature;
            case ReceiveEmote -> SmartScriptTypeMaskId.Creature;
            case HasAura -> SmartScriptTypeMaskId.Creature;
            case TargetBuffed -> SmartScriptTypeMaskId.Creature;
            case Reset -> SmartScriptTypeMaskId.Creature;
            case IcLos -> SmartScriptTypeMaskId.Creature;
            case PassengerBoarded -> SmartScriptTypeMaskId.Creature;
            case PassengerRemoved -> SmartScriptTypeMaskId.Creature;
            case Charmed -> SmartScriptTypeMaskId.Creature;
            case CharmedTarget -> SmartScriptTypeMaskId.Creature;
            case SpellHitTarget -> SmartScriptTypeMaskId.Creature;
            case Damaged -> SmartScriptTypeMaskId.Creature;
            case DamagedTarget -> SmartScriptTypeMaskId.Creature;
            case Movementinform -> SmartScriptTypeMaskId.Creature;
            case SummonDespawned -> SmartScriptTypeMaskId.Creature + SmartScriptTypeMaskId.Gameobject;
            case CorpseRemoved -> SmartScriptTypeMaskId.Creature;
            case AiInit -> SmartScriptTypeMaskId.Creature + SmartScriptTypeMaskId.Gameobject;
            case DataSet -> SmartScriptTypeMaskId.Creature + SmartScriptTypeMaskId.Gameobject;
            case WaypointStart -> SmartScriptTypeMaskId.Creature;
            case WaypointReached -> SmartScriptTypeMaskId.Creature;
            case TransportAddplayer -> SmartScriptTypeMaskId.Transport;
            case TransportAddcreature -> SmartScriptTypeMaskId.Transport;
            case TransportRemovePlayer -> SmartScriptTypeMaskId.Transport;
            case TransportRelocate -> SmartScriptTypeMaskId.Transport;
            case InstancePlayerEnter -> SmartScriptTypeMaskId.Instance;
            case AreatriggerOntrigger -> SmartScriptTypeMaskId.Areatrigger + SmartScriptTypeMaskId.AreatrigggerEntity;
            case QuestAccepted -> SmartScriptTypeMaskId.Quest;
            case QuestObjCompletion -> SmartScriptTypeMaskId.Quest;
            case QuestRewarded -> SmartScriptTypeMaskId.Quest;
            case QuestCompletion -> SmartScriptTypeMaskId.Quest;
            case QuestFail -> SmartScriptTypeMaskId.Quest;
            case TextOver -> SmartScriptTypeMaskId.Creature + SmartScriptTypeMaskId.Gameobject;
            case ReceiveHeal -> SmartScriptTypeMaskId.Creature;
            case JustSummoned -> SmartScriptTypeMaskId.Creature;
            case WaypointPaused -> SmartScriptTypeMaskId.Creature;
            case WaypointResumed -> SmartScriptTypeMaskId.Creature;
            case WaypointStopped -> SmartScriptTypeMaskId.Creature;
            case WaypointEnded -> SmartScriptTypeMaskId.Creature;
            case TimedEventTriggered -> SmartScriptTypeMaskId.Creature + SmartScriptTypeMaskId.Gameobject;
            case Update -> SmartScriptTypeMaskId.Creature + SmartScriptTypeMaskId.Gameobject + SmartScriptTypeMaskId.AreatrigggerEntity;
            case Link -> SmartScriptTypeMaskId.Creature + SmartScriptTypeMaskId.Gameobject + SmartScriptTypeMaskId.Areatrigger + SmartScriptTypeMaskId.Event + SmartScriptTypeMaskId.Gossip + SmartScriptTypeMaskId.Quest + SmartScriptTypeMaskId.Spell + SmartScriptTypeMaskId.Transport + SmartScriptTypeMaskId.Instance + SmartScriptTypeMaskId.AreatrigggerEntity;
            case GossipSelect -> SmartScriptTypeMaskId.Creature + SmartScriptTypeMaskId.Gameobject;
            case JustCreated -> SmartScriptTypeMaskId.Creature + SmartScriptTypeMaskId.Gameobject;
            case GossipHello -> SmartScriptTypeMaskId.Creature + SmartScriptTypeMaskId.Gameobject;
            case FollowCompleted -> SmartScriptTypeMaskId.Creature;
            case PhaseChange -> SmartScriptTypeMaskId.Creature + SmartScriptTypeMaskId.Gameobject;
            case IsBehindTarget -> SmartScriptTypeMaskId.Creature;
            case GameEventStart -> SmartScriptTypeMaskId.Creature + SmartScriptTypeMaskId.Gameobject;
            case GameEventEnd -> SmartScriptTypeMaskId.Creature + SmartScriptTypeMaskId.Gameobject;
            case GoLootStateChanged -> SmartScriptTypeMaskId.Gameobject;
            case GoEventInform -> SmartScriptTypeMaskId.Gameobject;
            case ActionDone -> SmartScriptTypeMaskId.Creature;
            case OnSpellclick -> SmartScriptTypeMaskId.Creature;
            case FriendlyHealthPCT -> SmartScriptTypeMaskId.Creature;
            case DistanceCreature -> SmartScriptTypeMaskId.Creature;
            case DistanceGameobject -> SmartScriptTypeMaskId.Creature;
            case CounterSet -> SmartScriptTypeMaskId.Creature + SmartScriptTypeMaskId.Gameobject;
            case SceneStart -> SmartScriptTypeMaskId.Scene;
            case SceneTrigger -> SmartScriptTypeMaskId.Scene;
            case SceneCancel -> SmartScriptTypeMaskId.Scene;
            case SceneComplete -> SmartScriptTypeMaskId.Scene;
            case SummonedUnitDies -> SmartScriptTypeMaskId.Creature + SmartScriptTypeMaskId.Gameobject;
            case OnSpellCast -> SmartScriptTypeMaskId.Creature;
            case OnSpellFailed -> SmartScriptTypeMaskId.Creature;
            case OnSpellStart -> SmartScriptTypeMaskId.Creature;
            case OnDespawn -> SmartScriptTypeMaskId.Creature;
            default -> 0;
        };
    }


    public static void tcSaiIsBooleanValid(SmartScriptHolder e, int value) {
        tcSaiIsBooleanValid(e, value, null);
    }

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: public static void TC_SAI_IS_BOOLEAN_VALID(SmartScriptHolder e, uint value, [CallerArgumentExpression("value")] string valueName = null)
//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
    public static void tcSaiIsBooleanValid(SmartScriptHolder e, int value, String valueName) {
        if (value > 1) {
            Log.outError(LogFilter.Sql, String.format("SmartAIMgr: %1$s uses param %2$s of type Boolean with value %3$s, valid values are 0 or 1, skipped.", e, valueName, value));
        }
    }

    private static boolean eventHasInvoker(SmartEvents smartEvent) {
        switch (smartEvent) {
            // white list of events that actually have an invoker passed to them
            case Aggro:
            case Death:
            case Kill:
            case SummonedUnit:
            case SummonedUnitDies:
            case SpellHit:
            case SpellHitTarget:
            case Damaged:
            case ReceiveHeal:
            case ReceiveEmote:
            case JustSummoned:
            case DamagedTarget:
            case SummonDespawned:
            case PassengerBoarded:
            case PassengerRemoved:
            case GossipHello:
            case GossipSelect:
            case AcceptedQuest:
            case RewardQuest:
            case FollowCompleted:
            case OnSpellclick:
            case GoLootStateChanged:
            case AreatriggerOntrigger:
            case IcLos:
            case OocLos:
            case DistanceCreature:
            case FriendlyHealthPCT:
            case FriendlyIsCc:
            case FriendlyMissingBuff:
            case ActionDone:
            case Range:
            case VictimCasting:
            case TargetBuffed:
            case InstancePlayerEnter:
            case TransportAddcreature:
            case DataSet:
            case QuestAccepted:
            case QuestObjCompletion:
            case QuestCompletion:
            case QuestFail:
            case QuestRewarded:
            case SceneStart:
            case SceneTrigger:
            case SceneCancel:
            case SceneComplete:
                return true;
            default:
                return false;
        }
    }

    private static boolean isTargetValid(SmartScriptHolder e) {
        if (Math.abs(e.target.o) > 2 * MathFunctions.PI) {
            Log.outError(LogFilter.Sql, String.format("SmartAIMgr: %1$s has abs(`target.o` = %2$s) > 2*PI (orientation is expressed in radians)", e, e.target.o));
        }

        switch (e.getTargetType()) {
            case CreatureDistance:
            case CreatureRange: {
                if (e.target.unitDistance.creature != 0 && Global.getObjectMgr().getCreatureTemplate(e.target.unitDistance.creature) == null) {
                    Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s uses non-existent Creature entry %2$s as target_param1, skipped.", e, e.target.unitDistance.creature));

                    return false;
                }

                break;
            }
            case GameobjectDistance:
            case GameobjectRange: {
                if (e.target.goDistance.entry != 0 && Global.getObjectMgr().getGameObjectTemplate(e.target.goDistance.entry) == null) {
                    Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s uses non-existent GameObject entry %2$s as target_param1, skipped.", e, e.target.goDistance.entry));

                    return false;
                }

                break;
            }
            case CreatureGuid: {
                if (e.target.unitGUID.entry != 0 && !isCreatureValid(e, e.target.unitGUID.entry)) {
                    return false;
                }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: ulong guid = e.Target.unitGUID.dbGuid;
                long guid = e.target.unitGUID.dbGuid;
                var data = Global.getObjectMgr().getCreatureData(guid);

                if (data == null) {
                    Log.outError(LogFilter.Sql, String.format("SmartAIMgr: %1$s using invalid creature guid %2$s as target_param1, skipped.", e, guid));

                    return false;
                } else if (e.target.unitGUID.entry != 0 && e.target.unitGUID.entry != data.id) {
                    Log.outError(LogFilter.Sql, String.format("SmartAIMgr: %1$s using invalid creature entry %2$s (expected %3$s) for guid %4$s as target_param1, skipped.", e, e.target.unitGUID.entry, data.id, guid));

                    return false;
                }

                break;
            }
            case GameobjectGuid: {
                if (e.target.goGUID.entry != 0 && !isGameObjectValid(e, e.target.goGUID.entry)) {
                    return false;
                }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: ulong guid = e.Target.goGUID.dbGuid;
                long guid = e.target.goGUID.dbGuid;
                var data = Global.getObjectMgr().getGameObjectData(guid);

                if (data == null) {
                    Log.outError(LogFilter.Sql, String.format("SmartAIMgr: %1$s using invalid gameobject guid %2$s as target_param1, skipped.", e, guid));

                    return false;
                } else if (e.target.goGUID.entry != 0 && e.target.goGUID.entry != data.id) {
                    Log.outError(LogFilter.Sql, String.format("SmartAIMgr: %1$s using invalid gameobject entry %2$s (expected %3$s) for guid %4$s as target_param1, skipped.", e, e.target.goGUID.entry, data.id, guid));

                    return false;
                }

                break;
            }
            case PlayerDistance:
            case ClosestPlayer: {
                if (e.target.playerDistance.dist == 0) {
                    Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s has maxDist 0 as target_param1, skipped.", e));

                    return false;
                }

                break;
            }
            case ActionInvoker:
            case ActionInvokerVehicle:
            case InvokerParty:
                if (e.getScriptType() != SmartScriptType.TimedActionlist && e.getEventType() != SmartEvents.Link && !eventHasInvoker(e.event.type)) {
                    Log.outError(LogFilter.Sql, String.format("SmartAIMgr: Entry %1$s SourceType %2$s Event %3$s Action %4$s has invoker target, but event does not provide any invoker!", e.entryOrGuid, e.getScriptType(), e.getEventType(), e.getActionType()));

                    return false;
                }

                break;
            case HostileSecondAggro:
            case HostileLastAggro:
            case HostileRandom:
            case HostileRandomNotTop:
                tcSaiIsBooleanValid(e, e.target.hostilRandom.playerOnly);

                break;
            case Farthest:
                tcSaiIsBooleanValid(e, e.target.farthest.playerOnly);
                tcSaiIsBooleanValid(e, e.target.farthest.isInLos);

                break;
            case ClosestCreature:
                tcSaiIsBooleanValid(e, e.target.unitClosest.dead);

                break;
            case ClosestEnemy:
                tcSaiIsBooleanValid(e, e.target.closestAttackable.playerOnly);

                break;
            case ClosestFriendly:
                tcSaiIsBooleanValid(e, e.target.closestFriendly.playerOnly);

                break;
            case OwnerOrSummoner:
                tcSaiIsBooleanValid(e, e.target.owner.useCharmerOrOwner);

                break;
            case ClosestGameobject:
            case PlayerRange:
            case Self:
            case Victim:
            case Position:
            case None:
            case ThreatList:
            case Stored:
            case LootRecipients:
            case VehiclePassenger:
            case ClosestUnspawnedGameobject:
                break;
            default:
                Log.outError(LogFilter.ScriptsAi, "SmartAIMgr: Not handled target_type({0}), Entry {1} SourceType {2} Event {3} Action {4}, skipped.", e.getTargetType(), e.entryOrGuid, e.getScriptType(), e.eventId, e.getActionType());

                return false;
        }

        if (!checkUnusedTargetParams(e)) {
            return false;
        }

        return true;
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: static bool IsSpellVisualKitValid(SmartScriptHolder e, uint entry)
    private static boolean isSpellVisualKitValid(SmartScriptHolder e, int entry) {
        if (!CliDB.spellVisualKitStorage.containsKey(entry)) {
            Log.outError(LogFilter.Sql, String.format("SmartAIMgr: Entry %1$s SourceType %2$s Event %3$s Action %4$s uses non-existent SpellVisualKit entry %5$s, skipped.", e.entryOrGuid, e.getScriptType(), e.eventId, e.getActionType(), entry));

            return false;
        }

        return true;
    }

    private static boolean checkUnusedEventParams(SmartScriptHolder e) {
        var paramsStructSize = switch (e.event.type) {
            case UpdateIc -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.MinMaxRepeat.class);
            case UpdateOoc -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.MinMaxRepeat.class);
            case HealthPct -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.MinMaxRepeat.class);
            case ManaPct -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.MinMaxRepeat.class);
            case Aggro -> 0;
            case Kill -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.Kill.class);
            case Death -> 0;
            case Evade -> 0;
            case SpellHit -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.SpellHit.class);
            case Range -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.MinMaxRepeat.class);
            case OocLos -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.Los.class);
            case Respawn -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.Respawn.class);
            case VictimCasting -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.TargetCasting.class);
            case FriendlyIsCc -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.FriendlyCC.class);
            case FriendlyMissingBuff -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.MissingBuff.class);
            case SummonedUnit -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.Summoned.class);
            case AcceptedQuest -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.Quest.class);
            case RewardQuest -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.Quest.class);
            case ReachedHome -> 0;
            case ReceiveEmote -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.Emote.class);
            case HasAura -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.Aura.class);
            case TargetBuffed -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.Aura.class);
            case Reset -> 0;
            case IcLos -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.Los.class);
            case PassengerBoarded -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.MinMax.class);
            case PassengerRemoved -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.MinMax.class);
            case Charmed -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.Charm.class);
            case SpellHitTarget -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.SpellHit.class);
            case Damaged -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.MinMaxRepeat.class);
            case DamagedTarget -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.MinMaxRepeat.class);
            case Movementinform -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.MovementInform.class);
            case SummonDespawned -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.Summoned.class);
            case CorpseRemoved -> 0;
            case AiInit -> 0;
            case DataSet -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.DataSet.class);
            case WaypointReached -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.Waypoint.class);
            case TransportAddplayer -> 0;
            case TransportAddcreature -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.TransportAddCreature.class);
            case TransportRemovePlayer -> 0;
            case TransportRelocate -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.TransportRelocate.class);
            case InstancePlayerEnter -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.InstancePlayerEnter.class);
            case AreatriggerOntrigger -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.Areatrigger.class);
            case QuestAccepted -> 0;
            case QuestObjCompletion -> 0;
            case QuestCompletion -> 0;
            case QuestRewarded -> 0;
            case QuestFail -> 0;
            case TextOver -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.TextOver.class);
            case ReceiveHeal -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.MinMaxRepeat.class);
            case JustSummoned -> 0;
            case WaypointPaused -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.Waypoint.class);
            case WaypointResumed -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.Waypoint.class);
            case WaypointStopped -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.Waypoint.class);
            case WaypointEnded -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.Waypoint.class);
            case TimedEventTriggered -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.TimedEvent.class);
            case Update -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.MinMaxRepeat.class);
            case Link -> 0;
            case GossipSelect -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.Gossip.class);
            case JustCreated -> 0;
            case GossipHello -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.GossipHello.class);
            case FollowCompleted -> 0;
            case GameEventStart -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.GameEvent.class);
            case GameEventEnd -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.GameEvent.class);
            case GoLootStateChanged -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.GoLootStateChanged.class);
            case GoEventInform -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.EventInform.class);
            case ActionDone -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.DoAction.class);
            case OnSpellclick -> 0;
            case FriendlyHealthPCT -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.FriendlyHealthPct.class);
            case DistanceCreature -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.Distance.class);
            case DistanceGameobject -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.Distance.class);
            case CounterSet -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.Counter.class);
            case SceneStart -> 0;
            case SceneTrigger -> 0;
            case SceneCancel -> 0;
            case SceneComplete -> 0;
            case SummonedUnitDies -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.Summoned.class);
            case OnSpellCast -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.SpellCast.class);
            case OnSpellFailed -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.SpellCast.class);
            case OnSpellStart -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.SpellCast.class);
            case OnDespawn -> 0;
            default -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.Raw.class);
        };

        var rawCount = system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.Raw.class) / (Integer.SIZE / Byte.SIZE);
        var paramsCount = paramsStructSize / (Integer.SIZE / Byte.SIZE);

        for (var index = paramsCount; index < rawCount; index++) {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: uint value = 0;
            int value = 0;

            switch (index) {
                case 0:
                    value = e.event.raw.param1;

                    break;
                case 1:
                    value = e.event.raw.param2;

                    break;
                case 2:
                    value = e.event.raw.param3;

                    break;
                case 3:
                    value = e.event.raw.param4;

                    break;
                case 4:
                    value = e.event.raw.param5;

                    break;
            }

            if (value != 0) {
                Log.outWarn(LogFilter.Sql, String.format("SmartAIMgr: %1$s has unused event_param%2$s with value %3$s, it should be 0.", e, index + 1, value));
            }
        }

        return true;
    }

    private static boolean checkUnusedActionParams(SmartScriptHolder e) {
        var paramsStructSize = switch (e.action.type) {
            case None -> 0;
            case Talk -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.Talk.class);
            case SetFaction -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.Faction.class);
            case MorphToEntryOrModel -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.MorphOrMount.class);
            case Sound -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.Sound.class);
            case PlayEmote -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.Emote.class);
            case FailQuest -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.Quest.class);
            case OfferQuest -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.QuestOffer.class);
            case SetReactState -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.React.class);
            case ActivateGobject -> 0;
            case RandomEmote -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.RandomEmote.class);
            case Cast -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.Cast.class);
            case SummonCreature -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.SummonCreature.class);
            case ThreatSinglePct -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.ThreatPCT.class);
            case ThreatAllPct -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.ThreatPCT.class);
            case CallAreaexploredoreventhappens -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.Quest.class);
            case SetIngamePhaseGroup -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.IngamePhaseGroup.class);
            case SetEmoteState -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.Emote.class);
            case AutoAttack -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.AutoAttack.class);
            case AllowCombatMovement -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.CombatMove.class);
            case SetEventPhase -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.SetEventPhase.class);
            case IncEventPhase -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.IncEventPhase.class);
            case Evade -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.Evade.class);
            case FleeForAssist -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.FleeAssist.class);
            case CallGroupeventhappens -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.Quest.class);
            case CombatStop -> 0;
            case RemoveAurasFromSpell -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.RemoveAura.class);
            case Follow -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.Follow.class);
            case RandomPhase -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.RandomPhase.class);
            case RandomPhaseRange -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.RandomPhaseRange.class);
            case ResetGobject -> 0;
            case CallKilledmonster -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.KilledMonster.class);
            case SetInstData -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.SetInstanceData.class);
            case SetInstData64 -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.SetInstanceData64.class);
            case UpdateTemplate -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.UpdateTemplate.class);
            case Die -> 0;
            case SetInCombatWithZone -> 0;
            case CallForHelp -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.CallHelp.class);
            case SetSheath -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.SetSheath.class);
            case ForceDespawn -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.ForceDespawn.class);
            case SetInvincibilityHpLevel -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.InvincHP.class);
            case MountToEntryOrModel -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.MorphOrMount.class);
            case SetIngamePhaseId -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.IngamePhaseId.class);
            case SetData -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.SetData.class);
            case AttackStop -> 0;
            case SetVisibility -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.Visibility.class);
            case SetActive -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.Active.class);
            case AttackStart -> 0;
            case SummonGo -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.SummonGO.class);
            case KillUnit -> 0;
            case ActivateTaxi -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.Taxi.class);
            case WpStart -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.WpStart.class);
            case WpPause -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.WpPause.class);
            case WpStop -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.WpStop.class);
            case AddItem -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.Item.class);
            case RemoveItem -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.Item.class);
            case SetRun -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.SetRun.class);
            case SetDisableGravity -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.SetDisableGravity.class);
            case Teleport -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.Teleport.class);
            case SetCounter -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.SetCounter.class);
            case StoreTargetList -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.StoreTargets.class);
            case WpResume -> 0;
            case SetOrientation -> 0;
            case CreateTimedEvent -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.TimeEvent.class);
            case Playmovie -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.Movie.class);
            case MoveToPos -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.MoveToPos.class);
            case EnableTempGobj -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.EnableTempGO.class);
            case Equip -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.Equip.class);
            case CloseGossip -> 0;
            case TriggerTimedEvent -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.TimeEvent.class);
            case RemoveTimedEvent -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.TimeEvent.class);
            case CallScriptReset -> 0;
            case SetRangedMovement -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.SetRangedMovement.class);
            case CallTimedActionlist -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.TimedActionList.class);
            case SetNpcFlag -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.Flag.class);
            case AddNpcFlag -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.Flag.class);
            case RemoveNpcFlag -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.Flag.class);
            case SimpleTalk -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.SimpleTalk.class);
            case SelfCast -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.Cast.class);
            case CrossCast -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.CrossCast.class);
            case CallRandomTimedActionlist -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.RandTimedActionList.class);
            case CallRandomRangeTimedActionlist -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.RandRangeTimedActionList.class);
            case RandomMove -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.MoveRandom.class);
            case SetUnitFieldBytes1 -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.SetunitByte.class);
            case RemoveUnitFieldBytes1 -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.DelunitByte.class);
            case InterruptSpell -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.InterruptSpellCasting.class);
            case AddDynamicFlag -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.Flag.class);
            case RemoveDynamicFlag -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.Flag.class);
            case JumpToPos -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.Jump.class);
            case SendGossipMenu -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.SendGossipMenu.class);
            case GoSetLootState -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.SetGoLootState.class);
            case SendTargetToTarget -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.SendTargetToTarget.class);
            case SetHomePos -> 0;
            case SetHealthRegen -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.SetHealthRegen.class);
            case SetRoot -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.SetRoot.class);
            case SummonCreatureGroup -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.CreatureGroup.class);
            case SetPower -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.Power.class);
            case AddPower -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.Power.class);
            case RemovePower -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.Power.class);
            case GameEventStop -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.GameEventStop.class);
            case GameEventStart -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.GameEventStart.class);
            case StartClosestWaypoint -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.ClosestWaypointFromList.class);
            case MoveOffset -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.MoveOffset.class);
            case RandomSound -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.RandomSound.class);
            case SetCorpseDelay -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.CorpseDelay.class);
            case DisableEvade -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.DisableEvade.class);
            case GoSetGoState -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.GoState.class);
            case AddThreat -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.Threat.class);
            case LoadEquipment -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.LoadEquipment.class);
            case TriggerRandomTimedEvent -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.RandomTimedEvent.class);
            case PauseMovement -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.PauseMovement.class);
            case PlayAnimkit -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.AnimKit.class);
            case ScenePlay -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.Scene.class);
            case SceneCancel -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.Scene.class);
            case SpawnSpawngroup -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.GroupSpawn.class);
            case DespawnSpawngroup -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.GroupSpawn.class);
            case RespawnBySpawnId -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.RespawnData.class);
            case InvokerCast -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.Cast.class);
            case PlayCinematic -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.Cinematic.class);
            case SetMovementSpeed -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.MovementSpeed.class);
            case PlaySpellVisualKit -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.SpellVisualKit.class);
            case OverrideLight -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.OverrideLight.class);
            case OverrideWeather -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.OverrideWeather.class);
            case SetAIAnimKit -> 0;
            case SetHover -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.SetHover.class);
            case SetHealthPct -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.SetHealthPct.class);
            case CreateConversation -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.Conversation.class);
            case SetImmunePC -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.SetImmunePC.class);
            case SetImmuneNPC -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.SetImmuneNPC.class);
            case SetUninteractible -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.SetUninteractible.class);
            case ActivateGameobject -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.ActivateGameObject.class);
            case AddToStoredTargetList -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.AddToStoredTargets.class);
            case BecomePersonalCloneForPlayer -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.BecomePersonalClone.class);
            case TriggerGameEvent -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.TriggerGameEvent.class);
            case DoAction -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.DoAction.class);
            default -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.Raw.class);
        };

        var rawCount = system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.Raw.class) / (Integer.SIZE / Byte.SIZE);
        var paramsCount = paramsStructSize / (Integer.SIZE / Byte.SIZE);

        for (var index = paramsCount; index < rawCount; index++) {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: uint value = 0;
            int value = 0;

            switch (index) {
                case 0:
                    value = e.action.raw.param1;

                    break;
                case 1:
                    value = e.action.raw.param2;

                    break;
                case 2:
                    value = e.action.raw.param3;

                    break;
                case 3:
                    value = e.action.raw.param4;

                    break;
                case 4:
                    value = e.action.raw.param5;

                    break;
                case 5:
                    value = e.action.raw.param6;

                    break;
            }

            if (value != 0) {
                Log.outWarn(LogFilter.Sql, String.format("SmartAIMgr: %1$s has unused action_param%2$s with value %3$s, it should be 0.", e, index + 1, value));
            }
        }

        return true;
    }

    private static boolean checkUnusedTargetParams(SmartScriptHolder e) {
        var paramsStructSize = switch (e.target.type) {
            case None -> 0;
            case Self -> 0;
            case Victim -> 0;
            case HostileSecondAggro -> system.Runtime.InteropServices.Marshal.SizeOf(SmartTarget.HostilRandom.class);
            case HostileLastAggro -> system.Runtime.InteropServices.Marshal.SizeOf(SmartTarget.HostilRandom.class);
            case HostileRandom -> system.Runtime.InteropServices.Marshal.SizeOf(SmartTarget.HostilRandom.class);
            case HostileRandomNotTop -> system.Runtime.InteropServices.Marshal.SizeOf(SmartTarget.HostilRandom.class);
            case ActionInvoker -> 0;
            case Position -> 0; //Uses X,Y,Z,O
            case CreatureRange -> system.Runtime.InteropServices.Marshal.SizeOf(SmartTarget.UnitRange.class);
            case CreatureGuid -> system.Runtime.InteropServices.Marshal.SizeOf(SmartTarget.UnitGUID.class);
            case CreatureDistance -> system.Runtime.InteropServices.Marshal.SizeOf(SmartTarget.UnitDistance.class);
            case Stored -> system.Runtime.InteropServices.Marshal.SizeOf(SmartTarget.Stored.class);
            case GameobjectRange -> system.Runtime.InteropServices.Marshal.SizeOf(SmartTarget.GoRange.class);
            case GameobjectGuid -> system.Runtime.InteropServices.Marshal.SizeOf(SmartTarget.GoGUID.class);
            case GameobjectDistance -> system.Runtime.InteropServices.Marshal.SizeOf(SmartTarget.GoDistance.class);
            case InvokerParty -> 0;
            case PlayerRange -> system.Runtime.InteropServices.Marshal.SizeOf(SmartTarget.PlayerRange.class);
            case PlayerDistance -> system.Runtime.InteropServices.Marshal.SizeOf(SmartTarget.PlayerDistance.class);
            case ClosestCreature -> system.Runtime.InteropServices.Marshal.SizeOf(SmartTarget.UnitClosest.class);
            case ClosestGameobject -> system.Runtime.InteropServices.Marshal.SizeOf(SmartTarget.GoClosest.class);
            case ClosestPlayer -> system.Runtime.InteropServices.Marshal.SizeOf(SmartTarget.PlayerDistance.class);
            case ActionInvokerVehicle -> 0;
            case OwnerOrSummoner -> system.Runtime.InteropServices.Marshal.SizeOf(SmartTarget.Owner.class);
            case ThreatList -> system.Runtime.InteropServices.Marshal.SizeOf(SmartTarget.ThreatList.class);
            case ClosestEnemy -> system.Runtime.InteropServices.Marshal.SizeOf(SmartTarget.ClosestAttackable.class);
            case ClosestFriendly -> system.Runtime.InteropServices.Marshal.SizeOf(SmartTarget.ClosestFriendly.class);
            case LootRecipients -> 0;
            case Farthest -> system.Runtime.InteropServices.Marshal.SizeOf(SmartTarget.Farthest.class);
            case VehiclePassenger -> system.Runtime.InteropServices.Marshal.SizeOf(SmartTarget.Vehicle.class);
            case ClosestUnspawnedGameobject -> system.Runtime.InteropServices.Marshal.SizeOf(SmartTarget.GoClosest.class);
            default -> system.Runtime.InteropServices.Marshal.SizeOf(SmartTarget.Raw.class);
        };

        var rawCount = system.Runtime.InteropServices.Marshal.SizeOf(SmartTarget.Raw.class) / (Integer.SIZE / Byte.SIZE);
        var paramsCount = paramsStructSize / (Integer.SIZE / Byte.SIZE);

        for (var index = paramsCount; index < rawCount; index++) {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: uint value = 0;
            int value = 0;

            switch (index) {
                case 0:
                    value = e.target.raw.param1;

                    break;
                case 1:
                    value = e.target.raw.param2;

                    break;
                case 2:
                    value = e.target.raw.param3;

                    break;
                case 3:
                    value = e.target.raw.param4;

                    break;
            }

            if (value != 0) {
                Log.outWarn(LogFilter.Sql, String.format("SmartAIMgr: %1$s has unused target_param%2$s with value %3$s, it must be 0, skipped.", e, index + 1, value));
            }
        }

        return true;
    }

    private boolean isEventValid(SmartScriptHolder e) {
        if (e.event.type.getValue() >= SmartEvents.End.getValue()) {
            Log.outError(LogFilter.ScriptsAi, "SmartAIMgr: EntryOrGuid {0} using event({1}) has invalid event type ({2}), skipped.", e.entryOrGuid, e.eventId, e.getEventType());

            return false;
        }

        // in SMART_SCRIPT_TYPE_TIMED_ACTIONLIST all event types are overriden by core
        if (e.getScriptType() != SmartScriptType.TimedActionlist && !(boolean)(getEventMask(e.event.type) & getTypeMask(e.getScriptType()))) {
            Log.outError(LogFilter.Scripts, "SmartAIMgr: EntryOrGuid {0}, event type {1} can not be used for Script type {2}", e.entryOrGuid, e.getEventType(), e.getScriptType());

            return false;
        }

        if (e.action.type.getValue() <= 0 || e.action.type.getValue() >= SmartActions.End.getValue()) {
            Log.outError(LogFilter.ScriptsAi, "SmartAIMgr: EntryOrGuid {0} using event({1}) has invalid action type ({2}), skipped.", e.entryOrGuid, e.eventId, e.getActionType());

            return false;
        }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: if (e.Event.event_phase_mask > (uint)SmartEventPhaseBits.All)
        if (e.event.eventPhaseMask > (int)SmartEventPhaseBits.All.getValue()) {
            Log.outError(LogFilter.ScriptsAi, "SmartAIMgr: EntryOrGuid {0} using event({1}) has invalid phase mask ({2}), skipped.", e.entryOrGuid, e.eventId, e.event.eventPhaseMask);

            return false;
        }

        if (e.event.eventFlags.getValue() > SmartEventFlags.All.getValue()) {
            Log.outError(LogFilter.ScriptsAi, "SmartAIMgr: EntryOrGuid {0} using event({1}) has invalid event flags ({2}), skipped.", e.entryOrGuid, e.eventId, e.event.eventFlags);

            return false;
        }

        if (e.link != 0 && e.link == e.eventId) {
            Log.outError(LogFilter.Sql, "SmartAIMgr: EntryOrGuid {0} SourceType {1}, Event {2}, Event is linking self (infinite loop), skipped.", e.entryOrGuid, e.getScriptType(), e.eventId);

            return false;
        }

        if (e.getScriptType() == SmartScriptType.TimedActionlist) {
            e.event.type = SmartEvents.UpdateOoc; //force default OOC, can change when calling the script!

            if (!isMinMaxValid(e, e.event.minMaxRepeat.min, e.event.minMaxRepeat.max)) {
                return false;
            }

            if (!isMinMaxValid(e, e.event.minMaxRepeat.repeatMin, e.event.minMaxRepeat.repeatMax)) {
                return false;
            }
        } else {
            switch (e.event.type) {
                case Update:
                case UpdateIc:
                case UpdateOoc:
                case HealthPct:
                case ManaPct:
                case Range:
                case Damaged:
                case DamagedTarget:
                case ReceiveHeal:
                    if (!isMinMaxValid(e, e.event.minMaxRepeat.min, e.event.minMaxRepeat.max)) {
                        return false;
                    }

                    if (!isMinMaxValid(e, e.event.minMaxRepeat.repeatMin, e.event.minMaxRepeat.repeatMax)) {
                        return false;
                    }

                    break;
                case SpellHit:
                case SpellHitTarget:
                    if (e.event.spellHit.spell != 0) {
                        var spellInfo = Global.getSpellMgr().getSpellInfo(e.event.spellHit.spell, Difficulty.None);

                        if (spellInfo == null) {
                            Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s uses non-existent Spell entry %2$s, skipped.", e, e.event.spellHit.spell));

                            return false;
                        }

                        if (e.event.spellHit.school != 0 && (SpellSchoolMask.forValue(e.event.spellHit.school).getValue() & spellInfo.schoolMask.getValue()) != spellInfo.schoolMask.getValue()) {
                            Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s uses Spell entry %2$s with invalid school mask, skipped.", e, e.event.spellHit.spell));

                            return false;
                        }
                    }

                    if (!isMinMaxValid(e, e.event.spellHit.cooldownMin, e.event.spellHit.cooldownMax)) {
                        return false;
                    }

                    break;
                case OnSpellCast:
                case OnSpellFailed:
                case OnSpellStart: {
                    if (!isSpellValid(e, e.event.spellCast.spell)) {
                        return false;
                    }

                    if (!isMinMaxValid(e, e.event.spellCast.cooldownMin, e.event.spellCast.cooldownMax)) {
                        return false;
                    }

                    break;
                }
                case OocLos:
                case IcLos:
                    if (!isMinMaxValid(e, e.event.los.cooldownMin, e.event.los.cooldownMax)) {
                        return false;
                    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: if (e.Event.los.hostilityMode >= (uint)LOSHostilityMode.End)
                    if (e.event.los.hostilityMode >= (int)LOSHostilityMode.End.getValue()) {
                        Log.outError(LogFilter.Sql, String.format("SmartAIMgr: %1$s uses hostilityMode with invalid value %2$s (max allowed value %3$s), skipped.", e, e.event.los.hostilityMode, LOSHostilityMode.End - 1));

                        return false;
                    }

                    tcSaiIsBooleanValid(e, e.event.los.playerOnly);

                    break;
                case Respawn:
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: if (e.Event.respawn.type == (uint)SmartRespawnCondition.Map && CliDB.MapStorage.LookupByKey(e.Event.respawn.map) == null)
                    if (e.event.respawn.type == (int)SmartRespawnCondition.Map.getValue() && CliDB.mapStorage.LookupByKey(e.event.respawn.map) == null) {
                        Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s uses non-existent Map entry %2$s, skipped.", e, e.event.respawn.map));

                        return false;
                    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: if (e.Event.respawn.type == (uint)SmartRespawnCondition.Area && !CliDB.AreaTableStorage.ContainsKey(e.Event.respawn.area))
                    if (e.event.respawn.type == (int)SmartRespawnCondition.Area.getValue() && !CliDB.areaTableStorage.containsKey(e.event.respawn.area)) {
                        Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s uses non-existent Area entry %2$s, skipped.", e, e.event.respawn.area));

                        return false;
                    }

                    break;
                case FriendlyIsCc:
                    if (!isMinMaxValid(e, e.event.friendlyCC.repeatMin, e.event.friendlyCC.repeatMax)) {
                        return false;
                    }

                    break;
                case FriendlyMissingBuff: {
                    if (!isSpellValid(e, e.event.missingBuff.spell)) {
                        return false;
                    }

                    if (!notNULL(e, e.event.missingBuff.radius)) {
                        return false;
                    }

                    if (!isMinMaxValid(e, e.event.missingBuff.repeatMin, e.event.missingBuff.repeatMax)) {
                        return false;
                    }

                    break;
                }
                case Kill:
                    if (!isMinMaxValid(e, e.event.kill.cooldownMin, e.event.kill.cooldownMax)) {
                        return false;
                    }

                    if (e.event.kill.creature != 0 && !isCreatureValid(e, e.event.kill.creature)) {
                        return false;
                    }

                    tcSaiIsBooleanValid(e, e.event.kill.playerOnly);

                    break;
                case VictimCasting:
                    if (e.event.targetCasting.spellId > 0 && !Global.getSpellMgr().hasSpellInfo(e.event.targetCasting.spellId, Difficulty.None)) {
                        Log.outError(LogFilter.Sql, String.format("SmartAIMgr: Entry %1$s SourceType %2$s Event %3$s Action %4$s uses non-existent Spell entry %5$s, skipped.", e.entryOrGuid, e.getScriptType(), e.eventId, e.getActionType(), e.event.spellHit.spell));

                        return false;
                    }

                    if (!isMinMaxValid(e, e.event.minMax.repeatMin, e.event.minMax.repeatMax)) {
                        return false;
                    }

                    break;
                case PassengerBoarded:
                case PassengerRemoved:
                    if (!isMinMaxValid(e, e.event.minMax.repeatMin, e.event.minMax.repeatMax)) {
                        return false;
                    }

                    break;
                case SummonDespawned:
                case SummonedUnit:
                case SummonedUnitDies:
                    if (e.event.summoned.creature != 0 && !isCreatureValid(e, e.event.summoned.creature)) {
                        return false;
                    }

                    if (!isMinMaxValid(e, e.event.summoned.cooldownMin, e.event.summoned.cooldownMax)) {
                        return false;
                    }

                    break;
                case AcceptedQuest:
                case RewardQuest:
                    if (e.event.quest.questId != 0 && !isQuestValid(e, e.event.quest.questId)) {
                        return false;
                    }

                    if (!isMinMaxValid(e, e.event.quest.cooldownMin, e.event.quest.cooldownMax)) {
                        return false;
                    }

                    break;
                case ReceiveEmote: {
                    if (e.event.emote.emoteId != 0 && !isTextEmoteValid(e, e.event.emote.emoteId)) {
                        return false;
                    }

                    if (!isMinMaxValid(e, e.event.emote.cooldownMin, e.event.emote.cooldownMax)) {
                        return false;
                    }

                    break;
                }
                case HasAura:
                case TargetBuffed: {
                    if (!isSpellValid(e, e.event.aura.spell)) {
                        return false;
                    }

                    if (!isMinMaxValid(e, e.event.aura.repeatMin, e.event.aura.repeatMax)) {
                        return false;
                    }

                    break;
                }
                case TransportAddcreature: {
                    if (e.event.transportAddCreature.creature != 0 && !isCreatureValid(e, e.event.transportAddCreature.creature)) {
                        return false;
                    }

                    break;
                }
                case Movementinform: {
                    if (MotionMaster.isInvalidMovementGeneratorType(MovementGeneratorType.forValue(e.event.movementInform.type))) {
                        Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s uses invalid Motion type %2$s, skipped.", e, e.event.movementInform.type));

                        return false;
                    }

                    break;
                }
                case DataSet: {
                    if (!isMinMaxValid(e, e.event.dataSet.cooldownMin, e.event.dataSet.cooldownMax)) {
                        return false;
                    }

                    break;
                }
                case AreatriggerOntrigger: {
                    if (e.event.areatrigger.id != 0 && (e.getScriptType() == SmartScriptType.AreaTriggerEntity || e.getScriptType() == SmartScriptType.AreaTriggerEntityServerside)) {
                        Log.outError(LogFilter.Sql, String.format("SmartAIMgr: Entry %1$s SourceType %2$s Event %3$s Action %4$s areatrigger param not supported for SMART_SCRIPT_TYPE_AREATRIGGER_ENTITY and SMART_SCRIPT_TYPE_AREATRIGGER_ENTITY_SERVERSIDE, skipped.", e.entryOrGuid, e.getScriptType(), e.eventId, e.getActionType()));

                        return false;
                    }

                    if (e.event.areatrigger.id != 0 && !isAreaTriggerValid(e, e.event.areatrigger.id)) {
                        return false;
                    }

                    break;
                }
                case TextOver: {
                    if (!isTextValid(e, e.event.textOver.textGroupID)) {
                        return false;
                    }

                    break;
                }
                case GameEventStart:
                case GameEventEnd: {
                    var events = Global.getGameEventMgr().getEventMap();

                    if (e.event.gameEvent.gameEventId >= events.getLength() || !events[e.event.gameEvent.gameEventId].isValid()) {
                        return false;
                    }

                    break;
                }
                case FriendlyHealthPCT:
                    if (!isMinMaxValid(e, e.event.friendlyHealthPct.repeatMin, e.event.friendlyHealthPct.repeatMax)) {
                        return false;
                    }

                    if (e.event.friendlyHealthPct.maxHpPct > 100 || e.event.friendlyHealthPct.minHpPct > 100) {
                        Log.outError(LogFilter.Sql, String.format("SmartAIMgr: %1$s has pct value above 100, skipped.", e));

                        return false;
                    }

                    switch (e.getTargetType()) {
                        case CreatureRange:
                        case CreatureGuid:
                        case CreatureDistance:
                        case ClosestCreature:
                        case ClosestPlayer:
                        case PlayerRange:
                        case PlayerDistance:
                            break;
                        case ActionInvoker:
                            if (!notNULL(e, e.event.friendlyHealthPct.radius)) {
                                return false;
                            }

                            break;
                        default:
                            Log.outError(LogFilter.Sql, String.format("SmartAIMgr: %1$s uses invalid target_type %2$s, skipped.", e, e.getTargetType()));

                            return false;
                    }

                    break;
                case DistanceCreature:
                    if (e.event.distance.guid == 0 && e.event.distance.entry == 0) {
                        Log.outError(LogFilter.Sql, "SmartAIMgr: Event SMART_EVENT_DISTANCE_CREATURE did not provide creature guid or entry, skipped.");

                        return false;
                    }

                    if (e.event.distance.guid != 0 && e.event.distance.entry != 0) {
                        Log.outError(LogFilter.Sql, "SmartAIMgr: Event SMART_EVENT_DISTANCE_CREATURE provided both an entry and guid, skipped.");

                        return false;
                    }

                    if (e.event.distance.guid != 0 && Global.getObjectMgr().getCreatureData(e.event.distance.guid) == null) {
                        Log.outError(LogFilter.Sql, "SmartAIMgr: Event SMART_EVENT_DISTANCE_CREATURE using invalid creature guid {0}, skipped.", e.event.distance.guid);

                        return false;
                    }

                    if (e.event.distance.entry != 0 && Global.getObjectMgr().getCreatureTemplate(e.event.distance.entry) == null) {
                        Log.outError(LogFilter.Sql, "SmartAIMgr: Event SMART_EVENT_DISTANCE_CREATURE using invalid creature entry {0}, skipped.", e.event.distance.entry);

                        return false;
                    }

                    break;
                case DistanceGameobject:
                    if (e.event.distance.guid == 0 && e.event.distance.entry == 0) {
                        Log.outError(LogFilter.Sql, "SmartAIMgr: Event SMART_EVENT_DISTANCE_GAMEOBJECT did not provide gameobject guid or entry, skipped.");

                        return false;
                    }

                    if (e.event.distance.guid != 0 && e.event.distance.entry != 0) {
                        Log.outError(LogFilter.Sql, "SmartAIMgr: Event SMART_EVENT_DISTANCE_GAMEOBJECT provided both an entry and guid, skipped.");

                        return false;
                    }

                    if (e.event.distance.guid != 0 && Global.getObjectMgr().getGameObjectData(e.event.distance.guid) == null) {
                        Log.outError(LogFilter.Sql, "SmartAIMgr: Event SMART_EVENT_DISTANCE_GAMEOBJECT using invalid gameobject guid {0}, skipped.", e.event.distance.guid);

                        return false;
                    }

                    if (e.event.distance.entry != 0 && Global.getObjectMgr().getGameObjectTemplate(e.event.distance.entry) == null) {
                        Log.outError(LogFilter.Sql, "SmartAIMgr: Event SMART_EVENT_DISTANCE_GAMEOBJECT using invalid gameobject entry {0}, skipped.", e.event.distance.entry);

                        return false;
                    }

                    break;
                case CounterSet:
                    if (!isMinMaxValid(e, e.event.counter.cooldownMin, e.event.counter.cooldownMax)) {
                        return false;
                    }

                    if (e.event.counter.id == 0) {
                        Log.outError(LogFilter.Sql, "SmartAIMgr: Event SMART_EVENT_COUNTER_SET using invalid counter id {0}, skipped.", e.event.counter.id);

                        return false;
                    }

                    if (e.event.counter.value == 0) {
                        Log.outError(LogFilter.Sql, "SmartAIMgr: Event SMART_EVENT_COUNTER_SET using invalid value {0}, skipped.", e.event.counter.value);

                        return false;
                    }

                    break;
                case Reset:
                    if (e.action.type == SmartActions.CallScriptReset) {
                        // There might be SMART_TARGET_* cases where this should be allowed, they will be handled if needed
                        Log.outError(LogFilter.Sql, String.format("SmartAIMgr: %1$s uses event SMART_EVENT_RESET and action SMART_ACTION_CALL_SCRIPT_RESET, skipped.", e));

                        return false;
                    }

                    break;
                case Charmed:
                    tcSaiIsBooleanValid(e, e.event.charm.onRemove);

                    break;
                case QuestObjCompletion:
                    if (Global.getObjectMgr().getQuestObjective(e.event.questObjective.id) == null) {
                        Log.outError(LogFilter.Sql, String.format("SmartAIMgr: Event SMART_EVENT_QUEST_OBJ_COMPLETION using invalid objective id %1$s, skipped.", e.event.questObjective.id));

                        return false;
                    }

                    break;
                case QuestAccepted:
                case QuestCompletion:
                case QuestFail:
                case QuestRewarded:
                    break;
                case Link:
                case GoLootStateChanged:
                case GoEventInform:
                case TimedEventTriggered:
                case InstancePlayerEnter:
                case TransportRelocate:
                case CorpseRemoved:
                case AiInit:
                case ActionDone:
                case TransportAddplayer:
                case TransportRemovePlayer:
                case Aggro:
                case Death:
                case Evade:
                case ReachedHome:
                case JustSummoned:
                case WaypointReached:
                case WaypointPaused:
                case WaypointResumed:
                case WaypointStopped:
                case WaypointEnded:
                case GossipSelect:
                case GossipHello:
                case JustCreated:
                case FollowCompleted:
                case OnSpellclick:
                case OnDespawn:
                case SceneStart:
                case SceneCancel:
                case SceneComplete:
                case SceneTrigger:
                    break;

                //Unused
                case TargetHealthPct:
                case FriendlyHealth:
                case TargetManaPct:
                case CharmedTarget:
                case WaypointStart:
                case PhaseChange:
                case IsBehindTarget:
                    Log.outError(LogFilter.Sql, String.format("SmartAIMgr: Unused event_type %1$s skipped.", e));

                    return false;
                default:
                    Log.outError(LogFilter.ScriptsAi, "SmartAIMgr: Not handled event_type({0}), Entry {1} SourceType {2} Event {3} Action {4}, skipped.", e.getEventType(), e.entryOrGuid, e.getScriptType(), e.eventId, e.getActionType());

                    return false;
            }
        }

        if (!checkUnusedEventParams(e)) {
            return false;
        }

        switch (e.getActionType()) {
            case Talk: {
                tcSaiIsBooleanValid(e, e.action.talk.useTalkTarget);

                if (!isTextValid(e, e.action.talk.textGroupId)) {
                    return false;
                }

                break;
            }
            case SimpleTalk: {
                if (!isTextValid(e, e.action.simpleTalk.textGroupId)) {
                    return false;
                }

                break;
            }
            case SetFaction:
                if (e.action.faction.factionId != 0 && CliDB.factionTemplateStorage.LookupByKey(e.action.faction.factionId) == null) {
                    Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s uses non-existent Faction %2$s, skipped.", e, e.action.faction.factionId));

                    return false;
                }

                break;
            case MorphToEntryOrModel:
            case MountToEntryOrModel:
                if (e.action.morphOrMount.creature != 0 || e.action.morphOrMount.model != 0) {
                    if (e.action.morphOrMount.creature > 0 && Global.getObjectMgr().getCreatureTemplate(e.action.morphOrMount.creature) == null) {
                        Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s uses non-existent Creature entry %2$s, skipped.", e, e.action.morphOrMount.creature));

                        return false;
                    }

                    if (e.action.morphOrMount.model != 0) {
                        if (e.action.morphOrMount.creature != 0) {
                            Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s has ModelID set with also set CreatureId, skipped.", e));

                            return false;
                        } else if (!CliDB.creatureDisplayInfoStorage.containsKey(e.action.morphOrMount.model)) {
                            Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s uses non-existent Model id %2$s, skipped.", e, e.action.morphOrMount.model));

                            return false;
                        }
                    }
                }

                break;
            case Sound:
                if (!isSoundValid(e, e.action.sound.soundId)) {
                    return false;
                }

                tcSaiIsBooleanValid(e, e.action.sound.onlySelf);

                break;
            case SetEmoteState:
            case PlayEmote:
                if (!isEmoteValid(e, e.action.emote.emoteId)) {
                    return false;
                }

                break;
            case PlayAnimkit:
                if (e.action.animKit.animKit != 0 && !isAnimKitValid(e, e.action.animKit.animKit)) {
                    return false;
                }

                if (e.action.animKit.type > 3) {
                    Log.outError(LogFilter.Sql, String.format("SmartAIMgr: %1$s uses invalid AnimKit type %2$s, skipped.", e, e.action.animKit.type));

                    return false;
                }

                break;
            case PlaySpellVisualKit:
                if (e.action.spellVisualKit.spellVisualKitId != 0 && !isSpellVisualKitValid(e, e.action.spellVisualKit.spellVisualKitId)) {
                    return false;
                }

                break;
            case OfferQuest:
                if (!isQuestValid(e, e.action.questOffer.questId)) {
                    return false;
                }

                tcSaiIsBooleanValid(e, e.action.questOffer.directAdd);

                break;
            case FailQuest:
                if (!isQuestValid(e, e.action.quest.questId)) {
                    return false;
                }

                break;
            case ActivateTaxi: {
                if (!CliDB.taxiPathStorage.containsKey(e.action.taxi.id)) {
                    Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s uses invalid Taxi path ID %2$s, skipped.", e, e.action.taxi.id));

                    return false;
                }

                break;
            }
            case RandomEmote:
                if (e.action.randomEmote.emote1 != 0 && !isEmoteValid(e, e.action.randomEmote.emote1)) {
                    return false;
                }

                if (e.action.randomEmote.emote2 != 0 && !isEmoteValid(e, e.action.randomEmote.emote2)) {
                    return false;
                }

                if (e.action.randomEmote.emote3 != 0 && !isEmoteValid(e, e.action.randomEmote.emote3)) {
                    return false;
                }

                if (e.action.randomEmote.emote4 != 0 && !isEmoteValid(e, e.action.randomEmote.emote4)) {
                    return false;
                }

                if (e.action.randomEmote.emote5 != 0 && !isEmoteValid(e, e.action.randomEmote.emote5)) {
                    return false;
                }

                if (e.action.randomEmote.emote6 != 0 && !isEmoteValid(e, e.action.randomEmote.emote6)) {
                    return false;
                }

                break;
            case RandomSound:
                if (e.action.randomSound.sound1 != 0 && !isSoundValid(e, e.action.randomSound.sound1)) {
                    return false;
                }

                if (e.action.randomSound.sound2 != 0 && !isSoundValid(e, e.action.randomSound.sound2)) {
                    return false;
                }

                if (e.action.randomSound.sound3 != 0 && !isSoundValid(e, e.action.randomSound.sound3)) {
                    return false;
                }

                if (e.action.randomSound.sound4 != 0 && !isSoundValid(e, e.action.randomSound.sound4)) {
                    return false;
                }

                tcSaiIsBooleanValid(e, e.action.randomSound.onlySelf);

                break;
            case Cast: {
                if (!isSpellValid(e, e.action.cast.spell)) {
                    return false;
                }

                var spellInfo = Global.getSpellMgr().getSpellInfo(e.action.cast.spell, Difficulty.None);

                for (var spellEffectInfo : spellInfo.getEffects()) {
                    if (spellEffectInfo.isEffect(SpellEffectName.KillCredit) || spellEffectInfo.isEffect(SpellEffectName.KillCredit2)) {
                        if (spellEffectInfo.targetA.getTarget() == Targets.UnitCaster) {
                            Log.outError(LogFilter.Sql, String.format("SmartAIMgr: %1$s Effect: SPELL_EFFECT_KILL_CREDIT: (SpellId: %2$s targetA: %3$s - targetB: %4$s) has invalid target for this Action", e, e.action.cast.spell, spellEffectInfo.targetA.getTarget(), spellEffectInfo.targetB.getTarget()));
                        }
                    }
                }

                break;
            }
            case CrossCast: {
                if (!isSpellValid(e, e.action.crossCast.spell)) {
                    return false;
                }

                var targetType = SmartTargets.forValue(e.action.crossCast.targetType);

                if (targetType == SmartTargets.CreatureGuid || targetType == SmartTargets.GameobjectGuid) {
                    if (e.action.crossCast.targetParam2 != 0) {
                        if (targetType == SmartTargets.CreatureGuid && !isCreatureValid(e, e.action.crossCast.targetParam2)) {
                            return false;
                        } else if (targetType == SmartTargets.GameobjectGuid && !isGameObjectValid(e, e.action.crossCast.targetParam2)) {
                            return false;
                        }
                    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: ulong guid = e.Action.crossCast.targetParam1;
                    long guid = e.action.crossCast.targetParam1;
                    var spawnType = targetType == SmartTargets.CreatureGuid ? SpawnObjectType.Creature : SpawnObjectType.GameObject;
                    var data = Global.getObjectMgr().getSpawnData(spawnType, guid);

                    if (data == null) {
                        Log.outError(LogFilter.Sql, String.format("SmartAIMgr: %1$s specifies invalid CasterTargetType guid (%2$s,%3$s)", e, spawnType, guid));

                        return false;
                    } else if (e.action.crossCast.targetParam2 != 0 && e.action.crossCast.targetParam2 != data.id) {
                        Log.outError(LogFilter.Sql, String.format("SmartAIMgr: %1$s specifies invalid entry %2$s (expected %3$s) for CasterTargetType guid (%4$s,%5$s)", e, e.action.crossCast.targetParam2, data.id, spawnType, guid));

                        return false;
                    }
                }

                break;
            }
            case InvokerCast:
                if (e.getScriptType() != SmartScriptType.TimedActionlist && e.getEventType() != SmartEvents.Link && !eventHasInvoker(e.event.type)) {
                    Log.outError(LogFilter.Sql, String.format("SmartAIMgr: %1$s has invoker cast action, but event does not provide any invoker!", e));

                    return false;
                }

                if (!isSpellValid(e, e.action.cast.spell)) {
                    return false;
                }

                break;
            case SelfCast:
                if (!isSpellValid(e, e.action.cast.spell)) {
                    return false;
                }

                break;
            case CallAreaexploredoreventhappens:
            case CallGroupeventhappens:
                var qid = Global.getObjectMgr().getQuestTemplate(e.action.quest.questId);

                if (qid != null) {
                    if (!qid.hasSpecialFlag(QuestSpecialFlags.ExplorationOrEvent)) {
                        Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s SpecialFlags for Quest entry %2$s does not include FLAGS_EXPLORATION_OR_EVENT(2), skipped.", e, e.action.quest.questId));

                        return false;
                    }
                } else {
                    Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s uses non-existent Quest entry %2$s, skipped.", e, e.action.quest.questId));

                    return false;
                }

                break;
            case SetEventPhase:
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: if (e.Action.setEventPhase.phase >= (uint)SmartPhase.Max)
                if (e.action.setEventPhase.phase >= (int)SmartPhase.Max.getValue()) {
                    Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s attempts to set phase %2$s. Phase mask cannot be used past phase %3$s, skipped.", e, e.action.setEventPhase.phase, SmartPhase.Max - 1));

                    return false;
                }

                break;
            case IncEventPhase:
                if (e.action.incEventPhase.inc == 0 && e.action.incEventPhase.dec == 0) {
                    Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s is incrementing phase by 0, skipped.", e));

                    return false;
                }
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: else if (e.Action.incEventPhase.inc > (uint)SmartPhase.Max || e.Action.incEventPhase.dec > (uint)SmartPhase.Max)
                else if (e.action.incEventPhase.inc > (int)SmartPhase.Max.getValue() || e.action.incEventPhase.dec > (int)SmartPhase.Max.getValue()) {
                    Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s attempts to increment phase by too large value, skipped.", e));

                    return false;
                }

                break;
            case RemoveAurasFromSpell:
                if (e.action.removeAura.spell != 0 && !isSpellValid(e, e.action.removeAura.spell)) {
                    return false;
                }

                tcSaiIsBooleanValid(e, e.action.removeAura.onlyOwnedAuras);

                break;
            case RandomPhase: {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: if (e.Action.randomPhase.phase1 >= (uint)SmartPhase.Max || e.Action.randomPhase.phase2 >= (uint)SmartPhase.Max || e.Action.randomPhase.phase3 >= (uint)SmartPhase.Max || e.Action.randomPhase.phase4 >= (uint)SmartPhase.Max || e.Action.randomPhase.phase5 >= (uint)SmartPhase.Max || e.Action.randomPhase.phase6 >= (uint)SmartPhase.Max)
                if (e.action.randomPhase.phase1 >= (int)SmartPhase.Max.getValue() || e.action.randomPhase.phase2 >= (int)SmartPhase.Max.getValue() || e.action.randomPhase.phase3 >= (int)SmartPhase.Max.getValue() || e.action.randomPhase.phase4 >= (int)SmartPhase.Max.getValue() || e.action.randomPhase.phase5 >= (int)SmartPhase.Max.getValue() || e.action.randomPhase.phase6 >= (int)SmartPhase.Max.getValue()) {
                    Log.outError(LogFilter.Sql, String.format("SmartAIMgr: Entry %1$s SourceType %2$s Event %3$s Action %4$s attempts to set invalid phase, skipped.", e.entryOrGuid, e.getScriptType(), e.eventId, e.getActionType()));

                    return false;
                }

                break;
            }
            case RandomPhaseRange: { //PhaseMin, PhaseMax
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: if (e.Action.randomPhaseRange.phaseMin >= (uint)SmartPhase.Max || e.Action.randomPhaseRange.phaseMax >= (uint)SmartPhase.Max)
                if (e.action.randomPhaseRange.phaseMin >= (int)SmartPhase.Max.getValue() || e.action.randomPhaseRange.phaseMax >= (int)SmartPhase.Max.getValue()) {
                    Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s attempts to set invalid phase, skipped.", e));

                    return false;
                }

                if (!isMinMaxValid(e, e.action.randomPhaseRange.phaseMin, e.action.randomPhaseRange.phaseMax)) {
                    return false;
                }

                break;
            }
            case SummonCreature:
                if (!isCreatureValid(e, e.action.summonCreature.creature)) {
                    return false;
                }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: if (e.Action.summonCreature.type < (uint)TempSummonType.TimedOrDeadDespawn || e.Action.summonCreature.type > (uint)TempSummonType.ManualDespawn)
                if (e.action.summonCreature.type < (int)TempSummonType.TimedOrDeadDespawn.getValue() || e.action.summonCreature.type > (int)TempSummonType.ManualDespawn.getValue()) {
                    Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s uses incorrect TempSummonType %2$s, skipped.", e, e.action.summonCreature.type));

                    return false;
                }

                tcSaiIsBooleanValid(e, e.action.summonCreature.attackInvoker);

                break;
            case CallKilledmonster:
                if (!isCreatureValid(e, e.action.killedMonster.creature)) {
                    return false;
                }

                if (e.getTargetType() == SmartTargets.Position) {
                    Log.outError(LogFilter.Sql, String.format("SmartAIMgr: %1$s uses incorrect TargetType %2$s, skipped.", e, e.getTargetType()));

                    return false;
                }

                break;
            case UpdateTemplate:
                if (e.action.updateTemplate.creature != 0 && !isCreatureValid(e, e.action.updateTemplate.creature)) {
                    return false;
                }

                tcSaiIsBooleanValid(e, e.action.updateTemplate.updateLevel);

                break;
            case SetSheath:
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: if (e.Action.setSheath.sheath != 0 && e.Action.setSheath.sheath >= (uint)SheathState.Max)
                if (e.action.setSheath.sheath != 0 && e.action.setSheath.sheath >= (int)SheathState.Max.getValue()) {
                    Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s uses incorrect Sheath state %2$s, skipped.", e, e.action.setSheath.sheath));

                    return false;
                }

                break;
            case SetReactState: {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: if (e.Action.react.state > (uint)ReactStates.Aggressive)
                if (e.action.react.state > (int)ReactStates.Aggressive.getValue()) {
                    Log.outError(LogFilter.ScriptsAi, "SmartAIMgr: Creature {0} Event {1} Action {2} uses invalid React State {3}, skipped.", e.entryOrGuid, e.eventId, e.getActionType(), e.action.react.state);

                    return false;
                }

                break;
            }
            case SummonGo:
                if (!isGameObjectValid(e, e.action.summonGO.entry)) {
                    return false;
                }

                break;
            case RemoveItem:
                if (!isItemValid(e, e.action.item.entry)) {
                    return false;
                }

                if (!notNULL(e, e.action.item.count)) {
                    return false;
                }

                break;
            case AddItem:
                if (!isItemValid(e, e.action.item.entry)) {
                    return false;
                }

                if (!notNULL(e, e.action.item.count)) {
                    return false;
                }

                break;
            case Teleport:
                if (!CliDB.mapStorage.containsKey(e.action.teleport.mapID)) {
                    Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s uses non-existent Map entry %2$s, skipped.", e, e.action.teleport.mapID));

                    return false;
                }

                break;
            case WpStop:
                if (e.action.wpStop.quest != 0 && !isQuestValid(e, e.action.wpStop.quest)) {
                    return false;
                }

                tcSaiIsBooleanValid(e, e.action.wpStop.fail);

                break;
            case WpStart: {
                var path = getPath(e.action.wpStart.pathID);

                if (path == null || path.nodes.Empty()) {
                    Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s uses non-existent WaypointPath id %2$s, skipped.", e, e.action.wpStart.pathID));

                    return false;
                }

                if (e.action.wpStart.quest != 0 && !isQuestValid(e, e.action.wpStart.quest)) {
                    return false;
                }

                tcSaiIsBooleanValid(e, e.action.wpStart.run);
                tcSaiIsBooleanValid(e, e.action.wpStart.repeat);

                break;
            }
            case CreateTimedEvent: {
                if (!isMinMaxValid(e, e.action.timeEvent.min, e.action.timeEvent.max)) {
                    return false;
                }

                if (!isMinMaxValid(e, e.action.timeEvent.repeatMin, e.action.timeEvent.repeatMax)) {
                    return false;
                }

                break;
            }
            case CallRandomRangeTimedActionlist: {
                if (!isMinMaxValid(e, e.action.randRangeTimedActionList.idMin, e.action.randRangeTimedActionList.idMax)) {
                    return false;
                }

                break;
            }
            case SetPower:
            case AddPower:
            case RemovePower:
                if (e.action.power.powerType > PowerType.Max.getValue()) {
                    Log.outError(LogFilter.Sql, String.format("SmartAIMgr: %1$s uses non-existent Power %2$s, skipped.", e, e.action.power.powerType));

                    return false;
                }

                break;
            case GameEventStop: {
                var eventId = e.action.gameEventStop.id;

                var events = Global.getGameEventMgr().getEventMap();

                if (eventId < 1 || eventId >= events.getLength()) {
                    Log.outError(LogFilter.Sql, String.format("SmartAIMgr: %1$s uses non-existent event, eventId %2$s, skipped.", e, e.action.gameEventStop.id));

                    return false;
                }

                var eventData = events[eventId];

                if (!eventData.isValid()) {
                    Log.outError(LogFilter.Sql, String.format("SmartAIMgr: %1$s uses non-existent event, eventId %2$s, skipped.", e, e.action.gameEventStop.id));

                    return false;
                }

                break;
            }
            case GameEventStart: {
                var eventId = e.action.gameEventStart.id;

                var events = Global.getGameEventMgr().getEventMap();

                if (eventId < 1 || eventId >= events.getLength()) {
                    Log.outError(LogFilter.Sql, String.format("SmartAIMgr: %1$s uses non-existent event, eventId %2$s, skipped.", e, e.action.gameEventStart.id));

                    return false;
                }

                var eventData = events[eventId];

                if (!eventData.isValid()) {
                    Log.outError(LogFilter.Sql, String.format("SmartAIMgr: %1$s uses non-existent event, eventId %2$s, skipped.", e, e.action.gameEventStart.id));

                    return false;
                }

                break;
            }
            case Equip: {
                if (e.getScriptType() == SmartScriptType.Creature) {
                    var equipId = (byte)e.action.equip.entry;

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: if (equipId != 0 && Global.ObjectMgr.GetEquipmentInfo((uint)e.EntryOrGuid, equipId) == null)
                    if (equipId != 0 && Global.getObjectMgr().getEquipmentInfo((int)e.entryOrGuid, equipId) == null) {
                        Log.outError(LogFilter.Sql, "SmartScript: SMART_ACTION_EQUIP uses non-existent equipment info id {0} for creature {1}, skipped.", equipId, e.entryOrGuid);

                        return false;
                    }
                }

                break;
            }
            case SetInstData: {
                if (e.action.setInstanceData.type > 1) {
                    Log.outError(LogFilter.Sql, String.format("SmartAIMgr: %1$s uses invalid data type %2$s (value range 0-1), skipped.", e, e.action.setInstanceData.type));

                    return false;
                } else if (e.action.setInstanceData.type == 1) {
                    if (e.action.setInstanceData.data > EncounterState.ToBeDecided.getValue()) {
                        Log.outError(LogFilter.Sql, String.format("SmartAIMgr: %1$s uses invalid boss state %2$s (value range 0-5), skipped.", e, e.action.setInstanceData.data));

                        return false;
                    }
                }

                break;
            }
            case SetIngamePhaseId: {
                var phaseId = e.action.ingamePhaseId.id;
                var apply = e.action.ingamePhaseId.apply;

                if (apply != 0 && apply != 1) {
                    Log.outError(LogFilter.Sql, "SmartScript: SMART_ACTION_SET_INGAME_PHASE_ID uses invalid apply value {0} (Should be 0 or 1) for creature {1}, skipped", apply, e.entryOrGuid);

                    return false;
                }

                if (!CliDB.phaseStorage.containsKey(phaseId)) {
                    Log.outError(LogFilter.Sql, "SmartScript: SMART_ACTION_SET_INGAME_PHASE_ID uses invalid phaseid {0} for creature {1}, skipped", phaseId, e.entryOrGuid);

                    return false;
                }

                break;
            }
            case SetIngamePhaseGroup: {
                var phaseGroup = e.action.ingamePhaseGroup.groupId;
                var apply = e.action.ingamePhaseGroup.apply;

                if (apply != 0 && apply != 1) {
                    Log.outError(LogFilter.Sql, "SmartScript: SMART_ACTION_SET_INGAME_PHASE_GROUP uses invalid apply value {0} (Should be 0 or 1) for creature {1}, skipped", apply, e.entryOrGuid);

                    return false;
                }

                if (Global.getDB2Mgr().getPhasesForGroup(phaseGroup).Empty()) {
                    Log.outError(LogFilter.Sql, "SmartScript: SMART_ACTION_SET_INGAME_PHASE_GROUP uses invalid phase group id {0} for creature {1}, skipped", phaseGroup, e.entryOrGuid);

                    return false;
                }

                break;
            }
            case ScenePlay: {
                if (Global.getObjectMgr().getSceneTemplate(e.action.scene.sceneId) == null) {
                    Log.outError(LogFilter.Sql, "SmartScript: SMART_ACTION_SCENE_PLAY uses sceneId {0} but scene don't exist, skipped", e.action.scene.sceneId);

                    return false;
                }

                break;
            }
            case SceneCancel: {
                if (Global.getObjectMgr().getSceneTemplate(e.action.scene.sceneId) == null) {
                    Log.outError(LogFilter.Sql, "SmartScript: SMART_ACTION_SCENE_CANCEL uses sceneId {0} but scene don't exist, skipped", e.action.scene.sceneId);

                    return false;
                }

                break;
            }
            case RespawnBySpawnId: {
                if (Global.getObjectMgr().getSpawnData(SpawnObjectType.forValue(e.action.respawnData.spawnType), e.action.respawnData.spawnId) == null) {
                    Log.outError(LogFilter.Sql, String.format("Entry %1$s SourceType %2$s Event %3$s Action %4$s specifies invalid spawn data (%5$s,%6$s)", e.entryOrGuid, e.getScriptType(), e.eventId, e.getActionType(), e.action.respawnData.spawnType, e.action.respawnData.spawnId));

                    return false;
                }

                break;
            }
            case EnableTempGobj: {
                if (e.action.enableTempGO.duration == 0) {
                    Log.outError(LogFilter.Sql, String.format("Entry %1$s SourceType %2$s Event %3$s Action %4$s does not specify duration", e.entryOrGuid, e.getScriptType(), e.eventId, e.getActionType()));

                    return false;
                }

                break;
            }
            case PlayCinematic: {
                if (!CliDB.cinematicSequencesStorage.containsKey(e.action.cinematic.entry)) {
                    Log.outError(LogFilter.Sql, String.format("SmartAIMgr: SMART_ACTION_PLAY_CINEMATIC %1$s uses invalid entry %2$s, skipped.", e, e.action.cinematic.entry));

                    return false;
                }

                break;
            }
            case PauseMovement: {
                if (e.action.pauseMovement.pauseTimer == 0) {
                    Log.outError(LogFilter.Sql, String.format("SmartAIMgr: %1$s does not specify pause duration", e));

                    return false;
                }

                tcSaiIsBooleanValid(e, e.action.pauseMovement.force);

                break;
            }
            case SetMovementSpeed: {
                if (e.action.movementSpeed.movementType >= MovementGeneratorType.Max.getValue()) {
                    Log.outError(LogFilter.Sql, String.format("SmartAIMgr: Entry %1$s SourceType %2$s Event %3$s Action %4$s uses invalid movementType %5$s, skipped.", e.entryOrGuid, e.getScriptType(), e.eventId, e.getActionType(), e.action.movementSpeed.movementType));

                    return false;
                }

                if (e.action.movementSpeed.speedInteger == 0 && e.action.movementSpeed.speedFraction == 0) {
                    Log.outError(LogFilter.Sql, String.format("SmartAIMgr: Entry %1$s SourceType %2$s Event %3$s Action %4$s uses speed 0, skipped.", e.entryOrGuid, e.getScriptType(), e.eventId, e.getActionType()));

                    return false;
                }

                break;
            }
            case OverrideLight: {
                var areaEntry = CliDB.areaTableStorage.LookupByKey(e.action.overrideLight.zoneId);

                if (areaEntry == null) {
                    Log.outError(LogFilter.Sql, String.format("SmartAIMgr: %1$s uses non-existent zoneId %2$s, skipped.", e, e.action.overrideLight.zoneId));

                    return false;
                }

                if (areaEntry.ParentAreaID != 0) {
                    Log.outError(LogFilter.Sql, String.format("SmartAIMgr: %1$s uses subzone (ID: %2$s) instead of zone, skipped.", e, e.action.overrideLight.zoneId));

                    return false;
                }

                if (!CliDB.lightStorage.containsKey(e.action.overrideLight.areaLightId)) {
                    Log.outError(LogFilter.Sql, String.format("SmartAIMgr: %1$s uses non-existent areaLightId %2$s, skipped.", e, e.action.overrideLight.areaLightId));

                    return false;
                }

                if (e.action.overrideLight.overrideLightId != 0 && !CliDB.lightStorage.containsKey(e.action.overrideLight.overrideLightId)) {
                    Log.outError(LogFilter.Sql, String.format("SmartAIMgr: %1$s uses non-existent overrideLightId %2$s, skipped.", e, e.action.overrideLight.overrideLightId));

                    return false;
                }

                break;
            }
            case OverrideWeather: {
                var areaEntry = CliDB.areaTableStorage.LookupByKey(e.action.overrideWeather.zoneId);

                if (areaEntry == null) {
                    Log.outError(LogFilter.Sql, String.format("SmartAIMgr: %1$s uses non-existent zoneId %2$s, skipped.", e, e.action.overrideWeather.zoneId));

                    return false;
                }

                if (areaEntry.ParentAreaID != 0) {
                    Log.outError(LogFilter.Sql, String.format("SmartAIMgr: %1$s uses subzone (ID: %2$s) instead of zone, skipped.", e, e.action.overrideWeather.zoneId));

                    return false;
                }

                break;
            }
            case SetAIAnimKit: {
                Log.outError(LogFilter.Sql, String.format("SmartAIMgr: Deprecated Event:(%1$s) skipped.", e));

                break;
            }
            case SetHealthPct: {
                if (e.action.setHealthPct.percent > 100 || e.action.setHealthPct.percent == 0) {
                    Log.outError(LogFilter.Sql, String.format("SmartAIMgr: %1$s is trying to set invalid HP percent %2$s, skipped.", e, e.action.setHealthPct.percent));

                    return false;
                }

                break;
            }
            case AutoAttack: {
                tcSaiIsBooleanValid(e, e.action.autoAttack.attack);

                break;
            }
            case AllowCombatMovement: {
                tcSaiIsBooleanValid(e, e.action.combatMove.move);

                break;
            }
            case CallForHelp: {
                tcSaiIsBooleanValid(e, e.action.callHelp.withEmote);

                break;
            }
            case SetVisibility: {
                tcSaiIsBooleanValid(e, e.action.visibility.state);

                break;
            }
            case SetActive: {
                tcSaiIsBooleanValid(e, e.action.active.state);

                break;
            }
            case SetRun: {
                tcSaiIsBooleanValid(e, e.action.setRun.run);

                break;
            }
            case SetDisableGravity: {
                tcSaiIsBooleanValid(e, e.action.setDisableGravity.disable);

                break;
            }
            case SetCounter: {
                tcSaiIsBooleanValid(e, e.action.setCounter.reset);

                break;
            }
            case CallTimedActionlist: {
                tcSaiIsBooleanValid(e, e.action.timedActionList.allowOverride);

                break;
            }
            case InterruptSpell: {
                tcSaiIsBooleanValid(e, e.action.interruptSpellCasting.withDelayed);
                tcSaiIsBooleanValid(e, e.action.interruptSpellCasting.withInstant);

                break;
            }
            case FleeForAssist: {
                tcSaiIsBooleanValid(e, e.action.fleeAssist.withEmote);

                break;
            }
            case MoveToPos: {
                tcSaiIsBooleanValid(e, e.action.moveToPos.transport);
                tcSaiIsBooleanValid(e, e.action.moveToPos.disablePathfinding);

                break;
            }
            case SetRoot: {
                tcSaiIsBooleanValid(e, e.action.setRoot.root);

                break;
            }
            case DisableEvade: {
                tcSaiIsBooleanValid(e, e.action.disableEvade.disable);

                break;
            }
            case LoadEquipment: {
                tcSaiIsBooleanValid(e, e.action.loadEquipment.force);

                break;
            }
            case SetHover: {
                tcSaiIsBooleanValid(e, e.action.setHover.enable);

                break;
            }
            case Evade: {
                tcSaiIsBooleanValid(e, e.action.evade.toRespawnPosition);

                break;
            }
            case SetHealthRegen: {
                tcSaiIsBooleanValid(e, e.action.setHealthRegen.regenHealth);

                break;
            }
            case CreateConversation: {
                if (Global.getConversationDataStorage().getConversationTemplate(e.action.conversation.id) == null) {
                    Log.outError(LogFilter.Sql, String.format("SmartAIMgr: SMART_ACTION_CREATE_CONVERSATION Entry %1$s SourceType %2$s Event %3$s Action %4$s uses invalid entry %5$s, skipped.", e.entryOrGuid, e.getScriptType(), e.eventId, e.getActionType(), e.action.conversation.id));

                    return false;
                }

                break;
            }
            case SetImmunePC: {
                tcSaiIsBooleanValid(e, e.action.setImmunePC.immunePC);

                break;
            }
            case SetImmuneNPC: {
                tcSaiIsBooleanValid(e, e.action.setImmuneNPC.immuneNPC);

                break;
            }
            case SetUninteractible: {
                tcSaiIsBooleanValid(e, e.action.setUninteractible.uninteractible);

                break;
            }
            case ActivateGameobject: {
                if (!notNULL(e, e.action.activateGameObject.gameObjectAction)) {
                    return false;
                }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: if (e.Action.activateGameObject.gameObjectAction >= (uint)GameObjectActions.Max)
                if (e.action.activateGameObject.gameObjectAction >= (int)GameObjectActions.Max.getValue()) {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: Log.outError(LogFilter.Sql, string.Format("SmartAIMgr: {0} has gameObjectAction parameter out of range (max allowed {1}, current value {2}), skipped.", e, (uint)GameObjectActions.Max - 1, e.Action.activateGameObject.gameObjectAction));
                    Log.outError(LogFilter.Sql, String.format("SmartAIMgr: %1$s has gameObjectAction parameter out of range (max allowed %2$s, current value %3$s), skipped.", e, (int)GameObjectActions.Max.getValue() - 1, e.action.activateGameObject.gameObjectAction));

                    return false;
                }

                break;
            }
            case StartClosestWaypoint:
            case Follow:
            case SetOrientation:
            case StoreTargetList:
            case CombatStop:
            case Die:
            case SetInCombatWithZone:
            case WpResume:
            case KillUnit:
            case SetInvincibilityHpLevel:
            case ResetGobject:
            case AttackStart:
            case ThreatAllPct:
            case ThreatSinglePct:
            case SetInstData64:
            case SetData:
            case AttackStop:
            case WpPause:
            case ForceDespawn:
            case Playmovie:
            case CloseGossip:
            case TriggerTimedEvent:
            case RemoveTimedEvent:
            case ActivateGobject:
            case CallScriptReset:
            case SetRangedMovement:
            case SetNpcFlag:
            case AddNpcFlag:
            case RemoveNpcFlag:
            case CallRandomTimedActionlist:
            case RandomMove:
            case SetUnitFieldBytes1:
            case RemoveUnitFieldBytes1:
            case JumpToPos:
            case SendGossipMenu:
            case GoSetLootState:
            case GoSetGoState:
            case SendTargetToTarget:
            case SetHomePos:
            case SummonCreatureGroup:
            case MoveOffset:
            case SetCorpseDelay:
            case AddThreat:
            case TriggerRandomTimedEvent:
            case SpawnSpawngroup:
            case AddToStoredTargetList:
            case DoAction:
                break;
            case BecomePersonalCloneForPlayer: {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: if (e.Action.becomePersonalClone.type < (uint)TempSummonType.TimedOrDeadDespawn || e.Action.becomePersonalClone.type > (uint)TempSummonType.ManualDespawn)
                if (e.action.becomePersonalClone.type < (int)TempSummonType.TimedOrDeadDespawn.getValue() || e.action.becomePersonalClone.type > (int)TempSummonType.ManualDespawn.getValue()) {
                    Log.outError(LogFilter.Sql, String.format("SmartAIMgr: %1$s uses incorrect TempSummonType %2$s, skipped.", e, e.action.becomePersonalClone.type));

                    return false;
                }

                break;
            }
            case TriggerGameEvent: {
                tcSaiIsBooleanValid(e, e.action.triggerGameEvent.useSaiTargetAsGameEventSource);

                break;
            }
            // No longer supported
            case SetUnitFlag:
            case RemoveUnitFlag:
            case InstallAITemplate:
            case SetSwim:
            case AddAura:
            case OverrideScriptBaseObject:
            case ResetScriptBaseObject:
            case SendGoCustomAnim:
            case SetDynamicFlag:
            case AddDynamicFlag:
            case RemoveDynamicFlag:
            case SetGoFlag:
            case AddGoFlag:
            case RemoveGoFlag:
            case SetCanFly:
            case RemoveAurasByType:
            case SetSightDist:
            case Flee:
            case RemoveAllGameobjects:
                Log.outError(LogFilter.Sql, String.format("SmartAIMgr: Unused action_type: %1$s Skipped.", e));

                return false;
            default:
                Log.outError(LogFilter.ScriptsAi, "SmartAIMgr: Not handled action_type({0}), event_type({1}), Entry {2} SourceType {3} Event {4}, skipped.", e.getActionType(), e.getEventType(), e.entryOrGuid, e.getScriptType(), e.eventId);

                return false;
        }

        if (!checkUnusedActionParams(e)) {
            return false;
        }

        return true;
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: static bool IsAnimKitValid(SmartScriptHolder e, uint entry)
    private static boolean isAnimKitValid(SmartScriptHolder e, int entry) {
        if (!CliDB.animKitStorage.containsKey(entry)) {
            Log.outError(LogFilter.Sql, String.format("SmartAIMgr: %1$s uses non-existent AnimKit entry %2$s, skipped.", e, entry));

            return false;
        }

        return true;
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: static bool IsTextValid(SmartScriptHolder e, uint id)
    private static boolean isTextValid(SmartScriptHolder e, int id) {
        if (e.getScriptType() != SmartScriptType.Creature) {
            return true;
        }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: uint entry;
        int entry;

        if (e.getEventType() == SmartEvents.TextOver) {
            entry = e.event.textOver.creatureEntry;
        } else {
            switch (e.getTargetType()) {
                case CreatureDistance:
                case CreatureRange:
                case ClosestCreature:
                    return true; // ignore
                default:
                    if (e.entryOrGuid < 0) {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: var guid = (ulong)-e.EntryOrGuid;
                        var guid = (long)-e.entryOrGuid;
                        var data = Global.getObjectMgr().getCreatureData(guid);

                        if (data == null) {
                            Log.outError(LogFilter.Sql, String.format("SmartAIMgr: %1$s using non-existent Creature guid %2$s, skipped.", e, guid));

                            return false;
                        } else {
                            entry = data.id;
                        }
                    } else {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: entry = (uint)e.EntryOrGuid;
                        entry = (int)e.entryOrGuid;
                    }

                    break;
            }
        }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: if (entry == 0 || !Global.CreatureTextMgr.TextExist(entry, (byte)id))
        if (entry == 0 || !Global.getCreatureTextMgr().textExist(entry, (byte)id)) {
            Log.outError(LogFilter.Sql, String.format("SmartAIMgr: %1$s using non-existent Text id %2$s, skipped.", e, id));

            return false;
        }

        return true;
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: static bool IsCreatureValid(SmartScriptHolder e, uint entry)
    private static boolean isCreatureValid(SmartScriptHolder e, int entry) {
        if (Global.getObjectMgr().getCreatureTemplate(entry) == null) {
            Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s uses non-existent Creature entry %2$s, skipped.", e, entry));

            return false;
        }

        return true;
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: static bool IsGameObjectValid(SmartScriptHolder e, uint entry)
    private static boolean isGameObjectValid(SmartScriptHolder e, int entry) {
        if (Global.getObjectMgr().getGameObjectTemplate(entry) == null) {
            Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s uses non-existent GameObject entry %2$s, skipped.", e, entry));

            return false;
        }

        return true;
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: static bool IsQuestValid(SmartScriptHolder e, uint entry)
    private static boolean isQuestValid(SmartScriptHolder e, int entry) {
        if (Global.getObjectMgr().getQuestTemplate(entry) == null) {
            Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s uses non-existent Quest entry %2$s, skipped.", e, entry));

            return false;
        }

        return true;
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: static bool IsSpellValid(SmartScriptHolder e, uint entry)
    private static boolean isSpellValid(SmartScriptHolder e, int entry) {
        if (!Global.getSpellMgr().hasSpellInfo(entry, Difficulty.None)) {
            Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s uses non-existent Spell entry %2$s, skipped.", e, entry));

            return false;
        }

        return true;
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: static bool IsMinMaxValid(SmartScriptHolder e, uint min, uint max)
    private static boolean isMinMaxValid(SmartScriptHolder e, int min, int max) {
        if (max < min) {
            Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s uses min/max params wrong (%2$s/%3$s), skipped.", e, min, max));

            return false;
        }

        return true;
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: static bool NotNULL(SmartScriptHolder e, uint data)
    private static boolean notNULL(SmartScriptHolder e, int data) {
        if (data == 0) {
            Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s Parameter can not be NULL, skipped.", e));

            return false;
        }

        return true;
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: static bool IsEmoteValid(SmartScriptHolder e, uint entry)
    private static boolean isEmoteValid(SmartScriptHolder e, int entry) {
        if (!CliDB.emotesStorage.containsKey(entry)) {
            Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s uses non-existent Emote entry %2$s, skipped.", e, entry));

            return false;
        }

        return true;
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: static bool IsItemValid(SmartScriptHolder e, uint entry)
    private static boolean isItemValid(SmartScriptHolder e, int entry) {
        if (!CliDB.itemSparseStorage.containsKey(entry)) {
            Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s uses non-existent Item entry %2$s, skipped.", e, entry));

            return false;
        }

        return true;
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: static bool IsTextEmoteValid(SmartScriptHolder e, uint entry)
    private static boolean isTextEmoteValid(SmartScriptHolder e, int entry) {
        if (!CliDB.emotesTextStorage.containsKey(entry)) {
            Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s uses non-existent Text Emote entry %2$s, skipped.", e, entry));

            return false;
        }

        return true;
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: static bool IsAreaTriggerValid(SmartScriptHolder e, uint entry)
    private static boolean isAreaTriggerValid(SmartScriptHolder e, int entry) {
        if (!CliDB.areaTriggerStorage.containsKey(entry)) {
            Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s uses non-existent AreaTrigger entry %2$s, skipped.", e, entry));

            return false;
        }

        return true;
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: static bool IsSoundValid(SmartScriptHolder e, uint entry)
    private static boolean isSoundValid(SmartScriptHolder e, int entry) {
        if (!CliDB.soundKitStorage.containsKey(entry)) {
            Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s uses non-existent Sound entry %2$s, skipped.", e, entry));

            return false;
        }

        return true;
    }
}