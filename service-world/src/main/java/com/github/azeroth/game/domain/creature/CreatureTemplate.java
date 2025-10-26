package com.github.azeroth.game.domain.creature;


import com.github.azeroth.common.EnumFlag;
import com.github.azeroth.common.LocalizedString;
import com.github.azeroth.dbc.defines.Difficulty;
import com.github.azeroth.defines.*;
import com.github.azeroth.game.domain.unit.NPCFlag;
import com.github.azeroth.game.domain.unit.UnitFlag;
import com.github.azeroth.game.domain.unit.UnitFlag2;
import com.github.azeroth.game.domain.unit.UnitFlag3;
import com.github.azeroth.utils.RandomUtil;

import java.util.ArrayList;
import java.util.EnumMap;


public class CreatureTemplate {

    public static final byte MAX_KILL_CREDIT = 2;
    public static final byte MAX_CREATURE_MODELS = 4;
    public static final byte MAX_CREATURE_NAMES = 4;
    public static final byte MAX_CREATURE_SPELLS = 8;


    public static  int CREATURE_REGEN_INTERVAL = 2;
    public static  int PET_FOCUS_REGEN_INTERVAL = 4;
    public static  int CREATURE_NOPATH_EVADE_TIME = 5;

    public int entry;
    public int[] killCredit = new int[MAX_KILL_CREDIT];
    public ArrayList<CreatureModel> models = new ArrayList<>(MAX_CREATURE_MODELS);
    public ArrayList<CreatureModelInfo> modelInfos = new ArrayList<>(MAX_CREATURE_MODELS);
    public LocalizedString name = new LocalizedString();
    public LocalizedString femaleName = new LocalizedString();
    public LocalizedString subName = new LocalizedString();
    public LocalizedString titleAlt = new LocalizedString();
    public LocalizedString iconName = new LocalizedString();
    public int[] gossipMenuIds;
    public short minLevel;
    public EnumMap<Difficulty, CreatureDifficulty> difficultyStore = new EnumMap<>(Difficulty.class);
    public short maxLevel;
    public Expansion healthScalingExpansion;
    public int requiredExpansion;
    public int vignetteID; // @todo Read Vignette.db2
    public int faction;
    public EnumFlag<NPCFlag> npcFlag;
    public float speedWalk;
    public float speedRun;
    public float scale;
    public CreatureClassification classification;
    public SpellSchool dmgSchool;
    public int baseAttackTime;
    public int rangeAttackTime;
    public float baseVariance;
    public float rangeVariance;
    public UnitClass unitClass;
    public EnumFlag<UnitFlag> unitFlags;
    public EnumFlag<UnitFlag2> unitFlags2;
    public EnumFlag<UnitFlag3> unitFlags3;
    public int dynamicFlags;
    public CreatureFamily family;
    public UnitClass trainerClass;
    public CreatureType type;
    public EnumFlag<CreatureTypeFlag> typeFlags;
    public EnumFlag<CreatureTypeFlag2> typeFlags2;
    public int lootId;
    public int pickPocketId;
    public int skinLootId;
    public int[] resistance = new int[SpellSchool.values().length];
    public int[] spells = new int[MAX_CREATURE_SPELLS];
    public int vehicleId;
    public int minGold;
    public int maxGold;
    public String aiName;
    public int movementType;
    public CreatureMovementData movement = new CreatureMovementData();
    public float hoverHeight;
    public float modHealth;
    public float modHealthExtra;
    public float modMana;
    public float modManaExtra;
    public float modArmor;
    public float modDamage;
    public float modExperience;
    public boolean racialLeader;
    public int movementId;
    public int creatureDifficultyID;
    public int widgetSetID;
    public int widgetSetUnitConditionID;
    public boolean regenHealth;
    public int creatureImmunitiesId;
    public long mechanicImmuneMask;
    public int spellSchoolImmuneMask;
    public EnumFlag<CreatureFlagExtra> flagsExtra;
    public String script;
    public int scriptID;
    public String stringId;

    public CreatureSummonedData summonedData;

    public int[] questItems;

    public CreatureAddon creatureTemplateAddon;


    public static int difficultyIDToDifficultyEntryIndex(Difficulty difficulty) {
        return switch (difficulty) {
            case NONE, NORMAL, RAID_10_N, RAID40, SCENARIO_3_MAN_N, NORMAL_RAID -> -1;
            case HEROIC, RAID_25_N, SCENARIO_3_MAN_HC, HEROIC_RAID -> 0;
            case RAID_10_HC, MYTHIC_KEYSTONE, MYTHIC_RAID -> 1;
            case RAID_25_HC -> 2;
            default -> -1;
        };
    }

    public final CreatureModel getModelByIdx(int idx) {
        return idx < models.size() ? models.get(idx) : null;
    }

    public final CreatureModel getRandomValidModel() {
        if (models.isEmpty()) {
            return null;
        }

        // If only one element, ignore the probability (even if 0)
        if (models.size() == 1) {
            return models.getFirst();
        }

        return RandomUtil.randomByWeight(models, model -> model.probability);
    }

    public final CreatureModel getFirstValidModel() {
        for (var model : models) {
            if (model.creatureDisplayId != 0) {
                return model;
            }
        }

        return null;
    }

    public final CreatureModel getModelWithDisplayId(int displayId) {
        for (var model : models) {
            if (displayId == model.creatureDisplayId) {
                return model;
            }
        }

        return null;
    }

    public final CreatureModel getFirstInvisibleModel() {
        for (int i = 0; i < models.size(); i++) {
            var model = models.get(i);
            var modelInfo = modelInfos.get(i);
            if (modelInfo != null && modelInfo.isTrigger) {
                return model;
            }
        }

        return CreatureModel.DEFAULT_INVISIBLE_MODEL;
    }

    public final CreatureModel getFirstVisibleModel() {
        for (int i = 0; i < models.size(); i++) {
            var model = models.get(i);
            var modelInfo = modelInfos.get(i);
            if (modelInfo != null && !modelInfo.isTrigger) {
                return model;
            }
        }

        return CreatureModel.DEFAULT_VISIBLE_MODEL;
    }

    public final SkillType getRequiredLootSkill() {
        if (typeFlags.hasFlag(CreatureTypeFlag.SKIN_WITH_HERBALISM)) {
            return SkillType.HERBALISM;
        } else if (typeFlags.hasFlag(CreatureTypeFlag.SKIN_WITH_MINING)) {
            return SkillType.MINING;
        } else if (typeFlags.hasFlag(CreatureTypeFlag.SKIN_WITH_ENGINEERING)) {
            return SkillType.ENGINEERING;
        } else {
            return SkillType.SKINNING; // normal case
        }
    }

    public final boolean isExotic() {
        return typeFlags.hasFlag(CreatureTypeFlag.TAMEABLE_EXOTIC);
    }

    public final boolean isTameable(boolean canTameExotic, CreatureDifficulty creatureDifficulty) {
        if (type != CreatureType.BEAST || family == CreatureFamily.NONE || !creatureDifficulty.typeFlag.hasFlag(CreatureTypeFlag.TAMEABLE)) {
            return false;
        }

        // if can tame exotic then can tame any tameable
        return canTameExotic || !isExotic();
    }

}
