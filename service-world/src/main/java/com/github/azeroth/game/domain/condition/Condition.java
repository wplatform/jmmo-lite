package com.github.azeroth.game.domain.condition;


import java.util.Objects;

public class Condition {


    private static final String[] STATIC_SOURCE_TYPE_DATA = {
                    "None",
                    "Creature Loot",
                    "Disenchant Loot",
                    "Fishing Loot",
                    "GameObject Loot",
                    "Item Loot",
                    "Mail Loot",
                    "Milling Loot",
                    "Pickpocketing Loot",
                    "Prospecting Loot",
                    "Reference Loot",
                    "Skinning Loot",
                    "Spell Loot",
                    "Spell Impl. Target",
                    "Gossip Menu",
                    "Gossip Menu Option",
                    "Creature Vehicle",
                    "Spell Expl. Target",
                    "Spell Click Event",
                    "Quest Available",
                    "Unused",
                    "Vehicle Spell",
                    "SmartScript",
                    "Npc Vendor",
                    "Spell Proc",
                    "Terrain Swap",
                    "Phase",
                    "Graveyard",
                    "AreaTrigger",
                    "ConversationLine",
                    "AreaTrigger Client Triggered",
                    "Trainer Spell",
                    "Object Visibility (by ID)",
                    "Spawn Group",
                    "Player Condition",
                    "Skill Line Ability"
            };


    public static final ConditionTypeInfo[] STATIC_CONDITION_TYPE_DATA = {
            ConditionTypeInfo.of("None",                      false, false, false, false ),
            ConditionTypeInfo.of("Aura",                      true, true,  true,  false ),
            ConditionTypeInfo.of("Item Stored",               true, true,  true,  false ),
            ConditionTypeInfo.of("Item Equipped",             true, false, false, false ),
            ConditionTypeInfo.of("Zone",                      true, false, false, false ),
            ConditionTypeInfo.of("Reputation",                true, true,  false, false ),
            ConditionTypeInfo.of("Team",                      true, false, false, false ),
            ConditionTypeInfo.of("Skill",                     true, true,  false, false ),
            ConditionTypeInfo.of("Quest Rewarded",            true, false, false, false ),
            ConditionTypeInfo.of("Quest Taken",               true, false, false, false ),
            ConditionTypeInfo.of("Quest Turned In",           true, false, false, false ),
            ConditionTypeInfo.of("Drunken",                   true, false, false, false ),
            ConditionTypeInfo.of("WorldState",                true, true,  false, false ),
            ConditionTypeInfo.of("Active Event",              true, false, false, false ),
            ConditionTypeInfo.of("Instance Info",             true, true,  true,  false ),
            ConditionTypeInfo.of("Quest None",                true, false, false, false ),
            ConditionTypeInfo.of("Class",                     true, false, false, false ),
            ConditionTypeInfo.of("Race",                      true, false, false, false ),
            ConditionTypeInfo.of("Achievement",               true, false, false, false ),
            ConditionTypeInfo.of("Title",                     true, false, false, false ),
            ConditionTypeInfo.of("SpawnMask",                 true, false, false, false ),
            ConditionTypeInfo.of("Gender",                    true, false, false, false ),
            ConditionTypeInfo.of("Unit State",                true, false, false, false ),
            ConditionTypeInfo.of("Map",                       true, false, false, false ),
            ConditionTypeInfo.of("Area",                      true, false, false, false ),
            ConditionTypeInfo.of("CreatureType",              true, false, false, false ),
            ConditionTypeInfo.of("Spell Known",               true, false, false, false ),
            ConditionTypeInfo.of("Phase",                     true, false, false, false ),
            ConditionTypeInfo.of("Level",                     true, true,  false, false ),
            ConditionTypeInfo.of("Quest Completed",           true, false, false, false ),
            ConditionTypeInfo.of("Near Creature",             true, true,  true,  false ),
            ConditionTypeInfo.of("Near GameObject",           true, true,  false, false ),
            ConditionTypeInfo.of("Near Item",                 true, true,  false, false ),
            ConditionTypeInfo.of("Object Entry or Guid",      true, true,  true,  false ),
            ConditionTypeInfo.of("Object TypeMask",           true, false, false, false ),
            ConditionTypeInfo.of("Relation",                  true, true,  false, false ),
            ConditionTypeInfo.of("Reaction",                  true, true,  false, false ),
            ConditionTypeInfo.of("Distance",                  true, true,  true,  false ),
            ConditionTypeInfo.of("Alive",                     false, false, false, false ),
            ConditionTypeInfo.of("Health Value",              true, true,  false, false ),
            ConditionTypeInfo.of("Health Pct",                true, true,  false, false ),
            ConditionTypeInfo.of("Realm Achievement",         true, false, false, false ),
            ConditionTypeInfo.of("In Water",                  false, false, false, false ),
            ConditionTypeInfo.of("Terrain Swap",              true, false, false, false ),
            ConditionTypeInfo.of("Sit/stand state",           true, true,  false, false ),
            ConditionTypeInfo.of("Daily Quest Completed",     true, false, false, false ),
            ConditionTypeInfo.of("Charmed",                   false, false, false, false ),
            ConditionTypeInfo.of("Pet type",                  true, false, false, false ),
            ConditionTypeInfo.of("On Taxi",                   false, false, false, false ),
            ConditionTypeInfo.of("Quest state mask",          true, true,  false, false ),
            ConditionTypeInfo.of("Quest objective progress",  true, false, true,  false ),
            ConditionTypeInfo.of("Map Difficulty",            true, false, false, false ),
            ConditionTypeInfo.of("Is Gamemaster",             true, false, false, false ),
            ConditionTypeInfo.of("Object Entry or Guid",      true, true,  true,  false ),
            ConditionTypeInfo.of("Object TypeMask",           true, false, false, false ),
            ConditionTypeInfo.of("BattlePet Species Learned", true, true,  true,  false ),
            ConditionTypeInfo.of("On Scenario Step",          true, false, false, false ),
            ConditionTypeInfo.of("Scene In Progress",         true, false, false, false ),
            ConditionTypeInfo.of("Player Condition",          true, false, false, false ),
            ConditionTypeInfo.of("Private Object",           false, false, false, false ),
            ConditionTypeInfo.of("Skill Line Ability",        true, false, false, false ),
        };





    public ConditionSourceType sourceType; //SourceTypeOrReferenceId
    public int sourceGroup;
    public int sourceEntry;
    public int sourceId; // So far, only used in CONDITION_SOURCE_TYPE_SMART_EVENT
    public int elseGroup;
    public ConditionType conditionType; //ConditionTypeOrReference
    public int conditionValue1;
    public int conditionValue2;
    public int conditionValue3;
    public int errorType;
    public int errorTextId;
    public int referenceId;
    public int scriptId;
    public byte conditionTarget;
    public boolean negativeCondition;

    public Condition() {
        sourceType = ConditionSourceType.NONE;
        conditionType = ConditionType.NONE;
    }

    public String toString(boolean ext) {
        StringBuilder sb = new StringBuilder();
        sb.append("[Condition ");
        sb.append("SourceType: ").append(sourceType);

        // Since we don't have access to the exact enum values, I'm using a reasonable approach
        // You may need to adjust based on your actual enum implementations
        if (sourceType != null && sourceType.ordinal() < ConditionSourceType.values().length) {
            // In Java, we typically get the enum name directly
            sb.append(" (").append(sourceType.name()).append(")");
        } else if (sourceType == ConditionSourceType.REFERENCE_CONDITION) {
            sb.append(" (Reference)");
        } else {
            sb.append(" (Unknown)");
        }

        // Check if sourceGroup can be set for this sourceType
        if (canHaveSourceGroupSet()) {
            sb.append(", SourceGroup: ").append(sourceGroup);
        }

        sb.append(", SourceEntry: ").append(sourceEntry);

        // Check if sourceId can be set for this sourceType
        if (canHaveSourceIdSet()) {
            sb.append(", SourceId: ").append(sourceId);
        }

        if (ext) {
            sb.append(", ConditionType: ").append(conditionType);
            if (conditionType != null && conditionType.ordinal() < ConditionType.values().length) {
                sb.append(" (").append(STATIC_CONDITION_TYPE_DATA[conditionType.ordinal()].name).append(")");
            } else {
                sb.append(" (Unknown)");
            }
        }

        sb.append("]");
        return sb.toString();
    }

    // Overloaded method with default parameter
    public String toString() {
        return toString(false);
    }


    public boolean canHaveSourceGroupSet() {
        return (sourceType == ConditionSourceType.CREATURE_LOOT_TEMPLATE ||
                sourceType == ConditionSourceType.DISENCHANT_LOOT_TEMPLATE ||
                sourceType == ConditionSourceType.FISHING_LOOT_TEMPLATE ||
                sourceType == ConditionSourceType.GAME_OBJECT_LOOT_TEMPLATE ||
                sourceType == ConditionSourceType.ITEM_LOOT_TEMPLATE ||
                sourceType == ConditionSourceType.MAIL_LOOT_TEMPLATE ||
                sourceType == ConditionSourceType.MILLING_LOOT_TEMPLATE ||
                sourceType == ConditionSourceType.PICKPOCKETING_LOOT_TEMPLATE ||
                sourceType == ConditionSourceType.PROSPECTING_LOOT_TEMPLATE ||
                sourceType == ConditionSourceType.REFERENCE_LOOT_TEMPLATE ||
                sourceType == ConditionSourceType.SKINNING_LOOT_TEMPLATE ||
                sourceType == ConditionSourceType.SPELL_LOOT_TEMPLATE ||
                sourceType == ConditionSourceType.GOSSIP_MENU ||
                sourceType == ConditionSourceType.GOSSIP_MENU_OPTION ||
                sourceType == ConditionSourceType.VEHICLE_SPELL ||
                sourceType == ConditionSourceType.SPELL_IMPLICIT_TARGET ||
                sourceType == ConditionSourceType.SPELL_CLICK_EVENT ||
                sourceType == ConditionSourceType.SMART_EVENT ||
                sourceType == ConditionSourceType.NPC_VENDOR ||
                sourceType == ConditionSourceType.PHASE ||
                sourceType == ConditionSourceType.GRAVEYARD ||
                sourceType == ConditionSourceType.AREA_TRIGGER ||
                sourceType == ConditionSourceType.TRAINER_SPELL ||
                sourceType == ConditionSourceType.OBJECT_ID_VISIBILITY ||
                sourceType == ConditionSourceType.REFERENCE_CONDITION);
    }


    public boolean canHaveSourceIdSet() {
        return (sourceType == ConditionSourceType.SMART_EVENT);
    }

    public boolean canHaveConditionType(ConditionType conditionType) {
        if (Objects.requireNonNull(sourceType) == ConditionSourceType.SPAWN_GROUP) {
            return switch (conditionType) {
                case NONE, ACTIVE_EVENT, INSTANCE_INFO, MAPID, WORLD_STATE, REALM_ACHIEVEMENT, DIFFICULTY_ID,
                     SCENARIO_STEP -> true;
                default -> false;
            };
        }
        return true;
    }

    public boolean isLoaded() { return conditionType != ConditionType.NONE || referenceId != 0 || scriptId != 0; }

}
