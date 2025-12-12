package com.github.azeroth.game.repository;


import com.github.azeroth.game.domain.gossip.GossipMenuItemsLocale;
import com.github.azeroth.game.domain.gossip.GossipMenuOption;
import com.github.azeroth.game.domain.gossip.GossipMenus;
import com.github.azeroth.game.domain.misc.*;
import com.github.azeroth.game.domain.misc.ClassExpansionRequirement;
import com.github.azeroth.game.domain.scene.SceneTemplate;
import com.github.azeroth.game.domain.spawn.SpawnGroupTemplateData;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Stream;
@Repository
public interface MiscRepository {

    
    @Query("SELECT entry, content_default, content_loc1, content_loc2, content_loc3, content_loc4, content_loc5, content_loc6, content_loc7, content_loc8 FROM trinity_string")
    Stream<SystemText> streamAllMessageText();

    
    @Query("SELECT raceID, expansion, achievementId FROM `race_unlock_requirement`")
    Stream<RaceUnlockRequirement> queryAllRaceUnlockRequirement();

    
    @Query("SELECT classID, raceID, activeExpansionLevel, AccountExpansionLevel FROM `class_expansion_requirement`")
    Stream<ClassExpansionRequirement> queryAllClassExpansionRequirement();

    
    @Query("SELECT map, parent, script FROM instance_template")
    Stream<InstanceTemplate> streamAllInstanceTemplate();

    
    @Query("SELECT id, position_x, position_y, position_z, orientation, map, name FROM game_tele")
    Stream<GameTele> streamAllGameTeleport();

    
    @Query("SELECT ID, `text`, nextPageID, playerConditionID, Flags FROM page_text")
    Stream<PageText> streamAllPageText();

    
    @Query("SELECT ID, locale, `Text` FROM page_text_locale")
    Stream<PageTextLocale> streamAllPageTextLocale();

    
    @Query("SELECT level, basexp FROM exploration_basexp")
    Stream<ExplorationBaseXp> streamsAllExplorationBaseXp();

    
    @Query("SELECT level, raceMask, mailTemplateId, senderEntry FROM mail_level_reward")
    Stream<MailLevelReward> streamsAllMailLevelReward();

    @Query("SELECT level, raceMask, mailTemplateId, senderEntry FROM mail_level_reward")
    List<Integer> queryAllTavernAreaTriggers();

    @Query("SELECT id, speed, treatSpeedAsMoveTimeSeconds, jumpGravity, spellVisualId, progressCurveId, parabolicCurveId FROM jump_charge_params")
    List<JumpChargeParams> queryAllJumpChargeParams();


    
    @Query("""
            SELECT ID, Probability0, Probability1, Probability2, Probability3, Probability4, Probability5, Probability6, Probability7,
            BroadcastTextID0, BroadcastTextID1, BroadcastTextID2, BroadcastTextID3, BroadcastTextID4, BroadcastTextID5, BroadcastTextID6, BroadcastTextID7 FROM npc_text
            """)
    Stream<NpcText> streamAllNpcText();

    
    @Query("SELECT menuId, TextId FROM gossip_menu")
    Stream<GossipMenus> streamAllGossipMenu();

    
    @Query("""
            SELECT MenuID, GossipOptionID, OptionID, OptionNpc, OptionText, OptionBroadcastTextID, Language, Flags, ActionMenuID, ActionPoiID, GossipNpcOptionID,
                    BoxCoded, BoxMoney, BoxText, BoxBroadcastTextID, SpellID, OverrideIconID
                    FROM gossip_menu_option ORDER BY MenuID, OptionID
            """)
    Stream<GossipMenuOption> streamAllGossipMenuOption();
    
    @Query("SELECT menuId, OptionID, locale, optionText, BoxText FROM gossip_menu_option_locale")
    Stream<GossipMenuItemsLocale> streamAllGossipMenuOptionLocale();

    
    @Query("SELECT MenuID, FriendshipFactionID FROM gossip_menu_addon")
    Stream<GossipMenuAddon> streamAllGossipMenuAddon();

    
    @Query("SELECT ID, positionX, positionY, positionZ, icon, flags, importance, name, WMOGroupID FROM points_of_interest")
    Stream<PointOfInterest> streamAllPointsOfInterest();

    
    @Query("SELECT ID, locale, Name FROM points_of_interest_locale")
    Stream<PointOfInterestLocale> streamAllPointsOfInterestLocale();

    
    @Query("SELECT alliance_id, horde_id FROM player_factionchange_titles")
    Stream<int[]> streamAllPlayerFactionChangeTitles();

    
    @Query("SELECT alliance_id, horde_id FROM player_factionchange_spells")
    Stream<int[]> streamAllPlayerFactionChangeSpells();

    
    @Query("SELECT alliance_id, horde_id FROM player_factionchange_reputations")
    Stream<int[]> streamAllPlayerFactionChangeReputations();

    
    @Query("SELECT alliance_id, horde_id FROM player_factionchange_quests")
    Stream<int[]> streamAllPlayerFactionChangeQuests();

    
    @Query("SELECT alliance_id, horde_id FROM player_factionchange_achievement")
    Stream<int[]> streamAllPlayerFactionChangeAchievement();


    
    @Query("SELECT alliance_id, horde_id FROM player_factionchange_items")
    Stream<int[]> streamAllPlayerFactionChangeItem();

    
    @Query("SELECT groupId, groupName, groupFlags FROM spawn_group_template")
    Stream<SpawnGroupTemplateData> streamAllSpawnGroupTemplate();


    
    @Query("SELECT groupId, groupName, groupFlags FROM spawn_group_template")
    Stream<SceneTemplate> streamAllSceneTemplates();

    
    @Query("SELECT areaId, PhaseId FROM `phase_area`")
    Stream<int[]> streamAllPhaseArea();

    
    @Query("SELECT mapId, TerrainSwapMap FROM `terrain_swap_defaults`")
    Stream<int[]> streamAllTerrainSwapDefaults();

    
    @Query("SELECT TerrainSwapMap, UiMapPhaseId  FROM `terrain_worldmap`")
    Stream<int[]> streamAllTerrainWorldMap();

    @Query("SELECT ID, GhostZone FROM graveyard_zone")
    Stream<int[]> streamAllGraveyardZone();
    
    @Query("SELECT SourceTypeOrReferenceId, SourceGroup, SourceEntry, SourceId, ElseGroup, ConditionTypeOrReference, ConditionTarget," +
            "ConditionValue1, ConditionValue2, ConditionValue3, ConditionStringValue1," +
            "NegativeCondition, ErrorType, ErrorTextId, ScriptName FROM conditions")
    Stream<int[]> streamAllConditions();


    @Query("SELECT entry, skill FROM skill_fishing_base_level")
    Stream<int[]> streamAllSkillFishingBaseLevel();


    @Query("SELECT ID, Value1, Value2, Value3, Value4, Value5, Value6, Value7, Value8, Value9, Value10, Value11, Value12, Value13, Value14, Value15, Value16 FROM skill_tiers")
    Stream<int[]> streamAllSkillTiers();

    @Query("SELECT `ID`, `Name` FROM `phase_name`")
    Stream<Object[]> streamAllPhaseName();


    @Query("SELECT id, delay, command, datalong, datalong2, dataint, x, y, z, o, effIndex FROM spell_scripts")
    Stream<Object[]> streamAllSpellScripts();


    @Query("SELECT id, delay, command, datalong, datalong2, dataint, x, y, z, o FROM event_scripts")
    Stream<Object[]> streamAllEventScripts();

    @Query("SELECT instanceMapId, bossStateId, bossStates, spawnGroupId, flags FROM instance_spawn_groups")
    Stream<InstanceSpawnGroup> streamAllInstanceSpawnGroups();

    @Query("SELECT groupId, spawnType, spawnId FROM spawn_group")
    Stream<int[]> streamAllSpawnGroups();

    @Modifying
    @Query("INSERT INTO game_tele (id, positionX, positionY, positionZ, orientation, map, name) VALUES (:id, :positionX, :positionY, :positionZ, :orientation, :map, :name)")
    void addGameTele(GameTele tele);

    @Modifying
    @Query("DELETE FROM game_tele WHERE id = :id")
    void deleteGameTele(GameTele tele);


    @Query("SELECT spell_id, ScriptName FROM spell_script_names")
    Stream<Object[]> streamAllSpellScriptNames();

    @Query("SELECT ID, MapID, LocX, LocY, LocZ, Facing, TransportSpawnId FROM world_safe_locs")
    Stream<WorldSafeLocEntry> streamAllWorldSafeLoc();

    // Converted from WorldDatabase.cpp
    @Query("SELECT CreatureID, GroupID, ID, Text, Type, Language, Probability, Emote, Duration, Sound, SoundPlayType, BroadcastTextId, TextRange FROM creature_text")
    List<CreatureText> findCreatureTexts();

    @Query("SELECT entryorguid, source_type, id, link, Difficulties, event_type, event_phase_mask, event_chance, event_flags, event_param1, event_param2, event_param3, event_param4, event_param5, event_param_string, action_type, action_param1, action_param2, action_param3, action_param4, action_param5, action_param6, action_param7, action_param_string, target_type, target_param1, target_param2, target_param3, target_param4, target_param_string, target_x, target_y, target_z, target_o FROM smart_scripts ORDER BY entryorguid, source_type, id, link")
    List<SmartScript> findSmartScripts();

    @Query("SELECT item, maxcount, incrtime, ExtendedCost, type, BonusListIDs, PlayerConditionID, IgnoreFiltering FROM npc_vendor WHERE entry = :entry ORDER BY slot ASC")
    List<NpcVendor> findNpcVendorsByEntry(@Param("entry") int entry);

    @Query("SELECT PathId, MoveType, Flags FROM waypoint_path")
    Stream<WaypointPath> streamAllWaypointPaths();

    @Query("SELECT PathId, NodeId, PositionX, PositionY, PositionZ, Orientation, Delay FROM waypoint_path_node ORDER BY PathId, NodeId")
    Stream<WaypointNode> streamAllWaypointPathNodes();

    @Query("SELECT PathId, MoveType, Flags FROM waypoint_path WHERE PathId = :pathId")
    List<WaypointPath> findWaypointPathByPathId(@Param("pathId") int pathId);

    @Query("SELECT MAX(PathId) FROM waypoint_path_node")
    List<Integer> findMaxWaypointPathNodePathId();

    @Query("SELECT MAX(NodeId) FROM waypoint_path_node WHERE PathId = :pathId")
    List<Integer> findMaxWaypointPathNodeIdByPathId(@Param("pathId") int pathId);

    @Query("SELECT PathId, NodeId, PositionX, PositionY, PositionZ, Orientation, Delay FROM waypoint_path_node WHERE PathId = :pathId ORDER BY NodeId")
    List<WaypointPathNode> findWaypointPathNodesByPathId(@Param("pathId") int pathId);

    @Query("SELECT NodeId, PositionX, PositionY, PositionZ, Orientation FROM waypoint_path_node WHERE PathId = :pathId")
    List<WaypointPathNodePosition> findWaypointPathNodePositionsByPathId(@Param("pathId") int pathId);

    @Query("SELECT PositionX, PositionY, PositionZ, Orientation FROM waypoint_path_node WHERE NodeId = 1 AND PathId = :pathId")
    List<WaypointPathNodePosition> findFirstWaypointPathNodePositionByPathId(@Param("pathId") int pathId);

    @Query("SELECT PositionX, PositionY, PositionZ, Orientation FROM waypoint_path_node WHERE PathId = :pathId ORDER BY NodeId DESC LIMIT 1")
    List<WaypointPathNodePosition> findLastWaypointPathNodePositionByPathId(@Param("pathId") int pathId);

    @Query("SELECT PathId, NodeId FROM waypoint_path_node WHERE (abs(PositionX - :x) <= :tolerance) and (abs(PositionY - :y) <= :tolerance) and (abs(PositionZ - :z) <= :tolerance)")
    List<WaypointPathNode> findWaypointPathNodesByPosition(@Param("x") double x, @Param("y") double y, @Param("z") double z, @Param("tolerance") double tolerance);

    @Query("SELECT guid FROM creature_addon WHERE guid = :guid")
    List<CreatureAddon> findCreatureAddonByGuid(@Param("guid") int guid);

    @Query("SELECT entry, KillCredit1, KillCredit2, name, femaleName, subname, TitleAlt, IconName, RequiredExpansion, VignetteID, faction, npcflag, speed_walk, speed_run, scale, Classification, dmgschool, BaseAttackTime, RangeAttackTime, BaseVariance, RangeVariance, unit_class, unit_flags, unit_flags2, unit_flags3, family, trainer_class, type, PetSpellDataId, VehicleId, AIName, MovementType, ctm.HoverInitiallyEnabled, ctm.Chase, ctm.Random, ctm.InteractionPauseTimer, ExperienceModifier, Civilian, RacialLeader, movementId, WidgetSetID, WidgetSetUnitConditionID, RegenHealth, CreatureImmunitiesId, flags_extra, ScriptName, StringId FROM creature_template ct LEFT JOIN creature_template_movement ctm ON ct.entry = ctm.CreatureId WHERE entry = :entry OR 1 = :flag")
    List<CreatureTemplate> findCreatureTemplate(@Param("entry") int entry, @Param("flag") int flag);

    @Query("SELECT guid FROM creature WHERE id = :id")
    List<Creature> findCreaturesById(@Param("id") int id);

    @Query("SELECT guid, id, position_x, position_y, position_z, map, (POW(position_x - :x, 2) + POW(position_y - :y, 2) + POW(position_z - :z, 2)) AS order_ FROM gameobject WHERE map = :map AND (POW(position_x - :x, 2) + POW(position_y - :y, 2) + POW(position_z - :z, 2)) <= :range ORDER BY order_")
    List<GameObject> findNearestGameobjects(@Param("x") double x, @Param("y") double y, @Param("z") double z, @Param("map") int map, @Param("range") double range);

    @Query("SELECT guid, id, position_x, position_y, position_z, map, (POW(position_x - :x, 2) + POW(position_y - :y, 2) + POW(position_z - :z, 2)) AS order_ FROM creature WHERE map = :map AND (POW(position_x - :x, 2) + POW(position_y - :y, 2) + POW(position_z - :z, 2)) <= :range ORDER BY order_")
    List<Creature> findNearestCreatures(@Param("x") double x, @Param("y") double y, @Param("z") double z, @Param("map") int map, @Param("range") double range);

    @Query("SELECT entry FROM disables WHERE entry = :entry AND sourceType = :sourceType")
    List<Disable> findDisables(@Param("entry") int entry, @Param("sourceType") int sourceType);

    @Query("SELECT AchievementRequired FROM guild_rewards_req_achievements WHERE ItemID = :itemId")
    List<GuildRewardAchievement> findGuildRewardsRequiredAchievements(@Param("itemId") int itemId);

    // Write operations converted from WorldDatabase.cpp
    @Modifying
    @Query("DELETE FROM linked_respawn WHERE guid = :guid AND linkType = :linkType")
    int deleteLinkedRespawn(@Param("guid") int guid, @Param("linkType") int linkType);

    @Modifying
    @Query("REPLACE INTO linked_respawn (guid, linkedGuid, linkType) VALUES (:guid, :linkedGuid, :linkType)")
    int replaceLinkedRespawn(@Param("guid") int guid, @Param("linkedGuid") int linkedGuid, @Param("linkType") int linkType);

    @Modifying
    @Query("DELETE FROM gameobject WHERE guid = :guid")
    int deleteGameObject(@Param("guid") int guid);

    @Modifying
    @Query("INSERT INTO graveyard_zone (ID, GhostZone) VALUES (:id, :ghostZone)")
    int insertGraveyardZone(@Param("id") int id, @Param("ghostZone") int ghostZone);

    @Modifying
    @Query("DELETE FROM graveyard_zone WHERE ID = :id AND GhostZone = :ghostZone")
    int deleteGraveyardZone(@Param("id") int id, @Param("ghostZone") int ghostZone);

    @Modifying
    @Query("INSERT INTO game_tele (id, position_x, position_y, position_z, orientation, map, name) VALUES (:id, :x, :y, :z, :orientation, :map, :name)")
    int insertGameTele(@Param("id") int id, @Param("x") double x, @Param("y") double y, @Param("z") double z, @Param("orientation") double orientation, @Param("map") int map, @Param("name") String name);

    @Modifying
    @Query("INSERT INTO npc_vendor (entry, item, maxcount, incrtime, extendedcost, type) VALUES(:entry, :item, :maxcount, :incrtime, :extendedcost, :type)")
    int insertNpcVendor(@Param("entry") int entry, @Param("item") int item, @Param("maxcount") int maxcount, @Param("incrtime") int incrtime, @Param("extendedcost") int extendedcost, @Param("type") int type);

    @Modifying
    @Query("UPDATE creature SET MovementType = :movementType WHERE guid = :guid")
    int updateCreatureMovementType(@Param("movementType") int movementType, @Param("guid") int guid);

    @Modifying
    @Query("INSERT INTO creature_formations (leaderGUID, memberGUID, dist, angle, groupAI) VALUES (:leaderGUID, :memberGUID, :dist, :angle, :groupAI)")
    int insertCreatureFormation(@Param("leaderGUID") int leaderGUID, @Param("memberGUID") int memberGUID, @Param("dist") float dist, @Param("angle") float angle, @Param("groupAI") int groupAI);

    @Modifying
    @Query("INSERT INTO waypoint_path_node (PathId, NodeId, PositionX, PositionY, PositionZ, Orientation) VALUES (:pathId, :nodeId, :x, :y, :z, :orientation)")
    int insertWaypointPathNode(@Param("pathId") int pathId, @Param("nodeId") int nodeId, @Param("x") double x, @Param("y") double y, @Param("z") double z, @Param("orientation") float orientation);

    @Modifying
    @Query("DELETE FROM waypoint_path_node WHERE PathId = :pathId AND NodeId = :nodeId")
    int deleteWaypointPathNode(@Param("pathId") int pathId, @Param("nodeId") int nodeId);

    @Modifying
    @Query("INSERT INTO creature_addon(guid, PathId) VALUES (:guid, :pathId)")
    int insertCreatureAddon(@Param("guid") int guid, @Param("pathId") int pathId);

    @Modifying
    @Query("INSERT INTO creature (guid, id , map, spawnDifficulties, PhaseId, PhaseGroup, modelid, equipment_id, position_x, position_y, position_z, orientation, spawntimesecs, wander_distance, currentwaypoint, curHealthPct, MovementType, npcflag, unit_flags, unit_flags2, unit_flags3) VALUES (:guid, :id, :map, :spawnDifficulties, :phaseId, :phaseGroup, :modelid, :equipmentId, :x, :y, :z, :orientation, :spawntimesecs, :wanderDistance, :currentwaypoint, :curHealthPct, :movementType, :npcflag, :unitFlags, :unitFlags2, :unitFlags3)")
    int insertCreature(@Param("guid") int guid, @Param("id") int id, @Param("map") int map, @Param("spawnDifficulties") int spawnDifficulties, @Param("phaseId") int phaseId, @Param("phaseGroup") int phaseGroup, @Param("modelid") int modelid, @Param("equipmentId") int equipmentId, @Param("x") double x, @Param("y") double y, @Param("z") double z, @Param("orientation") float orientation, @Param("spawntimesecs") int spawntimesecs, @Param("wanderDistance") float wanderDistance, @Param("currentwaypoint") int currentwaypoint, @Param("curHealthPct") int curHealthPct, @Param("movementType") int movementType, @Param("npcflag") int npcflag, @Param("unitFlags") int unitFlags, @Param("unitFlags2") int unitFlags2, @Param("unitFlags3") int unitFlags3);

    @Modifying
    @Query("INSERT INTO gameobject (guid, id, map, spawnDifficulties, PhaseId, PhaseGroup, position_x, position_y, position_z, orientation, rotation0, rotation1, rotation2, rotation3, spawntimesecs, animprogress, state) VALUES (:guid, :id, :map, :spawnDifficulties, :phaseId, :phaseGroup, :x, :y, :z, :orientation, :rotation0, :rotation1, :rotation2, :rotation3, :spawntimesecs, :animprogress, :state)")
    int insertGameObject(@Param("guid") int guid, @Param("id") int id, @Param("map") int map, @Param("spawnDifficulties") int spawnDifficulties, @Param("phaseId") int phaseId, @Param("phaseGroup") int phaseGroup, @Param("x") double x, @Param("y") double y, @Param("z") double z, @Param("orientation") float orientation, @Param("rotation0") float rotation0, @Param("rotation1") float rotation1, @Param("rotation2") float rotation2, @Param("rotation3") float rotation3, @Param("spawntimesecs") int spawntimesecs, @Param("animprogress") int animprogress, @Param("state") int state);

    @Modifying
    @Query("INSERT INTO disables (entry, sourceType, flags, comment) VALUES (:entry, :sourceType, :flags, :comment)")
    int insertDisable(@Param("entry") int entry, @Param("sourceType") int sourceType, @Param("flags") int flags, @Param("comment") String comment);

    @Modifying
    @Query("DELETE FROM disables WHERE entry = :entry AND sourceType = :sourceType")
    int deleteDisable(@Param("entry") int entry, @Param("sourceType") int sourceType);

    @Modifying
    @Query("INSERT INTO conditions (SourceTypeOrReferenceId, SourceGroup, SourceEntry, SourceId, ElseGroup, ConditionTypeOrReference, ConditionTarget, ConditionValue1, ConditionValue2, ConditionValue3, NegativeCondition, ErrorType, ErrorTextId, ScriptName, Comment) VALUES (:sourceType, :sourceGroup, :sourceEntry, :sourceId, :elseGroup, :conditionType, :conditionTarget, :value1, :value2, :value3, :negativeCondition, :errorType, :errorTextId, :scriptName, :comment)")
    int insertCondition(@Param("sourceType") int sourceType, @Param("sourceGroup") int sourceGroup, @Param("sourceEntry") int sourceEntry, @Param("sourceId") int sourceId, @Param("elseGroup") int elseGroup, @Param("conditionType") int conditionType, @Param("conditionTarget") int conditionTarget, @Param("value1") int value1, @Param("value2") int value2, @Param("value3") int value3, @Param("negativeCondition") int negativeCondition, @Param("errorType") int errorType, @Param("errorTextId") int errorTextId, @Param("scriptName") String scriptName, @Param("comment") String comment);

    // Additional missing operations from WorldDatabase.cpp
    @Modifying
    @Query("DELETE FROM game_tele WHERE name = :name")
    int deleteGameTele(@Param("name") String name);

    @Modifying
    @Query("DELETE FROM npc_vendor WHERE entry = :entry AND item = :item AND type = :type")
    int deleteNpcVendor(@Param("entry") int entry, @Param("item") int item, @Param("type") int type);

    @Modifying
    @Query("UPDATE waypoint_path_node SET NodeId = NodeId - 1 WHERE PathId = :pathId AND NodeId > :nodeId")
    int updateWaypointPathNodeIds(@Param("pathId") int pathId, @Param("nodeId") int nodeId);

    @Modifying
    @Query("UPDATE waypoint_path_node SET PositionX = :x, PositionY = :y, PositionZ = :z, Orientation = :orientation WHERE PathId = :pathId AND NodeId = :nodeId")
    int updateWaypointPathNodePosition(@Param("x") double x, @Param("y") double y, @Param("z") double z, @Param("orientation") float orientation, @Param("pathId") int pathId, @Param("nodeId") int nodeId);

    @Modifying
    @Query("UPDATE creature_addon SET PathId = :pathId WHERE guid = :guid")
    int updateCreatureAddonPath(@Param("pathId") int pathId, @Param("guid") int guid);

    @Modifying
    @Query("DELETE FROM creature WHERE guid = :guid")
    int deleteCreature(@Param("guid") int guid);

    @Modifying
    @Query("DELETE FROM game_event_creature WHERE guid = :guid")
    int deleteGameEventCreature(@Param("guid") int guid);

    @Modifying
    @Query("DELETE FROM game_event_model_equip WHERE guid = :guid")
    int deleteGameEventModelEquip(@Param("guid") int guid);

    @Modifying
    @Query("DELETE FROM spawn_group WHERE spawnType = :spawnType AND spawnId = :spawnId")
    int deleteSpawnGroupMember(@Param("spawnType") int spawnType, @Param("spawnId") int spawnId);

    @Modifying
    @Query("DELETE FROM gameobject_addon WHERE guid = :guid")
    int deleteGameObjectAddon(@Param("guid") int guid);

    @Modifying
    @Query("UPDATE creature_template SET faction = :faction WHERE entry = :entry")
    int updateCreatureFaction(@Param("faction") int faction, @Param("entry") int entry);

    @Modifying
    @Query("UPDATE creature_template SET npcflag = :npcflag WHERE entry = :entry")
    int updateCreatureNpcFlag(@Param("npcflag") int npcflag, @Param("entry") int entry);

    @Modifying
    @Query("UPDATE creature SET position_x = :x, position_y = :y, position_z = :z, orientation = :orientation WHERE guid = :guid")
    int updateCreaturePosition(@Param("x") double x, @Param("y") double y, @Param("z") double z, @Param("orientation") float orientation, @Param("guid") int guid);

    @Modifying
    @Query("UPDATE creature SET wander_distance = :distance, MovementType = :movementType WHERE guid = :guid")
    int updateCreatureWanderDistance(@Param("distance") float distance, @Param("movementType") int movementType, @Param("guid") int guid);

    @Modifying
    @Query("UPDATE creature SET spawntimesecs = :spawnTime WHERE guid = :guid")
    int updateCreatureSpawnTime(@Param("spawnTime") int spawnTime, @Param("guid") int guid);

    @Modifying
    @Query("UPDATE creature SET zoneId = :zoneId, areaId = :areaId WHERE guid = :guid")
    int updateCreatureZoneArea(@Param("zoneId") int zoneId, @Param("areaId") int areaId, @Param("guid") int guid);

    @Modifying
    @Query("UPDATE gameobject SET zoneId = :zoneId, areaId = :areaId WHERE guid = :guid")
    int updateGameObjectZoneArea(@Param("zoneId") int zoneId, @Param("areaId") int areaId, @Param("guid") int guid);

    // Remaining operations from WorldDatabase.cpp
    @Modifying
    @Query("DELETE FROM linked_respawn WHERE linkedGuid = :linkedGuid AND linkType = :linkType")
    int deleteLinkedRespawnByLinkedGuid(@Param("linkedGuid") int linkedGuid, @Param("linkType") int linkType);

    @Modifying
    @Query("DELETE FROM game_event_gameobject WHERE guid = :guid")
    int deleteGameEventGameObject(@Param("guid") int guid);

    @Modifying
    @Query("DELETE FROM creature_addon WHERE guid = :guid")
    int deleteCreatureAddon(@Param("guid") int guid);

    @Modifying
    @Query("UPDATE creature SET zoneId = :zoneId, areaId = :areaId WHERE guid = :guid")
    int updateCreatureZoneAreaData(@Param("zoneId") int zoneId, @Param("areaId") int areaId, @Param("guid") int guid);

    @Modifying
    @Query("UPDATE gameobject SET zoneId = :zoneId, areaId = :areaId WHERE guid = :guid")
    int updateGameObjectZoneAreaData(@Param("zoneId") int zoneId, @Param("areaId") int areaId, @Param("guid") int guid);
}