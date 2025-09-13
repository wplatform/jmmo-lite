package com.github.azeroth.game.spell;


import com.github.azeroth.common.EnumFlag;
import com.github.azeroth.common.Flag128;
import com.github.azeroth.common.LocalizedString;
import com.github.azeroth.dbc.defines.Difficulty;
import com.github.azeroth.dbc.domain.*;
import com.github.azeroth.defines.*;
import com.github.azeroth.game.entity.item.Item;
import com.github.azeroth.game.entity.item.ItemEnchantmentManager;
import com.github.azeroth.game.entity.item.enums.ItemClass;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.entity.player.model.SpellModifier;
import com.github.azeroth.game.entity.player.model.SpellModifierByClassMask;
import com.github.azeroth.game.entity.player.model.SpellPctModifierByLabel;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.spell.auras.enums.AuraType;
import com.github.azeroth.game.spell.enums.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


@Getter
@Setter
public class SpellInfo {

    private final ArrayList<SpellProcsPerMinuteMod> procPpmMods = new ArrayList<>();
    private final ArrayList<SpellEffectInfo> effects = new ArrayList<>();
    private final ArrayList<SpellXSpellVisual> visuals = new ArrayList<>();
    private final Difficulty difficulty = Difficulty.NONE;
    public SpellPower[] powerCosts = new SpellPower[SpellDefine.MaxPowersPerSpell];
    public int[] totem = new int[SpellDefine.MAX_SPELL_TOTEMS];
    public int[] totemCategory = new int[SpellDefine.MAX_SPELL_TOTEMS];
    public int[] reagent = new int[SpellDefine.MaxReagents];
    public int[] reagentCount = new int[SpellDefine.MaxReagents];
    public ArrayList<SpellReagentsCurrency> reagentsCurrency = new ArrayList<>();
    public int chargeCategoryId;
    public ArrayList<Integer> labels = new ArrayList<>();
    // SpellScalingEntry
    public ScalingInfo scaling = new ScalingInfo();
    private SpellSpecificType spellSpecific;
    private AuraStateType auraState = AuraStateType.NONE;
    private SpellDiminishInfo diminishInfo;
    private long allowedMechanicMask;
    private int id;
    private int categoryId;
    private DispelType dispel;
    private Mechanics mechanic;
    private EnumFlag<SpellAttr0> attributes;
    private EnumFlag<SpellAttr1> attributesEx;
    private EnumFlag<SpellAttr2> attributesEx2;
    private EnumFlag<SpellAttr3> attributesEx3;
    private EnumFlag<SpellAttr4> attributesEx4;
    private EnumFlag<SpellAttr5> attributesEx5;
    private EnumFlag<SpellAttr6> attributesEx6;
    private EnumFlag<SpellAttr7> attributesEx7;
    private EnumFlag<SpellAttr8> attributesEx8;
    private EnumFlag<SpellAttr9> attributesEx9;
    private EnumFlag<SpellAttr10> attributesEx10;
    private EnumFlag<SpellAttr11> attributesEx11;
    private EnumFlag<SpellAttr12> attributesEx12;
    private EnumFlag<SpellAttr13> attributesEx13;
    private EnumFlag<SpellAttr14> attributesEx14;
    private EnumFlag<SpellCustomAttributes> attributesCu;
    private HashSet<Integer> negativeEffects;

    private long stances;

    private long stancesNot;
    private SpellCastTargetFlag targets;

    private int targetCreatureType;

    private int requiresSpellFocus;

    private int facingCasterFlags;
    private AuraStateType casterAuraState = AuraStateType.NONE;
    private AuraStateType targetAuraState = AuraStateType.NONE;
    private AuraStateType excludeCasterAuraState = AuraStateType.NONE;
    private AuraStateType excludeTargetAuraState = AuraStateType.NONE;

    private int casterAuraSpell;

    private int targetAuraSpell;

    private int excludeCasterAuraSpell;

    private int excludeTargetAuraSpell;
    private AuraType casterAuraType = AuraType.NONE;
    private AuraType targetAuraType = AuraType.NONE;
    private AuraType excludeCasterAuraType = AuraType.NONE;
    private AuraType excludeTargetAuraType = AuraType.NONE;
    private SpellCastTime castTimeEntry;

    private int recoveryTime;

    private int categoryRecoveryTime;

    private int startRecoveryCategory;

    private int startRecoveryTime;

    private int cooldownAuraSpellId;
    private SpellInterruptFlag interruptFlags = SpellInterruptFlag.NONE;
    private SpellAuraInterruptFlag auraInterruptFlags = SpellAuraInterruptFlag.NONE;
    private SpellAuraInterruptFlag2 auraInterruptFlags2 = SpellAuraInterruptFlag2.NONE;
    private SpellAuraInterruptFlag channelInterruptFlags = SpellAuraInterruptFlag.NONE;
    private SpellAuraInterruptFlag2 channelInterruptFlags2 = SpellAuraInterruptFlag2.NONE;
    private procFlags procFlags;

    private int procChance;

    private int procCharges;

    private int procCooldown;
    private float procBasePpm;

    private int maxLevel;

    private int baseLevel;

    private int spellLevel;
    private SpellDuration durationEntry;
    private SpellRange rangeEntry;
    private float speed;
    private float launchDelay;

    private int stackAmount;
    private ItemClass equippedItemClass;
    private int equippedItemSubClassMask;
    private int equippedItemInventoryTypeMask;

    private int iconFileDataId;

    private int activeIconFileDataId;

    private int contentTuningId;

    private int showFutureSpellPlayerConditionId;
    private LocalizedString spellName;
    private float coneAngle;
    private float width;

    private int maxTargetLevel;

    private int maxAffectedTargets;
    private SpellFamilyName spellFamilyName;
    private Flag128 spellFamilyFlags;
    private SpellDmgClass dmgClass;
    private SpellPreventionType preventionType;
    private int requiredAreasId;
    private SpellSchoolMask schoolMask;

    private HashMap<Byte, SpellEmpowerStage> empowerStages = new HashMap<>();
    private SpellCastTargetFlag explicitTargetMask = SpellCastTargetFlag.NONE;
    private SpellInfo(SpellName spellName, Difficulty difficulty, SpellInfoLoadHelper data) {
        setId(spellName.id);
        setDifficulty(difficulty);

        for (var spellEffect : data.effects.entrySet()) {
            effects.EnsureWritableListIndex(spellEffect.getKey(), new SpellEffectInfo(this));
            effects.set(spellEffect.getKey(), new SpellEffectInfo(this, spellEffect.getValue()));
        }

        // Correct EffectIndex for blank effects
        for (var i = 0; i < effects.size(); ++i) {
            effects.get(i).effectIndex = i;
        }

        setNegativeEffects(new HashSet<Integer>());

        setSpellName(spellName.name);

        var _misc = data.misc;

        if (_misc != null) {
            setAttributes(SpellAttr0.forValue(_misc.Attributes[0]));
            setAttributesEx(SpellAttr1.forValue(_misc.Attributes[1]));
            setAttributesEx2(SpellAttr2.forValue(_misc.Attributes[2]));
            setAttributesEx3(SpellAttr3.forValue(_misc.Attributes[3]));
            setAttributesEx4(SpellAttr4.forValue(_misc.Attributes[4]));
            setAttributesEx5(SpellAttr5.forValue(_misc.Attributes[5]));
            setAttributesEx6(SpellAttr6.forValue(_misc.Attributes[6]));
            setAttributesEx7(SpellAttr7.forValue(_misc.Attributes[7]));
            setAttributesEx8(SpellAttr8.forValue(_misc.Attributes[8]));
            setAttributesEx9(SpellAttr9.forValue(_misc.Attributes[9]));
            setAttributesEx10(SpellAttr10.forValue(_misc.Attributes[10]));
            setAttributesEx11(SpellAttr11.forValue(_misc.Attributes[11]));
            setAttributesEx12(SpellAttr12.forValue(_misc.Attributes[12]));
            setAttributesEx13(SpellAttr13.forValue(_misc.Attributes[13]));
            setAttributesEx14(SpellAttr14.forValue(_misc.Attributes[14]));
            setCastTimeEntry(CliDB.SpellCastTimesStorage.get(_misc.CastingTimeIndex));
            setDurationEntry(CliDB.SpellDurationStorage.get(_misc.DurationIndex));
            setRangeEntry(CliDB.SpellRangeStorage.get(_misc.RangeIndex));
            setSpeed(_misc.speed);
            setLaunchDelay(_misc.launchDelay);
            setSchoolMask(spellSchoolMask.forValue(_misc.schoolMask));
            setIconFileDataId(_misc.SpellIconFileDataID);
            setActiveIconFileDataId(_misc.ActiveIconFileDataID);
            setContentTuningId(_misc.contentTuningID);
            setShowFutureSpellPlayerConditionId((int) _misc.ShowFutureSpellPlayerConditionID);
        }

        // SpellScalingEntry
        var scaling = data.scaling;

        if (scaling != null) {
            scaling.minScalingLevel = scaling.minScalingLevel;
            scaling.maxScalingLevel = scaling.maxScalingLevel;
            scaling.scalesFromItemLevel = scaling.scalesFromItemLevel;
        }

        // SpellAuraOptionsEntry
        var options = data.auraOptions;

        if (options != null) {
            setProcFlags(new ProcFlagsInit(options.ProcTypeMask));
            setProcChance(options.procChance);
            setProcCharges((int) options.procCharges);
            setProcCooldown(options.ProcCategoryRecovery);
            setStackAmount(options.CumulativeAura);

            var _ppm = CliDB.SpellProcsPerMinuteStorage.get(options.SpellProcsPerMinuteID);

            if (_ppm != null) {
                setProcBasePpm(_ppm.BaseProcRate);
                procPpmMods = global.getDB2Mgr().GetSpellProcsPerMinuteMods(_ppm.id);
            }
        }

        // SpellAuraRestrictionsEntry
        var aura = data.auraRestrictions;

        if (aura != null) {
            setCasterAuraState(AuraStateType.forValue(aura.casterAuraState));
            setTargetAuraState(AuraStateType.forValue(aura.targetAuraState));
            setExcludeCasterAuraState(AuraStateType.forValue(aura.excludeCasterAuraState));
            setExcludeTargetAuraState(AuraStateType.forValue(aura.excludeTargetAuraState));
            setCasterAuraSpell(aura.casterAuraSpell);
            setTargetAuraSpell(aura.targetAuraSpell);
            setExcludeCasterAuraSpell(aura.excludeCasterAuraSpell);
            setExcludeTargetAuraSpell(aura.excludeTargetAuraSpell);
            setCasterAuraType(AuraType.forValue(aura.casterAuraType));
            setTargetAuraType(AuraType.forValue(aura.targetAuraType));
            setExcludeCasterAuraType(AuraType.forValue(aura.excludeCasterAuraType));
            setExcludeTargetAuraType(AuraType.forValue(aura.excludeTargetAuraType));
        }

        setRequiredAreasId(-1);
        // SpellCastingRequirementsEntry
        var castreq = data.castingRequirements;

        if (castreq != null) {
            setRequiresSpellFocus(castreq.requiresSpellFocus);
            setFacingCasterFlags(castreq.facingCasterFlags);
            setRequiredAreasId(castreq.RequiredAreasID);
        }

        // SpellCategoriesEntry
        var categories = data.categories;

        if (categories != null) {
            setCategoryId(categories.category);
            setDispel(DispelType.forValue(categories.DispelType));
            setMechanic(mechanics.forValue(categories.mechanic));
            setStartRecoveryCategory(categories.startRecoveryCategory);
            setDmgClass(SpellDmgClass.forValue(categories.DefenseType));
            setPreventionType(SpellPreventionType.forValue(categories.preventionType));
            chargeCategoryId = categories.ChargeCategory;
        }

        // SpellClassOptionsEntry
        setSpellFamilyFlags(new flagArray128());
        var classOptions = data.classOptions;

        if (classOptions != null) {
            setSpellFamilyName(SpellFamilyName.forValue(classOptions.SpellClassSet));
            setSpellFamilyFlags(classOptions.spellClassMask);
        }

        // SpellCooldownsEntry
        var cooldowns = data.cooldowns;

        if (cooldowns != null) {
            setRecoveryTime(cooldowns.recoveryTime);
            setCategoryRecoveryTime(cooldowns.categoryRecoveryTime);
            setStartRecoveryTime(cooldowns.startRecoveryTime);
            setCooldownAuraSpellId(cooldowns.auraSpellID);
        }

        setEquippedItemClass(itemClass.NONE);
        setEquippedItemSubClassMask(0);
        setEquippedItemInventoryTypeMask(0);
        // SpellEquippedItemsEntry
        var equipped = data.equippedItems;

        if (equipped != null) {
            setEquippedItemClass(itemClass.forValue(equipped.equippedItemClass));
            setEquippedItemSubClassMask(equipped.EquippedItemSubclass);
            setEquippedItemInventoryTypeMask(equipped.EquippedItemInvTypes);
        }

        // SpellInterruptsEntry
        var interrupt = data.interrupts;

        if (interrupt != null) {
            setInterruptFlags(SpellInterruptFlag.forValue(interrupt.interruptFlags));
            setAuraInterruptFlags(SpellAuraInterruptFlag.forValue(interrupt.AuraInterruptFlags[0]));
            setAuraInterruptFlags2(SpellAuraInterruptFlag2.forValue(interrupt.AuraInterruptFlags[1]));
            setChannelInterruptFlags(SpellAuraInterruptFlag.forValue(interrupt.ChannelInterruptFlags[0]));
            setChannelInterruptFlags2(SpellAuraInterruptFlag2.forValue(interrupt.ChannelInterruptFlags[1]));
        }

        for (var label : data.labels) {
            labels.add(label.labelID);
        }

        // SpellLevelsEntry
        var levels = data.levels;

        if (levels != null) {
            setMaxLevel(levels.maxLevel);
            setBaseLevel(levels.baseLevel);
            setSpellLevel(levels.spellLevel);
        }

        // SpellPowerEntry
        powerCosts = data.powers;

        // SpellReagentsEntry
        var reagents = data.reagents;

        if (reagents != null) {
            for (var i = 0; i < SpellConst.MaxReagents; ++i) {
                Reagent[i] = reagents.Reagent[i];
                ReagentCount[i] = reagents.ReagentCount[i];
            }
        }

        reagentsCurrency = data.reagentsCurrency;

        // SpellShapeshiftEntry
        var shapeshift = data.shapeshift;

        if (shapeshift != null) {
            setStances(MathUtil.MakePair64(shapeshift.ShapeshiftMask[0], shapeshift.ShapeshiftMask[1]));
            setStancesNot(MathUtil.MakePair64(shapeshift.ShapeshiftExclude[0], shapeshift.ShapeshiftExclude[1]));
        }

        // SpellTargetRestrictionsEntry
        var target = data.targetRestrictions;

        if (target != null) {
            setTargets(SpellCastTargetFlag.forValue(target.targets));
            setConeAngle(target.ConeDegrees);
            setWidth(target.width);
            setTargetCreatureType(target.targetCreatureType);
            setMaxAffectedTargets(target.MaxTargets);
            setMaxTargetLevel(target.maxTargetLevel);
        }

        // SpellTotemsEntry
        var totem = data.totems;

        if (totem != null) {
            for (var i = 0; i < 2; ++i) {
                TotemCategory[i] = totem.RequiredTotemCategoryID[i];
                Totem[i] = totem.Totem[i];
            }
        }

        visuals = data.visuals;

        spellSpecific = SpellSpecificType.NORMAL;
        auraState = AuraStateType.NONE;

        setEmpowerStages(data.empowerStages.ToDictionary(a -> a.stage));
    }

    public spellInfo(SpellNameRecord spellName, Difficulty difficulty, ArrayList<SpellEffectRecord> effects) {
        setId(spellName.id);
        setDifficulty(difficulty);
        setSpellName(spellName.name);

        for (var spellEffect : effects) {
            effects.EnsureWritableListIndex(spellEffect.effectIndex, new SpellEffectInfo(this));
            effects.set(spellEffect.effectIndex, new SpellEffectInfo(this, spellEffect));
        }

        // Correct EffectIndex for blank effects
        for (var i = 0; i < effects.size(); ++i) {
            effects.get(i).effectIndex = i;
        }

        setNegativeEffects(new HashSet<Integer>());
    }


    public static int getDispelMask(DispelType type) {
        // If dispel all
        if (type == DispelType.ALL) {
            return (int) DispelType.AllMask.getValue();
        } else {
            return (int) (1 << type.getValue());
        }
    }

    public static SpellCastTargetFlag getTargetFlagMask(SpellTargetObjectTypes objType) {
        switch (objType) {
            case DEST:
                return SpellCastTargetFlag.DEST_LOCATION;
            case UNIT_AND_DEST:
                return SpellCastTargetFlag.DEST_LOCATION.or(SpellCastTargetFlag.UNIT).toElement().orElseThrow();
            case CORPSE_ALLY:
                return SpellCastTargetFlag.CORPSE_ALLY;
            case CORPSE_ENEMY:
                return SpellCastTargetFlag.CORPSE_ENEMY;
            case CORPSE:
                return SpellCastTargetFlag.CORPSE_MASK;
            case UNIT:
                return SpellCastTargetFlag.UNIT;
            case GOBJ:
                return SpellCastTargetFlag.GAME_OBJECT;
            case GOBJ_ITEM:
                return SpellCastTargetFlag.GAME_OBJECT_ITEM;
            case ITEM:
                return SpellCastTargetFlag.ITEM;
            case SRC:
                return SpellCastTargetFlag.SOURCE_LOCATION;
            default:
                return SpellCastTargetFlag.NONE;
        }
    }


    public final int getCategory() {
        return getCategoryId();
    }

    public final ArrayList<SpellEffectInfo> getEffects() {
        return effects;
    }

    public final ArrayList<SpellXSpellVisualRecord> getSpellVisuals() {
        return visuals;
    }


    public final int getId() {
        return id;
    }


    public final void setId(int value) {
        id = value;
    }

    public final Difficulty getDifficulty() {
        return Difficulty;
    }

    public final void setDifficulty(Difficulty value) {
        Difficulty = value;
    }


    public final int getCategoryId() {
        return categoryId;
    }


    public final void setCategoryId(int value) {
        categoryId = value;
    }

    public final DispelType getDispel() {
        return dispel;
    }



    public final void setMechanic(Mechanics value) {
        mechanic = value;
    }

    public final EnumFlag<SpellAttr0> getAttributes() {
        return attributes;
    }

    public final void setAttributes(EnumFlag<SpellAttr0> value) {
        attributes = value;
    }

    public final EnumFlag<SpellAttr1> getAttributesEx() {
        return attributesEx;
    }

    public final void setAttributesEx(EnumFlag<SpellAttr1> value) {
        attributesEx = value;
    }

    public final EnumFlag<SpellAttr2> getAttributesEx2() {
        return attributesEx2;
    }

    public final void setAttributesEx2(EnumFlag<SpellAttr2> value) {
        attributesEx2 = value;
    }

    public final EnumFlag<SpellAttr3> getAttributesEx3() {
        return attributesEx3;
    }

    public final void setAttributesEx3(EnumFlag<SpellAttr3> value) {
        attributesEx3 = value;
    }

    public final EnumFlag<SpellAttr4> getAttributesEx4() {
        return attributesEx4;
    }

    public final void setAttributesEx4(EnumFlag<SpellAttr4> value) {
        attributesEx4 = value;
    }

    public final EnumFlag<SpellAttr5> getAttributesEx5() {
        return attributesEx5;
    }

    public final void setAttributesEx5(EnumFlag<SpellAttr5> value) {
        attributesEx5 = value;
    }

    public final EnumFlag<SpellAttr6> getAttributesEx6() {
        return attributesEx6;
    }

    public final void setAttributesEx6(EnumFlag<SpellAttr6> value) {
        attributesEx6 = value;
    }

    public final EnumFlag<SpellAttr7> getAttributesEx7() {
        return attributesEx7;
    }

    public final void setAttributesEx7(EnumFlag<SpellAttr7> value) {
        attributesEx7 = value;
    }

    public final EnumFlag<SpellAttr8> getAttributesEx8() {
        return attributesEx8;
    }

    public final void setAttributesEx8(EnumFlag<SpellAttr8> value) {
        attributesEx8 = value;
    }

    public final EnumFlag<SpellAttr9> getAttributesEx9() {
        return attributesEx9;
    }

    public final void setAttributesEx9(EnumFlag<SpellAttr9> value) {
        attributesEx9 = value;
    }

    public final EnumFlag<SpellAttr10> getAttributesEx10() {
        return attributesEx10;
    }

    public final void setAttributesEx10(EnumFlag<SpellAttr10> value) {
        attributesEx10 = value;
    }

    public final EnumFlag<SpellAttr11> getAttributesEx11() {
        return attributesEx11;
    }

    public final void setAttributesEx11(EnumFlag<SpellAttr11> value) {
        attributesEx11 = value;
    }

    public final EnumFlag<SpellAttr12> getAttributesEx12() {
        return attributesEx12;
    }

    public final void setAttributesEx12(EnumFlag<SpellAttr12> value) {
        attributesEx12 = value;
    }

    public final EnumFlag<SpellAttr13> getAttributesEx13() {
        return attributesEx13;
    }

    public final void setAttributesEx13(EnumFlag<SpellAttr13> value) {
        attributesEx13 = value;
    }

    public final EnumFlag<SpellAttr14> getAttributesEx14() {
        return attributesEx14;
    }

    public final void setAttributesEx14(EnumFlag<SpellAttr14> value) {
        attributesEx14 = value;
    }

    public final EnumFlag<SpellCustomAttributes> getAttributesCu() {
        return attributesCu;
    }

    public final void setAttributesCu(EnumFlag<SpellCustomAttributes> value) {
        attributesCu = value;
    }

    public final HashSet<Integer> getNegativeEffects() {
        return negativeEffects;
    }

    public final void setNegativeEffects(HashSet<Integer> value) {
        negativeEffects = value;
    }


    public final long getStances() {
        return stances;
    }


    public final void setStances(long value) {
        stances = value;
    }


    public final long getStancesNot() {
        return stancesNot;
    }


    public final void setStancesNot(long value) {
        stancesNot = value;
    }

    public final SpellCastTargetFlag getTargets() {
        return targets;
    }

    public final void setTargets(SpellCastTargetFlag value) {
        targets = value;
    }


    public final int getTargetCreatureType() {
        return targetCreatureType;
    }


    public final void setTargetCreatureType(int value) {
        targetCreatureType = value;
    }


    public final int getRequiresSpellFocus() {
        return requiresSpellFocus;
    }


    public final void setRequiresSpellFocus(int value) {
        requiresSpellFocus = value;
    }


    public final int getFacingCasterFlags() {
        return facingCasterFlags;
    }


    public final void setFacingCasterFlags(int value) {
        facingCasterFlags = value;
    }

    public final AuraStateType getCasterAuraState() {
        return casterAuraState;
    }

    public final void setCasterAuraState(AuraStateType value) {
        casterAuraState = value;
    }

    public final AuraStateType getTargetAuraState() {
        return targetAuraState;
    }

    public final void setTargetAuraState(AuraStateType value) {
        targetAuraState = value;
    }

    public final AuraStateType getExcludeCasterAuraState() {
        return excludeCasterAuraState;
    }

    public final void setExcludeCasterAuraState(AuraStateType value) {
        excludeCasterAuraState = value;
    }

    public final AuraStateType getExcludeTargetAuraState() {
        return excludeTargetAuraState;
    }

    public final void setExcludeTargetAuraState(AuraStateType value) {
        excludeTargetAuraState = value;
    }


    public final int getCasterAuraSpell() {
        return casterAuraSpell;
    }


    public final void setCasterAuraSpell(int value) {
        casterAuraSpell = value;
    }


    public final int getTargetAuraSpell() {
        return targetAuraSpell;
    }


    public final void setTargetAuraSpell(int value) {
        targetAuraSpell = value;
    }


    public final int getExcludeCasterAuraSpell() {
        return excludeCasterAuraSpell;
    }


    public final void setExcludeCasterAuraSpell(int value) {
        excludeCasterAuraSpell = value;
    }


    public final int getExcludeTargetAuraSpell() {
        return excludeTargetAuraSpell;
    }


    public final void setExcludeTargetAuraSpell(int value) {
        excludeTargetAuraSpell = value;
    }

    public final AuraType getCasterAuraType() {
        return casterAuraType;
    }

    public final void setCasterAuraType(AuraType value) {
        casterAuraType = value;
    }

    public final AuraType getTargetAuraType() {
        return targetAuraType;
    }

    public final void setTargetAuraType(AuraType value) {
        targetAuraType = value;
    }

    public final AuraType getExcludeCasterAuraType() {
        return excludeCasterAuraType;
    }

    public final void setExcludeCasterAuraType(AuraType value) {
        excludeCasterAuraType = value;
    }

    public final AuraType getExcludeTargetAuraType() {
        return excludeTargetAuraType;
    }

    public final void setExcludeTargetAuraType(AuraType value) {
        excludeTargetAuraType = value;
    }

    public final SpellCastTimesRecord getCastTimeEntry() {
        return castTimeEntry;
    }

    public final void setCastTimeEntry(SpellCastTimesRecord value) {
        castTimeEntry = value;
    }


    public final int getRecoveryTime() {
        return recoveryTime;
    }


    public final void setRecoveryTime(int value) {
        recoveryTime = value;
    }


    public final int getCategoryRecoveryTime() {
        return categoryRecoveryTime;
    }


    public final void setCategoryRecoveryTime(int value) {
        categoryRecoveryTime = value;
    }


    public final int getStartRecoveryCategory() {
        return startRecoveryCategory;
    }


    public final void setStartRecoveryCategory(int value) {
        startRecoveryCategory = value;
    }


    public final int getStartRecoveryTime() {
        return startRecoveryTime;
    }


    public final void setStartRecoveryTime(int value) {
        startRecoveryTime = value;
    }


    public final int getCooldownAuraSpellId() {
        return cooldownAuraSpellId;
    }


    public final void setCooldownAuraSpellId(int value) {
        cooldownAuraSpellId = value;
    }

    public final SpellInterruptFlag getInterruptFlags() {
        return interruptFlags;
    }

    public final void setInterruptFlags(SpellInterruptFlag value) {
        interruptFlags = value;
    }

    public final SpellAuraInterruptFlag getAuraInterruptFlags() {
        return auraInterruptFlags;
    }

    public final void setAuraInterruptFlags(SpellAuraInterruptFlag value) {
        auraInterruptFlags = value;
    }

    public final SpellAuraInterruptFlag2 getAuraInterruptFlags2() {
        return auraInterruptFlags2;
    }

    public final void setAuraInterruptFlags2(SpellAuraInterruptFlag2 value) {
        auraInterruptFlags2 = value;
    }

    public final SpellAuraInterruptFlag getChannelInterruptFlags() {
        return channelInterruptFlags;
    }

    public final void setChannelInterruptFlags(SpellAuraInterruptFlag value) {
        channelInterruptFlags = value;
    }

    public final SpellAuraInterruptFlag2 getChannelInterruptFlags2() {
        return channelInterruptFlags2;
    }

    public final void setChannelInterruptFlags2(SpellAuraInterruptFlag2 value) {
        channelInterruptFlags2 = value;
    }

    public final ProcFlagsInit getProcFlags() {
        return procFlags;
    }

    public final void setProcFlags(ProcFlagsInit value) {
        procFlags = value;
    }


    public final int getProcChance() {
        return procChance;
    }


    public final void setProcChance(int value) {
        procChance = value;
    }


    public final int getProcCharges() {
        return procCharges;
    }


    public final void setProcCharges(int value) {
        procCharges = value;
    }


    public final int getProcCooldown() {
        return procCooldown;
    }


    public final void setProcCooldown(int value) {
        procCooldown = value;
    }

    public final float getProcBasePpm() {
        return procBasePpm;
    }

    public final void setProcBasePpm(float value) {
        procBasePpm = value;
    }


    public final int getMaxLevel() {
        return maxLevel;
    }


    public final void setMaxLevel(int value) {
        maxLevel = value;
    }


    public final int getBaseLevel() {
        return baseLevel;
    }


    public final void setBaseLevel(int value) {
        baseLevel = value;
    }


    public final int getSpellLevel() {
        return spellLevel;
    }


    public final void setSpellLevel(int value) {
        spellLevel = value;
    }

    public final SpellDuration getDurationEntry() {
        return durationEntry;
    }


    public final FlagArray128 getSpellFamilyFlags() {
        return spellFamilyFlags;
    }

    public final void setSpellFamilyFlags(FlagArray128 value) {
        spellFamilyFlags = value;
    }

    public final SpellDmgClass getDmgClass() {
        return dmgClass;
    }

    public final void setDmgClass(SpellDmgClass value) {
        dmgClass = value;
    }

    public final SpellPreventionType getPreventionType() {
        return preventionType;
    }

    public final void setPreventionType(SpellPreventionType value) {
        preventionType = value;
    }

    public final int getRequiredAreasId() {
        return requiredAreasId;
    }

    public final void setRequiredAreasId(int value) {
        requiredAreasId = value;
    }

    public final SpellSchoolMask getSchoolMask() {
        return schoolMask;
    }

    public final void setSchoolMask(SpellSchoolMask value) {
        schoolMask = value;
    }


    public final HashMap<Byte, SpellEmpowerStageRecord> getEmpowerStages() {
        return empowerStages;
    }


    public final void setEmpowerStages(HashMap<Byte, SpellEmpowerStageRecord> value) {
        empowerStages = value;
    }

    public final SpellCastTargetFlag getExplicitTargetMask() {
        return explicitTargetMask;
    }

    public final SpellChainNode getChainEntry() {
        return chainEntry;
    }

    public final void setChainEntry(SpellChainNode value) {
        chainEntry = value;
    }

    public final boolean isPassiveStackableWithRanks() {
        return isPassive() && !hasEffect(SpellEffectName.ApplyAura);
    }

    public final boolean isMultiSlotAura() {
        return isPassive() || getId() == 55849 || getId() == 40075 || getId() == 44413;
    }

    public final boolean isStackableOnOneSlotWithDifferentCasters() {
        return getStackAmount() > 1 && !isChanneled() && !hasAttribute(SpellAttr3.DotStackingRule);
    }

    public final boolean isDeathPersistent() {
        return hasAttribute(SpellAttr3.AllowAuraWhileDead);
    }

    public final boolean isRequiringDeadTarget() {
        return hasAttribute(SpellAttr3.OnlyOnGhosts);
    }

    public final boolean getCanBeUsedInCombat() {
        return !hasAttribute(SpellAttr0.NotInCombatOnlyPeaceful);
    }

    public final boolean isPositive() {
        return getNegativeEffects().isEmpty();
    }

    public final boolean isChanneled() {
        return hasAttribute(SpellAttr1.IsChannelled.getValue() | SpellAttr1.IsSelfChannelled.getValue());
    }

    public final boolean isMoveAllowedChannel() {
        return isChanneled() && !getChannelInterruptFlags().hasFlag(SpellAuraInterruptFlag.Moving.getValue() | SpellAuraInterruptFlag.Turning.getValue());
    }

    public final boolean getNeedsComboPoints() {
        return hasAttribute(SpellAttr1.FinishingMoveDamage.getValue() | SpellAttr1.FinishingMoveDuration.getValue());
    }

    public final boolean isNextMeleeSwingSpell() {
        return hasAttribute(SpellAttr0.OnNextSwingNoDamage.getValue() | SpellAttr0.OnNextSwing.getValue());
    }

    public final boolean isAutoRepeatRangedSpell() {
        return hasAttribute(SpellAttr2.AutoRepeat);
    }

    public final boolean getHasInitialAggro() {
        return !(hasAttribute(SpellAttr1.NoThreat) || hasAttribute(SpellAttr2.NoInitialThreat) || hasAttribute(SpellAttr4.NoHarmfulThreat));
    }

    public final boolean getHasHitDelay() {
        return getSpeed() > 0.0f || getLaunchDelay() > 0.0f;
    }

    public final boolean isFinishingMove() {
        return attributesEx.hasAnyFlag(SpellAttr1.FINISHING_MOVE_DAMAGE, SpellAttr1.FINISHING_MOVE_DURATION);
    }

    public boolean isAffectedBySpellMods() {
        return !hasAttribute(SpellAttr3.IGNORE_CASTER_MODIFIERS);
    }

    public final boolean getHasAreaAuraEffect() {
        for (var effectInfo : effects) {
            if (effectInfo.isAreaAuraEffect()) {
                return true;
            }
        }

        return false;
    }

    public final boolean getHasOnlyDamageEffects() {
        for (var effectInfo : effects) {
            switch (effectInfo.effect) {
                case WeaponDamage:
                case WeaponDamageNoSchool:
                case NormalizedWeaponDmg:
                case WeaponPercentDamage:
                case SchoolDamage:
                case EnvironmentalDamage:
                case HealthLeech:
                case DamageFromMaxHealthPCT:
                    continue;
                default:
                    return false;
            }
        }

        return true;
    }

    public final boolean isExplicitDiscovery() {
        if (getEffects().size() < 2) {
            return false;
        }

        return ((getEffect(0).effect == SpellEffectName.CreateRandomItem || getEffect(0).effect == SpellEffectName.CreateLoot) && getEffect(1).effect == SpellEffectName.ScriptEffect) || getId() == 64323;
    }

    public final boolean isLootCrafting() {
        return hasEffect(SpellEffectName.CreateRandomItem) || hasEffect(SpellEffectName.CreateLoot);
    }

    public final boolean isProfession() {
        for (var effectInfo : effects) {
            if (effectInfo.isEffect(SpellEffectName.skill)) {
                var skill = (int) effectInfo.miscValue;

                if (global.getSpellMgr().isProfessionSkill(skill)) {
                    return true;
                }
            }
        }

        return false;
    }

    public final boolean isPrimaryProfession() {
        for (var effectInfo : effects) {
            if (effectInfo.isEffect(SpellEffectName.skill) && global.getSpellMgr().isPrimaryProfessionSkill((int) effectInfo.miscValue)) {
                return true;
            }
        }

        return false;
    }

    public final boolean isPrimaryProfessionFirstRank() {
        return isPrimaryProfession() && getRank() == 1;
    }

    public final boolean isAffectingArea() {
        for (var effectInfo : effects) {
            if (effectInfo.isEffect() && (effectInfo.isTargetingArea() || effectInfo.isEffect(SpellEffectName.PersistentAreaAura) || effectInfo.isAreaAuraEffect())) {
                return true;
            }
        }

        return false;
    }

    // checks if spell targets are selected from area, doesn't include spell effects in check (like area wide auras for example)
    public final boolean isTargetingArea() {
        for (var effectInfo : effects) {
            if (effectInfo.isEffect() && effectInfo.isTargetingArea()) {
                return true;
            }
        }

        return false;
    }

    public final boolean getNeedsExplicitUnitTarget() {
        return (boolean) (getExplicitTargetMask().getValue() & SpellCastTargetFlag.UnitMask.getValue());
    }

    public final boolean isPassive() {
        return hasAttribute(SpellAttr0.Passive);
    }

    public final boolean isAutocastable() {
        if (isPassive()) {
            return false;
        }

        if (hasAttribute(SpellAttr1.NoAutocastAi)) {
            return false;
        }

        return true;
    }

    public final boolean isStackableWithRanks() {
        if (isPassive()) {
            return false;
        }

        // All stance spells. if any better way, change it.
        for (var effectInfo : effects) {
            switch (getSpellFamilyName()) {
                case Paladin:
                    // Paladin aura Spell
                    if (effectInfo.effect == SpellEffectName.ApplyAreaAuraRaid) {
                        return false;
                    }

                    break;
                case Druid:
                    // Druid form Spell
                    if (effectInfo.effect == SpellEffectName.ApplyAura && effectInfo.applyAuraName == AuraType.ModShapeshift) {
                        return false;
                    }

                    break;
            }
        }

        return true;
    }

    public final boolean isCooldownStartedOnEvent() {
        if (hasAttribute(SpellAttr0.CooldownOnEvent)) {
            return true;
        }

        var category = CliDB.SpellCategoryStorage.get(getCategoryId());

        return category != null && category.flags.hasFlag(SpellCategoryFlags.CooldownStartsOnEvent);
    }

    public final boolean isAllowingDeadTarget() {
        if (hasAttribute(SpellAttr2.ALLOW_DEAD_TARGET) || getTargets().hasFlag(SpellCastTargetFlag.CorpseAlly.getValue() | SpellCastTargetFlag.CorpseEnemy.getValue().getValue() | SpellCastTargetFlag.UnitDead.getValue().getValue())) {
            return true;
        }

        for (var effectInfo : effects) {
            if (effectInfo.targetA.getObjectType() == SpellTargetObjectTypes.Corpse || effectInfo.targetB.getObjectType() == SpellTargetObjectTypes.Corpse) {
                return true;
            }
        }

        return false;
    }

    public final boolean isGroupBuff() {
        for (var effectInfo : effects) {
            switch (effectInfo.targetA.getCheckType()) {
                case Party:
                case Raid:
                case RaidClass:
                    return true;
            }
        }

        return false;
    }

    public final boolean isRangedWeaponSpell() {
        return (getSpellFamilyName() == SpellFamilyName.Hunter && !getSpellFamilyFlags().get(1).hasFlag(0x10000000)) || (boolean) (getEquippedItemSubClassMask() & ItemSubClassWeapon.MaskRanged.getValue()) || getAttributes().hasFlag(SpellAttr0.UsesRangedSlot);
    }

    public final DiminishingGroup getDiminishingReturnsGroupForSpell() {
        return diminishInfo.diminishGroup;
    }

    public final DiminishingReturnsType getDiminishingReturnsGroupType() {
        return diminishInfo.diminishReturnType;
    }

    public final DiminishingLevels getDiminishingReturnsMaxLevel() {
        return diminishInfo.diminishMaxLevel;
    }

    public final int getDiminishingReturnsLimitDuration() {
        return diminishInfo.diminishDurationLimit;
    }

    public final long getAllowedMechanicMask() {
        return allowedMechanicMask;
    }

    public final int getDuration() {
        if (getDurationEntry() == null) {
            return isPassive() ? -1 : 0;
        }

        return (getDurationEntry().duration == -1) ? -1 : Math.abs(getDurationEntry().duration);
    }

    public final int getMaxDuration() {
        if (getDurationEntry() == null) {
            return isPassive() ? -1 : 0;
        }

        return (getDurationEntry().maxDuration == -1) ? -1 : Math.abs(getDurationEntry().maxDuration);
    }

    public final int getMaxTicks() {
        int totalTicks = 0;
        var DotDuration = getDuration();

        for (var effectInfo : getEffects()) {
            if (!effectInfo.isEffect(SpellEffectName.ApplyAura)) {
                continue;
            }

            switch (effectInfo.applyAuraName) {
                case PeriodicDamage:
                case PeriodicDamagePercent:
                case PeriodicHeal:
                case ObsModHealth:
                case ObsModPower:
                case PeriodicTriggerSpellFromClient:
                case PowerBurn:
                case PeriodicLeech:
                case PeriodicManaLeech:
                case PeriodicEnergize:
                case PeriodicDummy:
                case PeriodicTriggerSpell:
                case PeriodicTriggerSpellWithValue:
                case PeriodicHealthFunnel:
                    // skip infinite periodics
                    if (effectInfo.applyAuraPeriod > 0 && DotDuration > 0) {
                        totalTicks = (int) DotDuration / effectInfo.applyAuraPeriod;

                        if (hasAttribute(SpellAttr5.ExtraInitialPeriod)) {
                            ++totalTicks;
                        }
                    }

                    break;
            }
        }

        return totalTicks;
    }

    public final int getRecoveryTime1() {
        return getRecoveryTime() > getCategoryRecoveryTime() ? getRecoveryTime() : getCategoryRecoveryTime();
    }

    public final boolean isRanked() {
        return getChainEntry() != null;
    }

    public final byte getRank() {
        if (getChainEntry() == null) {
            return 1;
        }

        return getChainEntry().rank;
    }

    public final boolean getHasAnyAuraInterruptFlag() {
        return getAuraInterruptFlags() != SpellAuraInterruptFlag.NONE || getAuraInterruptFlags2() != SpellAuraInterruptFlag2.NONE;
    }

    public final boolean hasEffect(SpellEffectName effect) {
        for (var effectInfo : effects) {
            if (effectInfo.isEffect(effect)) {
                return true;
            }
        }

        return false;
    }

    public final boolean hasAura(AuraType aura) {
        for (var effectInfo : effects) {
            if (effectInfo.isAura(aura)) {
                return true;
            }
        }

        return false;
    }

    public final boolean hasAreaAuraEffect() {
        for (var effectInfo : effects) {
            if (effectInfo.isAreaAuraEffect()) {
                return true;
            }
        }
        return false;
    }

    public final boolean isAbilityOfSkillType(SkillType skillType) {
        var bounds = global.getSpellMgr().getSkillLineAbilityMapBounds(getId());

        for (var spellIdx : bounds) {
            if (spellIdx.skillLine == (int) skillType.getValue()) {
                return true;
            }
        }

        return false;
    }

    public final boolean needsToBeTriggeredByCaster(SpellInfo triggeringSpell) {
        if (getNeedsExplicitUnitTarget()) {
            return true;
        }

        if (triggeringSpell.isChanneled()) {
            SpellCastTargetFlag mask = SpellCastTargetFlag.forValue(0);

            for (var effectInfo : effects) {
                if (effectInfo.targetA.getTarget() != targets.UnitCaster && effectInfo.targetA.getTarget() != targets.DestCaster && effectInfo.targetB.getTarget() != targets.UnitCaster && effectInfo.targetB.getTarget() != targets.DestCaster) {
                    mask = SpellCastTargetFlag.forValue(mask.getValue() | effectInfo.getProvidedTargetMask().getValue());
                }
            }

            if (mask.hasFlag(SpellCastTargetFlag.UnitMask)) {
                return true;
            }
        }

        return false;
    }

    public final boolean isPositiveEffect(int effIndex) {
        return !getNegativeEffects().contains(effIndex);
    }

    public final WeaponAttackType getAttackType() {
        WeaponAttackType result;

        switch (getDmgClass()) {
            case Melee:
                if (hasAttribute(SpellAttr3.RequiresOffHandWeapon)) {
                    result = WeaponAttackType.OffAttack;
                } else {
                    result = WeaponAttackType.BaseAttack;
                }

                break;
            case Ranged:
                result = isRangedWeaponSpell() ? WeaponAttackType.RangedAttack : WeaponAttackType.BaseAttack;

                break;
            default:
                // Wands
                if (isAutoRepeatRangedSpell()) {
                    result = WeaponAttackType.RangedAttack;
                } else {
                    result = WeaponAttackType.BaseAttack;
                }

                break;
        }

        return result;
    }

    public final boolean isItemFitToSpellRequirements(Item item) {
        // item neutral spell
        if (getEquippedItemClass() == itemClass.NONE) {
            return true;
        }

        // item dependent spell
        if (item && item.isFitToSpellRequirements(this)) {
            return true;
        }

        return false;
    }

    public final boolean isAffected(SpellFamilyName familyName, FlagArray128 familyFlags) {
        if (familyName == 0) {
            return true;
        }

        if (familyName != getSpellFamilyName()) {
            return false;
        }

        if (familyFlags && !(familyFlags & getSpellFamilyFlags())) {
            return false;
        }

        return true;
    }

    public final boolean isAffectedBySpellMod(SpellModifier mod) {
        if (!isAffectedBySpellMods()) {
            return false;
        }

        var affectSpell = global.getSpellMgr().getSpellInfo(mod.getSpellId(), getDifficulty());

        if (affectSpell == null) {
            return false;
        }

        switch (mod.getType()) {
            case Flat:
            case Pct:
                // TEMP: dont use IsAffected - !familyName and !familyFlags are not valid options for spell mods
                // TODO: investigate if the !familyName and !familyFlags conditions are even valid for all other (nonmod) uses of SpellInfo::IsAffected
                return affectSpell.getSpellFamilyName() == getSpellFamilyName() && (mod instanceof SpellModifierByClassMask ? (SpellModifierByClassMask) mod : null).mask & getSpellFamilyFlags();
            case LabelFlat:
                return hasLabel((int) (mod instanceof SpellFlatModifierByLabel ? (SpellFlatModifierByLabel) mod : null).value.labelID);
            case LabelPct:
                return hasLabel((int) (mod instanceof SpellPctModifierByLabel ? (SpellPctModifierByLabel) mod : null).value.labelID);
            default:
                break;
        }

        return false;
    }

    public final boolean canPierceImmuneAura(SpellInfo auraSpellInfo) {
        // aura can't be pierced
        if (auraSpellInfo == null || auraSpellInfo.hasAttribute(SpellAttr0.NoImmunities)) {
            return false;
        }

        // these spells pierce all avalible spells (Resurrection Sickness for example)
        if (hasAttribute(SpellAttr0.NoImmunities)) {
            return true;
        }

        // these spells (Cyclone for example) can pierce all...
        if (hasAttribute(SpellAttr1.ImmunityToHostileAndFriendlyEffects) || hasAttribute(SpellAttr2.NoSchoolImmunities)) {
            // ...but not these (Divine shield, Ice block, Cyclone and Banish for example)
            if (auraSpellInfo.getMechanic() != mechanics.ImmuneShield && auraSpellInfo.getMechanic() != mechanics.Invulnerability && (auraSpellInfo.getMechanic() != mechanics.Banish || (isRankOf(auraSpellInfo) && auraSpellInfo.getDispel() != DispelType.NONE))) // Banish shouldn't be immune to itself, but Cyclone should
            {
                return true;
            }
        }

        // Dispels other auras on immunity, check if this spell makes the unit immune to aura
        if (hasAttribute(SpellAttr1.ImmunityPurgesEffect) && canSpellProvideImmunityAgainstAura(auraSpellInfo)) {
            return true;
        }

        return false;
    }

    public final boolean canDispelAura(SpellInfo auraSpellInfo) {
        // These auras (like Divine Shield) can't be dispelled
        if (auraSpellInfo.hasAttribute(SpellAttr0.NoImmunities)) {
            return false;
        }

        // These spells (like Mass dispel) can dispel all auras
        if (hasAttribute(SpellAttr0.NoImmunities)) {
            return true;
        }

        // These auras (Cyclone for example) are not dispelable
        if ((auraSpellInfo.hasAttribute(SpellAttr1.ImmunityToHostileAndFriendlyEffects) && auraSpellInfo.getMechanic() != mechanics.NONE) || auraSpellInfo.hasAttribute(SpellAttr2.NoSchoolImmunities)) {
            return false;
        }

        return true;
    }

    public final boolean isSingleTarget() {
        // all other single target spells have if it has attributesEx5
        if (hasAttribute(SpellAttr5.LimitN)) {
            return true;
        }

        return false;
    }

    public final boolean isAuraExclusiveBySpecificWith(SpellInfo spellInfo) {
        var spellSpec1 = getSpellSpecific();
        var spellSpec2 = spellInfo.getSpellSpecific();

        switch (spellSpec1) {
            case WarlockArmor:
            case MageArmor:
            case ElementalShield:
            case MagePolymorph:
            case Presence:
            case Charm:
            case Scroll:
            case WarriorEnrage:
            case MageArcaneBrillance:
            case PriestDivineSpirit:
                return spellSpec1 == spellSpec2;
            case Food:
                return spellSpec2 == SpellSpecificType.Food || spellSpec2 == SpellSpecificType.FoodAndDrink;
            case Drink:
                return spellSpec2 == SpellSpecificType.Drink || spellSpec2 == SpellSpecificType.FoodAndDrink;
            case FoodAndDrink:
                return spellSpec2 == SpellSpecificType.Food || spellSpec2 == SpellSpecificType.Drink || spellSpec2 == SpellSpecificType.FoodAndDrink;
            default:
                return false;
        }
    }

    public final boolean isAuraExclusiveBySpecificPerCasterWith(SpellInfo spellInfo) {
        var spellSpec = getSpellSpecific();

        switch (spellSpec) {
            case Seal:
            case Hand:
            case Aura:
            case Sting:
            case Curse:
            case Bane:
            case Aspect:
            case WarlockCorruption:
                return spellSpec == spellInfo.getSpellSpecific();
            default:
                return false;
        }
    }

    public final SpellCastResult checkShapeshift(ShapeShiftForm form) {
        // talents that learn spells can have stance requirements that need ignore
        // (this requirement only for client-side stance show in talent description)
		/* TODO: 6.x fix this in proper way (probably spell flags/attributes?)
		if (CliDB.GetTalentSpellCost(id) > 0 && hasEffect(SpellEffects.LearnSpell))
		return SpellCastResult.SpellCastOk;
		*/

        //if (hasAttribute(SPELL_ATTR13_ACTIVATES_REQUIRED_SHAPESHIFT))
        //    return SPELL_CAST_OK;

        var stanceMask = (form != 0 ? 1 << (form.getValue() - 1) : 0);

        if ((boolean) (stanceMask & getStancesNot())) // can explicitly not be casted in this stance
        {
            return SpellCastResult.NotShapeshift;
        }

        if ((boolean) (stanceMask & getStances())) // can explicitly be casted in this stance
        {
            return SpellCastResult.SpellCastOk;
        }

        var actAsShifted = false;
        SpellShapeshiftFormRecord shapeInfo = null;

        if (form.getValue() > 0) {
            shapeInfo = CliDB.SpellShapeshiftFormStorage.get(form);

            if (shapeInfo == null) {
                Log.outError(LogFilter.spells, "GetErrorAtShapeshiftedCast: unknown shapeshift {0}", form);

                return SpellCastResult.SpellCastOk;
            }

            actAsShifted = !shapeInfo.flags.hasFlag(SpellShapeshiftFormFlags.Stance);
        }

        if (actAsShifted) {
            if (hasAttribute(SpellAttr0.NotShapeshifted) || (shapeInfo != null && shapeInfo.flags.hasFlag(SpellShapeshiftFormFlags.CanOnlyCastShapeshiftSpells))) // not while shapeshifted
            {
                return SpellCastResult.NotShapeshift;
            } else if (getStances() != 0) // needs other shapeshift
            {
                return SpellCastResult.OnlyShapeshift;
            }
        } else {
            // needs shapeshift
            if (!hasAttribute(SpellAttr2.AllowWhileNotShapeshiftedCasterForm) && getStances() != 0) {
                return SpellCastResult.OnlyShapeshift;
            }
        }

        return SpellCastResult.SpellCastOk;
    }

    public final SpellCastResult checkLocation(int map_id, int zone_id, int area_id, Player player) {
        // normal case
        if (getRequiredAreasId() > 0) {
            var found = false;
            var areaGroupMembers = global.getDB2Mgr().GetAreasForGroup((int) getRequiredAreasId());

            for (var areaId : areaGroupMembers) {
                if (areaId == zone_id || areaId == area_id) {
                    found = true;

                    break;
                }
            }

            if (!found) {
                return SpellCastResult.IncorrectArea;
            }
        }

        // continent limitation (virtual continent)
        if (hasAttribute(SpellAttr4.OnlyFlyingAreas)) {
            int mountFlags = 0;

            if (player && player.hasAuraType(AuraType.MountRestrictions)) {
                for (var auraEffect : player.getAuraEffectsByType(AuraType.MountRestrictions)) {
                    mountFlags |= (int) auraEffect.getMiscValue();
                }
            } else {
                var areaTable = CliDB.AreaTableStorage.get(area_id);

                if (areaTable != null) {
                    mountFlags = areaTable.MountFlags;
                }
            }

            if (!(boolean) (mountFlags & (int) AreaMountFlags.FlyingAllowed.getValue())) {
                return SpellCastResult.IncorrectArea;
            }

            if (player) {
                var mapToCheck = map_id;
                var mapEntry1 = CliDB.MapStorage.get(map_id);

                if (mapEntry1 != null) {
                    mapToCheck = (int) mapEntry1.CosmeticParentMapID;
                }

                if ((mapToCheck == 1116 || mapToCheck == 1464) && !player.hasSpell(191645)) // Draenor Pathfinder
                {
                    return SpellCastResult.IncorrectArea;
                } else if (mapToCheck == 1220 && !player.hasSpell(233368)) // Broken Isles Pathfinder
                {
                    return SpellCastResult.IncorrectArea;
                } else if ((mapToCheck == 1642 || mapToCheck == 1643) && !player.hasSpell(278833)) // Battle for Azeroth Pathfinder
                {
                    return SpellCastResult.IncorrectArea;
                }
            }
        }

        var mapEntry = CliDB.MapStorage.get(map_id);

        // raid instance limitation
        if (hasAttribute(SpellAttr6.NotInRaidInstances)) {
            if (mapEntry == null || mapEntry.isRaid()) {
                return SpellCastResult.NotInRaidInstance;
            }
        }

        // DB base check (if non empty then must fit at least single for allow)
        var saBounds = global.getSpellMgr().getSpellAreaMapBounds(getId());

        if (!saBounds.isEmpty()) {
            for (var bound : saBounds) {
                if (bound.isFitToRequirements(player, zone_id, area_id)) {
                    return SpellCastResult.SpellCastOk;
                }
            }

            return SpellCastResult.IncorrectArea;
        }

        // bg spell checks
        switch (getId()) {
            case 23333: // Warsong Flag
            case 23335: // Silverwing Flag
                return map_id == 489 && player != null && player.getInBattleground() ? SpellCastResult.SpellCastOk : SpellCastResult.RequiresArea;
            case 34976: // Netherstorm Flag
                return map_id == 566 && player != null && player.getInBattleground() ? SpellCastResult.SpellCastOk : SpellCastResult.RequiresArea;
            case 2584: // Waiting to Resurrect
            case 22011: // Spirit Heal Channel
            case 22012: // Spirit Heal
            case 42792: // Recently Dropped Flag
            case 43681: // Inactive
            case 44535: // Spirit Heal (mana)
                if (mapEntry == null) {
                    return SpellCastResult.IncorrectArea;
                }

                return zone_id == (int) areaId.wintergrasp.getValue() || (mapEntry.isBattleground() && player != null && player.getInBattleground()) ? SpellCastResult.SpellCastOk : SpellCastResult.RequiresArea;
            case 44521: // Preparation
            {
                if (player == null) {
                    return SpellCastResult.RequiresArea;
                }

                if (mapEntry == null) {
                    return SpellCastResult.IncorrectArea;
                }

                if (!mapEntry.isBattleground()) {
                    return SpellCastResult.RequiresArea;
                }

                var bg = player.getBattleground();

                return bg && bg.getStatus() == BattlegroundStatus.WaitJoin ? SpellCastResult.SpellCastOk : SpellCastResult.RequiresArea;
            }
            case 32724: // Gold team (Alliance)
            case 32725: // Green team (Alliance)
            case 35774: // Gold team (Horde)
            case 35775: // Green team (Horde)
                if (mapEntry == null) {
                    return SpellCastResult.IncorrectArea;
                }

                return mapEntry.IsBattleArena() && player != null && player.getInBattleground() ? SpellCastResult.SpellCastOk : SpellCastResult.RequiresArea;
            case 32727: // Arena Preparation
            {
                if (player == null) {
                    return SpellCastResult.RequiresArea;
                }

                if (mapEntry == null) {
                    return SpellCastResult.IncorrectArea;
                }

                if (!mapEntry.IsBattleArena()) {
                    return SpellCastResult.RequiresArea;
                }

                var bg = player.getBattleground();

                return bg && bg.getStatus() == BattlegroundStatus.WaitJoin ? SpellCastResult.SpellCastOk : SpellCastResult.RequiresArea;
            }
        }

        // aura limitations
        if (player) {
            for (var effectInfo : effects) {
                if (!effectInfo.isAura()) {
                    continue;
                }

                switch (effectInfo.applyAuraName) {
                    case ModShapeshift: {
                        var spellShapeshiftForm = CliDB.SpellShapeshiftFormStorage.get(effectInfo.miscValue);

                        if (spellShapeshiftForm != null) {
                            int mountType = spellShapeshiftForm.MountTypeID;

                            if (mountType != 0) {
                                if (player.getMountCapability(mountType) == null) {
                                    return SpellCastResult.NotHere;
                                }
                            }
                        }

                        break;
                    }
                    case Mounted: {
                        var mountType = (int) effectInfo.miscValueB;
                        var mountEntry = global.getDB2Mgr().GetMount(getId());

                        if (mountEntry != null) {
                            mountType = mountEntry.MountTypeID;
                        }

                        if (mountType != 0 && player.getMountCapability(mountType) == null) {
                            return SpellCastResult.NotHere;
                        }

                        break;
                    }
                }
            }
        }

        return SpellCastResult.SpellCastOk;
    }

    public final SpellCastResult checkTarget(WorldObject caster, WorldObject target) {
        return checkTarget(caster, target, true);
    }

    public final SpellCastResult checkTarget(WorldObject caster, WorldObject target, boolean implicit) {
        if (hasAttribute(SpellAttr1.ExcludeCaster) && caster == target) {
            return SpellCastResult.BadTargets;
        }

        // check visibility - ignore stealth for implicit (area) targets
        if (!hasAttribute(SpellAttr6.IgnorePhaseShift) && !caster.canSeeOrDetect(target, implicit)) {
            return SpellCastResult.BadTargets;
        }

        var unitTarget = target.toUnit();

        // creature/player specific target checks
        if (unitTarget != null) {
            // spells cannot be cast if target has a pet in combat either
            if (hasAttribute(SpellAttr1.OnlyPeacefulTargets) && (unitTarget.isInCombat() || unitTarget.hasUnitFlag(UnitFlag.PET_IN_COMBAT))) {
                return SpellCastResult.TargetAffectingCombat;
            }

            // only spells with SPELL_ATTR3_ONLY_TARGET_GHOSTS can target ghosts
            if (hasAttribute(SpellAttr3.OnlyOnGhosts) != unitTarget.hasAuraType(AuraType.Ghost)) {
                if (hasAttribute(SpellAttr3.OnlyOnGhosts)) {
                    return SpellCastResult.TargetNotGhost;
                } else {
                    return SpellCastResult.BadTargets;
                }
            }

            if (caster != unitTarget) {
                if (caster.isTypeId(TypeId.PLAYER)) {
                    // Do not allow these spells to target creatures not tapped by us (Banish, Polymorph, many quest spells)
                    if (hasAttribute(SpellAttr2.CannotCastOnTapped)) {
                        var targetCreature = unitTarget.toCreature();

                        if (targetCreature != null) {
                            if (targetCreature.getHasLootRecipient() && !targetCreature.isTappedBy(caster.toPlayer())) {
                                return SpellCastResult.CantCastOnTapped;
                            }
                        }
                    }

                    if (hasAttribute(SpellCustomAttributes.PickPocket)) {
                        var targetCreature = unitTarget.toCreature();

                        if (targetCreature == null) {
                            return SpellCastResult.BadTargets;
                        }

                        if (!targetCreature.getCanHaveLoot() || !loots.LootStorage.PICKPOCKETING.haveLootFor(targetCreature.getCreatureTemplate().pickPocketId)) {
                            return SpellCastResult.TargetNoPockets;
                        }
                    }

                    // Not allow disarm unarmed player
                    if (getMechanic() == mechanics.disarm) {
                        if (unitTarget.isTypeId(TypeId.PLAYER)) {
                            var player = unitTarget.toPlayer();

                            if (player.getWeaponForAttack(WeaponAttackType.BaseAttack) == null || !player.isUseEquipedWeapon(true)) {
                                return SpellCastResult.TargetNoWeapons;
                            }
                        } else if (unitTarget.getVirtualItemId(0) == 0) {
                            return SpellCastResult.TargetNoWeapons;
                        }
                    }
                }
            }
        }
        // corpse specific target checks
        else if (target.isTypeId(TypeId.Corpse)) {
            var corpseTarget = target.toCorpse();

            // cannot target bare bones
            if (corpseTarget.getCorpseType() == CorpseType.Bones) {
                return SpellCastResult.BadTargets;
            }

            // we have to use owner for some checks (aura preventing resurrection for example)
            var owner = global.getObjAccessor().findPlayer(corpseTarget.getOwnerGUID());

            if (owner != null) {
                unitTarget = owner;
            }
            // we're not interested in corpses without owner
            else {
                return SpellCastResult.BadTargets;
            }
        }
        // other types of objects - always valid
        else {
            return SpellCastResult.SpellCastOk;
        }

        // corpseOwner and unit specific target checks
        if (!unitTarget.isPlayer()) {
            if (hasAttribute(SpellAttr3.OnlyOnPlayer)) {
                return SpellCastResult.TargetNotPlayer;
            }

            if (hasAttribute(SpellAttr5.NotOnPlayerControlledNpc) && unitTarget.isControlledByPlayer()) {
                return SpellCastResult.TargetIsPlayerControlled;
            }
        } else if (hasAttribute(SpellAttr5.NotOnPlayer)) {
            return SpellCastResult.TargetIsPlayer;
        }

        if (!isAllowingDeadTarget() && !unitTarget.isAlive()) {
            return SpellCastResult.TargetsDead;
        }

        // check this flag only for implicit targets (chain and area), allow to explicitly target units for spells like Shield of Righteousness
        if (implicit && hasAttribute(SpellAttr6.DoNotChainToCrowdControlledTargets) && !unitTarget.canFreeMove()) {
            return SpellCastResult.BadTargets;
        }

        if (!checkTargetCreatureType(unitTarget)) {
            if (target.isTypeId(TypeId.PLAYER)) {
                return SpellCastResult.TargetIsPlayer;
            } else {
                return SpellCastResult.BadTargets;
            }
        }

        // check GM mode and GM invisibility - only for player casts (npc casts are controlled by AI) and negative spells
        if (unitTarget != caster && (caster.getAffectingPlayer() != null || !isPositive()) && unitTarget.isTypeId(TypeId.PLAYER)) {
            if (!unitTarget.toPlayer().isVisible()) {
                return SpellCastResult.BmOrInvisgod;
            }

            if (unitTarget.toPlayer().isGameMaster()) {
                return SpellCastResult.BmOrInvisgod;
            }
        }

        // not allow casting on flying player
        if (unitTarget.hasUnitState(UnitState.InFlight) && !hasAttribute(SpellCustomAttributes.AllowInflightTarget)) {
            return SpellCastResult.BadTargets;
        }

		/* TARGET_UNIT_MASTER gets blocked here for passengers, because the whole idea of this check is to
		not allow passengers to be implicitly hit by spells, however this target type should be an exception,
		if this is left it kills spells that award kill credit from vehicle to master (few spells),
		the use of these 2 covers passenger target check, logically, if vehicle cast this to master it should always hit
		him, because it would be it's passenger, there's no such case where this gets to fail legitimacy, this problem
		cannot be solved from within the check in other way since target type cannot be called for the spell currently
		Spell examples: [ID - 52864 Devour Water, ID - 52862 Devour Wind, ID - 49370 Wyrmrest Defender: Destabilize Azure Dragonshrine Effect] */
        var unitCaster = caster.toUnit();

        if (unitCaster != null) {
            if (!unitCaster.isVehicle() && unitCaster.getCharmerOrOwner() != target) {
                if (getTargetAuraState() != 0 && !unitTarget.hasAuraState(getTargetAuraState(), this, unitCaster)) {
                    return SpellCastResult.TargetAurastate;
                }

                if (getExcludeTargetAuraState() != 0 && unitTarget.hasAuraState(getExcludeTargetAuraState(), this, unitCaster)) {
                    return SpellCastResult.TargetAurastate;
                }
            }
        }

        if (getTargetAuraSpell() != 0 && !unitTarget.hasAura(getTargetAuraSpell())) {
            return SpellCastResult.TargetAurastate;
        }

        if (getExcludeTargetAuraSpell() != 0 && unitTarget.hasAura(getExcludeTargetAuraSpell())) {
            return SpellCastResult.TargetAurastate;
        }

        if (unitTarget.hasAuraType(AuraType.PreventResurrection) && !hasAttribute(SpellAttr7.BypassNoResurrectAura)) {
            if (hasEffect(SpellEffectName.SelfResurrect) || hasEffect(SpellEffectName.Resurrect)) {
                return SpellCastResult.TargetCannotBeResurrected;
            }
        }

        if (hasAttribute(SpellAttr8.BattleResurrection)) {
            var map = caster.getMap();

            if (map) {
                var iMap = map.toInstanceMap();

                if (iMap) {
                    var instance = iMap.getInstanceScript();

                    if (instance != null) {
                        if (instance.getCombatResurrectionCharges() == 0 && instance.isEncounterInProgress()) {
                            return SpellCastResult.TargetCannotBeResurrected;
                        }
                    }
                }
            }
        }

        return SpellCastResult.SpellCastOk;
    }

    public final SpellCastResult checkExplicitTarget(WorldObject caster, WorldObject target) {
        return checkExplicitTarget(caster, target, null);
    }

    public final SpellCastResult checkExplicitTarget(WorldObject caster, WorldObject target, Item itemTarget) {
        var neededTargets = getExplicitTargetMask();

        if (target == null) {
            if ((boolean) (neededTargets.getValue() & (SpellCastTargetFlag.UnitMask.getValue() | SpellCastTargetFlag.GameobjectMask.getValue().getValue() | SpellCastTargetFlag.CorpseMask.getValue().getValue()).getValue())) {
                if (!(boolean) (neededTargets.getValue() & SpellCastTargetFlag.GameobjectItem.getValue()) || itemTarget == null) {
                    return SpellCastResult.BadTargets;
                }
            }

            return SpellCastResult.SpellCastOk;
        }

        var unitTarget = target.toUnit();

        if (unitTarget != null) {
            if (neededTargets.hasFlag(SpellCastTargetFlag.UnitEnemy.getValue() | SpellCastTargetFlag.UnitAlly.getValue().getValue() | SpellCastTargetFlag.UnitRaid.getValue().getValue().getValue() | SpellCastTargetFlag.UnitParty.getValue().getValue().getValue().getValue() | SpellCastTargetFlag.UnitMinipet.getValue().getValue().getValue().getValue().getValue() | SpellCastTargetFlag.UnitPassenger.getValue().getValue().getValue().getValue().getValue())) {
                var unitCaster = caster.toUnit();

                if (neededTargets.hasFlag(SpellCastTargetFlag.UnitEnemy)) {
                    if (caster.isValidAttackTarget(unitTarget, this)) {
                        return SpellCastResult.SpellCastOk;
                    }
                }

                if (neededTargets.hasFlag(SpellCastTargetFlag.UnitAlly) || (neededTargets.hasFlag(SpellCastTargetFlag.UnitParty) && unitCaster != null && unitCaster.isInPartyWith(unitTarget)) || (neededTargets.hasFlag(SpellCastTargetFlag.UnitRaid) && unitCaster != null && unitCaster.isInRaidWith(unitTarget))) {
                    if (caster.isValidAssistTarget(unitTarget, this)) {
                        return SpellCastResult.SpellCastOk;
                    }
                }

                if (neededTargets.hasFlag(SpellCastTargetFlag.UnitMinipet) && unitCaster != null) {
                    if (Objects.equals(unitTarget.getGUID(), unitCaster.getCritterGUID())) {
                        return SpellCastResult.SpellCastOk;
                    }
                }

                if (neededTargets.hasFlag(SpellCastTargetFlag.UnitPassenger) && unitCaster != null) {
                    if (unitTarget.isOnVehicle(unitCaster)) {
                        return SpellCastResult.SpellCastOk;
                    }
                }

                return SpellCastResult.BadTargets;
            }
        }

        return SpellCastResult.SpellCastOk;
    }

    public final SpellCastResult checkVehicle(Unit caster) {
        // All creatures should be able to cast as passengers freely, restriction and attribute are only for players
        if (!caster.isTypeId(TypeId.PLAYER)) {
            return SpellCastResult.SpellCastOk;
        }

        var vehicle = caster.getVehicle1();

        if (vehicle) {
            VehicleSeatFlags checkMask = VehicleSeatFlags.forValue(0);

            for (var effectInfo : effects) {
                if (effectInfo.isAura(AuraType.ModShapeshift)) {
                    var shapeShiftFromEntry = CliDB.SpellShapeshiftFormStorage.get((int) effectInfo.miscValue);

                    if (shapeShiftFromEntry != null && !shapeShiftFromEntry.flags.hasFlag(SpellShapeshiftFormFlags.Stance)) {
                        checkMask = VehicleSeatFlags.forValue(checkMask.getValue() | VehicleSeatFlags.Uncontrolled.getValue());
                    }

                    break;
                }
            }

            if (hasAura(AuraType.Mounted)) {
                checkMask = VehicleSeatFlags.forValue(checkMask.getValue() | VehicleSeatFlags.CanCastMountSpell.getValue());
            }

            if (checkMask == 0) {
                checkMask = VehicleSeatFlags.CanAttack;
            }

            var vehicleSeat = vehicle.GetSeatForPassenger(caster);

            if (!hasAttribute(SpellAttr6.AllowWhileRidingVehicle) && !hasAttribute(SpellAttr0.AllowWhileMounted) && (vehicleSeat.flags & checkMask.getValue()) != checkMask.getValue()) {
                return SpellCastResult.CantDoThatRightNow;
            }

            // Can only summon uncontrolled minions/guardians when on controlled vehicle
            if (vehicleSeat.hasFlag(VehicleSeatFlags.CanControl.getValue() | VehicleSeatFlags.unk2.getValue())) {
                for (var effectInfo : effects) {
                    if (!effectInfo.isEffect(SpellEffectName.summon)) {
                        continue;
                    }

                    var props = CliDB.SummonPropertiesStorage.get(effectInfo.miscValueB);

                    if (props != null && props.Control != SummonCategory.Wild) {
                        return SpellCastResult.CantDoThatRightNow;
                    }
                }
            }
        }

        return SpellCastResult.SpellCastOk;
    }

    public final boolean checkTargetCreatureType(Unit target) {
        // Curse of Doom & Exorcism: not find another way to fix spell target check :/
        if (getSpellFamilyName() == SpellFamilyName.Warlock && getCategory() == 1179) {
            // not allow cast at player
            if (target.isTypeId(TypeId.PLAYER)) {
                return false;
            } else {
                return true;
            }
        }

        // if target is magnet (i.e Grounding totem) the check is skipped
        if (target.isMagnet()) {
            return true;
        }


        var creatureType = target.getCreatureTypeMask();

        return getTargetCreatureType() == 0 || creatureType == 0 || (boolean) (creatureType & getTargetCreatureType());
    }

    public final long getAllEffectsMechanicMask() {
        long mask = 0;

        if (getMechanic() != 0) {
            mask |= 1 << getMechanic().getValue();
        }

        for (var effectInfo : effects) {
            if (effectInfo.isEffect() && effectInfo.mechanic != 0) {
                mask |= 1 << effectInfo.mechanic.getValue();
            }
        }

        return mask;
    }

    public final long getEffectMechanicMask(int effIndex) {
        long mask = 0;

        if (getMechanic() != 0) {
            mask |= 1 << getMechanic().getValue();
        }

        if (getEffect(effIndex).isEffect() && getEffect(effIndex).mechanic != 0) {
            mask |= 1 << getEffect(effIndex).mechanic.getValue();
        }

        return mask;
    }

    public final long getSpellMechanicMaskByEffectMask(int effectMask) {
        long mask = 0;

        if (getMechanic() != Mechanics.NONE) {
            mask |= 1L << getMechanic().ordinal();
        }

        for (var effectInfo : effects) {
            if ((effectMask & (1 << effectInfo.effectIndex.ordinal())) != 0 && effectInfo.mechanic != Mechanics.NONE) {
                mask |= 1L << effectInfo.mechanic.ordinal();
            }
        }

        return mask;
    }

    public final Mechanics getEffectMechanic(int effIndex) {
        if (getEffect(effIndex).isEffect() && getEffect(effIndex).mechanic != 0) {
            return getEffect(effIndex).mechanic;
        }

        if (getMechanic() != 0) {
            return getMechanic();
        }

        return mechanics.NONE;
    }

    public final int getDispelMask() {
        return getDispelMask(getDispel());
    }

    public final SpellCastTargetFlag getExplicitTargetMask() {
        return getExplicitTargetMask();
    }

    public final void setExplicitTargetMask(SpellCastTargetFlag value) {
        explicitTargetMask = value;
    }

    public final AuraStateType getAuraState() {
        return auraState;
    }

    public final void _LoadAuraState() {
        auraState = AuraStateType.NONE;

        // Faerie Fire
        if (getCategory() == 1133) {
            auraState = AuraStateType.FaerieFire;
        }

        // Swiftmend state on regrowth, rejuvenation, Wild Growth
        if (getSpellFamilyName() == SpellFamilyName.Druid && (getSpellFamilyFlags().get(0).hasFlag(0x50) || getSpellFamilyFlags().get(1).hasFlag(0x4000000))) {
            auraState = AuraStateType.DruidPeriodicHeal;
        }

        // Deadly poison aura state
        if (getSpellFamilyName() == SpellFamilyName.Rogue && getSpellFamilyFlags().get(0).hasFlag(0x10000)) {
            auraState = AuraStateType.RoguePoisoned;
        }

        // Enrage aura state
        if (getDispel() == DispelType.Enrage) {
            auraState = AuraStateType.Enraged;
        }

        // Bleeding aura state
        if ((boolean) (getAllEffectsMechanicMask() & (1 << mechanics.Bleed.getValue()))) {
            auraState = AuraStateType.Bleed;
        }

        if ((boolean) (getSchoolMask().getValue() & spellSchoolMask.Frost.getValue())) {
            for (var effectInfo : effects) {
                if (effectInfo.isAura(AuraType.ModStun) || effectInfo.isAura(AuraType.ModRoot) || effectInfo.isAura(AuraType.ModRoot2)) {
                    auraState = AuraStateType.Frozen;
                }
            }
        }

        switch (getId()) {
            case 1064: // Dazed
                auraState = AuraStateType.Dazed;

                break;
            case 32216: // Victorious
                auraState = AuraStateType.Victorious;

                break;
            case 71465: // Divine Surge
            case 50241: // Evasive Charges
                auraState = AuraStateType.RaidEncounter;

                break;
            case 6950: // Faerie Fire
            case 9806: // Phantom Strike
            case 9991: // Touch of Zanzil
            case 13424: // Faerie Fire
            case 13752: // Faerie Fire
            case 16432: // Plague Mist
            case 20656: // Faerie Fire
            case 25602: // Faerie Fire
            case 32129: // Faerie Fire
            case 35325: // Glowing Blood
            case 35328: // Lambent Blood
            case 35329: // Vibrant Blood
            case 35331: // Black Blood
            case 49163: // Perpetual Instability
            case 65863: // Faerie Fire
            case 79559: // Luxscale Light
            case 82855: // Dazzling
            case 102953: // In the Rumpus
            case 127907: // Phosphorescence
            case 127913: // Phosphorescence
            case 129007: // Zijin Sting
            case 130159: // Fae Touch
            case 142537: // Spotter Smoke
            case 168455: // Spotted!
            case 176905: // Super Sticky Glitter Bomb
            case 189502: // Marked
            case 201785: // Intruder Alert!
            case 201786: // Intruder Alert!
            case 201935: // Spotted!
            case 239233: // Smoke Bomb
            case 319400: // Glitter Burst
            case 321470: // Dimensional Shifter Mishap
            case 331134: // Spotted
                auraState = AuraStateType.FaerieFire;

                break;
            default:
                break;
        }
    }

    public final SpellSpecificType getSpellSpecific() {
        return spellSpecific;
    }

    public final void _LoadSpellSpecific() {
        spellSpecific = SpellSpecificType.NORMAL;

        switch (getSpellFamilyName()) {
            case Generic: {
                // Food / Drinks (mostly)
                if (hasAuraInterruptFlag(SpellAuraInterruptFlag.standing)) {
                    var food = false;
                    var drink = false;

                    for (var effectInfo : effects) {
                        if (!effectInfo.isAura()) {
                            continue;
                        }

                        switch (effectInfo.applyAuraName) {
                            // Food
                            case ModRegen:
                            case ObsModHealth:
                                food = true;

                                break;
                            // Drink
                            case ModPowerRegen:
                            case ObsModPower:
                                drink = true;

                                break;
                            default:
                                break;
                        }
                    }

                    if (food && drink) {
                        spellSpecific = SpellSpecificType.FoodAndDrink;
                    } else if (food) {
                        spellSpecific = SpellSpecificType.Food;
                    } else if (drink) {
                        spellSpecific = SpellSpecificType.Drink;
                    }
                }
                // scrolls effects
                else {
                    var firstRankSpellInfo = getFirstRankSpell();

                    switch (firstRankSpellInfo.getId()) {
                        case 8118: // Strength
                        case 8099: // Stamina
                        case 8112: // Spirit
                        case 8096: // Intellect
                        case 8115: // Agility
                        case 8091: // Armor
                            spellSpecific = SpellSpecificType.Scroll;

                            break;
                    }
                }

                break;
            }
            case Mage: {
                // family flags 18(Molten), 25(Frost/Ice), 28(Mage)
                if (getSpellFamilyFlags().get(0).hasFlag(0x12040000)) {
                    spellSpecific = SpellSpecificType.MageArmor;
                }

                // Arcane brillance and Arcane intelect (normal check fails because of flags difference)
                if (getSpellFamilyFlags().get(0).hasFlag(0x400)) {
                    spellSpecific = SpellSpecificType.MageArcaneBrillance;
                }

                if (getSpellFamilyFlags().get(0).hasFlag(0x1000000) && getEffect(0).isAura(AuraType.ModConfuse)) {
                    spellSpecific = SpellSpecificType.MagePolymorph;
                }

                break;
            }
            case Warrior: {
                if (getId() == 12292) // Death Wish
                {
                    spellSpecific = SpellSpecificType.WarriorEnrage;
                }

                break;
            }
            case Warlock: {
                // Warlock (Bane of Doom | Bane of Agony | Bane of Havoc)
                if (getId() == 603 || getId() == 980 || getId() == 80240) {
                    spellSpecific = SpellSpecificType.Bane;
                }

                // only warlock curses have this
                if (getDispel() == DispelType.Curse) {
                    spellSpecific = SpellSpecificType.Curse;
                }

                // Warlock (Demon armor | Demon Skin | Fel armor)
                if (getSpellFamilyFlags().get(1).hasFlag(0x20000020) || getSpellFamilyFlags().get(2).hasFlag(0x00000010)) {
                    spellSpecific = SpellSpecificType.WarlockArmor;
                }

                //seed of corruption and corruption
                if (getSpellFamilyFlags().get(1).hasFlag(0x10) || getSpellFamilyFlags().get(0).hasFlag(0x2)) {
                    spellSpecific = SpellSpecificType.WarlockCorruption;
                }

                break;
            }
            case Priest: {
                // Divine Spirit and Prayer of Spirit
                if (getSpellFamilyFlags().get(0).hasFlag(0x20)) {
                    spellSpecific = SpellSpecificType.PriestDivineSpirit;
                }

                break;
            }
            case Hunter: {
                // only hunter stings have this
                if (getDispel() == DispelType.Poison) {
                    spellSpecific = SpellSpecificType.Sting;
                }

                // only hunter aspects have this (but not all aspects in hunter family)
                if (getSpellFamilyFlags() & new flagArray128(0x00200000, 0x00000000, 0x00001010, 0x00000000)) {
                    spellSpecific = SpellSpecificType.Aspect;
                }

                break;
            }
            case Paladin: {
                // Collection of all the seal family flags. No other paladin spell has any of those.
                if (getSpellFamilyFlags().get(1).hasFlag(0xA2000800)) {
                    spellSpecific = SpellSpecificType.Seal;
                }

                if (getSpellFamilyFlags().get(0).hasFlag(0x00002190)) {
                    spellSpecific = SpellSpecificType.Hand;
                }

                // only paladin auras have this (for palaldin class family)
                switch (getId()) {
                    case 465: // Devotion Aura
                    case 32223: // Crusader Aura
                    case 183435: // Retribution Aura
                    case 317920: // Concentration Aura
                        spellSpecific = SpellSpecificType.aura;

                        break;
                    default:
                        break;
                }

                break;
            }
            case Shaman: {
                // family flags 10 (Lightning), 42 (Earth), 37 (Water), proc shield from T2 8 pieces bonus
                if (getSpellFamilyFlags().get(1).hasFlag(0x420) || getSpellFamilyFlags().get(0).hasFlag(0x00000400) || getId() == 23552) {
                    spellSpecific = SpellSpecificType.ElementalShield;
                }

                break;
            }
            case Deathknight:
                if (getId() == 48266 || getId() == 48263 || getId() == 48265) {
                    spellSpecific = SpellSpecificType.Presence;
                }

                break;
        }

        for (var effectInfo : effects) {
            if (effectInfo.isEffect(SpellEffectName.ApplyAura)) {
                switch (effectInfo.applyAuraName) {
                    case ModCharm:
                    case ModPossessPet:
                    case ModPossess:
                    case AoeCharm:
                        spellSpecific = SpellSpecificType.charm;

                        break;
                    case TrackCreatures:
                        // @workaround For non-stacking tracking spells (We need generic solution)
                        if (getId() == 30645) // Gas Cloud Tracking
                        {
                            spellSpecific = SpellSpecificType.NORMAL;
                        }

                        break;
                    case TrackResources:
                    case TrackStealthed:
                        spellSpecific = SpellSpecificType.Tracker;

                        break;
                }
            }
        }
    }

    public final void _LoadSpellDiminishInfo() {
        SpellDiminishInfo diminishInfo = new SpellDiminishInfo();
        diminishInfo.diminishGroup = diminishingGroupCompute();
        diminishInfo.diminishReturnType = diminishingTypeCompute(diminishInfo.diminishGroup);
        diminishInfo.diminishMaxLevel = diminishingMaxLevelCompute(diminishInfo.diminishGroup);
        diminishInfo.diminishDurationLimit = diminishingLimitDurationCompute();

        diminishInfo = diminishInfo;
    }

    public final void _LoadImmunityInfo() {
        for (var effect : effects) {
            int schoolImmunityMask = 0;
            int applyHarmfulAuraImmunityMask = 0;
            long mechanicImmunityMask = 0;
            int dispelImmunity = 0;
            int damageImmunityMask = 0;

            var miscVal = effect.miscValue;
            var amount = effect.calcValue();

            var immuneInfo = effect.getImmunityInfo();

            switch (effect.applyAuraName) {
                case MechanicImmunityMask: {
                    switch (miscVal) {
                        case 96: // Free Friend, Uncontrollable Frenzy, Warlord's Presence
                        {
                            mechanicImmunityMask |= (long) mechanics.ImmuneToMovementImpairmentAndLossControlMask.getValue();

                            immuneInfo.auraTypeImmune.add(AuraType.ModStun);
                            immuneInfo.auraTypeImmune.add(AuraType.ModDecreaseSpeed);
                            immuneInfo.auraTypeImmune.add(AuraType.ModRoot);
                            immuneInfo.auraTypeImmune.add(AuraType.ModConfuse);
                            immuneInfo.auraTypeImmune.add(AuraType.ModFear);
                            immuneInfo.auraTypeImmune.add(AuraType.ModRoot2);

                            break;
                        }
                        case 1615: // Incite Rage, Wolf Spirit, Overload, Lightning Tendrils
                        {
                            switch (getId()) {
                                case 43292: // Incite Rage
                                case 49172: // Wolf Spirit
                                    mechanicImmunityMask |= (long) mechanics.ImmuneToMovementImpairmentAndLossControlMask.getValue();

                                    immuneInfo.auraTypeImmune.add(AuraType.ModStun);
                                    immuneInfo.auraTypeImmune.add(AuraType.ModDecreaseSpeed);
                                    immuneInfo.auraTypeImmune.add(AuraType.ModRoot);
                                    immuneInfo.auraTypeImmune.add(AuraType.ModConfuse);
                                    immuneInfo.auraTypeImmune.add(AuraType.ModFear);
                                    immuneInfo.auraTypeImmune.add(AuraType.ModRoot2);

									goto case 61869
                                    ;
                                    // no break intended
                                case 61869: // Overload
                                case 63481:
                                case 61887: // Lightning Tendrils
                                case 63486:
                                    mechanicImmunityMask |= (1 << mechanics.Interrupt.getValue()) | (1 << mechanics.Silence.getValue());

                                    immuneInfo.spellEffectImmune.add(SpellEffectName.knockBack);
                                    immuneInfo.spellEffectImmune.add(SpellEffectName.KnockBackDest);

                                    break;
                                default:
                                    break;
                            }

                            break;
                        }
                        case 679: // Mind Control, Avenging Fury
                        {
                            if (getId() == 57742) // Avenging Fury
                            {
                                mechanicImmunityMask |= (long) mechanics.ImmuneToMovementImpairmentAndLossControlMask.getValue();

                                immuneInfo.auraTypeImmune.add(AuraType.ModStun);
                                immuneInfo.auraTypeImmune.add(AuraType.ModDecreaseSpeed);
                                immuneInfo.auraTypeImmune.add(AuraType.ModRoot);
                                immuneInfo.auraTypeImmune.add(AuraType.ModConfuse);
                                immuneInfo.auraTypeImmune.add(AuraType.ModFear);
                                immuneInfo.auraTypeImmune.add(AuraType.ModRoot2);
                            }

                            break;
                        }
                        case 1557: // Startling Roar, Warlord Roar, Break Bonds, Stormshield
                        {
                            if (getId() == 64187) // Stormshield
                            {
                                mechanicImmunityMask |= 1 << mechanics.Stun.getValue();
                                immuneInfo.auraTypeImmune.add(AuraType.ModStun);
                            } else {
                                mechanicImmunityMask |= (long) mechanics.ImmuneToMovementImpairmentAndLossControlMask.getValue();

                                immuneInfo.auraTypeImmune.add(AuraType.ModStun);
                                immuneInfo.auraTypeImmune.add(AuraType.ModDecreaseSpeed);
                                immuneInfo.auraTypeImmune.add(AuraType.ModRoot);
                                immuneInfo.auraTypeImmune.add(AuraType.ModConfuse);
                                immuneInfo.auraTypeImmune.add(AuraType.ModFear);
                                immuneInfo.auraTypeImmune.add(AuraType.ModRoot2);
                            }

                            break;
                        }
                        case 1614: // Fixate
                        case 1694: // Fixated, Lightning Tendrils
                        {
                            immuneInfo.spellEffectImmune.add(SpellEffectName.AttackMe);
                            immuneInfo.auraTypeImmune.add(AuraType.ModTaunt);

                            break;
                        }
                        case 1630: // Fervor, Berserk
                        {
                            if (getId() == 64112) // Berserk
                            {
                                immuneInfo.spellEffectImmune.add(SpellEffectName.AttackMe);
                                immuneInfo.auraTypeImmune.add(AuraType.ModTaunt);
                            } else {
                                mechanicImmunityMask |= (long) mechanics.ImmuneToMovementImpairmentAndLossControlMask.getValue();

                                immuneInfo.auraTypeImmune.add(AuraType.ModStun);
                                immuneInfo.auraTypeImmune.add(AuraType.ModDecreaseSpeed);
                                immuneInfo.auraTypeImmune.add(AuraType.ModRoot);
                                immuneInfo.auraTypeImmune.add(AuraType.ModConfuse);
                                immuneInfo.auraTypeImmune.add(AuraType.ModFear);
                                immuneInfo.auraTypeImmune.add(AuraType.ModRoot2);
                            }

                            break;
                        }
                        case 477: // Bladestorm
                        case 1733: // bladestorm, Killing Spree
                        {
                            if (amount == 0) {
                                mechanicImmunityMask |= (long) mechanics.ImmuneToMovementImpairmentAndLossControlMask.getValue();

                                immuneInfo.spellEffectImmune.add(SpellEffectName.knockBack);
                                immuneInfo.spellEffectImmune.add(SpellEffectName.KnockBackDest);

                                immuneInfo.auraTypeImmune.add(AuraType.ModStun);
                                immuneInfo.auraTypeImmune.add(AuraType.ModDecreaseSpeed);
                                immuneInfo.auraTypeImmune.add(AuraType.ModRoot);
                                immuneInfo.auraTypeImmune.add(AuraType.ModConfuse);
                                immuneInfo.auraTypeImmune.add(AuraType.ModFear);
                                immuneInfo.auraTypeImmune.add(AuraType.ModRoot2);
                            }

                            break;
                        }
                        case 878: // Whirlwind, Fog of corruption, Determination
                        {
                            if (getId() == 66092) // Determination
                            {
                                mechanicImmunityMask |= (1 << mechanics.Snare.getValue()) | (1 << mechanics.Stun.getValue()) | (1 << mechanics.Disoriented.getValue()) | (1 << mechanics.Freeze.getValue());

                                immuneInfo.auraTypeImmune.add(AuraType.ModStun);
                                immuneInfo.auraTypeImmune.add(AuraType.ModDecreaseSpeed);
                            }

                            break;
                        }
                        default:
                            break;
                    }

                    if (immuneInfo.auraTypeImmune.isEmpty()) {
                        if (miscVal.hasFlag(1 << 10)) {
                            immuneInfo.auraTypeImmune.add(AuraType.ModStun);
                        }

                        if (miscVal.hasFlag(1 << 1)) {
                            immuneInfo.auraTypeImmune.add(AuraType.Transform);
                        }

                        // These flag can be recognized wrong:
                        if (miscVal.hasFlag(1 << 6)) {
                            immuneInfo.auraTypeImmune.add(AuraType.ModDecreaseSpeed);
                        }

                        if (miscVal.hasFlag(1 << 0)) {
                            immuneInfo.auraTypeImmune.add(AuraType.ModRoot);
                            immuneInfo.auraTypeImmune.add(AuraType.ModRoot2);
                        }

                        if (miscVal.hasFlag(1 << 2)) {
                            immuneInfo.auraTypeImmune.add(AuraType.ModConfuse);
                        }

                        if (miscVal.hasFlag(1 << 9)) {
                            immuneInfo.auraTypeImmune.add(AuraType.ModFear);
                        }

                        if (miscVal.hasFlag(1 << 7)) {
                            immuneInfo.auraTypeImmune.add(AuraType.ModDisarm);
                        }
                    }

                    break;
                }
                case MechanicImmunity: {
                    switch (getId()) {
                        case 42292: // PvP trinket
                        case 59752: // Every Man for Himself
                            mechanicImmunityMask |= (long) mechanics.ImmuneToMovementImpairmentAndLossControlMask.getValue();
                            immuneInfo.auraTypeImmune.add(AuraType.UseNormalMovementSpeed);

                            break;
                        case 34471: // The Beast Within
                        case 19574: // Bestial Wrath
                        case 46227: // Medallion of Immunity
                        case 53490: // Bullheaded
                        case 65547: // PvP Trinket
                        case 134946: // Supremacy of the Alliance
                        case 134956: // Supremacy of the Horde
                        case 195710: // Honorable Medallion
                        case 208683: // Gladiator's Medallion
                            mechanicImmunityMask |= (long) mechanics.ImmuneToMovementImpairmentAndLossControlMask.getValue();

                            break;
                        case 54508: // Demonic Empowerment
                            mechanicImmunityMask |= (1 << mechanics.Snare.getValue()) | (1 << mechanics.Root.getValue()) | (1 << mechanics.Stun.getValue());

                            break;
                        default:
                            if (miscVal < 1) {
                                break;
                            }

                            mechanicImmunityMask |= 1 << miscVal;

                            break;
                    }

                    break;
                }
                case EffectImmunity: {
                    immuneInfo.spellEffectImmune.add(SpellEffectName.forValue(miscVal));

                    break;
                }
                case StateImmunity: {
                    immuneInfo.auraTypeImmune.add(AuraType.forValue(miscVal));

                    break;
                }
                case SchoolImmunity: {
                    schoolImmunityMask |= (int) miscVal;

                    break;
                }
                case ModImmuneAuraApplySchool: {
                    applyHarmfulAuraImmunityMask |= (int) miscVal;

                    break;
                }
                case DamageImmunity: {
                    damageImmunityMask |= (int) miscVal;

                    break;
                }
                case DispelImmunity: {
                    dispelImmunity = (int) miscVal;

                    break;
                }
                default:
                    break;
            }

            immuneInfo.schoolImmuneMask = schoolImmunityMask;
            immuneInfo.applyHarmfulAuraImmuneMask = applyHarmfulAuraImmunityMask;
            immuneInfo.mechanicImmuneMask = mechanicImmunityMask;
            immuneInfo.dispelImmune = dispelImmunity;
            immuneInfo.damageSchoolMask = damageImmunityMask;

            allowedMechanicMask |= immuneInfo.mechanicImmuneMask;
        }

        if (hasAttribute(SpellAttr5.AllowWhileStunned)) {
            switch (getId()) {
                case 22812: // Barkskin
                case 47585: // Dispersion
                    allowedMechanicMask |= (1 << mechanics.Stun.getValue()) | (1 << mechanics.Freeze.getValue()) | (1 << mechanics.Knockout.getValue()) | (1 << mechanics.Sleep.getValue());

                    break;
                case 49039: // Lichborne, don't allow normal stuns
                    break;
                default:
                    allowedMechanicMask |= (1 << mechanics.Stun.getValue());

                    break;
            }
        }

        if (hasAttribute(SpellAttr5.AllowWhileConfused)) {
            allowedMechanicMask |= (1 << mechanics.Disoriented.getValue());
        }

        if (hasAttribute(SpellAttr5.AllowWhileFleeing)) {
            switch (getId()) {
                case 22812: // Barkskin
                case 47585: // Dispersion
                    allowedMechanicMask |= (1 << mechanics.fear.getValue()) | (1 << mechanics.Horror.getValue());

                    break;
                default:
                    allowedMechanicMask |= (1 << mechanics.fear.getValue());

                    break;
            }
        }
    }

    public final void applyAllSpellImmunitiesTo(Unit target, SpellEffectInfo spellEffectInfo, boolean apply) {
        var immuneInfo = spellEffectInfo.getImmunityInfo();

        var schoolImmunity = immuneInfo.schoolImmuneMask;

        if (schoolImmunity != 0) {
            target.applySpellImmune(getId(), SpellImmunity.school, schoolImmunity, apply);

            if (apply && hasAttribute(SpellAttr1.ImmunityPurgesEffect)) {
                target.removeAppliedAuras(aurApp ->
                {
                    var auraSpellInfo = aurApp.base.spellInfo;

                    return (((int) auraSpellInfo.getSchoolMask() & schoolImmunity) != 0 && canDispelAura(auraSpellInfo) && (isPositive() != aurApp.IsPositive) && !auraSpellInfo.isPassive && auraSpellInfo.id != getId()); // Don't remove self
                });
            }

            if (apply && (schoolImmunity & (int) spellSchoolMask.NORMAL.getValue()) != 0) {
                target.removeAurasWithInterruptFlags(SpellAuraInterruptFlag.InvulnerabilityBuff);
            }
        }

        var mechanicImmunity = immuneInfo.mechanicImmuneMask;

        if (mechanicImmunity != 0) {
            for (int i = 0; i < mechanics.max.getValue(); ++i) {
                if ((boolean) (mechanicImmunity & (1 << (int) i))) {
                    target.applySpellImmune(getId(), SpellImmunity.mechanic, i, apply);
                }
            }

            if (hasAttribute(SpellAttr1.ImmunityPurgesEffect)) {
                // exception for purely snare mechanic (eg. hands of freedom)!
                if (apply) {
                    target.removeAurasWithMechanic(mechanicImmunity, AuraRemoveMode.Default, getId());
                } else {
                    ArrayList<aura> aurasToUpdateTargets = new ArrayList<>();

                    target.removeAppliedAuras(aurApp ->
                    {
                        var aura = aurApp.base;

                        if ((aura.spellInfo.getAllEffectsMechanicMask() & mechanicImmunity) != 0) {
                            aurasToUpdateTargets.add(aura);
                        }

                        // only update targets, don't remove anything
                        return false;
                    });

                    for (var aura : aurasToUpdateTargets) {
                        aura.updateTargetMap(aura.getCaster());
                    }
                }
            }
        }

        var dispelImmunity = immuneInfo.dispelImmune;

        if (dispelImmunity != 0) {
            target.applySpellImmune(getId(), SpellImmunity.dispel, dispelImmunity, apply);

            if (apply && hasAttribute(SpellAttr1.ImmunityPurgesEffect)) {
                target.removeAppliedAuras(aurApp ->
                {
                    var spellInfo = aurApp.base.spellInfo;

                    if ((int) spellInfo.dispel == dispelImmunity) {
                        return true;
                    }

                    return false;
                });
            }
        }

        var damageImmunity = immuneInfo.damageSchoolMask;

        if (damageImmunity != 0) {
            target.applySpellImmune(getId(), SpellImmunity.damage, damageImmunity, apply);

            if (apply && (damageImmunity & (int) spellSchoolMask.NORMAL.getValue()) != 0) {
                target.removeAurasWithInterruptFlags(SpellAuraInterruptFlag.InvulnerabilityBuff);
            }
        }

        for (var auraType : immuneInfo.auraTypeImmune) {
            target.applySpellImmune(getId(), SpellImmunity.state, auraType, apply);

            if (apply && hasAttribute(SpellAttr1.ImmunityPurgesEffect)) {
                target.removeAurasByType(auraType, aurApp ->
                {
                    // if the aura has SPELL_ATTR0_NO_IMMUNITIES, then it cannot be removed by immunity
                    return !aurApp.base.spellInfo.hasAttribute(SpellAttr0.NoImmunities);
                });
            }
        }

        for (var effectType : immuneInfo.spellEffectImmune) {
            target.applySpellImmune(getId(), SpellImmunity.effect, effectType, apply);
        }
    }

    public final boolean spellCancelsAuraEffect(AuraEffect aurEff) {
        if (!hasAttribute(SpellAttr1.ImmunityPurgesEffect)) {
            return false;
        }

        if (aurEff.getSpellInfo().hasAttribute(SpellAttr0.NoImmunities)) {
            return false;
        }

        for (var effectInfo : getEffects()) {
            if (!effectInfo.isEffect(SpellEffectName.ApplyAura)) {
                continue;
            }

            var miscValue = (int) effectInfo.miscValue;

            switch (effectInfo.applyAuraName) {
                case StateImmunity:
                    if (miscValue != (int) aurEff.getAuraType().getValue()) {
                        continue;
                    }

                    break;
                case SchoolImmunity:
                case ModImmuneAuraApplySchool:
                    if (aurEff.getSpellInfo().hasAttribute(SpellAttr2.NoSchoolImmunities) || !(boolean) ((int) aurEff.getSpellInfo().getSchoolMask().getValue() & miscValue)) {
                        continue;
                    }

                    break;
                case DispelImmunity:
                    if (miscValue != (int) aurEff.getSpellInfo().getDispel().getValue()) {
                        continue;
                    }

                    break;
                case MechanicImmunity:
                    if (miscValue != (int) aurEff.getSpellInfo().getMechanic().getValue()) {
                        if (miscValue != (int) aurEff.getSpellEffectInfo().mechanic.getValue()) {
                            continue;
                        }
                    }

                    break;
                default:
                    continue;
            }

            return true;
        }

        return false;
    }


    public final long getMechanicImmunityMask(Unit caster) {
        var casterMechanicImmunityMask = caster.getMechanicImmunityMask();
        long mechanicImmunityMask = 0;

        if (canBeInterrupted(null, caster, true)) {
            if ((casterMechanicImmunityMask & (1 << mechanics.Silence.getValue())) != 0) {
                mechanicImmunityMask |= (1 << mechanics.Silence.getValue());
            }

            if ((casterMechanicImmunityMask & (1 << mechanics.Interrupt.getValue())) != 0) {
                mechanicImmunityMask |= (1 << mechanics.Interrupt.getValue());
            }
        }

        return mechanicImmunityMask;
    }


    public final float getMinRange() {
        return getMinRange(false);
    }

    public final float getMinRange(boolean positive) {
        if (getRangeEntry() == null) {
            return 0.0f;
        }

        return getRangeEntry().RangeMin[positive ? 1 : 0];
    }


    public final float getMaxRange(boolean positive, WorldObject caster) {
        return getMaxRange(positive, caster, null);
    }

    public final float getMaxRange(boolean positive) {
        return getMaxRange(positive, null, null);
    }

    public final float getMaxRange() {
        return getMaxRange(false, null, null);
    }

    public final float getMaxRange(boolean positive, WorldObject caster, Spell spell) {
        if (getRangeEntry() == null) {
            return 0.0f;
        }

        var range = getRangeEntry().RangeMax[positive ? 1 : 0];

        if (caster != null) {
            var modOwner = caster.getSpellModOwner();

            if (modOwner != null) {
                tangible.RefObject<Float> tempRef_range = new tangible.RefObject<Float>(range);
                modOwner.applySpellMod(this, SpellModOp.Range, tempRef_range, spell);
                range = tempRef_range.refArgValue;
            }
        }

        return range;
    }


    public final int calcDuration() {
        return calcDuration(null);
    }

    public final int calcDuration(WorldObject caster) {
        var duration = getDuration();

        if (caster) {
            var modOwner = caster.getSpellModOwner();

            if (modOwner) {
                tangible.RefObject<Integer> tempRef_duration = new tangible.RefObject<Integer>(duration);
                modOwner.applySpellMod(this, SpellModOp.duration, tempRef_duration);
                duration = tempRef_duration.refArgValue;
            }
        }

        return duration;
    }


    public final int calcCastTime() {
        return calcCastTime(null);
    }

    public final int calcCastTime(Spell spell) {
        var castTime = 0;

        if (getCastTimeEntry() != null) {
            castTime = Math.max(getCastTimeEntry().base, getCastTimeEntry().Minimum);
        }

        if (castTime <= 0) {
            return 0;
        }

        if (spell != null) {
            tangible.RefObject<Integer> tempRef_castTime = new tangible.RefObject<Integer>(castTime);
            spell.getCaster().modSpellCastTime(this, tempRef_castTime, spell);
            castTime = tempRef_castTime.refArgValue;
        }

        if (hasAttribute(SpellAttr0.UsesRangedSlot) && (!isAutoRepeatRangedSpell()) && !hasAttribute(SpellAttr9.aimedShot)) {
            castTime += 500;
        }

        return (castTime > 0) ? castTime : 0;
    }


    public final SpellPowerCost calcPowerCost(Power powerType, boolean optionalCost, WorldObject caster, SpellSchoolMask schoolMask) {
        return calcPowerCost(powerType, optionalCost, caster, schoolMask, null);
    }

    public final SpellPowerCost calcPowerCost(Power powerType, boolean optionalCost, WorldObject caster, SpellSchoolMask schoolMask, Spell spell) {
        // gameobject casts don't use power
        var unitCaster = caster.toUnit();

        if (unitCaster == null) {
            return null;
        }

        var spellPowerRecord = powerCosts.FirstOrDefault(spellPowerEntry ->
        {
            if (spellPowerEntry != null) {
                spellPowerEntry.powerType;
            }
        } == powerType);

        if (spellPowerRecord == null) {
            return null;
        }

        return calcPowerCost(spellPowerRecord, optionalCost, caster, schoolMask, spell);
    }


    public final SpellPowerCost calcPowerCost(SpellPowerRecord power, boolean optionalCost, WorldObject caster, SpellSchoolMask schoolMask) {
        return calcPowerCost(power, optionalCost, caster, schoolMask, null);
    }

    public final SpellPowerCost calcPowerCost(SpellPowerRecord power, boolean optionalCost, WorldObject caster, SpellSchoolMask schoolMask, Spell spell) {
        // gameobject casts don't use power
        var unitCaster = caster.toUnit();

        if (!unitCaster) {
            return null;
        }

        if (power.RequiredAuraSpellID != 0 && !unitCaster.hasAura(power.RequiredAuraSpellID)) {
            return null;
        }

        SpellPowerCost cost = new SpellPowerCost();

        // Spell drain all exist power on cast (Only paladin lay of Hands)
        if (hasAttribute(SpellAttr1.UseAllMana)) {
            // If power type - health drain all
            if (power.powerType == powerType.health) {
                cost.power = powerType.health;
                cost.amount = (int) unitCaster.getHealth();

                return cost;
            }

            // Else drain all power
            if (power.powerType.getValue() < powerType.max.getValue()) {
                cost.power = power.powerType;
                cost.amount = unitCaster.getPower(cost.power);

                return cost;
            }

            Log.outError(LogFilter.spells, String.format("SpellInfo.CalcPowerCost: Unknown power type '%1$s' in spell %2$s", power.powerType, getId()));

            return null;
        }

        // Base powerCost
        double powerCost = 0;

        if (!optionalCost) {
            powerCost = power.ManaCost;

            // PCT cost from total amount
            if (power.PowerCostPct != 0) {
                switch (power.powerType) {
                    // health as power used
                    case Health:
                        if (MathUtil.fuzzyEq(power.PowerCostPct, 0.0f)) {
                            powerCost += (int) MathUtil.CalculatePct(unitCaster.getMaxHealth(), power.PowerCostMaxPct);
                        } else {
                            powerCost += (int) MathUtil.CalculatePct(unitCaster.getMaxHealth(), power.PowerCostPct);
                        }

                        break;
                    case Mana:
                        powerCost += (int) MathUtil.CalculatePct(unitCaster.getCreateMana(), power.PowerCostPct);

                        break;
                    case AlternatePower:
                        Log.outError(LogFilter.spells, String.format("SpellInfo.CalcPowerCost: Unknown power type '%1$s' in spell %2$s", power.powerType, getId()));

                        return null;
                    default: {
                        var powerTypeEntry = global.getDB2Mgr().GetPowerTypeEntry(power.powerType);

                        if (powerTypeEntry != null) {
                            powerCost += MathUtil.CalculatePct(powerTypeEntry.MaxBasePower, power.PowerCostPct);

                            break;
                        }

                        Log.outError(LogFilter.spells, String.format("SpellInfo.CalcPowerCost: Unknown power type '%1$s' in spell %2$s", power.powerType, getId()));

                        return null;
                    }
                }
            }
        } else {
            powerCost = power.OptionalCost;

            if (power.OptionalCostPct != 0) {
                switch (power.powerType) {
                    // health as power used
                    case Health:
                        powerCost += (int) MathUtil.CalculatePct(unitCaster.getMaxHealth(), power.OptionalCostPct);

                        break;
                    case Mana:
                        powerCost += (int) MathUtil.CalculatePct(unitCaster.getCreateMana(), power.OptionalCostPct);

                        break;
                    case AlternatePower:
                        Log.outError(LogFilter.spells, String.format("SpellInfo::CalcPowerCost: Unsupported power type POWER_ALTERNATE_POWER in spell %1$s for optional cost percent", getId()));

                        return null;
                    default: {
                        var powerTypeEntry = global.getDB2Mgr().GetPowerTypeEntry(power.powerType);

                        if (powerTypeEntry != null) {
                            powerCost += (int) MathUtil.CalculatePct(powerTypeEntry.MaxBasePower, power.OptionalCostPct);

                            break;
                        }

                        Log.outError(LogFilter.spells, String.format("SpellInfo::CalcPowerCost: Unknown power type '%1$s' in spell %2$s for optional cost percent", power.powerType, getId()));

                        return null;
                    }
                }
            }

            powerCost += unitCaster.getTotalAuraModifier(AuraType.ModAdditionalPowerCost, aurEff ->
            {
                return aurEff.miscValue == power.powerType.getValue() && aurEff.isAffectingSpell(this);
            });
        }

        var initiallyNegative = powerCost < 0;

        // Shiv - costs 20 + weaponSpeed*10 energy (apply only to non-triggered spell with energy cost)
        if (hasAttribute(SpellAttr4.WeaponSpeedCostScaling)) {
            int speed = 0;
            var ss = CliDB.SpellShapeshiftFormStorage.get(unitCaster.getShapeshiftForm());

            if (ss != null) {
                speed = ss.CombatRoundTime;
            } else {
                var slot = WeaponAttackType.BaseAttack;

                if (!hasAttribute(SpellAttr3.RequiresMainHandWeapon) && hasAttribute(SpellAttr3.RequiresOffHandWeapon)) {
                    slot = WeaponAttackType.OffAttack;
                }

                speed = unitCaster.getBaseAttackTime(slot);
            }

            powerCost += speed / 100;
        }

        if (power.powerType != powerType.health) {
            if (!optionalCost) {
                // Flat mod from caster auras by spell school and power type
                for (var aura : unitCaster.getAuraEffectsByType(AuraType.ModPowerCostSchool)) {
                    if ((aura.getMiscValue() & schoolMask.getValue()) == 0) {
                        continue;
                    }

                    if ((aura.getMiscValueB() & (1 << power.powerType.getValue())) == 0) {
                        continue;
                    }

                    powerCost += aura.getAmount();
                }
            }

            // PCT mod from user auras by spell school and power type
            for (var schoolCostPct : unitCaster.getAuraEffectsByType(AuraType.ModPowerCostSchoolPct)) {
                if ((schoolCostPct.getMiscValue() & schoolMask.getValue()) == 0) {
                    continue;
                }

                if ((schoolCostPct.getMiscValueB() & (1 << power.powerType.getValue())) == 0) {
                    continue;
                }

                powerCost += MathUtil.CalculatePct(powerCost, schoolCostPct.getAmount());
            }
        }

        // Apply cost mod by spell
        var modOwner = unitCaster.getSpellModOwner();

        if (modOwner != null) {
            var mod = SpellModOp.max;

            switch (power.orderIndex) {
                case 0:
                    mod = SpellModOp.PowerCost0;

                    break;
                case 1:
                    mod = SpellModOp.PowerCost1;

                    break;
                case 2:
                    mod = SpellModOp.PowerCost2;

                    break;
                default:
                    break;
            }

            if (mod != SpellModOp.max) {
                if (!optionalCost) {
                    tangible.RefObject<Double> tempRef_powerCost = new tangible.RefObject<Double>(powerCost);
                    modOwner.applySpellMod(this, mod, tempRef_powerCost, spell);
                    powerCost = tempRef_powerCost.refArgValue;
                } else {
                    // optional cost ignores flat modifiers
                    double flatMod = 0;
                    double pctMod = 1.0f;
                    tangible.RefObject<Double> tempRef_flatMod = new tangible.RefObject<Double>(flatMod);
                    tangible.RefObject<Double> tempRef_pctMod = new tangible.RefObject<Double>(pctMod);
                    modOwner.getSpellModValues(this, mod, spell, powerCost, tempRef_flatMod, tempRef_pctMod);
                    pctMod = tempRef_pctMod.refArgValue;
                    flatMod = tempRef_flatMod.refArgValue;
                    powerCost = (powerCost * pctMod);
                }
            }
        }

        if (!unitCaster.isControlledByPlayer() && MathUtil.fuzzyEq(power.PowerCostPct, 0.0f) && getSpellLevel() != 0 && power.powerType == powerType.mana) {
            if (hasAttribute(SpellAttr0.ScalesWithCreatureLevel)) {
                var spellScaler = CliDB.NpcManaCostScalerGameTable.GetRow(getSpellLevel());
                var casterScaler = CliDB.NpcManaCostScalerGameTable.GetRow(unitCaster.getLevel());

                if (spellScaler != null && casterScaler != null) {
                    powerCost *= (int) (casterScaler.Scaler / spellScaler.Scaler);
                }
            }
        }

        if (power.powerType == powerType.mana) {
            powerCost = (int) ((double) powerCost * (1.0f + unitCaster.getUnitData().manaCostMultiplier));
        }

        // power cost cannot become negative if initially positive
        if (initiallyNegative != (powerCost < 0)) {
            powerCost = 0;
        }

        cost.power = power.powerType;
        cost.amount = (int) powerCost;

        return cost;
    }


    public final ArrayList<SpellPowerCost> calcPowerCost(WorldObject caster, SpellSchoolMask schoolMask) {
        return calcPowerCost(caster, schoolMask, null);
    }

    public final ArrayList<SpellPowerCost> calcPowerCost(WorldObject caster, SpellSchoolMask schoolMask, Spell spell) {
        ArrayList<SpellPowerCost> costs = new ArrayList<>();

        if (caster.isUnit()) {

//			SpellPowerCost getOrCreatePowerCost(Power powerType)
//				{
//					var itr = costs.find(cost => cost.power == powerType);
//
//					if (itr != null)
//						return itr;
//
//					SpellPowerCost cost = new();
//					cost.power = powerType;
//					cost.amount = 0;
//					costs.add(cost);
//
//					return costs.last();
//				}

            for (var power : powerCosts) {
                if (power == null) {
                    continue;
                }

                var cost = calcPowerCost(power, false, caster, schoolMask, spell);

                if (cost != null) {
                    getOrCreatePowerCost(cost.power).amount += cost.amount;
                }

                var optionalCost = calcPowerCost(power, true, caster, schoolMask, spell);

                if (optionalCost != null) {
                    var cost1 = getOrCreatePowerCost(optionalCost.power);
                    var remainingPower = caster.toUnit().getPower(optionalCost.power) - cost1.amount;

                    if (remainingPower > 0) {
                        cost1.amount += Math.min(optionalCost.amount, remainingPower);
                    }
                }
            }
        }

        return costs;
    }

    public final double calcProcPPM(Unit caster, int itemLevel) {
        double ppm = getProcBasePpm();

        if (!caster) {
            return ppm;
        }

        for (var mod : procPpmMods) {
            switch (mod.type) {
                case Haste: {
                    ppm *= 1.0f + calcPPMHasteMod(mod, caster);

                    break;
                }
                case Crit: {
                    ppm *= 1.0f + calcPPMCritMod(mod, caster);

                    break;
                }
                case Class: {
                    if (caster.getClassMask().hasFlag((int) mod.param)) {
                        ppm *= 1.0f + mod.Coeff;
                    }

                    break;
                }
                case Spec: {
                    var plrCaster = caster.toPlayer();

                    if (plrCaster) {
                        if (plrCaster.getPrimarySpecialization() == mod.param) {
                            ppm *= 1.0f + mod.Coeff;
                        }
                    }

                    break;
                }
                case Race: {
                    if (SharedConst.GetMaskForRace(caster.getRace()).hasFlag((int) mod.param)) {
                        ppm *= 1.0f + mod.Coeff;
                    }

                    break;
                }
                case ItemLevel: {
                    ppm *= 1.0f + calcPPMItemLevelMod(mod, itemLevel);

                    break;
                }
                case Battleground: {
                    if (caster.getMap().isBattlegroundOrArena()) {
                        ppm *= 1.0f + mod.Coeff;
                    }

                    break;
                }
                default:
                    break;
            }
        }

        return ppm;
    }

    public final SpellInfo getFirstRankSpell() {
        if (getChainEntry() == null) {
            return this;
        }

        return getChainEntry().first;
    }

    public final SpellInfo getNextRankSpell() {
        if (getChainEntry() == null) {
            return null;
        }

        return getChainEntry().next;
    }


    public final SpellInfo getAuraRankForLevel(int level) {
        // ignore passive spells
        if (isPassive()) {
            return this;
        }

        // Client ignores spell with these attributes (sub_53D9D0)
        if (hasAttribute(SpellAttr0.AuraIsDebuff) || hasAttribute(SpellAttr2.AllowLowLevelBuff) || hasAttribute(SpellAttr3.OnlyProcOnCaster)) {
            return this;
        }

        var needRankSelection = false;

        for (var effectInfo : getEffects()) {
            if (isPositiveEffect(effectInfo.effectIndex) && (effectInfo.isEffect(SpellEffectName.ApplyAura) || effectInfo.isEffect(SpellEffectName.ApplyAreaAuraParty) || effectInfo.isEffect(SpellEffectName.ApplyAreaAuraRaid)) && effectInfo.scaling.coefficient != 0) {
                needRankSelection = true;

                break;
            }
        }

        // not required
        if (!needRankSelection) {
            return this;
        }

        for (var nextSpellInfo = this; nextSpellInfo != null; nextSpellInfo = nextSpellInfo.getPrevRankSpell()) {
            // if found appropriate level
            if ((level + 10) >= nextSpellInfo.getSpellLevel()) {
                return nextSpellInfo;
            }
        }

        // one rank less then
        // not found
        return null;
    }

    public final boolean isRankOf(SpellInfo spellInfo) {
        return getFirstRankSpell() == spellInfo.getFirstRankSpell();
    }

    public final boolean isDifferentRankOf(SpellInfo spellInfo) {
        if (getId() == spellInfo.getId()) {
            return false;
        }

        return isRankOf(spellInfo);
    }

    public final boolean isHighRankOf(SpellInfo spellInfo) {
        if (getChainEntry() != null && spellInfo.getChainEntry() != null) {
            if (getChainEntry().first == spellInfo.getChainEntry().first) {
                if (getChainEntry().rank > spellInfo.getChainEntry().rank) {
                    return true;
                }
            }
        }

        return false;
    }


    public final int getSpellXSpellVisualId(WorldObject caster) {
        return getSpellXSpellVisualId(caster, null);
    }

    public final int getSpellXSpellVisualId() {
        return getSpellXSpellVisualId(null, null);
    }

    public final int getSpellXSpellVisualId(WorldObject caster, WorldObject viewer) {
        for (var visual : visuals) {
            var playerCondition = CliDB.PlayerConditionStorage.get(visual.CasterPlayerConditionID);

            if (playerCondition != null) {
                if (!caster || !caster.isPlayer() || !ConditionManager.isPlayerMeetingCondition(caster.toPlayer(), playerCondition)) {
                    continue;
                }
            }

            var unitCondition = CliDB.UnitConditionStorage.get(visual.CasterUnitConditionID);

            if (unitCondition != null) {
                if (!caster || !caster.isUnit() || !ConditionManager.isUnitMeetingCondition(caster.toUnit(), (viewer == null ? null : viewer.toUnit()), unitCondition)) {
                    continue;
                }
            }

            return visual.id;
        }

        return 0;
    }


    public final int getSpellVisual(WorldObject caster) {
        return getSpellVisual(caster, null);
    }

    public final int getSpellVisual() {
        return getSpellVisual(null, null);
    }

    public final int getSpellVisual(WorldObject caster, WorldObject viewer) {
        var visual = CliDB.SpellXSpellVisualStorage.get(getSpellXSpellVisualId(caster, viewer));

        if (visual != null) {
            //if (visual.LowViolenceSpellVisualID && forPlayer.GetViolenceLevel() operator 2)
            //    return visual.LowViolenceSpellVisualID;
            return visual.spellVisualID;
        }

        return 0;
    }

    public final void initializeExplicitTargetMask() {
        var srcSet = false;
        var dstSet = false;
        var targetMask = getTargets();

        // prepare target mask using effect target entries
        for (var effectInfo : getEffects()) {
            if (!effectInfo.isEffect()) {
                continue;
            }

            tangible.RefObject<Boolean> tempRef_srcSet = new tangible.RefObject<Boolean>(srcSet);
            tangible.RefObject<Boolean> tempRef_dstSet = new tangible.RefObject<Boolean>(dstSet);
            targetMask = SpellCastTargetFlag.forValue(targetMask.getValue() | effectInfo.targetA.getExplicitTargetMask(tempRef_srcSet, tempRef_dstSet).getValue());
            dstSet = tempRef_dstSet.refArgValue;
            srcSet = tempRef_srcSet.refArgValue;
            tangible.RefObject<Boolean> tempRef_srcSet2 = new tangible.RefObject<Boolean>(srcSet);
            tangible.RefObject<Boolean> tempRef_dstSet2 = new tangible.RefObject<Boolean>(dstSet);
            targetMask = SpellCastTargetFlag.forValue(targetMask.getValue() | effectInfo.targetB.getExplicitTargetMask(tempRef_srcSet2, tempRef_dstSet2).getValue());
            dstSet = tempRef_dstSet2.refArgValue;
            srcSet = tempRef_srcSet2.refArgValue;

            // add explicit target flags based on spell effects which have SpellEffectImplicitTargetTypes.Explicit and no valid target provided
            if (effectInfo.getImplicitTargetType() != SpellEffectImplicitTargetTypes.Explicit) {
                continue;
            }

            // extend explicit target mask only if valid targets for effect could not be provided by target types
            var effectTargetMask = effectInfo.getMissingTargetMask(srcSet, dstSet, targetMask);

            // don't add explicit object/dest flags when spell has no max range
            if (getMaxRange(true) == 0.0f && getMaxRange(false) == 0.0f) {
                effectTargetMask = SpellCastTargetFlag.forValue(effectTargetMask.getValue() & ~(SpellCastTargetFlag.UnitMask.getValue() | SpellCastTargetFlag.GAMEOBJECT.getValue().getValue() | SpellCastTargetFlag.CorpseMask.getValue().getValue().getValue() | SpellCastTargetFlag.DestLocation.getValue().getValue().getValue()).getValue());
            }

            targetMask = SpellCastTargetFlag.forValue(targetMask.getValue() | effectTargetMask.getValue());
        }

        setExplicitTargetMask(targetMask);
    }

    public final boolean isPositiveTarget(SpellEffectInfo effect) {
        if (!effect.isEffect()) {
            return true;
        }

        return effect.targetA.getCheckType() != SpellTargetCheckTypes.Enemy && effect.targetB.getCheckType() != SpellTargetCheckTypes.Enemy;
    }

    public final void initializeSpellPositivity() {
        ArrayList<Tuple<spellInfo, Integer>> visited = new ArrayList<Tuple<spellInfo, Integer>>();

        for (var effect : getEffects()) {
            if (!isPositiveEffectImpl(this, effect, visited)) {
                getNegativeEffects().add(effect.effectIndex);
            }
        }


        // additional checks after effects marked
        for (var spellEffectInfo : getEffects()) {
            if (!spellEffectInfo.isEffect() || !isPositiveEffect(spellEffectInfo.effectIndex)) {
                continue;
            }

            switch (spellEffectInfo.applyAuraName) {
                // has other non positive effect?
                // then it should be marked negative if has same target as negative effect (ex 8510, 8511, 8893, 10267)
                case Dummy:
                case ModStun:
                case ModFear:
                case ModTaunt:
                case Transform:
                case ModAttackspeed:
                case ModDecreaseSpeed: {
                    for (var j = spellEffectInfo.effectIndex + 1; j < getEffects().size(); ++j) {
                        if (!isPositiveEffect(j) && spellEffectInfo.targetA.getTarget() == getEffect(j).targetA.getTarget() && spellEffectInfo.targetB.getTarget() == getEffect(j).targetB.getTarget()) {
                            getNegativeEffects().add(spellEffectInfo.effectIndex);
                        }
                    }

                    break;
                }
                default:
                    break;
            }
        }
    }

    public final void unloadImplicitTargetConditionLists() {
        // find the same instances of ConditionList and delete them.
        for (var effectInfo : effects) {
            var cur = effectInfo.implicitTargetConditions;

            if (cur == null) {
                continue;
            }

            for (var j = effectInfo.effectIndex; j < effects.size(); ++j) {
                var eff = effects.get(j);

                if (eff.implicitTargetConditions == cur) {
                    eff.implicitTargetConditions = null;
                }
            }
        }
    }

    public final boolean meetsFutureSpellPlayerCondition(Player player) {
        if (getShowFutureSpellPlayerConditionId() == 0) {
            return false;
        }

        var playerCondition = CliDB.PlayerConditionStorage.get(getShowFutureSpellPlayerConditionId());

        return playerCondition == null || ConditionManager.isPlayerMeetingCondition(player, playerCondition);
    }


    public final boolean hasLabel(int labelId) {
        return labels.contains(labelId);
    }

    public final SpellEffectInfo getEffect(SpellEffIndex index) {
        return effects.get(index.ordinal());
    }

    public final boolean tryGetEffect(int index, tangible.OutObject<SpellEffectInfo> spellEffectInfo) {
        spellEffectInfo.outArgValue = null;

        if (effects.size() < index) {
            return false;
        }

        spellEffectInfo.outArgValue = effects.get(index);

        return spellEffectInfo.outArgValue != null;
    }

    public final boolean hasTargetType(Target target) {
        for (var effectInfo : effects) {
            if (effectInfo.targetA.getTarget() == target || effectInfo.targetB.getTarget() == target) {
                return true;
            }
        }

        return false;
    }

    public boolean hasAttribute(SpellAttr0 attribute) {
        return attributes.hasFlag(attribute);
    }

    public boolean hasAttribute(SpellAttr1 attribute) {
        return attributesEx.hasFlag(attribute);
    }

    public boolean hasAttribute(SpellAttr2 attribute) {
        return attributesEx2.hasFlag(attribute);
    }

    public boolean hasAttribute(SpellAttr3 attribute) {
        return attributesEx3.hasFlag(attribute);
    }

    public boolean hasAttribute(SpellAttr4 attribute) {
        return attributesEx4.hasFlag(attribute);
    }

    public boolean hasAttribute(SpellAttr5 attribute) {
        return attributesEx5.hasFlag(attribute);
    }

    public boolean hasAttribute(SpellAttr6 attribute) {
        return attributesEx6.hasFlag(attribute);
    }

    public boolean hasAttribute(SpellAttr7 attribute) {
        return attributesEx7.hasFlag(attribute);
    }

    public boolean hasAttribute(SpellAttr8 attribute) {
        return attributesEx8.hasFlag(attribute);
    }

    public boolean hasAttribute(SpellAttr9 attribute) {
        return attributesEx9.hasFlag(attribute);
    }

    public boolean hasAttribute(SpellAttr10 attribute) {
        return attributesEx10.hasFlag(attribute);
    }

    public boolean hasAttribute(SpellAttr11 attribute) {
        return attributesEx11.hasFlag(attribute);
    }

    public boolean hasAttribute(SpellAttr12 attribute) {
        return attributesEx12.hasFlag(attribute);
    }

    public boolean hasAttribute(SpellAttr13 attribute) {
        return attributesEx13.hasFlag(attribute);
    }

    public boolean hasAttribute(SpellCustomAttributes customAttribute) {
        return (attributesCu & customAttribute.value) != 0;
    }

    public final boolean canBeInterrupted(WorldObject interruptCaster, Unit interruptTarget) {
        return canBeInterrupted(interruptCaster, interruptTarget, false);
    }

    public final boolean canBeInterrupted(WorldObject interruptCaster, Unit interruptTarget, boolean ignoreImmunity) {
        return hasAttribute(SpellAttr7.CanAlwaysBeInterrupted) || hasChannelInterruptFlag(SpellAuraInterruptFlag.damage.getValue() | SpellAuraInterruptFlag.EnteringCombat.getValue()) || (interruptTarget.isPlayer() && getInterruptFlags().hasFlag(SpellInterruptFlag.DamageCancelsPlayerOnly)) || getInterruptFlags().hasFlag(SpellInterruptFlag.DamageCancels) || (interruptCaster != null && interruptCaster.isUnit() && interruptCaster.toUnit().hasAuraTypeWithMiscvalue(AuraType.AllowInterruptSpell, (int) getId())) || (((interruptTarget.getMechanicImmunityMask() & (1L << mechanics.Interrupt.getValue())) == 0 || ignoreImmunity) && !interruptTarget.hasAuraTypeWithAffectMask(AuraType.PreventInterrupt, this) && getPreventionType().hasFlag(SpellPreventionType.Silence));
    }

    public final boolean hasAuraInterruptFlag(SpellAuraInterruptFlag flag) {
        return getAuraInterruptFlags().hasFlag(flag);
    }

    public final boolean hasAuraInterruptFlag(SpellAuraInterruptFlag2 flag) {
        return getAuraInterruptFlags2().hasFlag(flag);
    }

    public final boolean hasChannelInterruptFlag(SpellAuraInterruptFlag flag) {
        return getChannelInterruptFlags().hasFlag(flag);
    }

    public final boolean hasChannelInterruptFlag(SpellAuraInterruptFlag2 flag) {
        return getChannelInterruptFlags2().hasFlag(flag);
    }

    private DiminishingGroup diminishingGroupCompute() {
        if (isPositive()) {
            return DiminishingGroup.NONE;
        }

        if (hasAura(AuraType.ModTaunt)) {
            return DiminishingGroup.Taunt;
        }

        switch (getId()) {
            case 20549: // War Stomp (Racial - Tauren)
            case 24394: // Intimidation
            case 118345: // Pulverize (Primal Earth Elemental)
            case 118905: // Static charge (Capacitor totem)
                return DiminishingGroup.Stun;
            case 107079: // Quaking Palm
                return DiminishingGroup.Incapacitate;
            case 155145: // Arcane Torrent (Racial - Blood Elf)
                return DiminishingGroup.Silence;
            case 108199: // Gorefiend's Grasp
            case 191244: // Sticky Bomb
                return DiminishingGroup.AOEKnockback;
            default:
                break;
        }

        // Explicit Diminishing Groups
        switch (getSpellFamilyName()) {
            case Generic:
                // Frost Tomb
                if (getId() == 48400) {
                    return DiminishingGroup.NONE;
                }
                // Gnaw
                else if (getId() == 47481) {
                    return DiminishingGroup.Stun;
                }
                // ToC Icehowl Arctic Breath
                else if (getId() == 66689) {
                    return DiminishingGroup.NONE;
                }
                // Black Plague
                else if (getId() == 64155) {
                    return DiminishingGroup.NONE;
                }
                // Screams of the Dead (King Ymiron)
                else if (getId() == 51750) {
                    return DiminishingGroup.NONE;
                }
                // Crystallize (Keristrasza heroic)
                else if (getId() == 48179) {
                    return DiminishingGroup.NONE;
                }

                break;
            case Mage: {
                // Frost Nova -- 122
                if (getSpellFamilyFlags().get(0).hasFlag(0x40)) {
                    return DiminishingGroup.Root;
                }

                // Freeze (Water Elemental) -- 33395
                if (getSpellFamilyFlags().get(2).hasFlag(0x200)) {
                    return DiminishingGroup.Root;
                }

                // Dragon's Breath -- 31661
                if (getSpellFamilyFlags().get(0).hasFlag(0x800000)) {
                    return DiminishingGroup.Incapacitate;
                }

                // Polymorph -- 118
                if (getSpellFamilyFlags().get(0).hasFlag(0x1000000)) {
                    return DiminishingGroup.Incapacitate;
                }

                // Ring of Frost -- 82691
                if (getSpellFamilyFlags().get(2).hasFlag(0x40)) {
                    return DiminishingGroup.Incapacitate;
                }

                // Ice Nova -- 157997
                if (getSpellFamilyFlags().get(2).hasFlag(0x800000)) {
                    return DiminishingGroup.Incapacitate;
                }

                break;
            }
            case Warrior: {
                // Shockwave -- 132168
                if (getSpellFamilyFlags().get(1).hasFlag(0x8000)) {
                    return DiminishingGroup.Stun;
                }

                // Storm Bolt -- 132169
                if (getSpellFamilyFlags().get(2).hasFlag(0x1000)) {
                    return DiminishingGroup.Stun;
                }

                // Intimidating Shout -- 5246
                if (getSpellFamilyFlags().get(0).hasFlag(0x40000)) {
                    return DiminishingGroup.Disorient;
                }

                break;
            }
            case Warlock: {
                // Mortal Coil -- 6789
                if (getSpellFamilyFlags().get(0).hasFlag(0x80000)) {
                    return DiminishingGroup.Incapacitate;
                }

                // Banish -- 710
                if (getSpellFamilyFlags().get(1).hasFlag(0x8000000)) {
                    return DiminishingGroup.Incapacitate;
                }

                // Fear -- 118699
                if (getSpellFamilyFlags().get(1).hasFlag(0x400)) {
                    return DiminishingGroup.Disorient;
                }

                // Howl of Terror -- 5484
                if (getSpellFamilyFlags().get(1).hasFlag(0x8)) {
                    return DiminishingGroup.Disorient;
                }

                // Shadowfury -- 30283
                if (getSpellFamilyFlags().get(1).hasFlag(0x1000)) {
                    return DiminishingGroup.Stun;
                }

                // Summon Infernal -- 22703
                if (getSpellFamilyFlags().get(0).hasFlag(0x1000)) {
                    return DiminishingGroup.Stun;
                }

                // 170995 -- Cripple
                if (getId() == 170995) {
                    return DiminishingGroup.LimitOnly;
                }

                break;
            }
            case WarlockPet: {
                // Fellash -- 115770
                // Whiplash -- 6360
                if (getSpellFamilyFlags().get(0).hasFlag(0x8000000)) {
                    return DiminishingGroup.AOEKnockback;
                }

                // Mesmerize (Shivarra pet) -- 115268
                // Seduction (Succubus pet) -- 6358
                if (getSpellFamilyFlags().get(0).hasFlag(0x2000000)) {
                    return DiminishingGroup.Disorient;
                }

                // Axe Toss (Felguard pet) -- 89766
                if (getSpellFamilyFlags().get(1).hasFlag(0x4)) {
                    return DiminishingGroup.Stun;
                }

                break;
            }
            case Druid: {
                // Maim -- 22570
                if (getSpellFamilyFlags().get(1).hasFlag(0x80)) {
                    return DiminishingGroup.Stun;
                }

                // Mighty Bash -- 5211
                if (getSpellFamilyFlags().get(0).hasFlag(0x2000)) {
                    return DiminishingGroup.Stun;
                }

                // Rake -- 163505 -- no flags on the stun
                if (getId() == 163505) {
                    return DiminishingGroup.Stun;
                }

                // Incapacitating Roar -- 99, no flags on the stun, 14
                if (getSpellFamilyFlags().get(1).hasFlag(0x1)) {
                    return DiminishingGroup.Incapacitate;
                }

                // Cyclone -- 33786
                if (getSpellFamilyFlags().get(1).hasFlag(0x20)) {
                    return DiminishingGroup.Disorient;
                }

                // Solar Beam -- 81261
                if (getId() == 81261) {
                    return DiminishingGroup.Silence;
                }

                // Typhoon -- 61391
                if (getSpellFamilyFlags().get(1).hasFlag(0x1000000)) {
                    return DiminishingGroup.AOEKnockback;
                }

                // Ursol's Vortex -- 118283, no family flags
                if (getId() == 118283) {
                    return DiminishingGroup.AOEKnockback;
                }

                // Entangling Roots -- 339
                if (getSpellFamilyFlags().get(0).hasFlag(0x200)) {
                    return DiminishingGroup.Root;
                }

                // Mass Entanglement -- 102359
                if (getSpellFamilyFlags().get(2).hasFlag(0x4)) {
                    return DiminishingGroup.Root;
                }

                break;
            }
            case Rogue: {
                // Between the Eyes -- 199804
                if (getSpellFamilyFlags().get(0).hasFlag(0x800000)) {
                    return DiminishingGroup.Stun;
                }

                // Cheap Shot -- 1833
                if (getSpellFamilyFlags().get(0).hasFlag(0x400)) {
                    return DiminishingGroup.Stun;
                }

                // Kidney Shot -- 408
                if (getSpellFamilyFlags().get(0).hasFlag(0x200000)) {
                    return DiminishingGroup.Stun;
                }

                // Gouge -- 1776
                if (getSpellFamilyFlags().get(0).hasFlag(0x8)) {
                    return DiminishingGroup.Incapacitate;
                }

                // Sap -- 6770
                if (getSpellFamilyFlags().get(0).hasFlag(0x80)) {
                    return DiminishingGroup.Incapacitate;
                }

                // Blind -- 2094
                if (getSpellFamilyFlags().get(0).hasFlag(0x1000000)) {
                    return DiminishingGroup.Disorient;
                }

                // Garrote -- 1330
                if (getSpellFamilyFlags().get(1).hasFlag(0x20000000)) {
                    return DiminishingGroup.Silence;
                }

                break;
            }
            case Hunter: {
                // charge (Tenacity pet) -- 53148, no flags
                if (getId() == 53148) {
                    return DiminishingGroup.Root;
                }

                // Ranger's Net -- 200108
                // Tracker's Net -- 212638
                if (getId() == 200108 || getId() == 212638) {
                    return DiminishingGroup.Root;
                }

                // Binding Shot -- 117526, no flags
                if (getId() == 117526) {
                    return DiminishingGroup.Stun;
                }

                // Freezing Trap -- 3355
                if (getSpellFamilyFlags().get(0).hasFlag(0x8)) {
                    return DiminishingGroup.Incapacitate;
                }

                // Wyvern Sting -- 19386
                if (getSpellFamilyFlags().get(1).hasFlag(0x1000)) {
                    return DiminishingGroup.Incapacitate;
                }

                // Bursting Shot -- 224729
                if (getSpellFamilyFlags().get(2).hasFlag(0x40)) {
                    return DiminishingGroup.Disorient;
                }

                // Scatter Shot -- 213691
                if (getSpellFamilyFlags().get(2).hasFlag(0x8000)) {
                    return DiminishingGroup.Disorient;
                }

                // Spider Sting -- 202933
                if (getId() == 202933) {
                    return DiminishingGroup.Silence;
                }

                break;
            }
            case Paladin: {
                // Repentance -- 20066
                if (getSpellFamilyFlags().get(0).hasFlag(0x4)) {
                    return DiminishingGroup.Incapacitate;
                }

                // Blinding Light -- 105421
                if (getId() == 105421) {
                    return DiminishingGroup.Disorient;
                }

                // Avenger's Shield -- 31935
                if (getSpellFamilyFlags().get(0).hasFlag(0x4000)) {
                    return DiminishingGroup.Silence;
                }

                // Hammer of Justice -- 853
                if (getSpellFamilyFlags().get(0).hasFlag(0x800)) {
                    return DiminishingGroup.Stun;
                }

                break;
            }
            case Shaman: {
                // Hex -- 51514
                // Hex -- 196942 (Voodoo totem)
                if (getSpellFamilyFlags().get(1).hasFlag(0x8000)) {
                    return DiminishingGroup.Incapacitate;
                }

                // Thunderstorm -- 51490
                if (getSpellFamilyFlags().get(1).hasFlag(0x2000)) {
                    return DiminishingGroup.AOEKnockback;
                }

                // Earthgrab Totem -- 64695
                if (getSpellFamilyFlags().get(2).hasFlag(0x4000)) {
                    return DiminishingGroup.Root;
                }

                // Lightning Lasso -- 204437
                if (getSpellFamilyFlags().get(3).hasFlag(0x2000000)) {
                    return DiminishingGroup.Stun;
                }

                break;
            }
            case Deathknight: {
                // Chains of Ice -- 96294
                if (getId() == 96294) {
                    return DiminishingGroup.Root;
                }

                // Blinding Sleet -- 207167
                if (getId() == 207167) {
                    return DiminishingGroup.Disorient;
                }

                // Strangulate -- 47476
                if (getSpellFamilyFlags().get(0).hasFlag(0x200)) {
                    return DiminishingGroup.Silence;
                }

                // Asphyxiate -- 108194
                if (getSpellFamilyFlags().get(2).hasFlag(0x100000)) {
                    return DiminishingGroup.Stun;
                }

                // Gnaw (Ghoul) -- 91800, no flags
                if (getId() == 91800) {
                    return DiminishingGroup.Stun;
                }

                // Monstrous Blow (Ghoul w/ Dark Transformation active) -- 91797
                if (getId() == 91797) {
                    return DiminishingGroup.Stun;
                }

                // Winter is Coming -- 207171
                if (getId() == 207171) {
                    return DiminishingGroup.Stun;
                }

                break;
            }
            case Priest: {
                // Holy Word: Chastise -- 200200
                if (getSpellFamilyFlags().get(2).hasFlag(0x20) && getSpellVisual() == 52021) {
                    return DiminishingGroup.Stun;
                }

                // Mind Bomb -- 226943
                if (getId() == 226943) {
                    return DiminishingGroup.Stun;
                }

                // Mind Control -- 605
                if (getSpellFamilyFlags().get(0).hasFlag(0x20000) && getSpellVisual() == 39068) {
                    return DiminishingGroup.Incapacitate;
                }

                // Holy Word: Chastise -- 200196
                if (getSpellFamilyFlags().get(2).hasFlag(0x20) && getSpellVisual() == 52019) {
                    return DiminishingGroup.Incapacitate;
                }

                // Psychic Scream -- 8122
                if (getSpellFamilyFlags().get(0).hasFlag(0x10000)) {
                    return DiminishingGroup.Disorient;
                }

                // Silence -- 15487
                if (getSpellFamilyFlags().get(1).hasFlag(0x200000) && getSpellVisual() == 39025) {
                    return DiminishingGroup.Silence;
                }

                // Shining Force -- 204263
                if (getId() == 204263) {
                    return DiminishingGroup.AOEKnockback;
                }

                break;
            }
            case Monk: {
                // Disable -- 116706, no flags
                if (getId() == 116706) {
                    return DiminishingGroup.Root;
                }

                // Fists of Fury -- 120086
                if (getSpellFamilyFlags().get(1).hasFlag(0x800000) && !getSpellFamilyFlags().get(2).hasFlag(0x8)) {
                    return DiminishingGroup.Stun;
                }

                // Leg Sweep -- 119381
                if (getSpellFamilyFlags().get(1).hasFlag(0x200)) {
                    return DiminishingGroup.Stun;
                }

                // Incendiary Breath (honor talent) -- 202274, no flags
                if (getId() == 202274) {
                    return DiminishingGroup.Incapacitate;
                }

                // Paralysis -- 115078
                if (getSpellFamilyFlags().get(2).hasFlag(0x800000)) {
                    return DiminishingGroup.Incapacitate;
                }

                // Song of Chi-Ji -- 198909
                if (getId() == 198909) {
                    return DiminishingGroup.Disorient;
                }

                break;
            }
            case DemonHunter:
                switch (getId()) {
                    case 179057: // Chaos Nova
                    case 211881: // Fel Eruption
                    case 200166: // Metamorphosis
                    case 205630: // Illidan's Grasp
                        return DiminishingGroup.Stun;
                    case 217832: // Imprison
                    case 221527: // Imprison
                        return DiminishingGroup.Incapacitate;
                    default:
                        break;
                }

                break;
            default:
                break;
        }

        return DiminishingGroup.NONE;
    }

    private DiminishingReturnsType diminishingTypeCompute(DiminishingGroup group) {
        switch (group) {
            case Taunt:
            case Stun:
                return DiminishingReturnsType.All;
            case LimitOnly:
            case None:
                return DiminishingReturnsType.NONE;
            default:
                return DiminishingReturnsType.player;
        }
    }

    private DiminishingLevels diminishingMaxLevelCompute(DiminishingGroup group) {
        switch (group) {
            case Taunt:
                return DiminishingLevels.TauntImmune;
            case AOEKnockback:
                return DiminishingLevels.Level2;
            default:
                return DiminishingLevels.Immune;
        }
    }

    private int diminishingLimitDurationCompute() {
        // Explicit diminishing duration
        switch (getSpellFamilyName()) {
            case Mage:
                // Dragon's Breath - 3 seconds in PvP
                if (getSpellFamilyFlags().get(0).hasFlag(0x800000)) {
                    return 3 * time.InMilliseconds;
                }

                break;
            case Warlock:
                // Cripple - 4 seconds in PvP
                if (getId() == 170995) {
                    return 4 * time.InMilliseconds;
                }

                break;
            case Hunter:
                // Binding Shot - 3 seconds in PvP
                if (getId() == 117526) {
                    return 3 * time.InMilliseconds;
                }

                // Wyvern Sting - 6 seconds in PvP
                if (getSpellFamilyFlags().get(1).hasFlag(0x1000)) {
                    return 6 * time.InMilliseconds;
                }

                break;
            case Monk:
                // Paralysis - 4 seconds in PvP regardless of if they are facing you
                if (getSpellFamilyFlags().get(2).hasFlag(0x800000)) {
                    return 4 * time.InMilliseconds;
                }

                break;
            case DemonHunter:
                switch (getId()) {
                    case 217832: // Imprison
                    case 221527: // Imprison
                        return 4 * time.InMilliseconds;
                    default:
                        break;
                }

                break;
            default:
                break;
        }

        return 8 * time.InMilliseconds;
    }

    private boolean canSpellProvideImmunityAgainstAura(SpellInfo auraSpellInfo) {
        if (auraSpellInfo == null) {
            return false;
        }

        for (var effectInfo : effects) {
            if (!effectInfo.isEffect()) {
                continue;
            }

            var immuneInfo = effectInfo.getImmunityInfo();

            if (!auraSpellInfo.hasAttribute(SpellAttr1.ImmunityToHostileAndFriendlyEffects) && !auraSpellInfo.hasAttribute(SpellAttr2.NoSchoolImmunities)) {
                var schoolImmunity = immuneInfo.schoolImmuneMask;

                if (schoolImmunity != 0) {
                    if (((int) auraSpellInfo.getSchoolMask().getValue() & schoolImmunity) != 0) {
                        return true;
                    }
                }
            }

            var mechanicImmunity = immuneInfo.mechanicImmuneMask;

            if (mechanicImmunity != 0) {
                if ((mechanicImmunity & (1 << auraSpellInfo.getMechanic().getValue())) != 0) {
                    return true;
                }
            }

            var dispelImmunity = immuneInfo.dispelImmune;

            if (dispelImmunity != 0) {
                if ((int) auraSpellInfo.getDispel().getValue() == dispelImmunity) {
                    return true;
                }
            }

            var immuneToAllEffects = true;

            for (var auraSpellEffectInfo : auraSpellInfo.getEffects()) {
                if (!auraSpellEffectInfo.isEffect()) {
                    continue;
                }

                if (!immuneInfo.spellEffectImmune.contains(auraSpellEffectInfo.effect)) {
                    immuneToAllEffects = false;

                    break;
                }

                var mechanic = (int) auraSpellEffectInfo.mechanic.getValue();

                if (mechanic != 0) {
                    if (!(boolean) (immuneInfo.mechanicImmuneMask & (1 << (int) mechanic))) {
                        immuneToAllEffects = false;

                        break;
                    }
                }

                if (!auraSpellInfo.hasAttribute(SpellAttr3.AlwaysHit)) {
                    var auraName = auraSpellEffectInfo.applyAuraName;

                    if (auraName != 0) {
                        var isImmuneToAuraEffectApply = false;

                        if (!immuneInfo.auraTypeImmune.contains(auraName)) {
                            isImmuneToAuraEffectApply = true;
                        }

                        if (!isImmuneToAuraEffectApply && !auraSpellInfo.isPositiveEffect(auraSpellEffectInfo.effectIndex) && !auraSpellInfo.hasAttribute(SpellAttr2.NoSchoolImmunities)) {
                            var applyHarmfulAuraImmunityMask = immuneInfo.applyHarmfulAuraImmuneMask;

                            if (applyHarmfulAuraImmunityMask != 0) {
                                if (((int) auraSpellInfo.getSchoolMask().getValue() & applyHarmfulAuraImmunityMask) != 0) {
                                    isImmuneToAuraEffectApply = true;
                                }
                            }
                        }

                        if (!isImmuneToAuraEffectApply) {
                            immuneToAllEffects = false;

                            break;
                        }
                    }
                }
            }

            if (immuneToAllEffects) {
                return true;
            }
        }

        return false;
    }

    private double calcPPMHasteMod(SpellProcsPerMinuteModRecord mod, Unit caster) {
        double haste = caster.getUnitData().modHaste;
        double rangedHaste = caster.getUnitData().modRangedHaste;
        double spellHaste = caster.getUnitData().modSpellHaste;
        double regenHaste = caster.getUnitData().modHasteRegen;

        switch (mod.param) {
            case 1:
                return (1.0f / haste - 1.0f) * mod.Coeff;
            case 2:
                return (1.0f / rangedHaste - 1.0f) * mod.Coeff;
            case 3:
                return (1.0f / spellHaste - 1.0f) * mod.Coeff;
            case 4:
                return (1.0f / regenHaste - 1.0f) * mod.Coeff;
            case 5:
                return (1.0f / Math.min(Math.min(Math.min(haste, rangedHaste), spellHaste), regenHaste) - 1.0f) * mod.Coeff;
            default:
                break;
        }

        return 0.0f;
    }

    private double calcPPMCritMod(SpellProcsPerMinuteModRecord mod, Unit caster) {
        var player = caster.toPlayer();

        if (player == null) {
            return 0.0f;
        }

        double crit = player.getActivePlayerData().critPercentage;
        double rangedCrit = player.getActivePlayerData().rangedCritPercentage;
        double spellCrit = player.getActivePlayerData().spellCritPercentage;

        switch (mod.param) {
            case 1:
                return crit * mod.Coeff * 0.01f;
            case 2:
                return rangedCrit * mod.Coeff * 0.01f;
            case 3:
                return spellCrit * mod.Coeff * 0.01f;
            case 4:
                return Math.min(Math.min(crit, rangedCrit), spellCrit) * mod.Coeff * 0.01f;
            default:
                break;
        }

        return 0.0f;
    }

    private double calcPPMItemLevelMod(SpellProcsPerMinuteModRecord mod, int itemLevel) {
        if (itemLevel == mod.param) {
            return 0.0f;
        }

        double itemLevelPoints = ItemEnchantmentManager.getRandomPropertyPoints((int) itemLevel, itemQuality.Rare, inventoryType.chest, 0);
        double basePoints = ItemEnchantmentManager.getRandomPropertyPoints(mod.param, itemQuality.Rare, inventoryType.chest, 0);

        if (itemLevelPoints == basePoints) {
            return 0.0f;
        }

        return ((itemLevelPoints / basePoints) - 1.0f) * mod.Coeff;
    }

    private SpellInfo getLastRankSpell() {
        if (getChainEntry() == null) {
            return null;
        }

        return getChainEntry().last;
    }

    private SpellInfo getPrevRankSpell() {
        if (getChainEntry() == null) {
            return null;
        }

        return getChainEntry().prev;
    }

    private boolean isPositiveEffectImpl(SpellInfo spellInfo, SpellEffectInfo effect, ArrayList<Tuple<spellInfo, Integer>> visited) {
        if (!effect.isEffect()) {
            return true;
        }

        // attribute may be already set in DB
        if (!spellInfo.isPositiveEffect(effect.effectIndex)) {
            return false;
        }

        // passive auras like talents are all positive
        if (spellInfo.isPassive()) {
            return true;
        }

        // not found a single positive spell with this attribute
        if (spellInfo.hasAttribute(SpellAttr0.AuraIsDebuff)) {
            return false;
        }

        if (spellInfo.hasAttribute(SpellAttr4.AuraIsBuff)) {
            return true;
        }

        visited.add(Tuple.create(spellInfo, effect.effectIndex));

        //We need scaling level info for some auras that compute bp 0 or positive but should be debuffs
        var bpScalePerLevel = effect.realPointsPerLevel;
        var bp = effect.calcValue();

        switch (spellInfo.getSpellFamilyName()) {
            case Generic:
                switch (spellInfo.getId()) {
                    case 40268: // Spiritual Vengeance, Teron Gorefiend, Black Temple
                    case 61987: // Avenging Wrath Marker
                    case 61988: // Divine Shield exclude aura
                    case 64412: // Phase Punch, Algalon the Observer, Ulduar
                    case 72410: // Rune of Blood, Saurfang, Icecrown Citadel
                    case 71204: // Touch of Insignificance, Lady Deathwhisper, Icecrown Citadel
                        return false;
                    case 24732: // Bat Costume
                    case 30877: // Tag Murloc
                    case 61716: // Rabbit Costume
                    case 61734: // Noblegarden Bunny
                    case 62344: // Fists of Stone
                    case 50344: // Dream Funnel
                    case 61819: // Manabonked! (item)
                    case 61834: // Manabonked! (minigob)
                    case 73523: // Rigor Mortis
                        return true;
                    default:
                        break;
                }

                break;
            case Rogue:
                switch (spellInfo.getId()) {
                    // Envenom must be considered as a positive effect even though it deals damage
                    case 32645: // Envenom
                        return true;
                    case 40251: // Shadow of Death, Teron Gorefiend, Black Temple
                        return false;
                    default:
                        break;
                }

                break;
            case Warrior:
                // Slam, Execute
                if ((spellInfo.getSpellFamilyFlags().get(0) & 0x20200000) != 0) {
                    return false;
                }

                break;
            default:
                break;
        }

        switch (spellInfo.getMechanic()) {
            case ImmuneShield:
                return true;
            default:
                break;
        }

        // Special case: effects which determine positivity of whole spell
        if (spellInfo.hasAttribute(SpellAttr1.AuraUnique)) {
            // check for targets, there seems to be an assortment of dummy triggering spells that should be negative
            for (var otherEffect : spellInfo.getEffects()) {
                if (!isPositiveTarget(otherEffect)) {
                    return false;
                }
            }
        }

        for (var otherEffect : spellInfo.getEffects()) {
            switch (otherEffect.effect) {
                case Heal:
                case LearnSpell:
                case SkillStep:
                case HealPct:
                    return true;
                case Instakill:
                    if (otherEffect.effectIndex != effect.effectIndex && otherEffect.targetA.getTarget() == effect.targetA.getTarget() && otherEffect.targetB.getTarget() == effect.targetB.getTarget()) {
                        return false;
                    }

                    break;
                default:
                    break;
            }

            if (otherEffect.isAura()) {
                switch (otherEffect.applyAuraName) {
                    case ModStealth:
                    case ModUnattackable:
                        return true;
                    case SchoolHealAbsorb:
                    case Empathy:
                    case ModSpellDamageFromCaster:
                    case PreventsFleeing:
                        return false;
                    default:
                        break;
                }
            }
        }

        switch (effect.effect) {
            case WeaponDamage:
            case WeaponDamageNoSchool:
            case NormalizedWeaponDmg:
            case WeaponPercentDamage:
            case SchoolDamage:
            case EnvironmentalDamage:
            case HealthLeech:
            case Instakill:
            case PowerDrain:
            case StealBeneficialBuff:
            case InterruptCast:
            case Pickpocket:
            case GameObjectDamage:
            case DurabilityDamage:
            case DurabilityDamagePct:
            case ApplyAreaAuraEnemy:
            case Tamecreature:
            case Distract:
                return false;
            case Energize:
            case EnergizePct:
            case HealPct:
            case HealMaxHealth:
            case HealMechanical:
                return true;
            case KnockBack:
            case Charge:
            case PersistentAreaAura:
            case AttackMe:
            case PowerBurn:
                // check targets
                if (!isPositiveTarget(effect)) {
                    return false;
                }

                break;
            case Dispel:
                // non-positive dispel
                switch (DispelType.forValue(effect.miscValue)) {
                    case Stealth:
                    case Invisibility:
                    case Enrage:
                        return false;
                    default:
                        break;
                }

                // also check targets
                if (!isPositiveTarget(effect)) {
                    return false;
                }

                break;
            case DispelMechanic:
                if (!isPositiveTarget(effect)) {
                    // non-positive mechanic dispel on negative target
                    switch (mechanics.forValue(effect.miscValue)) {
                        case Bandage:
                        case Shield:
                        case Mount:
                        case Invulnerability:
                            return false;
                        default:
                            break;
                    }
                }

                break;
            case Threat:
            case ModifyThreatPercent:
                // check targets AND basepoints
                if (!isPositiveTarget(effect) && bp > 0) {
                    return false;
                }

                break;
            default:
                break;
        }

        if (effect.isAura()) {
            // non-positive aura use
            switch (effect.applyAuraName) {
                case ModStat: // dependent from basepoint sign (negative -> negative)
                case ModSkill:
                case ModSkill2:
                case ModDodgePercent:
                case ModHealingDone:
                case ModDamageDoneCreature:
                case ObsModHealth:
                case ObsModPower:
                case ModCritPct:
                case ModHitChance:
                case ModSpellHitChance:
                case ModSpellCritChance:
                case ModRangedHaste:
                case ModMeleeRangedHaste:
                case ModCastingSpeedNotStack:
                case HasteSpells:
                case ModRecoveryRateBySpellLabel:
                case ModDetectRange:
                case ModIncreaseHealthPercent:
                case ModTotalStatPercentage:
                case ModIncreaseSwimSpeed:
                case ModPercentStat:
                case ModIncreaseHealth:
                case ModSpeedAlways:
                    if (bp < 0 || bpScalePerLevel < 0) //TODO: What if both are 0? Should it be a buff or debuff?
                    {
                        return false;
                    }

                    break;
                case ModAttackspeed: // some buffs have negative bp, check both target and bp
                case ModMeleeHaste:
                case ModDamageDone:
                case ModResistance:
                case ModResistancePct:
                case ModRating:
                case ModAttackPower:
                case ModRangedAttackPower:
                case ModDamagePercentDone:
                case ModSpeedSlowAll:
                case MeleeSlow:
                case ModAttackPowerPct:
                case ModHealingDonePercent:
                case ModHealingPct:
                    if (!isPositiveTarget(effect) || bp < 0) {
                        return false;
                    }

                    break;
                case ModDamageTaken: // dependent from basepoint sign (positive . negative)
                case ModMeleeDamageTaken:
                case ModMeleeDamageTakenPct:
                case ModPowerCostSchool:
                case ModPowerCostSchoolPct:
                case ModMechanicDamageTakenPercent:
                    if (bp > 0) {
                        return false;
                    }

                    break;
                case ModDamagePercentTaken: // check targets and basepoints (ex recklessness)
                    if (!isPositiveTarget(effect) && bp > 0) {
                        return false;
                    }

                    break;
                case ModHealthRegenPercent: // check targets and basepoints (target enemy and negative bp -> negative)
                    if (!isPositiveTarget(effect) && bp < 0) {
                        return false;
                    }

                    break;
                case AddTargetTrigger:
                    return true;
                case PeriodicTriggerSpellWithValue:
                case PeriodicTriggerSpellFromClient:
                    var spellTriggeredProto = global.getSpellMgr().getSpellInfo(effect.triggerSpell, spellInfo.getDifficulty());

                    if (spellTriggeredProto != null) {
                        // negative targets of main spell return early
                        for (var spellTriggeredEffect : spellTriggeredProto.getEffects()) {
                            // already seen this
                            if (visited.contains(Tuple.create(spellTriggeredProto, spellTriggeredEffect.effectIndex))) {
                                continue;
                            }

                            if (!spellTriggeredEffect.isEffect()) {
                                continue;
                            }

                            // if non-positive trigger cast targeted to positive target this main cast is non-positive
                            // this will place this spell auras as debuffs
                            if (isPositiveTarget(spellTriggeredEffect) && !isPositiveEffectImpl(spellTriggeredProto, spellTriggeredEffect, visited)) {
                                return false;
                            }
                        }
                    }

                    break;
                case PeriodicTriggerSpell:
                case ModStun:
                case Transform:
                case ModDecreaseSpeed:
                case ModFear:
                case ModTaunt:
                    // special auras: they may have non negative target but still need to be marked as debuff
                    // checked again after all effects (SpellInfo::_InitializeSpellPositivity)
                case ModPacify:
                case ModPacifySilence:
                case ModDisarm:
                case ModDisarmOffhand:
                case ModDisarmRanged:
                case ModCharm:
                case AoeCharm:
                case ModPossess:
                case ModLanguage:
                case DamageShield:
                case ProcTriggerSpell:
                case ModAttackerMeleeHitChance:
                case ModAttackerRangedHitChance:
                case ModAttackerSpellHitChance:
                case ModAttackerMeleeCritChance:
                case ModAttackerRangedCritChance:
                case ModAttackerSpellAndWeaponCritChance:
                case Dummy:
                case PeriodicDummy:
                case ModHealing:
                case ModWeaponCritPercent:
                case PowerBurn:
                case ModCooldown:
                case ModChargeCooldown:
                case ModIncreaseSpeed:
                case ModParryPercent:
                case SetVehicleId:
                case PeriodicEnergize:
                case EffectImmunity:
                case OverrideClassScripts:
                case ModShapeshift:
                case ModThreat:
                case ProcTriggerSpellWithValue:
                    // check target for positive and negative spells
                    if (!isPositiveTarget(effect)) {
                        return false;
                    }

                    break;
                case ModConfuse:
                case ChannelDeathItem:
                case ModRoot:
                case ModRoot2:
                case ModSilence:
                case ModDetaunt:
                case Ghost:
                case ModLeech:
                case PeriodicManaLeech:
                case ModStalked:
                case PreventResurrection:
                case PeriodicDamage:
                case PeriodicWeaponPercentDamage:
                case PeriodicDamagePercent:
                case MeleeAttackPowerAttackerBonus:
                case RangedAttackPowerAttackerBonus:
                    return false;
                case MechanicImmunity: {
                    // non-positive immunities
                    switch (mechanics.forValue(effect.miscValue)) {
                        case Bandage:
                        case Shield:
                        case Mount:
                        case Invulnerability:
                            return false;
                        default:
                            break;
                    }

                    break;
                }
                case AddFlatModifier: // mods
                case AddPctModifier:
                case AddFlatModifierBySpellLabel:
                case AddPctModifierBySpellLabel: {
                    switch (SpellModOp.forValue(effect.miscValue)) {
                        case ChangeCastTime: // dependent from basepoint sign (positive . negative)
                        case Period:
                        case PowerCostOnMiss:
                        case StartCooldown:
                            if (bp > 0) {
                                return false;
                            }

                            break;
                        case Cooldown:
                        case PowerCost0:
                        case PowerCost1:
                        case PowerCost2:
                            if (!spellInfo.isPositive() && bp > 0) // dependent on prev effects too (ex Arcane power)
                            {
                                return false;
                            }

                            break;
                        case PointsIndex0: // always positive
                        case PointsIndex1:
                        case PointsIndex2:
                        case PointsIndex3:
                        case PointsIndex4:
                        case Points:
                        case Hate:
                        case ChainAmplitude:
                        case Amplitude:
                            return true;
                        case Duration:
                        case CritChance:
                        case HealingAndDamage:
                        case ChainTargets:
                            if (!spellInfo.isPositive() && bp < 0) // dependent on prev effects too
                            {
                                return false;
                            }

                            break;
                        default: // dependent from basepoint sign (negative . negative)
                            if (bp < 0) {
                                return false;
                            }

                            break;
                    }

                    break;
                }
                default:
                    break;
            }
        }

        // negative spell if triggered spell is negative
        if (effect.applyAuraName == 0 && effect.triggerSpell != 0) {
            var spellTriggeredProto = global.getSpellMgr().getSpellInfo(effect.triggerSpell, spellInfo.getDifficulty());

            if (spellTriggeredProto != null) {
                // spells with at least one negative effect are considered negative
                // some self-applied spells have negative effects but in self casting case negative check ignored.
                for (var spellTriggeredEffect : spellTriggeredProto.getEffects()) {
                    // already seen this
                    if (visited.contains(Tuple.create(spellTriggeredProto, spellTriggeredEffect.effectIndex))) {
                        continue;
                    }

                    if (!spellTriggeredEffect.isEffect()) {
                        continue;
                    }

                    if (!isPositiveEffectImpl(spellTriggeredProto, spellTriggeredEffect, visited)) {
                        return false;
                    }
                }
            }
        }

        // ok, positive
        return true;
    }


    public final static class ScalingInfo {

        public int minScalingLevel;

        public int maxScalingLevel;

        public int scalesFromItemLevel;

        public ScalingInfo clone() {
            ScalingInfo varCopy = new scalingInfo();

            varCopy.minScalingLevel = this.minScalingLevel;
            varCopy.maxScalingLevel = this.maxScalingLevel;
            varCopy.scalesFromItemLevel = this.scalesFromItemLevel;

            return varCopy;
        }
    }


}
