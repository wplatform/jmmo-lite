package com.github.azeroth.game.achievement;


import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.map.InstanceMap;
import com.github.azeroth.game.listener.interfaces.iachievement.IAchievementCriteriaOnCheck;

public class CriteriaData {
    
    public CriteriaDataType dataType = CriteriaDataType.values()[0];

    
    public creatureStruct creature = new creatureStruct();

    
    public classRaceStruct classRace = new classRaceStruct();

    
    public healthStruct health = new healthStruct();

    
    public auraStruct aura = new auraStruct();

    
    public valueStruct value = new valueStruct();

    
    public levelStruct level = new levelStruct();

    
    public genderStruct gender = new genderStruct();

    
    public mapPlayersStruct mapPlayers = new mapPlayersStruct();

    
    public teamStruct teamId = new teamStruct();

    
    public drunkStruct drunk = new drunkStruct();

    
    public holidayStruct holiday = new holidayStruct();

    
    public bgLossTeamScoreStruct battlegroundScore = new bgLossTeamScoreStruct();

    
    public equippedItemStruct equippedItem = new equippedItemStruct();

    
    public mapIdStruct mapId = new mapIdStruct();

    
    public knownTitleStruct knownTitle = new knownTitleStruct();

    
    public gameEventStruct gameEvent = new gameEventStruct();

    
    public itemQualityStruct itemQuality = new itemQualityStruct();

    
    public rawStruct raw = new rawStruct();

    
    public int scriptId;

    public CriteriaData() {
        dataType = CriteriaDataType.NONE;

        raw.value1 = 0;
        raw.value2 = 0;
        scriptId = 0;
    }


    public CriteriaData(CriteriaDataType _dataType, int _value1, int _value2, int scriptId) {
        dataType = _dataType;

        raw.value1 = _value1;
        raw.value2 = _value2;
        scriptId = scriptId;
    }

    public final boolean isValid(Criteria criteria) {
        if (dataType.getValue() >= CriteriaDataType.max.getValue()) {
            Logs.SQL.error("Table `criteria_data` for criteria (Entry: {0}) has wrong data type ({1}), ignored.", criteria.id, dataType);

            return false;
        }

        switch (criteria.entry.type) {
            case KillCreature:
            case KillAnyCreature:
            case WinBattleground:
            case MaxDistFallenWithoutDying:
            case CompleteQuest: // only hardcoded list
            case CastSpell:
            case WinAnyRankedArena:
            case DoEmote:
            case KillPlayer:
            case WinDuel:
            case GetLootByType:
            case LandTargetedSpellOnTarget:
            case BeSpellTarget:
            case GainAura:
            case EquipItemInSlot:
            case RollNeed:
            case RollGreed:
            case TrackedWorldStateUIModified:
            case EarnHonorableKill:
            case CompleteDailyQuest: // only Children's Week achievements
            case UseItem: // only Children's Week achievements
            case DeliveredKillingBlow:
            case ReachLevel:
            case Login:
            case LootAnyItem:
            case ObtainAnyItem:
                break;
            default:
                if (dataType != CriteriaDataType.script) {
                    Logs.SQL.error("Table `criteria_data` has data for non-supported criteria type (Entry: {0} Type: {1}), ignored.", criteria.id, CriteriaType.forValue(criteria.entry.type));

                    return false;
                }

                break;
        }

        switch (dataType) {
            case None:
            case InstanceScript:
                return true;
            case TCreature:
                if (creature.id == 0 || global.getObjectMgr().getCreatureTemplate(creature.id) == null) {
                    Logs.SQL.error("Table `criteria_data` (Entry: {0} Type: {1}) for data type CRITERIA_DATA_TYPE_CREATURE ({2}) has non-existing creature id in value1 ({3}), ignored.", criteria.id, criteria.entry.type, dataType, creature.id);

                    return false;
                }

                return true;
            case TPlayerClassRace:
                if (classRace.classId == 0 && classRace.raceId == 0) {
                    Logs.SQL.error("Table `criteria_data` (Entry: {0} Type: {1}) for data type CRITERIA_DATA_TYPE_T_PLAYER_CLASS_RACE ({2}) must not have 0 in either value field, ignored.", criteria.id, criteria.entry.type, dataType);

                    return false;
                }

                if (classRace.classId != 0 && ((1 << (int) (classRace.ClassId - 1)) & playerClass.ClassMaskAllPlayable.getValue()) == 0) {
                    Logs.SQL.error("Table `criteria_data` (Entry: {0} Type: {1}) for data type CRITERIA_DATA_TYPE_T_PLAYER_CLASS_RACE ({2}) has non-existing class in value1 ({3}), ignored.", criteria.id, criteria.entry.type, dataType, classRace.classId);

                    return false;
                }

                if (classRace.raceId != 0 && (SharedConst.GetMaskForRace(race.forValue(classRace.raceId)).getValue() & (long) SharedConst.RaceMaskAllPlayable.getValue()) == 0) {
                    Logs.SQL.error("Table `criteria_data` (Entry: {0} Type: {1}) for data type CRITERIA_DATA_TYPE_T_PLAYER_CLASS_RACE ({2}) has non-existing race in value2 ({3}), ignored.", criteria.id, criteria.entry.type, dataType, classRace.raceId);

                    return false;
                }

                return true;
            case TPlayerLessHealth:
                if (health.percent < 1 || health.percent > 100) {
                    Logs.SQL.error("Table `criteria_data` (Entry: {0} Type: {1}) for data type CRITERIA_DATA_TYPE_PLAYER_LESS_HEALTH ({2}) has wrong percent value in value1 ({3}), ignored.", criteria.id, criteria.entry.type, dataType, health.percent);

                    return false;
                }

                return true;
            case SAura:
            case TAura: {
                var spellEntry = global.getSpellMgr().getSpellInfo(aura.spellId, Difficulty.NONE);

                if (spellEntry == null) {
                    Logs.SQL.error("Table `criteria_data` (Entry: {0} Type: {1}) for data type {2} has wrong spell id in value1 ({3}), ignored.", criteria.id, criteria.entry.type, dataType, aura.spellId);

                    return false;
                }

                if (spellEntry.getEffects().size() <= aura.effectIndex) {
                    Logs.SQL.error("Table `criteria_data` (Entry: {0} Type: {1}) for data type {2} has wrong spell effect index in value2 ({3}), ignored.", criteria.id, criteria.entry.type, dataType, aura.effectIndex);

                    return false;
                }

                if (spellEntry.getEffect(aura.effectIndex).applyAuraName == 0) {
                    Logs.SQL.error("Table `criteria_data` (Entry: {0} Type: {1}) for data type {2} has non-aura spell effect (ID: {3} Effect: {4}), ignores.", criteria.id, criteria.entry.type, dataType, aura.spellId, aura.effectIndex);

                    return false;
                }

                return true;
            }
            case Value:
                if (value.comparisonType >= ComparisionType.max.getValue()) {
                    Logs.SQL.error("Table `criteria_data` (Entry: {0} Type: {1}) for data type CRITERIA_DATA_TYPE_VALUE ({2}) has wrong ComparisionType in value2 ({3}), ignored.", criteria.id, criteria.entry.type, dataType, value.comparisonType);

                    return false;
                }

                return true;
            case TLevel:
                if (level.min > SharedConst.GTMaxLevel) {
                    Logs.SQL.error("Table `criteria_data` (Entry: {0} Type: {1}) for data type CRITERIA_DATA_TYPE_T_LEVEL ({2}) has wrong minlevel in value1 ({3}), ignored.", criteria.id, criteria.entry.type, dataType, level.min);

                    return false;
                }

                return true;
            case TGender:
                if (gender.gender > gender.NONE.getValue()) {
                    Logs.SQL.error("Table `criteria_data` (Entry: {0} Type: {1}) for data type CRITERIA_DATA_TYPE_T_GENDER ({2}) has wrong gender in value1 ({3}), ignored.", criteria.id, criteria.entry.type, dataType, gender.gender);

                    return false;
                }

                return true;
            case Script:
                if (scriptId == 0) {
                    Logs.SQL.error("Table `criteria_data` (Entry: {0} Type: {1}) for data type CRITERIA_DATA_TYPE_SCRIPT ({2}) does not have ScriptName set, ignored.", criteria.id, criteria.entry.type, dataType);

                    return false;
                }

                return true;
            case MapPlayerCount:
                if (mapPlayers.maxCount <= 0) {
                    Logs.SQL.error("Table `criteria_data` (Entry: {0} Type: {1}) for data type CRITERIA_DATA_TYPE_MAP_PLAYER_COUNT ({2}) has wrong max players count in value1 ({3}), ignored.", criteria.id, criteria.entry.type, dataType, mapPlayers.maxCount);

                    return false;
                }

                return true;
            case TTeam:
                if (teamId.team != Team.ALLIANCE.getValue() && teamId.team != Team.Horde.getValue()) {
                    Logs.SQL.error("Table `criteria_data` (Entry: {0} Type: {1}) for data type CRITERIA_DATA_TYPE_T_TEAM ({2}) has unknown team in value1 ({3}), ignored.", criteria.id, criteria.entry.type, dataType, teamId.team);

                    return false;
                }

                return true;
            case SDrunk:
                if (drunk.state >= 4) {
                    Logs.SQL.error("Table `criteria_data` (Entry: {0} Type: {1}) for data type CRITERIA_DATA_TYPE_S_DRUNK ({2}) has unknown drunken state in value1 ({3}), ignored.", criteria.id, criteria.entry.type, dataType, drunk.state);

                    return false;
                }

                return true;
            case Holiday:
                if (!CliDB.HolidaysStorage.containsKey(holiday.id)) {
                    Logs.SQL.error("Table `criteria_data`(Entry: {0} Type: {1}) for data type CRITERIA_DATA_TYPE_HOLIDAY ({2}) has unknown holiday in value1 ({3}), ignored.", criteria.id, criteria.entry.type, dataType, holiday.id);

                    return false;
                }

                return true;
            case GameEvent: {
                var events = global.getGameEventMgr().getEventMap();

                if (gameEvent.id < 1 || gameEvent.id >= events.length) {
                    Logs.SQL.error("Table `criteria_data` (Entry: {0} Type: {1}) for data type CRITERIA_DATA_TYPE_GAME_EVENT ({2}) has unknown game_event in value1 ({3}), ignored.", criteria.id, criteria.entry.type, dataType, gameEvent.id);

                    return false;
                }

                return true;
            }
            case BgLossTeamScore:
                return true; // not check correctness node indexes
            case SEquippedItem:
                if (equippedItem.itemQuality >= (int) itemQuality.max.getValue()) {
                    Logs.SQL.error("Table `achievement_criteria_requirement` (Entry: {0} Type: {1}) for requirement ACHIEVEMENT_CRITERIA_REQUIRE_S_EQUIPED_ITEM ({2}) has unknown quality state in value1 ({3}), ignored.", criteria.id, criteria.entry.type, dataType, equippedItem.itemQuality);

                    return false;
                }

                return true;
            case MapId:
                if (!CliDB.MapStorage.containsKey(mapId.id)) {
                    Logs.SQL.error("Table `criteria_data` (Entry: {0} Type: {1}) for data type CRITERIA_DATA_TYPE_MAP_ID ({2}) contains an unknown map entry in value1 ({3}), ignored.", criteria.id, criteria.entry.type, dataType, mapId.id);

                    return false;
                }

                return true;
            case SPlayerClassRace:
                if (classRace.classId == 0 && classRace.raceId == 0) {
                    Logs.SQL.error("Table `criteria_data` (Entry: {0} Type: {1}) for data type CRITERIA_DATA_TYPE_S_PLAYER_CLASS_RACE ({2}) must not have 0 in either value field, ignored.", criteria.id, criteria.entry.type, dataType);

                    return false;
                }

                if (classRace.classId != 0 && ((1 << (int) (classRace.ClassId - 1)) & playerClass.ClassMaskAllPlayable.getValue()) == 0) {
                    Logs.SQL.error("Table `criteria_data` (Entry: {0} Type: {1}) for data type CRITERIA_DATA_TYPE_S_PLAYER_CLASS_RACE ({2}) has non-existing class in value1 ({3}), ignored.", criteria.id, criteria.entry.type, dataType, classRace.classId);

                    return false;
                }

                if (classRace.raceId != 0 && ((long) SharedConst.GetMaskForRace(race.forValue(classRace.raceId)).getValue() & SharedConst.RaceMaskAllPlayable.getValue()) == 0) {
                    Logs.SQL.error("Table `criteria_data` (Entry: {0} Type: {1}) for data type CRITERIA_DATA_TYPE_S_PLAYER_CLASS_RACE ({2}) has non-existing race in value2 ({3}), ignored.", criteria.id, criteria.entry.type, dataType, classRace.raceId);

                    return false;
                }

                return true;
            case SKnownTitle:
                if (!CliDB.CharTitlesStorage.containsKey(knownTitle.id)) {
                    Logs.SQL.error("Table `criteria_data` (Entry: {0} Type: {1}) for data type CRITERIA_DATA_TYPE_S_KNOWN_TITLE ({2}) contains an unknown title_id in value1 ({3}), ignore.", criteria.id, criteria.entry.type, dataType, knownTitle.id);

                    return false;
                }

                return true;
            case SItemQuality:
                if (itemQuality.quality >= (int) itemQuality.max.getValue()) {
                    Logs.SQL.error("Table `criteria_data` (Entry: {0} Type: {1}) for data type CRITERIA_DATA_TYPE_S_ITEM_QUALITY ({2}) contains an unknown quality state value in value1 ({3}), ignored.", criteria.id, criteria.entry.type, dataType, itemQuality.quality);

                    return false;
                }

                return true;
            default:
                Logs.SQL.error("Table `criteria_data` (Entry: {0} Type: {1}) contains data of a non-supported data type ({2}), ignored.", criteria.id, criteria.entry.type, dataType);

                return false;
        }
    }


    public final boolean meets(int criteriaId, Player source, WorldObject target, int miscValue1) {
        return meets(criteriaId, source, target, miscValue1, 0);
    }

    public final boolean meets(int criteriaId, Player source, WorldObject target) {
        return meets(criteriaId, source, target, 0, 0);
    }

    public final boolean meets(int criteriaId, Player source, WorldObject target, int miscValue1, int miscValue2) {
        switch (dataType) {
            case None:
                return true;
            case TCreature:
                if (target == null || !target.isTypeId(TypeId.UNIT)) {
                    return false;
                }

                return target.getEntry() == creature.id;
            case TPlayerClassRace:
                if (target == null || !target.isTypeId(TypeId.PLAYER)) {
                    return false;
                }

                if (classRace.classId != 0 && classRace.classId != (int) target.toPlayer().getClass().getValue()) {
                    return false;
                }

                if (classRace.raceId != 0 && classRace.raceId != (int) target.toPlayer().getRace().getValue()) {
                    return false;
                }

                return true;
            case SPlayerClassRace:
                if (source == null || !source.isTypeId(TypeId.PLAYER)) {
                    return false;
                }

                if (classRace.classId != 0 && classRace.classId != (int) source.toPlayer().getClass().getValue()) {
                    return false;
                }

                if (classRace.raceId != 0 && classRace.raceId != (int) source.toPlayer().getRace().getValue()) {
                    return false;
                }

                return true;
            case TPlayerLessHealth:
                if (target == null || !target.isTypeId(TypeId.PLAYER)) {
                    return false;
                }

                return !target.toPlayer().healthAbovePct((int) health.percent);
            case SAura:
                return source.hasAuraEffect(aura.spellId, (byte) aura.effectIndex);
            case TAura: {
                if (target == null) {
                    return false;
                }

                var unitTarget = target.toUnit();

                if (unitTarget == null) {
                    return false;
                }

                return unitTarget.hasAuraEffect(aura.spellId, aura.effectIndex);
            }
            case Value:
                return MathUtil.CompareValues(ComparisionType.forValue(value.comparisonType), miscValue1, value.value);
            case TLevel:
                if (target == null) {
                    return false;
                }

                return target.getLevelForTarget(source) >= level.min;
            case TGender: {
                if (target == null) {
                    return false;
                }

                var unitTarget = target.toUnit();

                if (unitTarget == null) {
                    return false;
                }

                return unitTarget.getGender() == gender.forValue((byte) gender.gender);
            }
            case Script: {
                Unit unitTarget = null;

                if (target) {
                    unitTarget = target.toUnit();
                }

                return global.getScriptMgr().<IAchievementCriteriaOnCheck>RunScriptRet(p -> p.OnCheck(source.toPlayer(), unitTarget.toUnit()), scriptId);
            }
            case MapPlayerCount:
                return source.getMap().getPlayersCountExceptGMs() <= mapPlayers.maxCount;
            case TTeam:
                if (target == null || !target.isTypeId(TypeId.PLAYER)) {
                    return false;
                }

                return (int) target.toPlayer().getTeam().getValue() == teamId.team;
            case SDrunk:
                return player.getDrunkenstateByValue(source.getDrunkValue()) >= DrunkenState.forValue(drunk.state);
            case Holiday:
                return global.getGameEventMgr().isHolidayActive(HolidayIds.forValue(holiday.id));
            case GameEvent:
                return global.getGameEventMgr().isEventActive((short) gameEvent.id);
            case BgLossTeamScore: {
                var bg = source.getBattleground();

                if (!bg) {
                    return false;
                }

                var score = bg.getTeamScore(bg.getPlayerTeam(source.getGUID()) == Team.ALLIANCE ? TeamId.HORDE : TeamId.ALLIANCE);

                return score >= battlegroundScore.min && score <= battlegroundScore.max;
            }
            case InstanceScript: {
                if (!source.isInWorld()) {
                    return false;
                }

                var map = source.getMap();

                if (!map.isDungeon()) {
                    Log.outError(LogFilter.achievement, "Achievement system call AchievementCriteriaDataType.InstanceScript ({0}) for achievement criteria {1} for non-dungeon/non-raid map {2}", CriteriaDataType.InstanceScript, criteriaId, map.getId());

                    return false;
                }

                var instance = ((InstanceMap) map).getInstanceScript();

                if (instance == null) {
                    Log.outError(LogFilter.achievement, "Achievement system call criteria_data_INSTANCE_SCRIPT ({0}) for achievement criteria {1} for map {2} but map does not have a instance script", CriteriaDataType.InstanceScript, criteriaId, map.getId());

                    return false;
                }

                Unit unitTarget = null;

                if (target != null) {
                    unitTarget = target.toUnit();
                }

                return instance.checkAchievementCriteriaMeet(criteriaId, source, unitTarget, miscValue1);
            }
            case SEquippedItem: {
                var entry = global.getCriteriaMgr().getCriteria(criteriaId);

                var itemId = entry.entry.type == CriteriaType.EquipItemInSlot ? miscValue2 : miscValue1;
                var itemTemplate = global.getObjectMgr().getItemTemplate(itemId);

                if (itemTemplate == null) {
                    return false;
                }

                return itemTemplate.getBaseItemLevel() >= equippedItem.itemLevel && (int) itemTemplate.getQuality().getValue() >= equippedItem.itemQuality;
            }
            case MapId:
                return source.getLocation().getMapId() == mapId.id;
            case SKnownTitle: {
                var titleInfo = CliDB.CharTitlesStorage.get(knownTitle.id);

                if (titleInfo != null) {
                    return source && source.hasTitle(titleInfo.MaskID);
                }

                return false;
            }
            case SItemQuality: {
                var pProto = global.getObjectMgr().getItemTemplate(miscValue1);

                if (pProto == null) {
                    return false;
                }

                return (int) pProto.getQuality().getValue() == itemQuality.quality;
            }
            default:
                break;
        }

        return false;
    }


    ///#region Structs

    // criteria_data_TYPE_NONE              = 0 (no data)
    // criteria_data_TYPE_T_CREATURE        = 1
    public final static class CreatureStruct {

        public int id;

        public CreatureStruct clone() {
            CreatureStruct varCopy = new creatureStruct();

            varCopy.id = this.id;

            return varCopy;
        }
    }

    // criteria_data_TYPE_T_PLAYER_CLASS_RACE = 2
    // criteria_data_TYPE_S_PLAYER_CLASS_RACE = 21
    public final static class ClassRaceStruct {

        public int classId;

        public int raceId;

        public ClassRaceStruct clone() {
            ClassRaceStruct varCopy = new classRaceStruct();

            varCopy.classId = this.classId;
            varCopy.raceId = this.raceId;

            return varCopy;
        }
    }

    // criteria_data_TYPE_T_PLAYER_LESS_HEALTH = 3
    public final static class HealthStruct {

        public int percent;

        public HealthStruct clone() {
            HealthStruct varCopy = new healthStruct();

            varCopy.percent = this.percent;

            return varCopy;
        }
    }

    // criteria_data_TYPE_S_AURA            = 5
    // criteria_data_TYPE_T_AURA            = 7
    public final static class AuraStruct {

        public int spellId;
        public int effectIndex;

        public AuraStruct clone() {
            AuraStruct varCopy = new auraStruct();

            varCopy.spellId = this.spellId;
            varCopy.effectIndex = this.effectIndex;

            return varCopy;
        }
    }

    // criteria_data_TYPE_VALUE             = 8
    public final static class ValueStruct {

        public int value;

        public int comparisonType;

        public ValueStruct clone() {
            ValueStruct varCopy = new valueStruct();

            varCopy.value = this.value;
            varCopy.comparisonType = this.comparisonType;

            return varCopy;
        }
    }

    // criteria_data_TYPE_T_LEVEL           = 9
    public final static class LevelStruct {

        public int min;

        public LevelStruct clone() {
            LevelStruct varCopy = new levelStruct();

            varCopy.min = this.min;

            return varCopy;
        }
    }

    // criteria_data_TYPE_T_GENDER          = 10
    public final static class GenderStruct {

        public int gender;

        public GenderStruct clone() {
            GenderStruct varCopy = new genderStruct();

            varCopy.gender = this.gender;

            return varCopy;
        }
    }

    // criteria_data_TYPE_SCRIPT            = 11 (no data)
    // criteria_data_TYPE_MAP_PLAYER_COUNT  = 13
    public final static class MapPlayersStruct {

        public int maxCount;

        public MapPlayersStruct clone() {
            MapPlayersStruct varCopy = new mapPlayersStruct();

            varCopy.maxCount = this.maxCount;

            return varCopy;
        }
    }

    // criteria_data_TYPE_T_TEAM            = 14
    public final static class TeamStruct {

        public int team;

        public TeamStruct clone() {
            TeamStruct varCopy = new teamStruct();

            varCopy.team = this.team;

            return varCopy;
        }
    }

    // criteria_data_TYPE_S_DRUNK           = 15
    public final static class DrunkStruct {

        public int state;

        public DrunkStruct clone() {
            DrunkStruct varCopy = new drunkStruct();

            varCopy.state = this.state;

            return varCopy;
        }
    }

    // criteria_data_TYPE_HOLIDAY           = 16
    public final static class HolidayStruct {

        public int id;

        public HolidayStruct clone() {
            HolidayStruct varCopy = new holidayStruct();

            varCopy.id = this.id;

            return varCopy;
        }
    }

    // criteria_data_TYPE_BG_LOSS_TEAM_SCORE= 17
    public final static class BgLossTeamScoreStruct {

        public int min;

        public int max;

        public BgLossTeamScoreStruct clone() {
            BgLossTeamScoreStruct varCopy = new bgLossTeamScoreStruct();

            varCopy.min = this.min;
            varCopy.max = this.max;

            return varCopy;
        }
    }

    // criteria_data_INSTANCE_SCRIPT        = 18 (no data)
    // criteria_data_TYPE_S_EQUIPED_ITEM    = 19
    public final static class EquippedItemStruct {

        public int itemLevel;

        public int itemQuality;

        public EquippedItemStruct clone() {
            EquippedItemStruct varCopy = new equippedItemStruct();

            varCopy.itemLevel = this.itemLevel;
            varCopy.itemQuality = this.itemQuality;

            return varCopy;
        }
    }

    // criteria_data_TYPE_MAP_ID            = 20
    public final static class MapIdStruct {

        public int id;

        public MapIdStruct clone() {
            MapIdStruct varCopy = new mapIdStruct();

            varCopy.id = this.id;

            return varCopy;
        }
    }

    // criteria_data_TYPE_KNOWN_TITLE       = 23
    public final static class KnownTitleStruct {

        public int id;

        public KnownTitleStruct clone() {
            KnownTitleStruct varCopy = new knownTitleStruct();

            varCopy.id = this.id;

            return varCopy;
        }
    }

    // CRITERIA_DATA_TYPE_S_ITEM_QUALITY    = 24
    public final static class ItemQualityStruct {

        public int quality;

        public ItemQualityStruct clone() {
            ItemQualityStruct varCopy = new itemQualityStruct();

            varCopy.quality = this.quality;

            return varCopy;
        }
    }

    // criteria_data_TYPE_GAME_EVENT           = 25
    public final static class GameEventStruct {

        public int id;

        public GameEventStruct clone() {
            GameEventStruct varCopy = new gameEventStruct();

            varCopy.id = this.id;

            return varCopy;
        }
    }

    // raw
    public final static class RawStruct {

        public int value1;

        public int value2;

        public RawStruct clone() {
            RawStruct varCopy = new rawStruct();

            varCopy.value1 = this.value1;
            varCopy.value2 = this.value2;

            return varCopy;
        }
    }


    ///#endregion
}
