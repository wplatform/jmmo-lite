package com.github.azeroth.game.repository;


import com.github.azeroth.game.domain.gobject.*;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;

import java.util.stream.Stream;

public interface GameObjectRepository {


    @Query("""
            SELECT gameobject.guid, id, map, position_x, position_y, position_z, orientation,
            "rotation0, rotation1, rotation2, rotation3, spawntimesecs, animprogress, state, spawnDifficulties, eventEntry, poolSpawnId,
            "phaseUseFlags, phaseid, phasegroup, terrainSwapMap, ScriptName, StringId
            "FROM gameobject LEFT OUTER JOIN game_event_gameobject ON gameobject.guid = game_event_gameobject.guid
            "LEFT OUTER JOIN pool_members ON pool_members.type = 1 AND gameobject.guid = pool_members.spawnId
            """)
    Stream<GameObjectData> streamAllGameObject();


    @Query("""
         SELECT entry, type, displayId, name, IconName, castBarCaption, unk1, size,
            Data0, Data1, Data2, Data3, Data4, Data5, Data6, Data7, Data8, Data9, Data10, Data11, Data12,
            Data13, Data14, Data15, Data16, Data17, Data18, Data19, Data20, Data21, Data22, Data23, Data24, Data25, Data26, Data27, Data28,
            Data29, Data30, Data31, Data32, RequiredLevel, AIName, ScriptName, StringId
         FROM gameobject_template
            """)
    Stream<GameObjectTemplate> streamAllGameObjectTemplate();


    @Query("SELECT entry, faction, flags, mingold, maxgold, artkit0, artkit1, artkit2, artkit3, artkit4, WorldEffectID, AIAnimKitID FROM gameobject_template_addon")
    Stream<GameObjectTemplateAddon> streamAllGameObjectTemplateAddons();
    @Query("SELECT spawnId, faction, flags FROM gameobject_overrides")
    Stream<GameObjectOverride> streamAllGameObjectOverrides();
    @Query("SELECT guid, parent_rotation0, parent_rotation1, parent_rotation2, parent_rotation3, invisibilityType, invisibilityValue, WorldEffectID, AIAnimKitID FROM gameobject_addon")
    Stream<GameObjectAddon> streamAllGameObjectAddons();
    @Query("SELECT GameObjectEntry, ItemId, Idx FROM gameobject_questitem ORDER BY Idx ASC")
    Stream<int[]> streamAllGameObjectQuestItem();

    @Query("SELECT entry, locale, name, castBarCaption, unk1 FROM gameobject_template_locale")
    Stream<GameObjectLocale> streamAllGameObjectTemplateLocales();

    @Modifying
    @Query("UPDATE gameobject SET zoneId = :zoneId, areaId = :areaId WHERE guid = :guid")
    void updateGameObjectZoneAndAreaId(int zoneId, int areaId, long guid);


}
