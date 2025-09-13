package com.github.azeroth.defines;

import com.github.azeroth.common.Locale;

public interface SharedDefine {

    // Client expected level limitation, like as used in DBC item max levels for "until max player level"
    // use as default max player level, must be fit max level for used client
    // also see MAX_LEVEL and STRONG_MAX_LEVEL define
    int DEFAULT_MAX_LEVEL = 110;

    // client supported max level for player/pets/etc. Avoid overflow or client stability affected.
    // also see GT_MAX_LEVEL define
    int MAX_LEVEL = 110;

    // Server side limitation. Base at used code requirements.
    // also see MAX_LEVEL and GT_MAX_LEVEL define
    int STRONG_MAX_LEVEL = 255;

    float GROUND_HEIGHT_TOLERANCE = 0.05f; // Extra tolerance to z position to check if it is in air or on ground.
    float Z_OFFSET_FIND_HEIGHT = 0.5f;

    Locale DEFAULT_LOCALE = Locale.zhCN;

    int MAX_TALENT_TIERS = 7;
    int MAX_TALENT_COLUMNS = 3;
    int MAX_PVP_TALENT_TIERS = 6;
    int MAX_PVP_TALENT_COLUMNS = 3;


    int MIN_SPECIALIZATION_LEVEL = 10;
    int MAX_SPECIALIZATIONS = 4;
    int PET_SPEC_OVERRIDE_CLASS_INDEX = PlayerClass.values().length;

    int MAX_NPC_TEXT_OPTIONS = 8;


    short BATTLE_PET_SPECIES_MAX_ID = 2164;


    byte LINE_OF_SIGHT_CHECK_VMAP = 0x1; // check static floor layout data

    byte LINE_OF_SIGHT_CHECK_GOBJECT = 0x2; // check dynamic game object data

    byte LINE_OF_SIGHT_ALL_CHECKS = (LINE_OF_SIGHT_CHECK_VMAP | LINE_OF_SIGHT_CHECK_GOBJECT);


    // used in script definitions
    byte EFFECT_FIRST_FOUND = (byte) 254;

    byte EFFECT_ALL = (byte) 255;

    int MAX_CHARACTERS_PER_REALM = 16;



    int MAX_UNIT_CLASSES = 4;
    int CLASS_MASK_ALL_CREATURES = (1 << (UnitClass.WARRIOR.getValue() - 1))
            | (1 << (UnitClass.PALADIN.getValue() - 1))
            | (1 << (UnitClass.ROGUE.getValue() - 1))
            | (1 << (UnitClass.MAGE.getValue() - 1));

    int CLASS_MASK_WAND_USERS = (1 << (PlayerClass.PRIEST.ordinal() - 1))
            | (1 << (PlayerClass.MAGE.ordinal() - 1))
            | (1 << (PlayerClass.WARLOCK.ordinal() - 1));

    int PLAYER_MAX_BATTLEGROUND_QUEUES = 3;
    int MIN_REPUTATION_RANK = ReputationRank.HATED.ordinal();
    int MAX_REPUTATION_RANK = ReputationRank.values().length;
    int MAX_SPILLOVER_FACTIONS = 5;
    int MAX_STATS = 4;
    int MAX_POWERS_PER_CLASS = 6;
    int[] ITEM_QUALITY_COLORS = {
            0xff9d9d9d, // GREY
            0xffffffff, // WHITE
            0xff1eff00, // GREEN
            0xff0070dd, // BLUE
            0xffa335ee, // PURPLE
            0xffff8000, // ORANGE
            0xffe6cc80, // LIGHT YELLOW
            0xff00ccff, // LIGHT BLUE
            0xff00ccff  // LIGHT BLUE
    };
    int MAX_QUEST_DIFFICULTY = 5;
    int[] QUEST_DIFFICULTY_COLORS = {0xff40c040, 0xff808080, 0xffffff00, 0xffff8040, 0xffff2020};

    static int getMaxLevelForExpansion(int expansion) {
        return switch (expansion) {
            case 0 -> 60;
            case 1 -> 70;
            case 2 -> 80;
            case 3 -> 85;
            case 4 -> 90;
            case 5 -> 100;
            case 6 -> 110;
            default -> 0;
        };
    }

    static SpellSchoolMask getMaskForSchool(SpellSchool school) {
        return SpellSchoolMask.values()[1 << school.ordinal()];
    }

    static SpellSchool getFirstSchoolInMask(SpellSchoolMask mask) {
        for (int i = 0; i < SpellSchool.values().length; i++) {
            if ((mask.value & (1 << i)) != 0) {
                return SpellSchool.values()[i];
            }
        }
        return SpellSchool.NORMAL;
    }


// ***********************************
// Spell Attributes definitions
// ***********************************




    byte MAX_SHEATHETYPE = 8;


    byte PLAYER_CUSTOM_DISPLAY_SIZE = 3;


    int PER_CASTER_AURA_STATE_MASK = ((1 << (AuraStateType.RAID_ENCOUNTER_2.ordinal() - 1)) | (1 << (AuraStateType.ROGUE_POISONED.ordinal() - 1)));


    // Used for spell 42292 Immune Movement Impairment and Loss of Control (0x49967ca6)
    int IMMUNE_TO_MOVEMENT_IMPAIRMENT_AND_LOSS_CONTROL_MASK = (
            (1 << Mechanics.CHARM.ordinal()) | (1 << Mechanics.DISORIENTED.ordinal()) | (1 << Mechanics.FEAR.ordinal()) |
                    (1 << Mechanics.ROOT.ordinal()) | (1 << Mechanics.SLEEP.ordinal()) | (1 << Mechanics.SNARE.ordinal()) |
                    (1 << Mechanics.STUN.ordinal()) | (1 << Mechanics.FREEZE.ordinal()) | (1 << Mechanics.SILENCE.ordinal())
                    | (1 << Mechanics.DISARM.ordinal()) | (1 << Mechanics.KNOCKOUT.ordinal()) |
                    (1 << Mechanics.POLYMORPH.ordinal()) | (1 << Mechanics.BANISH.ordinal()) | (1 << Mechanics.SHACKLE.ordinal()) |
                    (1 << Mechanics.TURN.ordinal()) | (1 << Mechanics.HORROR.ordinal()) | (1 << Mechanics.DAZE.ordinal()) |
                    (1 << Mechanics.SAPPED.ordinal()));

    ;

    int DISPEL_ALL_MASK = ((1 << DispelType.MAGIC.ordinal()) | (1 << DispelType.CURSE.ordinal()) | (1 << DispelType.DISEASE.ordinal()) | (1 << DispelType.POISON.ordinal()));


    int MAX_GAME_OBJECT_DATA = 33;             // Max number of uint32 vars in gameobject_template data field


    int MAX_GO_STATE = 3;
    int MAX_GO_STATE_TRANSPORT_STOP_FRAMES = 9;



    int CREATURE_TYPEMASK_DEMON_OR_UNDEAD = (1 << (CreatureType.DEMON.ordinal() - 1)) | (1 << (CreatureType.UNDEAD.ordinal() - 1));
    int CREATURE_TYPEMASK_HUMANOID_OR_UNDEAD = (1 << (CreatureType.HUMANOID.ordinal() - 1)) | (1 << (CreatureType.UNDEAD.ordinal() - 1));
    int CREATURE_TYPEMASK_MECHANICAL_OR_ELEMENTAL = (1 << (CreatureType.MECHANICAL.ordinal() - 1)) | (1 << (CreatureType.ELEMENTAL.ordinal() - 1));


    static PlayerClass classByQuestSort(QuestSort questSort) {
        return switch (questSort) {
            case WARLOCK -> PlayerClass.WARLOCK;
            case WARRIOR -> PlayerClass.WARRIOR;
            case SHAMAN -> PlayerClass.SHAMAN;
            case PALADIN -> PlayerClass.PALADIN;
            case MAGE -> PlayerClass.MAGE;
            case ROGUE -> PlayerClass.ROGUE;
            case HUNTER -> PlayerClass.HUNTER;
            case PRIEST -> PlayerClass.PRIEST;
            case DRUID -> PlayerClass.DRUID;
            case DEATH_KNIGHT -> PlayerClass.DEATH_KNIGHT;
            case DEMON_HUNTER -> PlayerClass.DEMON_HUNTER;
            default -> PlayerClass.NONE;
        };
    }

    ;

    static SkillType skillByLockType(LockType locktype) {
        return switch (locktype) {
            case LOCKTYPE_HERBALISM -> SkillType.HERBALISM;
            case LOCKTYPE_MINING -> SkillType.MINING;
            case LOCKTYPE_FISHING -> SkillType.FISHING;
            case LOCKTYPE_INSCRIPTION -> SkillType.INSCRIPTION;
            case LOCKTYPE_ARCHAEOLOGY -> SkillType.ARCHAEOLOGY;
            case LOCKTYPE_LUMBER_MILL -> SkillType.LOGGING;
            case LOCKTYPE_CLASSIC_HERBALISM -> SkillType.HERBALISM_2;
            case LOCKTYPE_OUTLAND_HERBALISM -> SkillType.OUTLAND_HERBALISM;
            case LOCKTYPE_NORTHREND_HERBALISM -> SkillType.NORTHREND_HERBALISM;
            case LOCKTYPE_CATACLYSM_HERBALISM -> SkillType.CATACLYSM_HERBALISM;
            case LOCKTYPE_PANDARIA_HERBALISM -> SkillType.PANDARIA_HERBALISM;
            case LOCKTYPE_DRAENOR_HERBALISM -> SkillType.DRAENOR_HERBALISM;
            case LOCKTYPE_LEGION_HERBALISM -> SkillType.LEGION_HERBALISM;
            case LOCKTYPE_KUL_TIRAN_HERBALISM -> SkillType.KUL_TIRAN_HERBALISM;
            case LOCKTYPE_CLASSIC_MINING -> SkillType.MINING_2;
            case LOCKTYPE_OUTLAND_MINING -> SkillType.OUTLAND_MINING;
            case LOCKTYPE_NORTHREND_MINING -> SkillType.NORTHREND_MINING;
            case LOCKTYPE_CATACLYSM_MINING -> SkillType.CATACLYSM_MINING;
            case LOCKTYPE_PANDARIA_MINING -> SkillType.PANDARIA_MINING;
            case LOCKTYPE_DRAENOR_MINING -> SkillType.DRAENOR_MINING;
            case LOCKTYPE_LEGION_MINING -> SkillType.LEGION_MINING;
            case LOCKTYPE_KUL_TIRAN_MINING -> SkillType.KUL_TIRAN_MINING;
            default -> SkillType.NONE;
        };
    }

    static SkillType skillByQuestSort(QuestSort QuestSort) {
        return switch (QuestSort) {
            case HERBALISM -> SkillType.HERBALISM;
            case FISHING -> SkillType.FISHING;
            case BLACKSMITHING -> SkillType.BLACKSMITHING;
            case ALCHEMY -> SkillType.ALCHEMY;
            case LEATHERWORKING -> SkillType.LEATHERWORKING;
            case ENGINEERING -> SkillType.ENGINEERING;
            case TAILORING -> SkillType.TAILORING;
            case COOKING -> SkillType.COOKING;
            case JEWELCRAFTING -> SkillType.JEWELCRAFTING;
            case INSCRIPTION -> SkillType.INSCRIPTION;
            case ARCHAEOLOGY -> SkillType.ARCHAEOLOGY;
            default -> SkillType.NONE;
        };
    }


    byte PLAYER_CORPSE_LOOT_ENTRY = 1;


    byte MAX_WEATHER_TYPE = 4;


    int GM_SILENCE_AURA = 1852;


    byte MAX_PET_DIET = 9;

    byte CHAIN_SPELL_JUMP_RADIUS = 8;


    byte MAX_TOTEM_SLOT = 5;
    byte MAX_GAMEOBJECT_SLOT = 4;


    byte PVP_TEAMS_COUNT = 2;


    short MAX_BATTLEGROUND_TYPE_ID = 845;


    int CURRENCY_PRECISION = 100;


    byte MAX_ACCOUNT_TUTORIAL_VALUES = 8;

    // max+1 for player race
    int MAX_RACES = Race.COMPANION_PTERRODAX.ordinal();


    int REPUTATION_CAP = 42000;
    int REPUTATION_BOTTOM = -42000;


}


