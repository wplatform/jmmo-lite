package com.github.azeroth.game.domain.condition;




/*! Documentation on implementing a new ConditionType:
    Step 1: Check for the lowest free ID. Look for UNUSED_XX in the enum.
            Then define the new condition type.

    Step 2: Determine and map the parameters for the new condition type.

    Step 3: Add a case block to ConditionMgr::isConditionTypeValid with the new condition type
            and validate the parameters.

    Step 4: Define the maximum available condition targets in ConditionMgr::GetMaxAvailableConditionTargets.

    Step 5: Define the grid searcher mask in Condition::GetSearcherTypeMaskForCondition.

    Step 6: Add a case block to ConditionMgr::Meets with the new condition type.

    Step 7: Define condition name and expected condition values in ConditionMgr::StaticConditionTypeData.
*/
public enum ConditionType {
    NONE                     ,//= 0,                    // 0                      0              0                  always true
    AURA                     ,//= 1,                    // spell_id               effindex       0                  true if target has aura of spell_id with effect effindex
    ITEM                     ,//= 2,                    // item_id                count          bank               true if has #count of item_ids (if 'bank' is set it searches in bank slots too)
    ITEM_EQUIPPED            ,//= 3,                    // item_id                0              0                  true if has item_id equipped
    ZONEID                   ,//= 4,                    // zone_id                0              0                  true if in zone_id
    REPUTATION_RANK          ,//= 5,                    // faction_id             rankMask       0                  true if has min_rank for faction_id
    TEAM                     ,//= 6,                    // player_team            0,             0                  469 - Alliance, 67 - Horde)
    SKILL                    ,//= 7,                    // skill_id               skill_value    0                  true if has skill_value for skill_id
    QUESTREWARDED            ,//= 8,                    // quest_id               0              0                  true if quest_id was rewarded before
    QUESTTAKEN               ,//= 9,                    // quest_id               0,             0                  true while quest active
    DRUNKENSTATE             ,//= 10,                   // DrunkenState           0,             0                  true if player is drunk enough
    WORLD_STATE              ,//= 11,                   // index                  value          0                  true if world has the value for the index
    ACTIVE_EVENT             ,//= 12,                   // event_id               0              0                  true if event is active
    INSTANCE_INFO            ,//= 13,                   // entry                  data           type               true if the instance info defined by type (enum InstanceInfo) equals data.
    QUEST_NONE               ,//= 14,                   // quest_id               0              0                  true if doesn't have quest saved
    CLASS                    ,//= 15,                   // class                  0              0                  true if player's class is equal to class
    RACE                     ,//= 16,                   // race                   0              0                  true if player's race is equal to race
    ACHIEVEMENT              ,//= 17,                   // achievement_id         0              0                  true if achievement is complete
    TITLE                    ,//= 18,                   // title id               0              0                  true if player has title
    SPAWNMASK_DEPRECATED     ,//= 19,                   // DEPRECATED
    GENDER                   ,//= 20,                   // gender                 0              0                  true if player's gender is equal to gender
    UNIT_STATE               ,//= 21,                   // unitState              0              0                  true if unit has unitState
    MAPID                    ,//= 22,                   // map_id                 0              0                  true if in map_id
    AREAID                   ,//= 23,                   // area_id                0              0                  true if in area_id
    CREATURE_TYPE            ,//= 24,                   // cinfo.type             0              0                  true if creature_template.type = value1
    SPELL                    ,//= 25,                   // spell_id               0              0                  true if player has learned spell
    PHASEID                  ,//= 26,                   // phaseid                0              0                  true if object is in phaseid
    LEVEL                    ,//= 27,                   // level                  ComparisonType 0                  true if unit's level is equal to param1 (param2 can modify the statement)
    QUEST_COMPLETE           ,//= 28,                   // quest_id               0              0                  true if player has quest_id with all objectives complete, but not yet rewarded
    NEAR_CREATURE            ,//= 29,                   // creature entry         distance       dead (0/1)         true if there is a creature of entry in range
    NEAR_GAMEOBJECT          ,//= 30,                   // gameobject entry       distance       0                  true if there is a gameobject of entry in range
    OBJECT_ENTRY_GUID_LEGACY ,//= 31,                   // LEGACY_TypeID          entry          guid               true if object is type TypeID and the entry is 0 or matches entry of the object or matches guid of the object
    TYPE_MASK_LEGACY         ,//= 32,                   // LEGACY_TypeMask        0              0                  true if object is type object's TypeMask matches provided TypeMask
    RELATION_TO              ,//= 33,                   // ConditionTarget        RelationType   0                  true if object is in given relation with object specified by ConditionTarget
    REACTION_TO              ,//= 34,                   // ConditionTarget        rankMask       0                  true if object's reaction matches rankMask object specified by ConditionTarget
    DISTANCE_TO              ,//= 35,                   // ConditionTarget        distance       ComparisonType     true if object and ConditionTarget are within distance given by parameters
    ALIVE                    ,//= 36,                   // 0                      0              0                  true if unit is alive
    HP_VAL                   ,//= 37,                   // hpVal                  ComparisonType 0                  true if unit's hp matches given value
    HP_PCT                   ,//= 38,                   // hpPct                  ComparisonType 0                  true if unit's hp matches given pct
    REALM_ACHIEVEMENT        ,//= 39,                   // achievement_id         0              0                  true if realm achievement is complete
    IN_WATER                 ,//= 40,                   // 0                      0              0                  true if unit in water
    TERRAIN_SWAP             ,//= 41,                   // terrainSwap            0              0                  true if object is in terrainswap
    STAND_STATE              ,//= 42,                   // stateType              state          0                  true if unit matches specified sitstate (0,x: has exactly state x; 1,0: any standing state; 1,1: any sitting state;)
    DAILY_QUEST_DONE         ,//= 43,                   // quest id               0              0                  true if daily quest has been completed for the day
    CHARMED                  ,//= 44,                   // 0                      0              0                  true if unit is currently charmed
    PET_TYPE                 ,//= 45,                   // mask                   0              0                  true if player has a pet of given type(s)
    TAXI                     ,//= 46,                   // 0                      0              0                  true if player is on taxi
    QUESTSTATE               ,//= 47,                   // quest_id               state_mask     0                  true if player is in any of the provided quest states for the quest (1 = not taken, 2 = completed, 8 = in progress, 32 = failed, 64 = rewarded)
    QUEST_OBJECTIVE_PROGRESS ,//= 48,                   // ID                     0              progressValue      true if player has ID objective progress equal to ConditionValue3 (and quest is in quest log)
    DIFFICULTY_ID            ,//= 49,                   // Difficulty             0              0                  true is map has difficulty id
    GAMEMASTER               ,//= 50,                   // canBeGM                0              0                  true if player is gamemaster (or can be gamemaster)
    OBJECT_ENTRY_GUID        ,//= 51,                   // TypeID                 entry          guid               true if object is type TypeID and the entry is 0 or matches entry of the object or matches guid of the object
    TYPE_MASK                ,//= 52,                   // TypeMask               0              0                  true if object is type object's TypeMask matches provided TypeMask
    BATTLE_PET_COUNT         ,//= 53,                   // SpecieId               count          ComparisonType     true if player has `count` of battle pet species
    SCENARIO_STEP            ,//= 54,                   // ScenarioStepId         0              0                  true if player is at scenario with current step equal to ScenarioStepID
    SCENE_IN_PROGRESS        ,//= 55,                   // SceneScriptPackageId   0              0                  true if player is playing a scene with ScriptPackageId equal to given value
    PLAYER_CONDITION         ,//= 56,                   // PlayerConditionId      0              0                  true if player satisfies PlayerCondition
    PRIVATE_OBJECT           ,//= 57,                   // 0                      0              0                  true if entity is private object
    STRING_ID                ,//= 58,
}                                   
                                       