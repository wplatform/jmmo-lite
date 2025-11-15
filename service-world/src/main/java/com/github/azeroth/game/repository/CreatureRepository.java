package com.github.azeroth.game.repository;


import com.github.azeroth.game.domain.creature.*;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;

import java.util.List;
import java.util.stream.Stream;

public interface CreatureRepository {


    @Query(value = """
            SELECT entry, KillCredit1, KillCredit2, name, femaleName, subname,
                TitleAlt, IconName, RequiredExpansion, VignetteID, faction, npcflag,
                speed_walk, speed_run, scale, Classification, dmgschool, BaseAttackTime,
                RangeAttackTime, BaseVariance, RangeVariance, unit_class, unit_flags,
                unit_flags2, unit_flags3, family, trainer_class, type, VehicleId, AIName,
                MovementType, ctm.Ground, ctm.Swim, ctm.Flight, ctm.Rooted, ctm.Chase,
                ctm.Random, ctm.InteractionPauseTimer, ExperienceModifier, RacialLeader,
                movementId, RegenHealth, CreatureImmunitiesId, flags_extra, ScriptName, StringId
            FROM creature_template ct
            LEFT JOIN creature_template_movement ctm ON ct.entry = ctm.CreatureId WHERE entry = :entry OR 1 = :all
            """)
    Stream<CreatureTemplate> streamCreatureTemplate(int entry, int all);

    @Query(value = "SELECT creatureID, school, Resistance FROM creature_template_resistance")
    Stream<int[]> streamAllCreatureTemplateResistance();

    @Query(value = "SELECT creatureID, `Index`, Spell FROM creature_template_spell")
    Stream<int[]> streamAllCreatureTemplateSpell();

    @Query(value = "SELECT creatureID, creatureDisplayID, displayScale, Probability FROM creature_template_model ORDER BY Idx ASC")
    Stream<Object[]> streamAllCreatureTemplateModel();

    @Query(value = "SELECT creatureID, CreatureIDVisibleToSummoner, GroundMountDisplayID, FlyingMountDisplayID FROM creature_summoned_data")
    Stream<CreatureSummonedData> streamAllCreatureSummonedData();

    @Query(value = "SELECT entry, locale, name, nameAlt, title, TitleAlt FROM creature_template_locale")
    Stream<CreatureLocale> streamAllCreatureTemplateLocale();


    @Query(value = """
            SELECT creature.guid, id, map, position_x, position_y, position_z, orientation, modelid, equipment_id, spawntimesecs, wander_distance,
                   currentwaypoint, curHealthPct, MovementType, spawnDifficulties, eventEntry, poolSpawnId, creature.npcflag, creature.unit_flags,
                   creature.unit_flags2, creature.unit_flags3, creature.phaseUseFlags, creature.phaseid, creature.phasegroup, creature.terrainSwapMap,
                   creature.ScriptName, creature.StringId
            FROM creature
                   LEFT OUTER JOIN game_event_creature ON creature.guid = game_event_creature.guid
                   LEFT OUTER JOIN pool_members ON pool_members.type = 0 AND creature.guid = pool_members.spawnId
            """)
    Stream<CreatureData> streamAllCreature();


    @Query("SELECT summonerId, summonerType, groupId, entry, position_x, position_y, position_z, orientation, summonType, summonTime FROM creature_summon_groups")
    Stream<TempSummonData> streamsAllTempSummon();

    @Query("SELECT npc_entry, spell_id, cast_flags, user_type FROM npc_spellclick_spells")
    Stream<int[]> streamAllNpcSpellClickSpells();

    @Modifying
    @Query("UPDATE creature SET zoneId = :zoneId, areaId = :areaId WHERE guid = :guid")
    void updateCreatureZoneAndAreaId(int zoneId, int areaId, long guid);

    @Query("SELECT entry, item, maxcount, incrtime, ExtendedCost, type, BonusListIDs, PlayerConditionID, IgnoreFiltering FROM npc_vendor ORDER BY entry, slot ASC")
    Stream<VendorItem> streamAllNpcVendor();

    @Query("SELECT item, maxcount, incrtime, ExtendedCost, type, BonusListIDs, PlayerConditionID, IgnoreFiltering FROM npc_vendor WHERE entry = :entry ORDER BY slot ASC")
    List<VendorItem> queryNpcVendorByEntry(int entry);

    @Modifying
    @Query("DELETE FROM npc_vendor WHERE entry = :entry AND item = :item AND type = :type")
    void deleteNpcVendor(int entry, int item, int type);

    @Modifying
    @Query("INSERT INTO npc_vendor (entry, item, maxcount, incrtime, extendedcost, type) VALUES(:entry, :item, :maxcount, :incrtime, :extendedcost, :type)")
    void insertNpcVendor(int entry, int item, int maxcount, int incrtime, int extendedcost, int type);


    @Query("SELECT entry, path_id, mount, standState, animTier, visFlags, sheathState, PvPFlags, emote, aiAnimKit, movementAnimKit, meleeAnimKit, visibilityDistanceType, auras FROM creature_template_addon")
    Stream<CreatureAddon> streamAllCreatureTemplateAddon();

    @Query("SELECT Entry, NoNPCDamageBelowHealthPct FROM creature_template_sparring")
    Stream<Object[]> streamAllCreatureTemplateSparring();

    @Query("""
            SELECT Entry, DifficultyID, LevelScalingDeltaMin, LevelScalingDeltaMax, SandboxScalingId, HealthScalingExpansion,
                HealthModifier, ManaModifier, ArmorModifier, DamageModifier, CreatureDifficultyID, TypeFlags, TypeFlags2,
                LootID, PickPocketLootID, SkinLootID, GoldMin, GoldMax,
                StaticFlags1, StaticFlags2, StaticFlags3, StaticFlags4, StaticFlags5, StaticFlags6, StaticFlags7, StaticFlags8
            FROM creature_template_difficulty ORDER BY Entry
            """)
    Stream<CreatureDifficulty> streamAllCreatureTemplateDifficulty();

    @Query("SELECT displayID, boundingRadius, combatReach, DisplayID_Other_Gender FROM creature_model_info")
    Stream<CreatureModelInfo> streamAllCreatureModelInfo();

    @Query("SELECT level, `class` as klass, basemana, attackpower, rangedattackpower FROM creature_classlevelstats")
    Stream<CreatureBaseStats> streamAllCreatureClassLevelStats();


    @Query("SELECT guid, PathId, mount, StandState, AnimTier, VisFlags, SheathState, PvPFlags, emote, aiAnimKit, movementAnimKit, meleeAnimKit, visibilityDistanceType, auras FROM creature_addon")
    Stream<CreatureAddon> streamAllCreatureAddon();

    @Query("""
            SELECT cmo.SpawnId,
                COALESCE(cmo.Ground, ctm.Ground),
                COALESCE(cmo.Swim, ctm.Swim),
                COALESCE(cmo.Flight, ctm.Flight),
                COALESCE(cmo.Rooted, ctm.Rooted),
                COALESCE(cmo.Chase, ctm.Chase),
                COALESCE(cmo.Random, ctm.Random),
                COALESCE(cmo.InteractionPauseTimer, ctm.InteractionPauseTimer)
            FROM creature_movement_override AS cmo "
            LEFT JOIN creature AS c ON c.guid = cmo.SpawnId
            LEFT JOIN creature_template_movement AS ctm ON ctm.CreatureId = c.id
            """)
    Stream<CreatureMovementData> streamAllCreatureMovementOverride();

    @Query("SELECT CreatureID, ID, ItemID1, AppearanceModID1, ItemVisual1, ItemID2, AppearanceModID2, ItemVisual2, ItemID3, AppearanceModID3, ItemVisual3 FROM creature_equip_template")
    Stream<int[]> streamAllCreatureEquipTemplate();

    @Query("SELECT guid, linkedGuid, linkType FROM linked_respawn ORDER BY guid ASC")
    Stream<int[]> streamAllLinkedRespawn();

    @Query("SELECT leaderGUID, memberGUID, dist, angle, groupAI, point_1, point_2 FROM creature_formations ORDER BY leaderGUID")
    Stream<FormationInfo> streamAllCreatureFormations();

    @Modifying
    @Query("DELETE FROM linked_respawn WHERE guid = :guid AND linkType  = :linkType")
    void deleteLinkedRespawn(int guid, int linkType);

    @Modifying
    @Query("DELETE FROM linked_respawn WHERE linkedGuid = :linkedGuid AND linkType = :linkType")
    void deleteLinkedRespawnMaster(int linkedGuid, int linkType);

    @Modifying
    @Query("REPLACE INTO linked_respawn (guid, linkedGuid, linkType) VALUES (?, ?, ?)")
    void replaceLinkedRespawn(int guid, int linkedGuid, int linkType);

    @Query("SELECT CreatureEntry, DifficultyID, ItemId, Idx FROM creature_questitem ORDER BY Idx ASC")
    Stream<int[]> streamAllCreatureQuestItem();

    @Query("SELECT CreatureID, TrainerID, MenuID, OptionID FROM creature_trainer")
    Stream<int[]> streamAllCreatureTrainer();

    @Query("SELECT TrainerId, SpellId, MoneyCost, ReqSkillLine, ReqSkillRank, ReqAbility1, ReqAbility2, ReqAbility3, ReqLevel FROM trainer_spell")
    Stream<TrainerSpell> streamAllTrainerSpell();

    @Query("SELECT Id, Type, Greeting FROM trainer")
    Stream<TrainerEntry> streamAllTrainer();

    @Query("SELECT id, locale, Greeting_lang FROM trainer_locale")
    Stream<Object[]> streamAllTrainerLocale();
}
