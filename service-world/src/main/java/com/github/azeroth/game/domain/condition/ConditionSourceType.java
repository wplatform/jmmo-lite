package com.github.azeroth.game.domain.condition;

/*! Documentation on implementing a new ConditionSourceType:
    Step 1: Check for the lowest free ID. Look for UNUSED_XX in the enum.
            Then define the new source type.

    Step 2: Determine and map the parameters for the new condition source type.

    Step 3: Add a case block to ConditionMgr::isSourceTypeValid with the new condition type
            and validate the parameters.

    Step 4: If your condition can be grouped (determined in step 2), add a rule for it in
            ConditionMgr::CanHaveSourceGroupSet, following the example of the existing types.

    Step 5: Define the maximum available condition targets in ConditionMgr::GetMaxAvailableConditionTargets.

    Step 6: Define ConditionSourceType Name in ConditionMgr::StaticSourceTypeData.

    The following steps only apply if your condition can be grouped:

    Step 7: Determine how you are going to store your conditions. You need to add a new storage container
            for it in ConditionMgr class, along with a function like:
            ConditionList GetConditionsForXXXYourNewSourceTypeXXX(parameters...)

            The above function should be placed in upper level (practical) code that actually
            checks the conditions.

    Step 8: Implement loading for your source type in ConditionMgr::LoadConditions.

    Step 9: Implement memory cleaning for your source type in ConditionMgr::Clean.
*/

public enum ConditionSourceType {
    NONE                           ,//= 0,
    CREATURE_LOOT_TEMPLATE         ,//= 1,
    DISENCHANT_LOOT_TEMPLATE       ,//= 2,
    FISHING_LOOT_TEMPLATE          ,//= 3,
    GAME_OBJECT_LOOT_TEMPLATE      ,//= 4,
    ITEM_LOOT_TEMPLATE             ,//= 5,
    MAIL_LOOT_TEMPLATE             ,//= 6,
    MILLING_LOOT_TEMPLATE          ,//= 7,
    PICKPOCKETING_LOOT_TEMPLATE    ,//= 8,
    PROSPECTING_LOOT_TEMPLATE      ,//= 9,
    REFERENCE_LOOT_TEMPLATE        ,//= 10,
    SKINNING_LOOT_TEMPLATE         ,//= 11,
    SPELL_LOOT_TEMPLATE            ,//= 12,
    SPELL_IMPLICIT_TARGET          ,//= 13,
    GOSSIP_MENU                    ,//= 14,
    GOSSIP_MENU_OPTION             ,//= 15,
    CREATURE_TEMPLATE_VEHICLE      ,//= 16,
    SPELL                          ,//= 17,
    SPELL_CLICK_EVENT              ,//= 18,
    QUEST_AVAILABLE                ,//= 19,
    // Condition source type 20 unused                   ,//
    VEHICLE_SPELL                  ,//= 21,
    SMART_EVENT                    ,//= 22,
    NPC_VENDOR                     ,//= 23,
    SPELL_PROC                     ,//= 24,
    TERRAIN_SWAP                   ,//= 25,
    PHASE                          ,//= 26,
    GRAVEYARD                      ,//= 27,
    AREA_TRIGGER,//= 28,
    CONVERSATION_LINE              ,//= 29,
    AREA_TRIGGER_CLIENT_TRIGGERED,//= 30,
    TRAINER_SPELL                  ,//= 31,
    OBJECT_ID_VISIBILITY           ,//= 32,
    SPAWN_GROUP                    ,//= 33,
    PLAYER_CONDITION               ,//= 34,
    SKILL_LINE_ABILITY             ,//= 35,
    REFERENCE_CONDITION            ;//= MAX_DB_ALLOWED, // internal, not set in db
}
