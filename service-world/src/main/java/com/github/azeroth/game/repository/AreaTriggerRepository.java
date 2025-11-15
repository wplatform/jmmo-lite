package com.github.azeroth.game.repository;


import com.github.azeroth.dbc.domain.AreaTriggerEntry;
import com.github.azeroth.game.domain.areatrigger.AreaTriggerAction;
import com.github.azeroth.game.domain.areatrigger.AreaTriggerCreateProperty;
import com.github.azeroth.game.domain.areatrigger.AreaTriggerTemplate;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.stream.Stream;

@Repository
public interface AreaTriggerRepository {

    @Query("SELECT AreaTriggerId, IsCustom, ActionType, ActionParam, TargetType FROM `areatrigger_template_actions`")
    Stream<AreaTriggerAction> streamAllAreaTriggerTemplateActions();

    @Query("SELECT AreaTriggerCreatePropertiesId, IsCustom, Idx, VerticeX, VerticeY, VerticeTargetX, VerticeTargetY FROM `areatrigger_create_properties_polygon_vertex` ORDER BY `AreaTriggerCreatePropertiesId`, `IsCustom`, `Idx`")
    Stream<Object[]> streamAllAreaTriggerCreatePropertiesPolygonVertex();
    @Query("SELECT AreaTriggerCreatePropertiesId, IsCustom, X, Y, Z FROM `areatrigger_create_properties_spline_point` ORDER BY `AreaTriggerCreatePropertiesId`, `IsCustom`, `Idx`")
    Stream<Object[]> streamAllAreaTriggerCreatePropertiesSplinePoint();
    @Query("SELECT Id, IsCustom, Flags, ActionSetId, ActionSetFlags FROM `areatrigger_template`")
    Stream<AreaTriggerTemplate> streamAllAreaTriggerTemplate();

    @Query("SELECT entry, ScriptName FROM areatrigger_scripts")
    Stream<Object[]> streamAllAreaTriggerScripts();

    @Query("SELECT ID, PortLocID FROM areatrigger_teleport")
    Stream<int[]> streamAllAreaTriggerTeleport();

    @Query("SELECT Id, IsCustom, AreaTriggerId, IsAreatriggerCustom, Flags, MoveCurveId, ScaleCurveId, MorphCurveId, FacingCurveId, AnimId, AnimKitId, DecalPropertiesId, SpellForVisuals, TimeToTargetScale, Speed, Shape, ShapeData0, ShapeData1, ShapeData2, ShapeData3, ShapeData4, ShapeData5, ShapeData6, ShapeData7, ScriptName FROM `areatrigger_create_properties`")
    Stream<AreaTriggerCreateProperty> streamAllAreaTriggerCreateProperties();

    @Query("SELECT SpawnId, AreaTriggerCreatePropertiesId, IsCustom, MapId, SpawnDifficulties, PosX, PosY, PosZ, Orientation, PhaseUseFlags, PhaseId, PhaseGroup, ScriptName FROM `areatrigger`")
    Stream<AreaTriggerEntry> streamAllAreaTrigger();

}
