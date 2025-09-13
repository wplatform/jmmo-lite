package com.github.azeroth.game.ai;


import com.github.azeroth.game.domain.misc.WaypointNode;
import com.github.azeroth.game.movement.MotionMaster;
import game.waypointPath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;


public class SmartAIManager {
    private final MultiMap<Integer, SmartScriptHolder>[] eventMap = new MultiMap<Integer, SmartScriptHolder>[SmartScriptType.max.getValue()];
    private final HashMap<Integer, waypointPath> waypointStore = new HashMap<Integer, waypointPath>();

    private SmartAIManager() {
        for (byte i = 0; i < SmartScriptType.max.getValue(); i++) {
            _eventMap[i] = new MultiMap<Integer, SmartScriptHolder>();
        }
    }

    public static SmartScriptHolder findLinkedSourceEvent(ArrayList<SmartScriptHolder> list, int eventId) {
        var sch = tangible.ListHelper.find(list, p -> p.link == eventId);

        if (sch != null) {
            return sch;
        }

        return null;
    }

    public static int getTypeMask(SmartScriptType smartScriptType) {
        return switch (smartScriptType) {
            case Creature -> SmartScriptTypeMaskId.CREATURE;
            case GameObject -> SmartScriptTypeMaskId.GAMEOBJECT;
            case AreaTrigger -> SmartScriptTypeMaskId.Areatrigger;
            case Event -> SmartScriptTypeMaskId.event;
            case Gossip -> SmartScriptTypeMaskId.Gossip;
            case Quest -> SmartScriptTypeMaskId.Quest;
            case Spell -> SmartScriptTypeMaskId.spell;
            case Transport -> SmartScriptTypeMaskId.transport;
            case getInstance() -> SmartScriptTypeMaskId.instance;
            case TimedActionlist -> SmartScriptTypeMaskId.TimedActionlist;
            case Scene -> SmartScriptTypeMaskId.Scene;
            case AreaTriggerEntity -> SmartScriptTypeMaskId.AreatrigggerEntity;
            case AreaTriggerEntityServerside -> SmartScriptTypeMaskId.AreatrigggerEntity;
            default -> 0;
        };
    }

    public static int getEventMask(SmartEvents smartEvent) {
        return switch (smartEvent) {
            case UpdateIc -> SmartScriptTypeMaskId.CREATURE + SmartScriptTypeMaskId.TimedActionlist;
            case UpdateOoc ->
                    SmartScriptTypeMaskId.CREATURE + SmartScriptTypeMaskId.GAMEOBJECT + SmartScriptTypeMaskId.instance + SmartScriptTypeMaskId.AreatrigggerEntity;
            case HealthPct -> SmartScriptTypeMaskId.CREATURE;
            case ManaPct -> SmartScriptTypeMaskId.CREATURE;
            case Aggro -> SmartScriptTypeMaskId.CREATURE;
            case Kill -> SmartScriptTypeMaskId.CREATURE;
            case Death -> SmartScriptTypeMaskId.CREATURE;
            case Evade -> SmartScriptTypeMaskId.CREATURE;
            case SpellHit -> SmartScriptTypeMaskId.CREATURE + SmartScriptTypeMaskId.GAMEOBJECT;
            case Range -> SmartScriptTypeMaskId.CREATURE;
            case OocLos -> SmartScriptTypeMaskId.CREATURE;
            case Respawn -> SmartScriptTypeMaskId.CREATURE + SmartScriptTypeMaskId.GAMEOBJECT;
            case TargetHealthPct -> SmartScriptTypeMaskId.CREATURE;
            case VictimCasting -> SmartScriptTypeMaskId.CREATURE;
            case FriendlyHealth -> SmartScriptTypeMaskId.CREATURE;
            case FriendlyIsCc -> SmartScriptTypeMaskId.CREATURE;
            case FriendlyMissingBuff -> SmartScriptTypeMaskId.CREATURE;
            case SummonedUnit -> SmartScriptTypeMaskId.CREATURE + SmartScriptTypeMaskId.GAMEOBJECT;
            case TargetManaPct -> SmartScriptTypeMaskId.CREATURE;
            case AcceptedQuest -> SmartScriptTypeMaskId.CREATURE + SmartScriptTypeMaskId.GAMEOBJECT;
            case RewardQuest -> SmartScriptTypeMaskId.CREATURE + SmartScriptTypeMaskId.GAMEOBJECT;
            case ReachedHome -> SmartScriptTypeMaskId.CREATURE;
            case ReceiveEmote -> SmartScriptTypeMaskId.CREATURE;
            case HasAura -> SmartScriptTypeMaskId.CREATURE;
            case TargetBuffed -> SmartScriptTypeMaskId.CREATURE;
            case Reset -> SmartScriptTypeMaskId.CREATURE;
            case IcLos -> SmartScriptTypeMaskId.CREATURE;
            case PassengerBoarded -> SmartScriptTypeMaskId.CREATURE;
            case PassengerRemoved -> SmartScriptTypeMaskId.CREATURE;
            case Charmed -> SmartScriptTypeMaskId.CREATURE;
            case CharmedTarget -> SmartScriptTypeMaskId.CREATURE;
            case SpellHitTarget -> SmartScriptTypeMaskId.CREATURE;
            case Damaged -> SmartScriptTypeMaskId.CREATURE;
            case DamagedTarget -> SmartScriptTypeMaskId.CREATURE;
            case Movementinform -> SmartScriptTypeMaskId.CREATURE;
            case SummonDespawned -> SmartScriptTypeMaskId.CREATURE + SmartScriptTypeMaskId.GAMEOBJECT;
            case CorpseRemoved -> SmartScriptTypeMaskId.CREATURE;
            case AiInit -> SmartScriptTypeMaskId.CREATURE + SmartScriptTypeMaskId.GAMEOBJECT;
            case DataSet -> SmartScriptTypeMaskId.CREATURE + SmartScriptTypeMaskId.GAMEOBJECT;
            case WaypointStart -> SmartScriptTypeMaskId.CREATURE;
            case WaypointReached -> SmartScriptTypeMaskId.CREATURE;
            case TransportAddplayer -> SmartScriptTypeMaskId.transport;
            case TransportAddcreature -> SmartScriptTypeMaskId.transport;
            case TransportRemovePlayer -> SmartScriptTypeMaskId.transport;
            case TransportRelocate -> SmartScriptTypeMaskId.transport;
            case InstancePlayerEnter -> SmartScriptTypeMaskId.instance;
            case AreatriggerOntrigger -> SmartScriptTypeMaskId.Areatrigger + SmartScriptTypeMaskId.AreatrigggerEntity;
            case QuestAccepted -> SmartScriptTypeMaskId.Quest;
            case QuestObjCompletion -> SmartScriptTypeMaskId.Quest;
            case QuestRewarded -> SmartScriptTypeMaskId.Quest;
            case QuestCompletion -> SmartScriptTypeMaskId.Quest;
            case QuestFail -> SmartScriptTypeMaskId.Quest;
            case TextOver -> SmartScriptTypeMaskId.CREATURE + SmartScriptTypeMaskId.GAMEOBJECT;
            case ReceiveHeal -> SmartScriptTypeMaskId.CREATURE;
            case JustSummoned -> SmartScriptTypeMaskId.CREATURE;
            case WaypointPaused -> SmartScriptTypeMaskId.CREATURE;
            case WaypointResumed -> SmartScriptTypeMaskId.CREATURE;
            case WaypointStopped -> SmartScriptTypeMaskId.CREATURE;
            case WaypointEnded -> SmartScriptTypeMaskId.CREATURE;
            case TimedEventTriggered -> SmartScriptTypeMaskId.CREATURE + SmartScriptTypeMaskId.GAMEOBJECT;
            case Update ->
                    SmartScriptTypeMaskId.CREATURE + SmartScriptTypeMaskId.GAMEOBJECT + SmartScriptTypeMaskId.AreatrigggerEntity;
            case Link ->
                    SmartScriptTypeMaskId.CREATURE + SmartScriptTypeMaskId.GAMEOBJECT + SmartScriptTypeMaskId.Areatrigger + SmartScriptTypeMaskId.event + SmartScriptTypeMaskId.Gossip + SmartScriptTypeMaskId.Quest + SmartScriptTypeMaskId.spell + SmartScriptTypeMaskId.transport + SmartScriptTypeMaskId.instance + SmartScriptTypeMaskId.AreatrigggerEntity;
            case GossipSelect -> SmartScriptTypeMaskId.CREATURE + SmartScriptTypeMaskId.GAMEOBJECT;
            case JustCreated -> SmartScriptTypeMaskId.CREATURE + SmartScriptTypeMaskId.GAMEOBJECT;
            case GossipHello -> SmartScriptTypeMaskId.CREATURE + SmartScriptTypeMaskId.GAMEOBJECT;
            case FollowCompleted -> SmartScriptTypeMaskId.CREATURE;
            case PhaseChange -> SmartScriptTypeMaskId.CREATURE + SmartScriptTypeMaskId.GAMEOBJECT;
            case IsBehindTarget -> SmartScriptTypeMaskId.CREATURE;
            case GameEventStart -> SmartScriptTypeMaskId.CREATURE + SmartScriptTypeMaskId.GAMEOBJECT;
            case GameEventEnd -> SmartScriptTypeMaskId.CREATURE + SmartScriptTypeMaskId.GAMEOBJECT;
            case GoLootStateChanged -> SmartScriptTypeMaskId.GAMEOBJECT;
            case GoEventInform -> SmartScriptTypeMaskId.GAMEOBJECT;
            case ActionDone -> SmartScriptTypeMaskId.CREATURE;
            case OnSpellclick -> SmartScriptTypeMaskId.CREATURE;
            case FriendlyHealthPCT -> SmartScriptTypeMaskId.CREATURE;
            case DistanceCreature -> SmartScriptTypeMaskId.CREATURE;
            case DistanceGameobject -> SmartScriptTypeMaskId.CREATURE;
            case CounterSet -> SmartScriptTypeMaskId.CREATURE + SmartScriptTypeMaskId.GAMEOBJECT;
            case SceneStart -> SmartScriptTypeMaskId.Scene;
            case SceneTrigger -> SmartScriptTypeMaskId.Scene;
            case SceneCancel -> SmartScriptTypeMaskId.Scene;
            case SceneComplete -> SmartScriptTypeMaskId.Scene;
            case SummonedUnitDies -> SmartScriptTypeMaskId.CREATURE + SmartScriptTypeMaskId.GAMEOBJECT;
            case OnSpellCast -> SmartScriptTypeMaskId.CREATURE;
            case OnSpellFailed -> SmartScriptTypeMaskId.CREATURE;
            case OnSpellStart -> SmartScriptTypeMaskId.CREATURE;
            case OnDespawn -> SmartScriptTypeMaskId.CREATURE;
            default -> 0;
        };
    }

    public static void TC_SAI_IS_BOOLEAN_VALID(SmartScriptHolder e, int value) {
        TC_SAI_IS_BOOLEAN_VALID(e, value, null);
    }

    
    public static void TC_SAI_IS_BOOLEAN_VALID(SmartScriptHolder e, int value, String valueName) {
        if (value > 1) {
            Logs.SQL.error(String.format("SmartAIMgr: %1$s uses param %2$s of type Boolean with second %3$s, valid values are 0 or 1, skipped.", e, valueName, value));
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
        if (Math.abs(e.target.o) > 2 * MathUtil.PI) {
            Logs.SQL.error(String.format("SmartAIMgr: %1$s has abs(`target.o` = %2$s) > 2*PI (orientation is expressed in radians)", e, e.target.o));
        }

        switch (e.getTargetType()) {
            case CreatureDistance:
            case CreatureRange: {
                if (e.target.unitDistance.creature != 0 && global.getObjectMgr().getCreatureTemplate(e.target.unitDistance.creature) == null) {
                    Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s uses non-existent Creature entry %2$s as target_param1, skipped.", e, e.target.unitDistance.creature));

                    return false;
                }

                break;
            }
            case GameobjectDistance:
            case GameobjectRange: {
                if (e.target.goDistance.entry != 0 && global.getObjectMgr().getGameObjectTemplate(e.target.goDistance.entry) == null) {
                    Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s uses non-existent GameObject entry %2$s as target_param1, skipped.", e, e.target.goDistance.entry));

                    return false;
                }

                break;
            }
            case CreatureGuid: {
                if (e.target.unitGUID.entry != 0 && !isCreatureValid(e, e.target.unitGUID.entry)) {
                    return false;
                }

                long guid = e.target.unitGUID.dbGuid;
                var data = global.getObjectMgr().getCreatureData(guid);

                if (data == null) {
                    Logs.SQL.error(String.format("SmartAIMgr: %1$s using invalid creature guid %2$s as target_param1, skipped.", e, guid));

                    return false;
                } else if (e.target.unitGUID.entry != 0 && e.target.unitGUID.entry != data.id) {
                    Logs.SQL.error(String.format("SmartAIMgr: %1$s using invalid creature entry %2$s (expected %3$s) for guid %4$s as target_param1, skipped.", e, e.target.unitGUID.entry, data.id, guid));

                    return false;
                }

                break;
            }
            case GameobjectGuid: {
                if (e.target.goGUID.entry != 0 && !isGameObjectValid(e, e.target.goGUID.entry)) {
                    return false;
                }

                long guid = e.target.goGUID.dbGuid;
                var data = global.getObjectMgr().getGameObjectData(guid);

                if (data == null) {
                    Logs.SQL.error(String.format("SmartAIMgr: %1$s using invalid gameobject guid %2$s as target_param1, skipped.", e, guid));

                    return false;
                } else if (e.target.goGUID.entry != 0 && e.target.goGUID.entry != data.id) {
                    Logs.SQL.error(String.format("SmartAIMgr: %1$s using invalid gameobject entry %2$s (expected %3$s) for guid %4$s as target_param1, skipped.", e, e.target.goGUID.entry, data.id, guid));

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
                if (e.getScriptType() != SmartScriptType.TimedActionlist && e.getEventType() != SmartEvents.link && !eventHasInvoker(e.event.type)) {
                    Logs.SQL.error(String.format("SmartAIMgr: Entry %1$s SourceType %2$s Event %3$s Action %4$s has invoker target, but event does not provide any invoker!", e.entryOrGuid, e.getScriptType(), e.getEventType(), e.getActionType()));

                    return false;
                }

                break;
            case HostileSecondAggro:
            case HostileLastAggro:
            case HostileRandom:
            case HostileRandomNotTop:
                TC_SAI_IS_BOOLEAN_VALID(e, e.target.hostilRandom.playerOnly);

                break;
            case Farthest:
                TC_SAI_IS_BOOLEAN_VALID(e, e.target.farthest.playerOnly);
                TC_SAI_IS_BOOLEAN_VALID(e, e.target.farthest.isInLos);

                break;
            case ClosestCreature:
                TC_SAI_IS_BOOLEAN_VALID(e, e.target.unitClosest.dead);

                break;
            case ClosestEnemy:
                TC_SAI_IS_BOOLEAN_VALID(e, e.target.closestAttackable.playerOnly);

                break;
            case ClosestFriendly:
                TC_SAI_IS_BOOLEAN_VALID(e, e.target.closestFriendly.playerOnly);

                break;
            case OwnerOrSummoner:
                TC_SAI_IS_BOOLEAN_VALID(e, e.target.owner.useCharmerOrOwner);

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

    private static boolean isSpellVisualKitValid(SmartScriptHolder e, int entry) {
        if (!CliDB.SpellVisualKitStorage.containsKey(entry)) {
            Logs.SQL.error(String.format("SmartAIMgr: Entry %1$s SourceType %2$s Event %3$s Action %4$s uses non-existent SpellVisualKit entry %5$s, skipped.", e.entryOrGuid, e.getScriptType(), e.eventId, e.getActionType(), entry));

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
            case Kill -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.kill.class);
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
            case ReceiveEmote -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.emote.class);
            case HasAura -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.aura.class);
            case TargetBuffed -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.aura.class);
            case Reset -> 0;
            case IcLos -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.Los.class);
            case PassengerBoarded -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.MinMax.class);
            case PassengerRemoved -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.MinMax.class);
            case Charmed -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.charm.class);
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
            case TransportAddcreature ->
                    system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.TransportAddCreature.class);
            case TransportRemovePlayer -> 0;
            case TransportRelocate -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.TransportRelocate.class);
            case InstancePlayerEnter ->
                    system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.InstancePlayerEnter.class);
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
            case GameEventStart -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.gameEvent.class);
            case GameEventEnd -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.gameEvent.class);
            case GoLootStateChanged ->
                    system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.GoLootStateChanged.class);
            case GoEventInform -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.EventInform.class);
            case ActionDone -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.DoAction.class);
            case OnSpellclick -> 0;
            case FriendlyHealthPCT -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.FriendlyHealthPct.class);
            case DistanceCreature -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.distance.class);
            case DistanceGameobject -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.distance.class);
            case CounterSet -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.counter.class);
            case SceneStart -> 0;
            case SceneTrigger -> 0;
            case SceneCancel -> 0;
            case SceneComplete -> 0;
            case SummonedUnitDies -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.Summoned.class);
            case OnSpellCast -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.SpellCast.class);
            case OnSpellFailed -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.SpellCast.class);
            case OnSpellStart -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.SpellCast.class);
            case OnDespawn -> 0;
            default -> system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.raw.class);
        };

        var rawCount = system.Runtime.InteropServices.Marshal.SizeOf(SmartEvent.raw.class) / (Integer.SIZE / Byte.SIZE);
        var paramsCount = paramsStructSize / (Integer.SIZE / Byte.SIZE);

        for (var index = paramsCount; index < rawCount; index++) {
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
                Log.outWarn(LogFilter.Sql, String.format("SmartAIMgr: %1$s has unused event_param%2$s with second %3$s, it should be 0.", e, index + 1, value));
            }
        }

        return true;
    }

    private static boolean checkUnusedActionParams(SmartScriptHolder e) {
        var paramsStructSize = switch (e.action.type) {
            case None -> 0;
            case Talk -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.talk.class);
            case SetFaction -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.faction.class);
            case MorphToEntryOrModel -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.MorphOrMount.class);
            case Sound -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.Sound.class);
            case PlayEmote -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.emote.class);
            case FailQuest -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.Quest.class);
            case OfferQuest -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.QuestOffer.class);
            case SetReactState -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.React.class);
            case ActivateGobject -> 0;
            case RandomEmote -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.RandomEmote.class);
            case Cast -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.cast.class);
            case SummonCreature -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.SummonCreature.class);
            case ThreatSinglePct -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.ThreatPCT.class);
            case ThreatAllPct -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.ThreatPCT.class);
            case CallAreaexploredoreventhappens ->
                    system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.Quest.class);
            case SetIngamePhaseGroup ->
                    system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.IngamePhaseGroup.class);
            case SetEmoteState -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.emote.class);
            case AutoAttack -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.AutoAttack.class);
            case AllowCombatMovement -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.CombatMove.class);
            case SetEventPhase -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.SetEventPhase.class);
            case IncEventPhase -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.IncEventPhase.class);
            case Evade -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.Evade.class);
            case FleeForAssist -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.FleeAssist.class);
            case CallGroupeventhappens -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.Quest.class);
            case CombatStop -> 0;
            case RemoveAurasFromSpell -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.removeAura.class);
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
            case SetData -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.setData.class);
            case AttackStop -> 0;
            case SetVisibility -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.Visibility.class);
            case SetActive -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.active.class);
            case AttackStart -> 0;
            case SummonGo -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.SummonGO.class);
            case KillUnit -> 0;
            case ActivateTaxi -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.taxi.class);
            case WpStart -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.WpStart.class);
            case WpPause -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.WpPause.class);
            case WpStop -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.WpStop.class);
            case AddItem -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.item.class);
            case RemoveItem -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.item.class);
            case SetRun -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.SetRun.class);
            case SetDisableGravity ->
                    system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.SetDisableGravity.class);
            case Teleport -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.Teleport.class);
            case SetCounter -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.SetCounter.class);
            case StoreTargetList -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.StoreTargets.class);
            case WpResume -> 0;
            case SetOrientation -> 0;
            case CreateTimedEvent -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.TimeEvent.class);
            case Playmovie -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.Movie.class);
            case MoveToPos -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.MoveToPos.class);
            case EnableTempGobj -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.EnableTempGO.class);
            case Equip -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.equip.class);
            case CloseGossip -> 0;
            case TriggerTimedEvent -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.TimeEvent.class);
            case RemoveTimedEvent -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.TimeEvent.class);
            case CallScriptReset -> 0;
            case SetRangedMovement ->
                    system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.SetRangedMovement.class);
            case CallTimedActionlist ->
                    system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.TimedActionList.class);
            case SetNpcFlag -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.flag.class);
            case AddNpcFlag -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.flag.class);
            case RemoveNpcFlag -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.flag.class);
            case SimpleTalk -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.SimpleTalk.class);
            case SelfCast -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.cast.class);
            case CrossCast -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.CrossCast.class);
            case CallRandomTimedActionlist ->
                    system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.RandTimedActionList.class);
            case CallRandomRangeTimedActionlist ->
                    system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.RandRangeTimedActionList.class);
            case RandomMove -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.MoveRandom.class);
            case SetUnitFieldBytes1 -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.SetunitByte.class);
            case RemoveUnitFieldBytes1 -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.DelunitByte.class);
            case InterruptSpell ->
                    system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.InterruptSpellCasting.class);
            case AddDynamicFlag -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.flag.class);
            case RemoveDynamicFlag -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.flag.class);
            case JumpToPos -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.jump.class);
            case SendGossipMenu -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.SendGossipMenu.class);
            case GoSetLootState -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.SetGoLootState.class);
            case SendTargetToTarget ->
                    system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.SendTargetToTarget.class);
            case SetHomePos -> 0;
            case SetHealthRegen -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.SetHealthRegen.class);
            case SetRoot -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.SetRoot.class);
            case SummonCreatureGroup -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.CreatureGroup.class);
            case SetPower -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.power.class);
            case AddPower -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.power.class);
            case RemovePower -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.power.class);
            case GameEventStop -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.GameEventStop.class);
            case GameEventStart -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.GameEventStart.class);
            case StartClosestWaypoint ->
                    system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.ClosestWaypointFromList.class);
            case MoveOffset -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.MoveOffset.class);
            case RandomSound -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.RandomSound.class);
            case SetCorpseDelay -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.corpseDelay.class);
            case DisableEvade -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.DisableEvade.class);
            case GoSetGoState -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.goState.class);
            case AddThreat -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.threat.class);
            case LoadEquipment -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.LoadEquipment.class);
            case TriggerRandomTimedEvent ->
                    system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.RandomTimedEvent.class);
            case PauseMovement -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.PauseMovement.class);
            case PlayAnimkit -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.animKit.class);
            case ScenePlay -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.Scene.class);
            case SceneCancel -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.Scene.class);
            case SpawnSpawngroup -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.GroupSpawn.class);
            case DespawnSpawngroup -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.GroupSpawn.class);
            case RespawnBySpawnId -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.RespawnData.class);
            case InvokerCast -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.cast.class);
            case PlayCinematic -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.Cinematic.class);
            case SetMovementSpeed -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.MovementSpeed.class);
            case PlaySpellVisualKit -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.SpellVisualKit.class);
            case OverrideLight -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.OverrideLight.class);
            case OverrideWeather -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.OverrideWeather.class);
            case SetAIAnimKit -> 0;
            case SetHover -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.SetHover.class);
            case SetHealthPct -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.SetHealthPct.class);
            case CreateConversation -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.conversation.class);
            case SetImmunePC -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.SetImmunePC.class);
            case SetImmuneNPC -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.SetImmuneNPC.class);
            case SetUninteractible ->
                    system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.SetUninteractible.class);
            case ActivateGameobject ->
                    system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.ActivateGameObject.class);
            case AddToStoredTargetList ->
                    system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.AddToStoredTargets.class);
            case BecomePersonalCloneForPlayer ->
                    system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.BecomePersonalClone.class);
            case TriggerGameEvent -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.TriggerGameEvent.class);
            case DoAction -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.DoAction.class);
            default -> system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.raw.class);
        };

        var rawCount = system.Runtime.InteropServices.Marshal.SizeOf(SmartAction.raw.class) / (Integer.SIZE / Byte.SIZE);
        var paramsCount = paramsStructSize / (Integer.SIZE / Byte.SIZE);

        for (var index = paramsCount; index < rawCount; index++) {
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
                Log.outWarn(LogFilter.Sql, String.format("SmartAIMgr: %1$s has unused action_param%2$s with second %3$s, it should be 0.", e, index + 1, value));
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
            case CreatureGuid -> system.Runtime.InteropServices.Marshal.SizeOf(SmartTarget.unitGUID.class);
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
            case OwnerOrSummoner -> system.Runtime.InteropServices.Marshal.SizeOf(SmartTarget.owner.class);
            case ThreatList -> system.Runtime.InteropServices.Marshal.SizeOf(SmartTarget.threatList.class);
            case ClosestEnemy -> system.Runtime.InteropServices.Marshal.SizeOf(SmartTarget.ClosestAttackable.class);
            case ClosestFriendly -> system.Runtime.InteropServices.Marshal.SizeOf(SmartTarget.ClosestFriendly.class);
            case LootRecipients -> 0;
            case Farthest -> system.Runtime.InteropServices.Marshal.SizeOf(SmartTarget.Farthest.class);
            case VehiclePassenger -> system.Runtime.InteropServices.Marshal.SizeOf(SmartTarget.vehicle.class);
            case ClosestUnspawnedGameobject ->
                    system.Runtime.InteropServices.Marshal.SizeOf(SmartTarget.GoClosest.class);
            default -> system.Runtime.InteropServices.Marshal.SizeOf(SmartTarget.raw.class);
        };

        var rawCount = system.Runtime.InteropServices.Marshal.SizeOf(SmartTarget.raw.class) / (Integer.SIZE / Byte.SIZE);
        var paramsCount = paramsStructSize / (Integer.SIZE / Byte.SIZE);

        for (var index = paramsCount; index < rawCount; index++) {
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
                Log.outWarn(LogFilter.Sql, String.format("SmartAIMgr: %1$s has unused target_param%2$s with second %3$s, it must be 0, skipped.", e, index + 1, value));
            }
        }

        return true;
    }

    private static boolean isAnimKitValid(SmartScriptHolder e, int entry) {
        if (!CliDB.AnimKitStorage.containsKey(entry)) {
            Logs.SQL.error(String.format("SmartAIMgr: %1$s uses non-existent AnimKit entry %2$s, skipped.", e, entry));

            return false;
        }

        return true;
    }

    private static boolean isTextValid(SmartScriptHolder e, int id) {
        if (e.getScriptType() != SmartScriptType.CREATURE) {
            return true;
        }

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
                        var guid = (long) -e.entryOrGuid;
                        var data = global.getObjectMgr().getCreatureData(guid);

                        if (data == null) {
                            Logs.SQL.error(String.format("SmartAIMgr: %1$s using non-existent Creature guid %2$s, skipped.", e, guid));

                            return false;
                        } else {
                            entry = data.id;
                        }
                    } else {
                        entry = (int) e.entryOrGuid;
                    }

                    break;
            }
        }

        if (entry == 0 || !global.getCreatureTextMgr().textExist(entry, (byte) id)) {
            Logs.SQL.error(String.format("SmartAIMgr: %1$s using non-existent Text id %2$s, skipped.", e, id));

            return false;
        }

        return true;
    }

    private static boolean isCreatureValid(SmartScriptHolder e, int entry) {
        if (global.getObjectMgr().getCreatureTemplate(entry) == null) {
            Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s uses non-existent Creature entry %2$s, skipped.", e, entry));

            return false;
        }

        return true;
    }

    private static boolean isGameObjectValid(SmartScriptHolder e, int entry) {
        if (global.getObjectMgr().getGameObjectTemplate(entry) == null) {
            Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s uses non-existent GameObject entry %2$s, skipped.", e, entry));

            return false;
        }

        return true;
    }

    private static boolean isQuestValid(SmartScriptHolder e, int entry) {
        if (global.getObjectMgr().getQuestTemplate(entry) == null) {
            Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s uses non-existent Quest entry %2$s, skipped.", e, entry));

            return false;
        }

        return true;
    }

    private static boolean isSpellValid(SmartScriptHolder e, int entry) {
        if (!global.getSpellMgr().hasSpellInfo(entry, Difficulty.NONE)) {
            Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s uses non-existent Spell entry %2$s, skipped.", e, entry));

            return false;
        }

        return true;
    }

    private static boolean isMinMaxValid(SmartScriptHolder e, int min, int max) {
        if (max < min) {
            Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s uses min/max params wrong (%2$s/%3$s), skipped.", e, min, max));

            return false;
        }

        return true;
    }

    private static boolean notNULL(SmartScriptHolder e, int data) {
        if (data == 0) {
            Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s Parameter can not be NULL, skipped.", e));

            return false;
        }

        return true;
    }

    private static boolean isEmoteValid(SmartScriptHolder e, int entry) {
        if (!CliDB.EmotesStorage.containsKey(entry)) {
            Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s uses non-existent Emote entry %2$s, skipped.", e, entry));

            return false;
        }

        return true;
    }

    private static boolean isItemValid(SmartScriptHolder e, int entry) {
        if (!CliDB.ItemSparseStorage.containsKey(entry)) {
            Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s uses non-existent Item entry %2$s, skipped.", e, entry));

            return false;
        }

        return true;
    }

    private static boolean isTextEmoteValid(SmartScriptHolder e, int entry) {
        if (!CliDB.EmotesTextStorage.containsKey(entry)) {
            Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s uses non-existent Text Emote entry %2$s, skipped.", e, entry));

            return false;
        }

        return true;
    }

    private static boolean isAreaTriggerValid(SmartScriptHolder e, int entry) {
        if (!CliDB.AreaTriggerStorage.containsKey(entry)) {
            Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s uses non-existent AreaTrigger entry %2$s, skipped.", e, entry));

            return false;
        }

        return true;
    }

    private static boolean isSoundValid(SmartScriptHolder e, int entry) {
        if (!CliDB.SoundKitStorage.containsKey(entry)) {
            Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s uses non-existent Sound entry %2$s, skipped.", e, entry));

            return false;
        }

        return true;
    }

    public final void loadFromDB() {
        var oldMSTime = System.currentTimeMillis();

        for (byte i = 0; i < SmartScriptType.max.getValue(); i++) {
            _eventMap[i].clear(); //Drop Existing SmartAI List
        }

        var stmt = DB.World.GetPreparedStatement(WorldStatements.SEL_SMART_SCRIPTS);
        var result = DB.World.query(stmt);

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 SmartAI scripts. DB table `smartai_scripts` is empty.");

            return;
        }

        var count = 0;

        do {
            SmartScriptHolder temp = new SmartScriptHolder();

            temp.entryOrGuid = result.<Integer>Read(0);

            if (temp.entryOrGuid == 0) {
                if (ConfigMgr.GetDefaultValue("load.autoclean", false)) {
                    DB.World.execute(String.format("DELETE FROM smart_scripts WHERE entryorguid = %1$s", temp.entryOrGuid));
                } else {
                    Logs.SQL.error("SmartAIMgr.LoadFromDB: invalid entryorguid (0), skipped loading.");
                }

                continue;
            }

            var source_type = SmartScriptType.forValue(result.<Byte>Read(1));

            if (source_type.getValue() >= SmartScriptType.max.getValue()) {
                if (ConfigMgr.GetDefaultValue("load.autoclean", false)) {
                    DB.World.execute(String.format("DELETE FROM smart_scripts WHERE entryorguid = %1$s", temp.entryOrGuid));
                } else {
                    Logs.SQL.error("SmartAIMgr.LoadSmartAI: invalid source_type ({0}), skipped loading.", source_type);
                }

                continue;
            }

            if (temp.entryOrGuid >= 0) {
                switch (source_type) {
                    case Creature:
                        if (global.getObjectMgr().getCreatureTemplate((int) temp.entryOrGuid) == null) {
                            if (ConfigMgr.GetDefaultValue("load.autoclean", false)) {
                                DB.World.execute(String.format("DELETE FROM smart_scripts WHERE entryorguid = %1$s", temp.entryOrGuid));
                            } else {
                                Logs.SQL.error("SmartAIMgr.LoadSmartAI: Creature entry ({0}) does not exist, skipped loading.", temp.entryOrGuid);
                            }

                            continue;
                        }

                        break;

                    case GameObject: {
                        if (global.getObjectMgr().getGameObjectTemplate((int) temp.entryOrGuid) == null) {
                            if (ConfigMgr.GetDefaultValue("load.autoclean", false)) {
                                DB.World.execute(String.format("DELETE FROM smart_scripts WHERE entryorguid = %1$s", temp.entryOrGuid));
                            } else {
                                Logs.SQL.error("SmartAIMgr.LoadSmartAI: GameObject entry ({0}) does not exist, skipped loading.", temp.entryOrGuid);
                            }

                            continue;
                        }

                        break;
                    }
                    case AreaTrigger: {
                        if (CliDB.AreaTableStorage.get((int) temp.entryOrGuid) == null) {
                            if (ConfigMgr.GetDefaultValue("load.autoclean", false)) {
                                DB.World.execute(String.format("DELETE FROM smart_scripts WHERE entryorguid = %1$s", temp.entryOrGuid));
                            } else {
                                Logs.SQL.error("SmartAIMgr.LoadSmartAI: AreaTrigger entry ({0}) does not exist, skipped loading.", temp.entryOrGuid);
                            }

                            continue;
                        }

                        break;
                    }
                    case Scene: {
                        if (global.getObjectMgr().getSceneTemplate((int) temp.entryOrGuid) == null) {
                            if (ConfigMgr.GetDefaultValue("load.autoclean", false)) {
                                DB.World.execute(String.format("DELETE FROM smart_scripts WHERE entryorguid = %1$s", temp.entryOrGuid));
                            } else {
                                Logs.SQL.error("SmartAIMgr.LoadFromDB: Scene id ({0}) does not exist, skipped loading.", temp.entryOrGuid);
                            }

                            continue;
                        }

                        break;
                    }
                    case Quest: {
                        if (global.getObjectMgr().getQuestTemplate((int) temp.entryOrGuid) == null) {
                            if (ConfigMgr.GetDefaultValue("load.autoclean", false)) {
                                DB.World.execute(String.format("DELETE FROM smart_scripts WHERE entryorguid = %1$s", temp.entryOrGuid));
                            } else {
                                Logs.SQL.error(String.format("SmartAIMgr.LoadFromDB: Quest id (%1$s) does not exist, skipped loading.", temp.entryOrGuid));
                            }

                            continue;
                        }

                        break;
                    }
                    case TimedActionlist:
                        break; //nothing to check, really
                    case Max:
                    case AreaTriggerEntity: {
                        if (global.getAreaTriggerDataStorage().GetAreaTriggerTemplate(new areaTriggerId((int) temp.entryOrGuid, false)) == null) {
                            if (ConfigMgr.GetDefaultValue("load.autoclean", false)) {
                                DB.World.execute(String.format("DELETE FROM smart_scripts WHERE entryorguid = %1$s", temp.entryOrGuid));
                            } else {
                                Logs.SQL.error(String.format("SmartAIMgr.LoadFromDB: AreaTrigger entry (%1$s IsServerSide false) does not exist, skipped loading.", temp.entryOrGuid));
                            }

                            continue;
                        }

                        break;
                    }
                    case AreaTriggerEntityServerside: {
                        if (global.getAreaTriggerDataStorage().GetAreaTriggerTemplate(new areaTriggerId((int) temp.entryOrGuid, true)) == null) {
                            if (ConfigMgr.GetDefaultValue("load.autoclean", false)) {
                                DB.World.execute(String.format("DELETE FROM smart_scripts WHERE entryorguid = %1$s", temp.entryOrGuid));
                            } else {
                                Logs.SQL.error(String.format("SmartAIMgr.LoadFromDB: AreaTrigger entry (%1$s IsServerSide true) does not exist, skipped loading.", temp.entryOrGuid));
                            }

                            continue;
                        }

                        break;
                    }
                    default:
                        if (ConfigMgr.GetDefaultValue("load.autoclean", false)) {
                            DB.World.execute(String.format("DELETE FROM smart_scripts WHERE entryorguid = %1$s", temp.entryOrGuid));
                        } else {
                            Logs.SQL.error("SmartAIMgr.LoadFromDB: not yet implemented source_type {0}", source_type);
                        }

                        continue;
                }
            } else {
                switch (source_type) {
                    case Creature: {
                        var creature = global.getObjectMgr().getCreatureData((long) -temp.entryOrGuid);

                        if (creature == null) {
                            if (ConfigMgr.GetDefaultValue("load.autoclean", false)) {
                                DB.World.execute(String.format("DELETE FROM smart_scripts WHERE entryorguid = %1$s", temp.entryOrGuid));
                            } else {
                                Logs.SQL.error(String.format("SmartAIMgr.LoadFromDB: Creature guid (%1$s) does not exist, skipped loading.", -temp.entryOrGuid));
                            }

                            continue;
                        }

                        var creatureInfo = global.getObjectMgr().getCreatureTemplate(creature.id);

                        if (creatureInfo == null) {
                            if (ConfigMgr.GetDefaultValue("load.autoclean", false)) {
                                DB.World.execute(String.format("DELETE FROM smart_scripts WHERE entryorguid = %1$s", temp.entryOrGuid));
                            } else {
                                Logs.SQL.error(String.format("SmartAIMgr.LoadFromDB: Creature entry (%1$s) guid (%2$s) does not exist, skipped loading.", creature.id, -temp.entryOrGuid));
                            }

                            continue;
                        }

                        if (!Objects.equals(creatureInfo.AIName, "SmartAI")) {
                            if (ConfigMgr.GetDefaultValue("load.autoclean", false)) {
                                DB.World.execute(String.format("DELETE FROM smart_scripts WHERE entryorguid = %1$s", temp.entryOrGuid));
                            } else {
                                Logs.SQL.error(String.format("SmartAIMgr.LoadFromDB: Creature entry (%1$s) guid (%2$s) is not using SmartAI, skipped loading.", creature.id, -temp.entryOrGuid));
                            }

                            continue;
                        }

                        break;
                    }
                    case GameObject: {
                        var gameObject = global.getObjectMgr().getGameObjectData((long) -temp.entryOrGuid);

                        if (gameObject == null) {
                            if (ConfigMgr.GetDefaultValue("load.autoclean", false)) {
                                DB.World.execute(String.format("DELETE FROM smart_scripts WHERE entryorguid = %1$s", temp.entryOrGuid));
                            } else {
                                Logs.SQL.error(String.format("SmartAIMgr.LoadFromDB: GameObject guid (%1$s) does not exist, skipped loading.", -temp.entryOrGuid));
                            }

                            continue;
                        }

                        var gameObjectInfo = global.getObjectMgr().getGameObjectTemplate(gameObject.id);

                        if (gameObjectInfo == null) {
                            if (ConfigMgr.GetDefaultValue("load.autoclean", false)) {
                                DB.World.execute(String.format("DELETE FROM smart_scripts WHERE entryorguid = %1$s", temp.entryOrGuid));
                            } else {
                                Logs.SQL.error(String.format("SmartAIMgr.LoadFromDB: GameObject entry (%1$s) guid (%2$s) does not exist, skipped loading.", gameObject.id, -temp.entryOrGuid));
                            }

                            continue;
                        }

                        if (!Objects.equals(gameObjectInfo.AIName, "SmartGameObjectAI")) {
                            if (ConfigMgr.GetDefaultValue("load.autoclean", false)) {
                                DB.World.execute(String.format("DELETE FROM smart_scripts WHERE entryorguid = %1$s", temp.entryOrGuid));
                            } else {
                                Logs.SQL.error(String.format("SmartAIMgr.LoadFromDB: GameObject entry (%1$s) guid (%2$s) is not using SmartGameObjectAI, skipped loading.", gameObject.id, -temp.entryOrGuid));
                            }

                            continue;
                        }

                        break;
                    }
                    default:
                        if (ConfigMgr.GetDefaultValue("load.autoclean", false)) {
                            DB.World.execute(String.format("DELETE FROM smart_scripts WHERE entryorguid = %1$s", temp.entryOrGuid));
                        } else {
                            Logs.SQL.error(String.format("SmartAIMgr.LoadFromDB: GUID-specific scripting not yet implemented for source_type %1$s", source_type));
                        }

                        continue;
                }
            }

            temp.sourceType = source_type;
            temp.eventId = result.<SHORT>Read(2);
            temp.link = result.<SHORT>Read(3);
            temp.event.type = SmartEvents.forValue(result.<Byte>Read(4));
            temp.event.event_phase_mask = result.<SHORT>Read(5);
            temp.event.event_chance = result.<Byte>Read(6);
            temp.event.event_flags = SmartEventFlags.forValue(result.<SHORT>Read(7));

            temp.event.raw.param1 = result.<Integer>Read(8);
            temp.event.raw.param2 = result.<Integer>Read(9);
            temp.event.raw.param3 = result.<Integer>Read(10);
            temp.event.raw.param4 = result.<Integer>Read(11);
            temp.event.raw.param5 = result.<Integer>Read(12);
            temp.event.param_string = result.<String>Read(13);

            temp.action.type = SmartActions.forValue(result.<Byte>Read(14));
            temp.action.raw.param1 = result.<Integer>Read(15);
            temp.action.raw.param2 = result.<Integer>Read(16);
            temp.action.raw.param3 = result.<Integer>Read(17);
            temp.action.raw.param4 = result.<Integer>Read(18);
            temp.action.raw.param5 = result.<Integer>Read(19);
            temp.action.raw.param6 = result.<Integer>Read(20);
            temp.action.raw.param7 = result.<Integer>Read(21);

            temp.target.type = SmartTargets.forValue(result.<Byte>Read(22));
            temp.target.raw.param1 = result.<Integer>Read(23);
            temp.target.raw.param2 = result.<Integer>Read(24);
            temp.target.raw.param3 = result.<Integer>Read(25);
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
                    if (temp.event.minMaxRepeat.repeatMin == 0 && temp.event.minMaxRepeat.repeatMax == 0 && !temp.event.event_flags.hasFlag(SmartEventFlags.NotRepeatable) && temp.sourceType != SmartScriptType.TimedActionlist) {
                        temp.event.event_flags = SmartEventFlags.forValue(temp.event.event_flags.getValue() | SmartEventFlags.NotRepeatable.getValue());
                        Logs.SQL.error(String.format("SmartAIMgr.LoadFromDB: Entry %1$s SourceType %2$s, Event %3$s, Missing Repeat flag.", temp.entryOrGuid, temp.getScriptType(), temp.eventId));
                    }

                    break;
                case VictimCasting:
                case IsBehindTarget:
                    if (temp.event.minMaxRepeat.min == 0 && temp.event.minMaxRepeat.max == 0 && !temp.event.event_flags.hasFlag(SmartEventFlags.NotRepeatable) && temp.sourceType != SmartScriptType.TimedActionlist) {
                        temp.event.event_flags = SmartEventFlags.forValue(temp.event.event_flags.getValue() | SmartEventFlags.NotRepeatable.getValue());
                        Logs.SQL.error(String.format("SmartAIMgr.LoadFromDB: Entry %1$s SourceType %2$s, Event %3$s, Missing Repeat flag.", temp.entryOrGuid, temp.getScriptType(), temp.eventId));
                    }

                    break;
                case FriendlyIsCc:
                    if (temp.event.friendlyCC.repeatMin == 0 && temp.event.friendlyCC.repeatMax == 0 && !temp.event.event_flags.hasFlag(SmartEventFlags.NotRepeatable) && temp.sourceType != SmartScriptType.TimedActionlist) {
                        temp.event.event_flags = SmartEventFlags.forValue(temp.event.event_flags.getValue() | SmartEventFlags.NotRepeatable.getValue());
                        Logs.SQL.error(String.format("SmartAIMgr.LoadFromDB: Entry %1$s SourceType %2$s, Event %3$s, Missing Repeat flag.", temp.entryOrGuid, temp.getScriptType(), temp.eventId));
                    }

                    break;
                default:
                    break;
            }

            // creature entry / guid not found in storage, create empty event list for it and increase counters
            if (!_eventMap[source_type.getValue()].ContainsKey(temp.entryOrGuid)) {
                ++count;
            }

            // store the new event
            _eventMap[source_type.getValue()].add(temp.entryOrGuid, temp);
        } while (result.NextRow());

        // Post Loading Validation
        for (byte i = 0; i < SmartScriptType.max.getValue(); ++i) {
            if (_eventMap[i] == null) {
                continue;
            }

            for (var key : _eventMap[i].keySet()) {
                var list = _eventMap[i].get(key);

                for (var e : list) {
                    if (e.link != 0) {
                        if (findLinkedEvent(list, e.link) == null) {
                            Logs.SQL.error("SmartAIMgr.LoadFromDB: Entry {0} SourceType {1}, Event {2}, Link Event {3} not found or invalid.", e.entryOrGuid, e.getScriptType(), e.eventId, e.link);
                        }
                    }

                    if (e.getEventType() == SmartEvents.link) {
                        if (findLinkedSourceEvent(list, e.eventId) == null) {
                            Logs.SQL.error("SmartAIMgr.LoadFromDB: Entry {0} SourceType {1}, Event {2}, Link Source Event not found or invalid. Event will never trigger.", e.entryOrGuid, e.getScriptType(), e.eventId);
                        }
                    }
                }
            }
        }

        Log.outInfo(LogFilter.ServerLoading, "Loaded {0} SmartAI scripts in {1} ms", count, time.GetMSTimeDiffToNow(oldMSTime));
    }

    public final void loadWaypointFromDB() {
        var oldMSTime = System.currentTimeMillis();

        waypointStore.clear();

        var stmt = DB.World.GetPreparedStatement(WorldStatements.SEL_SMARTAI_WP);
        var result = DB.World.query(stmt);

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 SmartAI Waypoint Paths. DB table `waypoints` is empty.");

            return;
        }

        int count = 0;
        int total = 0;
        int lastEntry = 0;
        int lastId = 1;

        do {
            var entry = result.<Integer>Read(0);
            var id = result.<Integer>Read(1);
            var x = result.<Float>Read(2);
            var y = result.<Float>Read(3);
            var z = result.<Float>Read(4);
            Float o = null;

            if (!result.IsNull(5)) {
                o = result.<Float>Read(5);
            }

            var delay = result.<Integer>Read(6);

            if (lastEntry != entry) {
                lastId = 1;
                ++count;
            }

            if (lastId != id) {
                Logs.SQL.error(String.format("SmartWaypointMgr.LoadFromDB: Path entry %1$s, unexpected point id %2$s, expected %3$s.", entry, id, lastId));
            }

            ++lastId;

            if (!waypointStore.containsKey(entry)) {
                waypointStore.put(entry, new waypointPath());
            }

            var path = waypointStore.get(entry);
            path.id = entry;
            path.nodes.add(new WaypointNode(id, x, y, z, o, delay));

            lastEntry = entry;
            ++total;
        } while (result.NextRow());

        Log.outInfo(LogFilter.ServerLoading, String.format("Loaded %1$s SmartAI waypoint paths (total %2$s waypoints) in %3$s ms", count, total, time.GetMSTimeDiffToNow(oldMSTime)));
    }

    public final ArrayList<SmartScriptHolder> getScript(int entry, SmartScriptType type) {
        ArrayList<SmartScriptHolder> temp = new ArrayList<>();

        if (_eventMap[(int) type.getValue()].ContainsKey(entry)) {
            for (var holder : _eventMap[(int) type.getValue()].get(entry)) {
                temp.add(new SmartScriptHolder(holder));
            }
        } else {
            if (entry > 0) //first search is for guid (negative), do not drop error if not found
            {
                Log.outDebug(LogFilter.ScriptsAi, "SmartAIMgr.GetScript: Could not load Script for Entry {0} ScriptType {1}.", entry, type);
            }
        }

        return temp;
    }

    public final WaypointPath getPath(int id) {
        return waypointStore.get(id);
    }

    public final SmartScriptHolder findLinkedEvent(ArrayList<SmartScriptHolder> list, int link) {
        var sch = tangible.ListHelper.find(list, p -> p.eventId == link && p.getEventType() == SmartEvents.link);

        if (sch != null) {
            return sch;
        }

        return null;
    }

    private boolean isEventValid(SmartScriptHolder e) {
        if (e.event.type.getValue() >= SmartEvents.End.getValue()) {
            Log.outError(LogFilter.ScriptsAi, "SmartAIMgr: EntryOrGuid {0} using event({1}) has invalid event type ({2}), skipped.", e.entryOrGuid, e.eventId, e.getEventType());

            return false;
        }

        // in SMART_SCRIPT_TYPE_TIMED_ACTIONLIST all event types are overriden by core
        if (e.getScriptType() != SmartScriptType.TimedActionlist && !(boolean) (getEventMask(e.event.type) & getTypeMask(e.getScriptType()))) {
            Log.outError(LogFilter.Scripts, "SmartAIMgr: EntryOrGuid {0}, event type {1} can not be used for Script type {2}", e.entryOrGuid, e.getEventType(), e.getScriptType());

            return false;
        }

        if (e.action.type.getValue() <= 0 || e.action.type.getValue() >= SmartActions.End.getValue()) {
            Log.outError(LogFilter.ScriptsAi, "SmartAIMgr: EntryOrGuid {0} using event({1}) has invalid action type ({2}), skipped.", e.entryOrGuid, e.eventId, e.getActionType());

            return false;
        }

        if (e.event.event_phase_mask > (int) SmartEventPhaseBits.All.getValue()) {
            Log.outError(LogFilter.ScriptsAi, "SmartAIMgr: EntryOrGuid {0} using event({1}) has invalid phase mask ({2}), skipped.", e.entryOrGuid, e.eventId, e.event.event_phase_mask);

            return false;
        }

        if (e.event.event_flags.getValue() > SmartEventFlags.All.getValue()) {
            Log.outError(LogFilter.ScriptsAi, "SmartAIMgr: EntryOrGuid {0} using event({1}) has invalid event flags ({2}), skipped.", e.entryOrGuid, e.eventId, e.event.event_flags);

            return false;
        }

        if (e.link != 0 && e.link == e.eventId) {
            Logs.SQL.error("SmartAIMgr: EntryOrGuid {0} SourceType {1}, Event {2}, Event is linking self (infinite loop), skipped.", e.entryOrGuid, e.getScriptType(), e.eventId);

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
                        var spellInfo = global.getSpellMgr().getSpellInfo(e.event.spellHit.spell, Difficulty.NONE);

                        if (spellInfo == null) {
                            Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s uses non-existent Spell entry %2$s, skipped.", e, e.event.spellHit.spell));

                            return false;
                        }

                        if (e.event.spellHit.school != 0 && (spellSchoolMask.forValue(e.event.spellHit.school).getValue() & spellInfo.getSchoolMask().getValue()) != spellInfo.getSchoolMask().getValue()) {
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

                    if (e.event.los.hostilityMode >= (int) LOSHostilityMode.End.getValue()) {
                        Logs.SQL.error(String.format("SmartAIMgr: %1$s uses hostilityMode with invalid second %2$s (max allowed second %3$s), skipped.", e, e.event.los.hostilityMode, LOSHostilityMode.End - 1));

                        return false;
                    }

                    TC_SAI_IS_BOOLEAN_VALID(e, e.event.los.playerOnly);

                    break;
                case Respawn:
                    if (e.event.respawn.type == (int) SmartRespawnCondition.Map.getValue() && CliDB.MapStorage.get(e.event.respawn.map) == null) {
                        Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s uses non-existent Map entry %2$s, skipped.", e, e.event.respawn.map));

                        return false;
                    }

                    if (e.event.respawn.type == (int) SmartRespawnCondition.area.getValue() && !CliDB.AreaTableStorage.containsKey(e.event.respawn.area)) {
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

                    TC_SAI_IS_BOOLEAN_VALID(e, e.event.kill.playerOnly);

                    break;
                case VictimCasting:
                    if (e.event.targetCasting.spellId > 0 && !global.getSpellMgr().hasSpellInfo(e.event.targetCasting.spellId, Difficulty.NONE)) {
                        Logs.SQL.error(String.format("SmartAIMgr: Entry %1$s SourceType %2$s Event %3$s Action %4$s uses non-existent Spell entry %5$s, skipped.", e.entryOrGuid, e.getScriptType(), e.eventId, e.getActionType(), e.event.spellHit.spell));

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
                        Logs.SQL.error(String.format("SmartAIMgr: Entry %1$s SourceType %2$s Event %3$s Action %4$s areatrigger param not supported for SMART_SCRIPT_TYPE_AREATRIGGER_ENTITY and SMART_SCRIPT_TYPE_AREATRIGGER_ENTITY_SERVERSIDE, skipped.", e.entryOrGuid, e.getScriptType(), e.eventId, e.getActionType()));

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
                    var events = global.getGameEventMgr().getEventMap();

                    if (e.event.gameEvent.gameEventId >= events.length || !events[e.event.gameEvent.gameEventId].isValid()) {
                        return false;
                    }

                    break;
                }
                case FriendlyHealthPCT:
                    if (!isMinMaxValid(e, e.event.friendlyHealthPct.repeatMin, e.event.friendlyHealthPct.repeatMax)) {
                        return false;
                    }

                    if (e.event.friendlyHealthPct.maxHpPct > 100 || e.event.friendlyHealthPct.minHpPct > 100) {
                        Logs.SQL.error(String.format("SmartAIMgr: %1$s has pct second above 100, skipped.", e));

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
                            Logs.SQL.error(String.format("SmartAIMgr: %1$s uses invalid target_type %2$s, skipped.", e, e.getTargetType()));

                            return false;
                    }

                    break;
                case DistanceCreature:
                    if (e.event.distance.guid == 0 && e.event.distance.entry == 0) {
                        Logs.SQL.error("SmartAIMgr: Event SMART_EVENT_DISTANCE_CREATURE did not provide creature guid or entry, skipped.");

                        return false;
                    }

                    if (e.event.distance.guid != 0 && e.event.distance.entry != 0) {
                        Logs.SQL.error("SmartAIMgr: Event SMART_EVENT_DISTANCE_CREATURE provided both an entry and guid, skipped.");

                        return false;
                    }

                    if (e.event.distance.guid != 0 && global.getObjectMgr().getCreatureData(e.event.distance.guid) == null) {
                        Logs.SQL.error("SmartAIMgr: Event SMART_EVENT_DISTANCE_CREATURE using invalid creature guid {0}, skipped.", e.event.distance.guid);

                        return false;
                    }

                    if (e.event.distance.entry != 0 && global.getObjectMgr().getCreatureTemplate(e.event.distance.entry) == null) {
                        Logs.SQL.error("SmartAIMgr: Event SMART_EVENT_DISTANCE_CREATURE using invalid creature entry {0}, skipped.", e.event.distance.entry);

                        return false;
                    }

                    break;
                case DistanceGameobject:
                    if (e.event.distance.guid == 0 && e.event.distance.entry == 0) {
                        Logs.SQL.error("SmartAIMgr: Event SMART_EVENT_DISTANCE_GAMEOBJECT did not provide gameobject guid or entry, skipped.");

                        return false;
                    }

                    if (e.event.distance.guid != 0 && e.event.distance.entry != 0) {
                        Logs.SQL.error("SmartAIMgr: Event SMART_EVENT_DISTANCE_GAMEOBJECT provided both an entry and guid, skipped.");

                        return false;
                    }

                    if (e.event.distance.guid != 0 && global.getObjectMgr().getGameObjectData(e.event.distance.guid) == null) {
                        Logs.SQL.error("SmartAIMgr: Event SMART_EVENT_DISTANCE_GAMEOBJECT using invalid gameobject guid {0}, skipped.", e.event.distance.guid);

                        return false;
                    }

                    if (e.event.distance.entry != 0 && global.getObjectMgr().getGameObjectTemplate(e.event.distance.entry) == null) {
                        Logs.SQL.error("SmartAIMgr: Event SMART_EVENT_DISTANCE_GAMEOBJECT using invalid gameobject entry {0}, skipped.", e.event.distance.entry);

                        return false;
                    }

                    break;
                case CounterSet:
                    if (!isMinMaxValid(e, e.event.counter.cooldownMin, e.event.counter.cooldownMax)) {
                        return false;
                    }

                    if (e.event.counter.id == 0) {
                        Logs.SQL.error("SmartAIMgr: Event SMART_EVENT_COUNTER_SET using invalid counter id {0}, skipped.", e.event.counter.id);

                        return false;
                    }

                    if (e.event.counter.value == 0) {
                        Logs.SQL.error("SmartAIMgr: Event SMART_EVENT_COUNTER_SET using invalid second {0}, skipped.", e.event.counter.value);

                        return false;
                    }

                    break;
                case Reset:
                    if (e.action.type == SmartActions.CallScriptReset) {
                        // There might be SMART_TARGET_* cases where this should be allowed, they will be handled if needed
                        Logs.SQL.error(String.format("SmartAIMgr: %1$s uses event SMART_EVENT_RESET and action SMART_ACTION_CALL_SCRIPT_RESET, skipped.", e));

                        return false;
                    }

                    break;
                case Charmed:
                    TC_SAI_IS_BOOLEAN_VALID(e, e.event.charm.onRemove);

                    break;
                case QuestObjCompletion:
                    if (global.getObjectMgr().getQuestObjective(e.event.questObjective.id) == null) {
                        Logs.SQL.error(String.format("SmartAIMgr: Event SMART_EVENT_QUEST_OBJ_COMPLETION using invalid objective id %1$s, skipped.", e.event.questObjective.id));

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
                    Logs.SQL.error(String.format("SmartAIMgr: Unused event_type %1$s skipped.", e));

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
                TC_SAI_IS_BOOLEAN_VALID(e, e.action.talk.useTalkTarget);

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
                if (e.action.faction.factionId != 0 && CliDB.FactionTemplateStorage.get(e.action.faction.factionId) == null) {
                    Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s uses non-existent Faction %2$s, skipped.", e, e.action.faction.factionId));

                    return false;
                }

                break;
            case MorphToEntryOrModel:
            case MountToEntryOrModel:
                if (e.action.morphOrMount.creature != 0 || e.action.morphOrMount.model != 0) {
                    if (e.action.morphOrMount.creature > 0 && global.getObjectMgr().getCreatureTemplate(e.action.morphOrMount.creature) == null) {
                        Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s uses non-existent Creature entry %2$s, skipped.", e, e.action.morphOrMount.creature));

                        return false;
                    }

                    if (e.action.morphOrMount.model != 0) {
                        if (e.action.morphOrMount.creature != 0) {
                            Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s has ModelID set with also set creatureId, skipped.", e));

                            return false;
                        } else if (!CliDB.CreatureDisplayInfoStorage.containsKey(e.action.morphOrMount.model)) {
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

                TC_SAI_IS_BOOLEAN_VALID(e, e.action.sound.onlySelf);

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
                    Logs.SQL.error(String.format("SmartAIMgr: %1$s uses invalid AnimKit type %2$s, skipped.", e, e.action.animKit.type));

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

                TC_SAI_IS_BOOLEAN_VALID(e, e.action.questOffer.directAdd);

                break;
            case FailQuest:
                if (!isQuestValid(e, e.action.quest.questId)) {
                    return false;
                }

                break;
            case ActivateTaxi: {
                if (!CliDB.TaxiPathStorage.containsKey(e.action.taxi.id)) {
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

                TC_SAI_IS_BOOLEAN_VALID(e, e.action.randomSound.onlySelf);

                break;
            case Cast: {
                if (!isSpellValid(e, e.action.cast.spell)) {
                    return false;
                }

                var spellInfo = global.getSpellMgr().getSpellInfo(e.action.cast.spell, Difficulty.NONE);

                for (var spellEffectInfo : spellInfo.getEffects()) {
                    if (spellEffectInfo.isEffect(SpellEffectName.killCredit) || spellEffectInfo.isEffect(SpellEffectName.KillCredit2)) {
                        if (spellEffectInfo.targetA.getTarget() == targets.UnitCaster) {
                            Logs.SQL.error(String.format("SmartAIMgr: %1$s Effect: SPELL_EFFECT_KILL_CREDIT: (SpellId: %2$s targetA: %3$s - targetB: %4$s) has invalid target for this Action", e, e.action.cast.spell, spellEffectInfo.targetA.getTarget(), spellEffectInfo.targetB.getTarget()));
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

                    long guid = e.action.crossCast.targetParam1;
                    var spawnType = targetType == SmartTargets.CreatureGuid ? SpawnObjectType.Creature : SpawnObjectType.gameObject;
                    var data = global.getObjectMgr().getSpawnData(spawnType, guid);

                    if (data == null) {
                        Logs.SQL.error(String.format("SmartAIMgr: %1$s specifies invalid CasterTargetType guid (%2$s,%3$s)", e, spawnType, guid));

                        return false;
                    } else if (e.action.crossCast.targetParam2 != 0 && e.action.crossCast.targetParam2 != data.id) {
                        Logs.SQL.error(String.format("SmartAIMgr: %1$s specifies invalid entry %2$s (expected %3$s) for CasterTargetType guid (%4$s,%5$s)", e, e.action.crossCast.targetParam2, data.id, spawnType, guid));

                        return false;
                    }
                }

                break;
            }
            case InvokerCast:
                if (e.getScriptType() != SmartScriptType.TimedActionlist && e.getEventType() != SmartEvents.link && !eventHasInvoker(e.event.type)) {
                    Logs.SQL.error(String.format("SmartAIMgr: %1$s has invoker cast action, but event does not provide any invoker!", e));

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
                var qid = global.getObjectMgr().getQuestTemplate(e.action.quest.questId);

                if (qid != null) {
                    if (!qid.hasSpecialFlag(QuestSpecialFlag.ExplorationOrEvent)) {
                        Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s SpecialFlags for Quest entry %2$s does not include FLAGS_EXPLORATION_OR_EVENT(2), skipped.", e, e.action.quest.questId));

                        return false;
                    }
                } else {
                    Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s uses non-existent Quest entry %2$s, skipped.", e, e.action.quest.questId));

                    return false;
                }

                break;
            case SetEventPhase:
                if (e.action.setEventPhase.phase >= (int) SmartPhase.max.getValue()) {
                    Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s attempts to set phase %2$s. Phase mask cannot be used past phase %3$s, skipped.", e, e.action.setEventPhase.phase, SmartPhase.Max - 1));

                    return false;
                }

                break;
            case IncEventPhase:
                if (e.action.incEventPhase.inc == 0 && e.action.incEventPhase.dec == 0) {
                    Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s is incrementing phase by 0, skipped.", e));

                    return false;
                } else if (e.action.incEventPhase.inc > (int) SmartPhase.max.getValue() || e.action.incEventPhase.dec > (int) SmartPhase.max.getValue()) {
                    Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s attempts to increment phase by too large second, skipped.", e));

                    return false;
                }

                break;
            case RemoveAurasFromSpell:
                if (e.action.removeAura.spell != 0 && !isSpellValid(e, e.action.removeAura.spell)) {
                    return false;
                }

                TC_SAI_IS_BOOLEAN_VALID(e, e.action.removeAura.onlyOwnedAuras);

                break;
            case RandomPhase: {
                if (e.action.randomPhase.phase1 >= (int) SmartPhase.max.getValue() || e.action.randomPhase.phase2 >= (int) SmartPhase.max.getValue() || e.action.randomPhase.phase3 >= (int) SmartPhase.max.getValue() || e.action.randomPhase.phase4 >= (int) SmartPhase.max.getValue() || e.action.randomPhase.phase5 >= (int) SmartPhase.max.getValue() || e.action.randomPhase.phase6 >= (int) SmartPhase.max.getValue()) {
                    Logs.SQL.error(String.format("SmartAIMgr: Entry %1$s SourceType %2$s Event %3$s Action %4$s attempts to set invalid phase, skipped.", e.entryOrGuid, e.getScriptType(), e.eventId, e.getActionType()));

                    return false;
                }

                break;
            }
            case RandomPhaseRange: //PhaseMin, PhaseMax
            {
                if (e.action.randomPhaseRange.phaseMin >= (int) SmartPhase.max.getValue() || e.action.randomPhaseRange.phaseMax >= (int) SmartPhase.max.getValue()) {
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

                if (e.action.summonCreature.type < (int) TempSummonType.TimedOrDeadDespawn.getValue() || e.action.summonCreature.type > (int) TempSummonType.ManualDespawn.getValue()) {
                    Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s uses incorrect TempSummonType %2$s, skipped.", e, e.action.summonCreature.type));

                    return false;
                }

                TC_SAI_IS_BOOLEAN_VALID(e, e.action.summonCreature.attackInvoker);

                break;
            case CallKilledmonster:
                if (!isCreatureValid(e, e.action.killedMonster.creature)) {
                    return false;
                }

                if (e.getTargetType() == SmartTargets.position) {
                    Logs.SQL.error(String.format("SmartAIMgr: %1$s uses incorrect TargetType %2$s, skipped.", e, e.getTargetType()));

                    return false;
                }

                break;
            case UpdateTemplate:
                if (e.action.updateTemplate.creature != 0 && !isCreatureValid(e, e.action.updateTemplate.creature)) {
                    return false;
                }

                TC_SAI_IS_BOOLEAN_VALID(e, e.action.updateTemplate.updateLevel);

                break;
            case SetSheath:
                if (e.action.setSheath.sheath != 0 && e.action.setSheath.sheath >= (int) sheathState.max.getValue()) {
                    Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s uses incorrect Sheath state %2$s, skipped.", e, e.action.setSheath.sheath));

                    return false;
                }

                break;
            case SetReactState: {
                if (e.action.react.state > (int) ReactStates.Aggressive.getValue()) {
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
                if (!CliDB.MapStorage.containsKey(e.action.teleport.mapID)) {
                    Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s uses non-existent Map entry %2$s, skipped.", e, e.action.teleport.mapID));

                    return false;
                }

                break;
            case WpStop:
                if (e.action.wpStop.quest != 0 && !isQuestValid(e, e.action.wpStop.quest)) {
                    return false;
                }

                TC_SAI_IS_BOOLEAN_VALID(e, e.action.wpStop.fail);

                break;
            case WpStart: {
                var path = getPath(e.action.wpStart.pathID);

                if (path == null || path.nodes.isEmpty()) {
                    Log.outError(LogFilter.ScriptsAi, String.format("SmartAIMgr: %1$s uses non-existent WaypointPath id %2$s, skipped.", e, e.action.wpStart.pathID));

                    return false;
                }

                if (e.action.wpStart.quest != 0 && !isQuestValid(e, e.action.wpStart.quest)) {
                    return false;
                }

                TC_SAI_IS_BOOLEAN_VALID(e, e.action.wpStart.run);
                TC_SAI_IS_BOOLEAN_VALID(e, e.action.wpStart.repeat);

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
                if (e.action.power.powerType > powerType.max.getValue()) {
                    Logs.SQL.error(String.format("SmartAIMgr: %1$s uses non-existent Power %2$s, skipped.", e, e.action.power.powerType));

                    return false;
                }

                break;
            case GameEventStop: {
                var eventId = e.action.gameEventStop.id;

                var events = global.getGameEventMgr().getEventMap();

                if (eventId < 1 || eventId >= events.length) {
                    Logs.SQL.error(String.format("SmartAIMgr: %1$s uses non-existent event, eventId %2$s, skipped.", e, e.action.gameEventStop.id));

                    return false;
                }

                var eventData = events[eventId];

                if (!eventData.isValid()) {
                    Logs.SQL.error(String.format("SmartAIMgr: %1$s uses non-existent event, eventId %2$s, skipped.", e, e.action.gameEventStop.id));

                    return false;
                }

                break;
            }
            case GameEventStart: {
                var eventId = e.action.gameEventStart.id;

                var events = global.getGameEventMgr().getEventMap();

                if (eventId < 1 || eventId >= events.length) {
                    Logs.SQL.error(String.format("SmartAIMgr: %1$s uses non-existent event, eventId %2$s, skipped.", e, e.action.gameEventStart.id));

                    return false;
                }

                var eventData = events[eventId];

                if (!eventData.isValid()) {
                    Logs.SQL.error(String.format("SmartAIMgr: %1$s uses non-existent event, eventId %2$s, skipped.", e, e.action.gameEventStart.id));

                    return false;
                }

                break;
            }
            case Equip: {
                if (e.getScriptType() == SmartScriptType.CREATURE) {
                    var equipId = (byte) e.action.equip.entry;

                    if (equipId != 0 && global.getObjectMgr().getEquipmentInfo((int) e.entryOrGuid, equipId) == null) {
                        Logs.SQL.error("SmartScript: SMART_ACTION_EQUIP uses non-existent equipment info id {0} for creature {1}, skipped.", equipId, e.entryOrGuid);

                        return false;
                    }
                }

                break;
            }
            case SetInstData: {
                if (e.action.setInstanceData.type > 1) {
                    Logs.SQL.error(String.format("SmartAIMgr: %1$s uses invalid data type %2$s (second range 0-1), skipped.", e, e.action.setInstanceData.type));

                    return false;
                } else if (e.action.setInstanceData.type == 1) {
                    if (e.action.setInstanceData.data > EncounterState.ToBeDecided.getValue()) {
                        Logs.SQL.error(String.format("SmartAIMgr: %1$s uses invalid boss state %2$s (second range 0-5), skipped.", e, e.action.setInstanceData.data));

                        return false;
                    }
                }

                break;
            }
            case SetIngamePhaseId: {
                var phaseId = e.action.ingamePhaseId.id;
                var apply = e.action.ingamePhaseId.apply;

                if (apply != 0 && apply != 1) {
                    Logs.SQL.error("SmartScript: SMART_ACTION_SET_INGAME_PHASE_ID uses invalid apply second {0} (Should be 0 or 1) for creature {1}, skipped", apply, e.entryOrGuid);

                    return false;
                }

                if (!CliDB.PhaseStorage.containsKey(phaseId)) {
                    Logs.SQL.error("SmartScript: SMART_ACTION_SET_INGAME_PHASE_ID uses invalid phaseid {0} for creature {1}, skipped", phaseId, e.entryOrGuid);

                    return false;
                }

                break;
            }
            case SetIngamePhaseGroup: {
                var phaseGroup = e.action.ingamePhaseGroup.groupId;
                var apply = e.action.ingamePhaseGroup.apply;

                if (apply != 0 && apply != 1) {
                    Logs.SQL.error("SmartScript: SMART_ACTION_SET_INGAME_PHASE_GROUP uses invalid apply second {0} (Should be 0 or 1) for creature {1}, skipped", apply, e.entryOrGuid);

                    return false;
                }

                if (global.getDB2Mgr().GetPhasesForGroup(phaseGroup).isEmpty()) {
                    Logs.SQL.error("SmartScript: SMART_ACTION_SET_INGAME_PHASE_GROUP uses invalid phase group id {0} for creature {1}, skipped", phaseGroup, e.entryOrGuid);

                    return false;
                }

                break;
            }
            case ScenePlay: {
                if (global.getObjectMgr().getSceneTemplate(e.action.scene.sceneId) == null) {
                    Logs.SQL.error("SmartScript: SMART_ACTION_SCENE_PLAY uses sceneId {0} but scene don't exist, skipped", e.action.scene.sceneId);

                    return false;
                }

                break;
            }
            case SceneCancel: {
                if (global.getObjectMgr().getSceneTemplate(e.action.scene.sceneId) == null) {
                    Logs.SQL.error("SmartScript: SMART_ACTION_SCENE_CANCEL uses sceneId {0} but scene don't exist, skipped", e.action.scene.sceneId);

                    return false;
                }

                break;
            }
            case RespawnBySpawnId: {
                if (global.getObjectMgr().getSpawnData(SpawnObjectType.forValue(e.action.respawnData.spawnType), e.action.respawnData.spawnId) == null) {
                    Logs.SQL.error(String.format("Entry %1$s SourceType %2$s Event %3$s Action %4$s specifies invalid spawn data (%5$s,%6$s)", e.entryOrGuid, e.getScriptType(), e.eventId, e.getActionType(), e.action.respawnData.spawnType, e.action.respawnData.spawnId));

                    return false;
                }

                break;
            }
            case EnableTempGobj: {
                if (e.action.enableTempGO.duration == 0) {
                    Logs.SQL.error(String.format("Entry %1$s SourceType %2$s Event %3$s Action %4$s does not specify duration", e.entryOrGuid, e.getScriptType(), e.eventId, e.getActionType()));

                    return false;
                }

                break;
            }
            case PlayCinematic: {
                if (!CliDB.CinematicSequencesStorage.containsKey(e.action.cinematic.entry)) {
                    Logs.SQL.error(String.format("SmartAIMgr: SMART_ACTION_PLAY_CINEMATIC %1$s uses invalid entry %2$s, skipped.", e, e.action.cinematic.entry));

                    return false;
                }

                break;
            }
            case PauseMovement: {
                if (e.action.pauseMovement.pauseTimer == 0) {
                    Logs.SQL.error(String.format("SmartAIMgr: %1$s does not specify pause duration", e));

                    return false;
                }

                TC_SAI_IS_BOOLEAN_VALID(e, e.action.pauseMovement.force);

                break;
            }
            case SetMovementSpeed: {
                if (e.action.movementSpeed.movementType >= MovementGeneratorType.max.getValue()) {
                    Logs.SQL.error(String.format("SmartAIMgr: Entry %1$s SourceType %2$s Event %3$s Action %4$s uses invalid movementType %5$s, skipped.", e.entryOrGuid, e.getScriptType(), e.eventId, e.getActionType(), e.action.movementSpeed.movementType));

                    return false;
                }

                if (e.action.movementSpeed.speedInteger == 0 && e.action.movementSpeed.speedFraction == 0) {
                    Logs.SQL.error(String.format("SmartAIMgr: Entry %1$s SourceType %2$s Event %3$s Action %4$s uses speed 0, skipped.", e.entryOrGuid, e.getScriptType(), e.eventId, e.getActionType()));

                    return false;
                }

                break;
            }
            case OverrideLight: {
                var areaEntry = CliDB.AreaTableStorage.get(e.action.overrideLight.zoneId);

                if (areaEntry == null) {
                    Logs.SQL.error(String.format("SmartAIMgr: %1$s uses non-existent zoneId %2$s, skipped.", e, e.action.overrideLight.zoneId));

                    return false;
                }

                if (areaEntry.ParentAreaID != 0) {
                    Logs.SQL.error(String.format("SmartAIMgr: %1$s uses subzone (ID: %2$s) instead of zone, skipped.", e, e.action.overrideLight.zoneId));

                    return false;
                }

                if (!CliDB.LightStorage.containsKey(e.action.overrideLight.areaLightId)) {
                    Logs.SQL.error(String.format("SmartAIMgr: %1$s uses non-existent areaLightId %2$s, skipped.", e, e.action.overrideLight.areaLightId));

                    return false;
                }

                if (e.action.overrideLight.overrideLightId != 0 && !CliDB.LightStorage.containsKey(e.action.overrideLight.overrideLightId)) {
                    Logs.SQL.error(String.format("SmartAIMgr: %1$s uses non-existent overrideLightId %2$s, skipped.", e, e.action.overrideLight.overrideLightId));

                    return false;
                }

                break;
            }
            case OverrideWeather: {
                var areaEntry = CliDB.AreaTableStorage.get(e.action.overrideWeather.zoneId);

                if (areaEntry == null) {
                    Logs.SQL.error(String.format("SmartAIMgr: %1$s uses non-existent zoneId %2$s, skipped.", e, e.action.overrideWeather.zoneId));

                    return false;
                }

                if (areaEntry.ParentAreaID != 0) {
                    Logs.SQL.error(String.format("SmartAIMgr: %1$s uses subzone (ID: %2$s) instead of zone, skipped.", e, e.action.overrideWeather.zoneId));

                    return false;
                }

                break;
            }
            case SetAIAnimKit: {
                Logs.SQL.error(String.format("SmartAIMgr: Deprecated Event:(%1$s) skipped.", e));

                break;
            }
            case SetHealthPct: {
                if (e.action.setHealthPct.percent > 100 || e.action.setHealthPct.percent == 0) {
                    Logs.SQL.error(String.format("SmartAIMgr: %1$s is trying to set invalid HP percent %2$s, skipped.", e, e.action.setHealthPct.percent));

                    return false;
                }

                break;
            }
            case AutoAttack: {
                TC_SAI_IS_BOOLEAN_VALID(e, e.action.autoAttack.attack);

                break;
            }
            case AllowCombatMovement: {
                TC_SAI_IS_BOOLEAN_VALID(e, e.action.combatMove.move);

                break;
            }
            case CallForHelp: {
                TC_SAI_IS_BOOLEAN_VALID(e, e.action.callHelp.withEmote);

                break;
            }
            case SetVisibility: {
                TC_SAI_IS_BOOLEAN_VALID(e, e.action.visibility.state);

                break;
            }
            case SetActive: {
                TC_SAI_IS_BOOLEAN_VALID(e, e.action.active.state);

                break;
            }
            case SetRun: {
                TC_SAI_IS_BOOLEAN_VALID(e, e.action.setRun.run);

                break;
            }
            case SetDisableGravity: {
                TC_SAI_IS_BOOLEAN_VALID(e, e.action.setDisableGravity.disable);

                break;
            }
            case SetCounter: {
                TC_SAI_IS_BOOLEAN_VALID(e, e.action.setCounter.reset);

                break;
            }
            case CallTimedActionlist: {
                TC_SAI_IS_BOOLEAN_VALID(e, e.action.timedActionList.allowOverride);

                break;
            }
            case InterruptSpell: {
                TC_SAI_IS_BOOLEAN_VALID(e, e.action.interruptSpellCasting.withDelayed);
                TC_SAI_IS_BOOLEAN_VALID(e, e.action.interruptSpellCasting.withInstant);

                break;
            }
            case FleeForAssist: {
                TC_SAI_IS_BOOLEAN_VALID(e, e.action.fleeAssist.withEmote);

                break;
            }
            case MoveToPos: {
                TC_SAI_IS_BOOLEAN_VALID(e, e.action.moveToPos.transport);
                TC_SAI_IS_BOOLEAN_VALID(e, e.action.moveToPos.disablePathfinding);

                break;
            }
            case SetRoot: {
                TC_SAI_IS_BOOLEAN_VALID(e, e.action.setRoot.root);

                break;
            }
            case DisableEvade: {
                TC_SAI_IS_BOOLEAN_VALID(e, e.action.disableEvade.disable);

                break;
            }
            case LoadEquipment: {
                TC_SAI_IS_BOOLEAN_VALID(e, e.action.loadEquipment.force);

                break;
            }
            case SetHover: {
                TC_SAI_IS_BOOLEAN_VALID(e, e.action.setHover.enable);

                break;
            }
            case Evade: {
                TC_SAI_IS_BOOLEAN_VALID(e, e.action.evade.toRespawnPosition);

                break;
            }
            case SetHealthRegen: {
                TC_SAI_IS_BOOLEAN_VALID(e, e.action.setHealthRegen.regenHealth);

                break;
            }
            case CreateConversation: {
                if (global.getConversationDataStorage().GetConversationTemplate(e.action.conversation.id) == null) {
                    Logs.SQL.error(String.format("SmartAIMgr: SMART_ACTION_CREATE_CONVERSATION Entry %1$s SourceType %2$s Event %3$s Action %4$s uses invalid entry %5$s, skipped.", e.entryOrGuid, e.getScriptType(), e.eventId, e.getActionType(), e.action.conversation.id));

                    return false;
                }

                break;
            }
            case SetImmunePC: {
                TC_SAI_IS_BOOLEAN_VALID(e, e.action.setImmunePC.immunePC);

                break;
            }
            case SetImmuneNPC: {
                TC_SAI_IS_BOOLEAN_VALID(e, e.action.setImmuneNPC.immuneNPC);

                break;
            }
            case SetUninteractible: {
                TC_SAI_IS_BOOLEAN_VALID(e, e.action.setUninteractible.uninteractible);

                break;
            }
            case ActivateGameobject: {
                if (!notNULL(e, e.action.activateGameObject.gameObjectAction)) {
                    return false;
                }

                if (e.action.activateGameObject.gameObjectAction >= (int) GameObjectActions.max.getValue()) {
                    Logs.SQL.error(String.format("SmartAIMgr: %1$s has gameObjectAction parameter out of range (max allowed %2$s, current second %3$s), skipped.", e, (int) GameObjectActions.max.getValue() - 1, e.action.activateGameObject.gameObjectAction));

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
                if (e.action.becomePersonalClone.type < (int) TempSummonType.TimedOrDeadDespawn.getValue() || e.action.becomePersonalClone.type > (int) TempSummonType.ManualDespawn.getValue()) {
                    Logs.SQL.error(String.format("SmartAIMgr: %1$s uses incorrect TempSummonType %2$s, skipped.", e, e.action.becomePersonalClone.type));

                    return false;
                }

                break;
            }
            case TriggerGameEvent: {
                TC_SAI_IS_BOOLEAN_VALID(e, e.action.triggerGameEvent.useSaiTargetAsGameEventSource);

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
                Logs.SQL.error(String.format("SmartAIMgr: Unused action_type: %1$s Skipped.", e));

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
}
