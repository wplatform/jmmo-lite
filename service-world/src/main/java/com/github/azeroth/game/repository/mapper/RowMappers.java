package com.github.azeroth.game.repository.mapper;

import com.github.azeroth.common.EnumFlag;
import com.github.azeroth.common.Locale;
import com.github.azeroth.common.Logs;
import com.github.azeroth.dbc.defines.PhaseUseFlag;
import com.github.azeroth.defines.*;
import com.github.azeroth.game.domain.areatrigger.*;
import com.github.azeroth.game.domain.creature.*;
import com.github.azeroth.game.domain.gobject.*;
import com.github.azeroth.game.domain.misc.NpcText;
import com.github.azeroth.game.domain.misc.WorldSafeLocEntry;
import com.github.azeroth.game.domain.object.WorldLocation;
import com.github.azeroth.game.domain.player.PlayerInfo;
import com.github.azeroth.game.domain.reputation.RepSpilloverTemplate;
import com.github.azeroth.game.domain.unit.NPCFlag;
import com.github.azeroth.game.domain.unit.UnitFlag;
import com.github.azeroth.game.domain.unit.UnitFlag2;
import com.github.azeroth.game.domain.unit.UnitFlag3;
import org.springframework.jdbc.core.RowMapper;

public interface RowMappers {

    RowMapper<RepSpilloverTemplate> REP_SPILLOVER_TEMPLATE_RESULT_SET_EXTRACTOR = (rs, rowNum) -> {
        RepSpilloverTemplate repTemplate = new RepSpilloverTemplate();
        int index = 1;
        repTemplate.factionId = rs.getInt(index++);
        repTemplate.faction[0] = rs.getInt(index++);
        repTemplate.factionRate[0] = rs.getFloat(index++);
        repTemplate.factionRank[0] = rs.getInt(index++);
        repTemplate.faction[1] = rs.getInt(index++);
        repTemplate.factionRate[1] = rs.getFloat(index++);
        repTemplate.factionRank[1] = rs.getInt(index++);
        repTemplate.faction[2] = rs.getInt(index++);
        repTemplate.factionRate[2] = rs.getFloat(index++);
        repTemplate.factionRank[2] = rs.getInt(index++);
        repTemplate.faction[3] = rs.getInt(index++);
        repTemplate.factionRate[3] = rs.getFloat(index++);
        repTemplate.factionRank[3] = rs.getInt(index++);
        repTemplate.faction[4] = rs.getInt(index++);
        repTemplate.factionRate[4] = rs.getFloat(index++);
        repTemplate.factionRank[4] = rs.getInt(index++);
        return repTemplate;
    };


    RowMapper<NpcText> NPC_TEXT_SET_EXTRACTOR = (rs, rowNum) -> {
        NpcText npcText = new NpcText();
        npcText.id = rs.getInt(1);
        for (int i = 0; i < SharedDefine.MAX_NPC_TEXT_OPTIONS; ++i) {
            npcText.data[i].probability = rs.getFloat(2 + i);
            npcText.data[i].broadcastTextID = rs.getInt(2 + SharedDefine.MAX_NPC_TEXT_OPTIONS + i);
        }
        return npcText;
    };


    RowMapper<CreatureTemplate> CREATURE_TEMPLATE_ROW_MAPPER = (rs, rowNum) -> {

        CreatureTemplate creature = new CreatureTemplate();

        creature.entry = rs.getInt(1);

        for (int i = 0; i < CreatureTemplate.MAX_KILL_CREDIT; ++i)
            creature.killCredit[i] = rs.getInt(2 + i);

        creature.name.set(Locale.enUS, rs.getString(4));
        creature.femaleName.set(Locale.enUS, rs.getString(5));
        creature.subName.set(Locale.enUS, rs.getString(6));
        creature.titleAlt.set(Locale.enUS, rs.getString(7));
        creature.iconName.set(Locale.enUS, rs.getString(8));
        creature.requiredExpansion = rs.getInt(9);
        creature.vignetteID = rs.getInt(10);
        creature.faction = rs.getInt(11);
        creature.npcFlag = EnumFlag.of(NPCFlag.class, rs.getInt(12));
        creature.speedWalk = rs.getFloat(13);
        creature.speedRun = rs.getFloat(14);
        creature.scale = rs.getFloat(15);
        creature.classification = CreatureClassification.values()[rs.getByte(16)];
        int dmgSchool = rs.getInt(17);
        if (dmgSchool > -1 && dmgSchool < SpellSchool.values().length) {
            creature.dmgSchool = SpellSchool.values()[dmgSchool];
        } else {
            Logs.SQL.error("Creature (Entry: {}) has invalid unit_class ({}) in creature_template. Set to 1 (UNIT_CLASS_WARRIOR).", creature.entry, creature.unitClass);
            creature.unitClass = UnitClass.WARRIOR;
        }

        creature.baseAttackTime = rs.getInt(18);
        creature.rangeAttackTime = rs.getInt(19);
        creature.baseVariance = rs.getFloat(20);
        creature.rangeVariance = rs.getFloat(21);
        int unitClass = rs.getInt(22);
        if (dmgSchool > 0 && dmgSchool < SpellSchool.values().length) {
            creature.unitClass = UnitClass.values()[unitClass];
        } else {
            Logs.SQL.error("Creature (Entry: {}) has invalid spell school value ({}) in `dmgschool`.", creature.entry, unitClass);
            creature.dmgSchool = SpellSchool.NORMAL;
        }
        creature.unitFlags = EnumFlag.of(UnitFlag.class, rs.getInt(23));
        creature.unitFlags2 = EnumFlag.of(UnitFlag2.class, rs.getInt(24));
        creature.unitFlags3 = EnumFlag.of(UnitFlag3.class, rs.getInt(25));
        creature.family = CreatureFamily.values()[rs.getInt(26)];
        creature.trainerClass = UnitClass.values()[rs.getInt(27)];
        creature.type = CreatureType.values()[rs.getInt(28)];

        creature.vehicleId = rs.getInt(29);
        creature.aiName = rs.getString(30);
        creature.movementType = rs.getInt(31);

        int ground = rs.getInt(32);
        if (!rs.wasNull() && ground < CreatureGroundMovementType.values().length) {
            creature.movement.ground = CreatureGroundMovementType.values()[ground];
        }

        boolean swim = rs.getBoolean(33);
        if (!rs.wasNull())
            creature.movement.swim = swim;

        int flight = rs.getByte(34);
        if (!rs.wasNull() && ground < CreatureFlightMovementType.values().length)
            creature.movement.flight = CreatureFlightMovementType.values()[flight];

        boolean rooted = rs.getBoolean(35);
        if (!rs.wasNull())
            creature.movement.rooted = rooted;

        int chase = rs.getByte(36);
        if (!rs.wasNull() && ground < CreatureChaseMovementType.values().length)
            creature.movement.chase = CreatureChaseMovementType.values()[chase];

        int random = rs.getByte(37);
        if (!rs.wasNull() && ground < CreatureRandomMovementType.values().length)
            creature.movement.random = CreatureRandomMovementType.values()[random];

        int interactionPauseTimer = rs.getInt(38);
        if (!rs.wasNull())
            creature.movement.interactionPauseTimer = interactionPauseTimer;

        creature.modExperience = rs.getFloat(39);
        creature.racialLeader = rs.getBoolean(40);
        creature.movementId = rs.getInt(41);
        creature.regenHealth = rs.getBoolean(42);
        creature.creatureImmunitiesId = rs.getInt(43);
        creature.flagsExtra = EnumFlag.of(CreatureFlagExtra.class, rs.getInt(44));
        creature.script = rs.getString(45);
        creature.stringId = rs.getString(46);

        return creature;
    };


    RowMapper<CreatureMovementData> CREATURE_MOVEMENT_ROWMAPPER = (rs, rowNum) -> {

        CreatureMovementData movement = new CreatureMovementData();

        movement.spawnId = rs.getInt(1);

        int ground = rs.getInt(2);
        if (!rs.wasNull() && ground < CreatureGroundMovementType.values().length) {
            movement.ground = CreatureGroundMovementType.values()[ground];
        }

        boolean swim = rs.getBoolean(3);
        if (!rs.wasNull())
            movement.swim = swim;

        int flight = rs.getByte(4);
        if (!rs.wasNull() && ground < CreatureFlightMovementType.values().length)
            movement.flight = CreatureFlightMovementType.values()[flight];

        boolean rooted = rs.getBoolean(5);
        if (!rs.wasNull())
            movement.rooted = rooted;

        int chase = rs.getByte(6);
        if (!rs.wasNull() && ground < CreatureChaseMovementType.values().length)
            movement.chase = CreatureChaseMovementType.values()[chase];

        int random = rs.getByte(7);
        if (!rs.wasNull() && ground < CreatureRandomMovementType.values().length)
            movement.random = CreatureRandomMovementType.values()[random];

        int interactionPauseTimer = rs.getInt(8);
        if (!rs.wasNull())
            movement.interactionPauseTimer = interactionPauseTimer;


        return movement;
    };


    RowMapper<PlayerInfo> PLAYER_INFO_ROW_MAPPER = (rs, rowNum) -> {
        PlayerInfo playerInfo = new PlayerInfo();
        playerInfo.race = Race.values()[rs.getByte(1)];
        playerInfo.playClass = UnitClass.values()[rs.getByte(2)];
        playerInfo.mapId = rs.getInt(3);
        playerInfo.x = rs.getFloat(4);
        playerInfo.y = rs.getFloat(5);
        playerInfo.z = rs.getFloat(6);
        playerInfo.o = rs.getFloat(7);

        int introMovieId = rs.getInt(8);
        if (!rs.wasNull()) {
            playerInfo.introMovieId = introMovieId;
        }
        return playerInfo;
    };


    RowMapper<GameObjectTemplate> GAME_OBJECT_TEMPLATE_ROW_MAPPER = (rs, rowNum) -> {
        GameObjectTemplate got = new GameObjectTemplate();

        got.entry = rs.getInt(1);
        got.type = GameObjectType.values()[rs.getByte(2)];
        got.displayId = rs.getInt(3);
        got.name.set(Locale.enUS, rs.getString(4));
        got.iconName = rs.getString(5);
        got.castBarCaption.set(Locale.enUS, rs.getString(6));
        got.unk1.set(Locale.enUS, rs.getString(7));
        got.size = rs.getFloat(8);

        for (byte i = 0; i < got.raw.length; ++i)
            got.raw[i] = rs.getInt(9 + i);

        got.requiredLevel = rs.getInt(42);
        got.aiName = rs.getString(43);
        got.scriptName = rs.getString(44);
        got.stringId = rs.getString(45);

        return got;
    };


    RowMapper<GameObjectTemplateAddon> GAME_OBJECT_TEMPLATE_ADDON_ROW_MAPPER = (rs, rowNum) -> {
        GameObjectTemplateAddon gameObjectAddon = new GameObjectTemplateAddon();

        gameObjectAddon.entry = rs.getInt(1);
        gameObjectAddon.faction = rs.getInt(2);
        gameObjectAddon.flags = GameObjectFlag.values()[rs.getInt(3)];
        gameObjectAddon.mingold = rs.getInt(4);
        gameObjectAddon.maxgold = rs.getInt(5);
        gameObjectAddon.worldEffectId = rs.getInt(11);
        gameObjectAddon.aiAnimKitId = rs.getInt(12);

        for (byte i = 0; i < gameObjectAddon.artKits.length; ++i)
            gameObjectAddon.artKits[i] = rs.getInt(6 + i);
        return gameObjectAddon;
    };


    RowMapper<GameObjectData> GAME_OBJECTS_ROW_MAPPER = (rs, rowNum) -> {
        GameObjectData data = new GameObjectData();

        data.spawnId = rs.getInt(1);
        data.id = rs.getInt(2);
        data.mapId = rs.getInt(3);
        data.positionX = rs.getFloat(4);
        data.positionY = rs.getFloat(5);
        data.positionZ = rs.getFloat(6);
        data.positionO = rs.getFloat(7);
        data.rotation.x = rs.getFloat(8);
        data.rotation.y = rs.getFloat(9);
        data.rotation.z = rs.getFloat(10);
        data.rotation.w = rs.getFloat(11);
        data.spawnTimeSecs = rs.getInt(12);

        data.animProgress = rs.getInt(13);
        data.artKit = 0;
        data.goState = GOState.valueOf(rs.getByte(13));
        data.spawnDifficultiesText = rs.getString(14);
        data.gameEvent = rs.getInt(15);
        data.poolId = rs.getInt(16);
        data.phaseUseFlags = EnumFlag.of(PhaseUseFlag.class, rs.getInt(17));
        data.phaseId = rs.getInt(18);
        data.phaseGroup = rs.getInt(19);
        data.terrainSwapMap = rs.getInt(20);
        data.script = rs.getString(21);
        data.stringId = rs.getString(22);

        return data;
    };


    RowMapper<GameObjectAddon> GAME_OBJECT_ADDON_ROW_MAPPER = (rs, rowNum) -> {
        GameObjectAddon data = new GameObjectAddon();
        data.guid = rs.getInt(1);
        data.parentRotation = new QuaternionData(rs.getInt(2), rs.getInt(3), rs.getInt(4), rs.getInt(5));
        int invisibilityTypeValue = rs.getInt(6);
        if (invisibilityTypeValue >= InvisibilityType.values().length) {
            Logs.SQL.error("GameObject (GUID: {}) has invalid InvisibilityType in `gameobject_addon`, disabled invisibility", data.guid);
            data.invisibilityType = InvisibilityType.GENERAL;
            data.invisibilityValue = 0;
        } else {
            data.invisibilityType = InvisibilityType.values()[invisibilityTypeValue];
            data.invisibilityValue = rs.getInt(7);
        }
        data.worldEffectID = rs.getInt(8);
        data.aiAnimKitID = rs.getInt(8);
        return data;
    };


    RowMapper<CreatureData> CREATURE_ROW_MAPPER = (rs, rowNum) -> {
        CreatureData data = new CreatureData();

        data.spawnId = rs.getInt(1);
        data.id = rs.getInt(2);
        data.mapId = rs.getInt(3);
        data.positionX = rs.getFloat(4);
        data.positionY = rs.getFloat(5);
        data.positionZ = rs.getFloat(6);
        data.positionO = rs.getFloat(7);
        int displayId = rs.getInt(8);
        if (displayId == 0) {
            data.display = new CreatureModel(displayId, 1.0f, 1.0f);
        }

        data.equipmentId = rs.getByte(9);
        data.spawnTimeSecs = rs.getInt(10);
        data.wanderDistance = rs.getFloat(11);
        data.currentWaypoint = rs.getInt(12);
        data.curHealthPct = rs.getInt(13);
        data.movementType = rs.getByte(14);
        data.spawnDifficultiesText = rs.getString(15);
        data.gameEvent = rs.getInt(16);
        data.poolId = rs.getInt(17);

        long npcFlag = rs.getLong(18);
        if (!rs.wasNull())
            data.npcFlag = npcFlag;

        int unitFlags = rs.getInt(19);
        if (!rs.wasNull())
            data.unitFlags = EnumFlag.of(UnitFlag.class, unitFlags);

        int unitFlags2 = rs.getInt(20);
        if (!rs.wasNull())
            data.unitFlags2 = EnumFlag.of(UnitFlag2.class, unitFlags2);

        int unitFlags3 = rs.getInt(21);
        if (!rs.wasNull())
            data.unitFlags3 = EnumFlag.of(UnitFlag3.class, unitFlags3);

        data.phaseUseFlags = EnumFlag.of(PhaseUseFlag.class, rs.getByte(22));
        data.phaseId = rs.getInt(23);
        data.phaseGroup = rs.getInt(24);
        data.terrainSwapMap = rs.getInt(25);
        data.script = rs.getString(26);
        data.stringId = rs.getString(27);
        return data;
    };


    RowMapper<AreaTriggerAction> AREA_TRIGGER_ACTION_ROW_MAPPER = (rs, rowNum) -> {
        AreaTriggerAction data = new AreaTriggerAction();
        data.id = new AreaTriggerId(rs.getInt(1), rs.getBoolean(2));
        data.param = rs.getInt(3);
        int actionType = rs.getInt(4);
        int targetType = rs.getInt(5);
        if (actionType < AreaTriggerActionType.values().length) {
            data.actionType = AreaTriggerActionType.values()[actionType];

        }
        if (targetType < AreaTriggerActionUserTypes.values().length) {
            data.targetType = AreaTriggerActionUserTypes.values()[targetType];
        }
        return data;
    };

    RowMapper<AreaTriggerCreateProperty> AREA_TRIGGER_CREATE_PROPERTY_ROW_MAPPER = (rs, rowNum) -> {
        AreaTriggerCreateProperty data = new AreaTriggerCreateProperty();

        data.id = new AreaTriggerId(rs.getInt(1), rs.getBoolean(2));
        data.templateId = new AreaTriggerId(rs.getInt(3), rs.getBoolean(4));

        data.Flags = EnumFlag.of(AreaTriggerCreatePropertiesFlag.class, rs.getInt(5));

        data.MoveCurveId = rs.getInt(6);
        data.ScaleCurveId = rs.getInt(7);
        data.MorphCurveId = rs.getInt(8);
        data.FacingCurveId = rs.getInt(9);



        data.AnimId = rs.getInt(10);
        data.AnimKitId = rs.getInt(11);
        data.DecalPropertiesId = rs.getInt(12);

        int spellForVisuals = rs.getInt(13);
        if(!rs.wasNull()) {
            data.SpellForVisuals = spellForVisuals;
        }

        data.TimeToTargetScale     = rs.getInt(14);
        data.Speed                 = rs.getFloat(15);


        data.Shape.Type = AreaTriggerShapeType.values()[rs.getByte(16)];

        for (var i = 0; i < AreaTriggerDefine.MAX_AREA_TRIGGER_ENTITY_DATA; ++i)
            data.Shape.DefaultData.Data[i] = rs.getFloat(16 + i);

        data.scriptName = rs.getString(25);



        return data;
    };



    RowMapper<WorldSafeLocEntry> WORLD_SAFE_LOC_ENTRY_ROW_MAPPER = (rs, rowNum) -> {
        WorldSafeLocEntry data = new WorldSafeLocEntry();

        data.id = rs.getInt(1);

        data.loc = new WorldLocation(rs.getInt(2), rs.getFloat(3), rs.getFloat(4), rs.getFloat(5));

        int transportSpawnId = rs.getInt(6);
        if(!rs.wasNull()) {
            data.transportSpawnId = transportSpawnId;
        }

        return data;
    };

    RowMapper<FormationInfo> CREATURE_FORMATION_ROW_MAPPER = (rs, rowNum) -> {
        FormationInfo data = new FormationInfo();
        data.leaderSpawnId = rs.getInt(1);
        data.memberSpawnId = rs.getInt(2);
        data.followDist = rs.getFloat(3);
        data.followAngle = rs.getFloat(4) * (float) Math.PI / 180.0f;
        data.groupAI = EnumFlag.of(GroupAIFlag.class, rs.getShort(5));
        data.leaderWaypointIds[0] = rs.getInt(6);
        data.leaderWaypointIds[1] = rs.getInt(7);
        return data;
    };


}
