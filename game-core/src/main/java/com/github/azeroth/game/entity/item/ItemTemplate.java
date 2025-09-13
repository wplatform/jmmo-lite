package com.github.azeroth.game.entity.item;


import com.github.azeroth.common.EnumFlag;
import com.github.azeroth.dbc.domain.ChrSpecialization;
import com.github.azeroth.dbc.domain.ItemEffect;
import com.github.azeroth.dbc.domain.ItemEntry;
import com.github.azeroth.dbc.domain.ItemSparse;
import com.github.azeroth.defines.PlayerClass;
import com.github.azeroth.defines.SharedDefine;
import com.github.azeroth.defines.SkillType;
import com.github.azeroth.game.entity.item.enums.*;
import com.github.azeroth.game.entity.player.Player;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Locale;

@Data
public class ItemTemplate {
    private static final SkillType[] ITEM_WEAPON_SKILLS = {
            SkillType.AXES, SkillType.TWO_HANDED_AXES, SkillType.BOWS,
            SkillType.GUNS, SkillType.MACES, SkillType.TWO_HANDED_MACES,
            SkillType.POLEARMS, SkillType.SWORDS, SkillType.TWO_HANDED_SWORDS,
            SkillType.WARGLAIVES, SkillType.STAVES, SkillType.NONE, SkillType.NONE,
            SkillType.FIST_WEAPONS, SkillType.NONE, SkillType.DAGGERS, SkillType.NONE,
            SkillType.NONE, SkillType.CROSSBOWS, SkillType.WANDS, SkillType.FISHING
    };

    private static final SkillType[] ITEM_ARMOR_SKILLS = {
            SkillType.NONE, SkillType.CLOTH, SkillType.LEATHER,
            SkillType.MAIL, SkillType.PLATE_MAIL, SkillType.NONE,
            SkillType.SHIELD, SkillType.NONE, SkillType.NONE,
            SkillType.NONE, SkillType.NONE, SkillType.NONE
    };

    private static final SkillType[] ITEM_PROFESSION_SKILLS = {
            SkillType.BLACKSMITHING, SkillType.LEATHERWORKING, SkillType.ALCHEMY,
            SkillType.HERBALISM, SkillType.COOKING, SkillType.MINING,
            SkillType.TAILORING, SkillType.ENGINEERING, SkillType.ENCHANTING,
            SkillType.FISHING, SkillType.SKINNING, SkillType.JEWELCRAFTING,
            SkillType.INSCRIPTION, SkillType.ARCHAEOLOGY
    };

    private final SkillType[] itemProfessionSkills = {
            SkillType.BLACKSMITHING, SkillType.LEATHERWORKING, SkillType.ALCHEMY,     SkillType.HERBALISM,  SkillType.COOKING,
            SkillType.MINING,        SkillType.TAILORING,      SkillType.ENGINEERING, SkillType.ENCHANTING, SkillType.FISHING,
            SkillType.SKINNING,      SkillType.JEWELCRAFTING,  SkillType.INSCRIPTION, SkillType.ARCHAEOLOGY

    };

    private int maxDurability;
    private ArrayList<ItemEffect> effects = new ArrayList<>();
    // extra fields, not part of db2 files
    private int scriptId;
    private int foodType;
    private int minMoneyLoot;
    private int maxMoneyLoot;
    private EnumFlag<ItemFlagsCustom> flagsCu;
    private float spellPPMRate;
    private int randomBonusListTemplateId;
    private BitSet[] specializations = new BitSet[3];
    private int itemSpecClassMask;
    private ItemEntry basicData;
    private ItemSparse extendedData;

    public ItemTemplate(ItemEntry item, ItemSparse sparse) {

        this.basicData = item;
        this.extendedData = sparse;

        specializations[0] = new BitSet(PlayerClass.values().length * SharedDefine.MAX_SPECIALIZATIONS);
        specializations[1] = new BitSet(PlayerClass.values().length * SharedDefine.MAX_SPECIALIZATIONS);
        specializations[2] = new BitSet(PlayerClass.values().length * SharedDefine.MAX_SPECIALIZATIONS);
    }

    public static int calculateItemSpecBit(ChrSpecialization spec) {
        return (int) ((spec.getClassID() - 1) * SharedDefine.MAX_SPECIALIZATIONS + spec.getOrderIndex());
    }

    public final int getMaxDurability() {
        return maxDurability;
    }

    public final void setMaxDurability(int value) {
        maxDurability = value;
    }

    public final ArrayList<ItemEffect> getEffects() {
        return effects;
    }

    public final void setEffects(ArrayList<ItemEffect> value) {
        effects = value;
    }

    public final int getScriptId() {
        return scriptId;
    }

    public final void setScriptId(int value) {
        scriptId = value;
    }

    public final int getFoodType() {
        return foodType;
    }

    public final void setFoodType(int value) {
        foodType = value;
    }

    public final int getMinMoneyLoot() {
        return minMoneyLoot;
    }

    public final void setMinMoneyLoot(int value) {
        minMoneyLoot = value;
    }

    public final int getMaxMoneyLoot() {
        return maxMoneyLoot;
    }

    public final void setMaxMoneyLoot(int value) {
        maxMoneyLoot = value;
    }




    public final float getSpellPPMRate() {
        return spellPPMRate;
    }

    public final void setSpellPPMRate(float value) {
        spellPPMRate = value;
    }

    public final int getRandomBonusListTemplateId() {
        return randomBonusListTemplateId;
    }

    public final void setRandomBonusListTemplateId(int value) {
        randomBonusListTemplateId = value;
    }

    public final BitSet[] getSpecializations() {
        return specializations;
    }

    public final void setSpecializations(BitSet[] value) {
        specializations = value;
    }

    public final int getItemSpecClassMask() {
        return itemSpecClassMask;
    }

    public final void setItemSpecClassMask(int value) {
        itemSpecClassMask = value;
    }


    public final boolean getHasSignature() {
        return getMaxStackSize() == 1 && getClass() != itemClass.Consumable && getClass() != itemClass.Quest && !hasFlag(ItemFlags.NoCreator) && getId() != 6948;
    }

    public final int getId() {
        return basicData.id;
    }

    public final ItemClass getItemClass() {
        return basicData.classID;
    }

    public final ItemSubclassConsumable getSubClass() {
        return basicData.SubclassID;
    }

    public final ItemQuality getQuality() {
        return itemQuality.forValue(extendedData.OverallQualityID);
    }

    public final int getOtherFactionItemId() {
        return extendedData.FactionRelated;
    }

    public final float getPriceRandomValue() {
        return extendedData.PriceRandomValue;
    }

    public final float getPriceVariance() {
        return extendedData.PriceVariance;
    }

    public final int getBuyCount() {
        return Math.max(extendedData.VendorStackCount, 1);
    }

    public final int getBuyPrice() {
        return extendedData.BuyPrice;
    }

    public final int getSellPrice() {
        return extendedData.SellPrice;
    }

    public final InventoryType getInventoryType() {
        return extendedData.inventoryType;
    }

    public final int getAllowableClass() {
        return extendedData.AllowableClass;
    }

    public final long getAllowableRace() {
        return extendedData.AllowableRace;
    }

    public final int getBaseItemLevel() {
        return extendedData.itemLevel;
    }

    public final int getBaseRequiredLevel() {
        return extendedData.requiredLevel;
    }

    public final int getRequiredSkill() {
        return extendedData.RequiredSkill;
    }

    public final int getRequiredSkillRank() {
        return extendedData.RequiredSkillRank;
    }

    public final int getRequiredSpell() {
        return extendedData.RequiredAbility;
    }

    public final int getRequiredReputationFaction() {
        return extendedData.MinFactionID;
    }

    public final int getRequiredReputationRank() {
        return extendedData.MinReputation;
    }

    public final int getMaxCount() {
        return extendedData.maxCount;
    }

    public final int getContainerSlots() {
        return extendedData.ContainerSlots;
    }

    public final int getScalingStatContentTuning() {
        return extendedData.contentTuningID;
    }

    public final int getPlayerLevelToItemLevelCurveId() {
        return extendedData.PlayerLevelToItemLevelCurveID;
    }

    public final int getDamageType() {
        return extendedData.DamageType;
    }

    public final int getDelay() {
        return extendedData.ItemDelay;
    }

    public final float getRangedModRange() {
        return extendedData.ItemRange;
    }

    public final ItemBondingType getBonding() {
        return ItemBondingType.forValue(extendedData.bonding);
    }

    public final int getPageText() {
        return extendedData.PageID;
    }

    public final int getStartQuest() {
        return extendedData.StartQuestID;
    }

    public final int getLockID() {
        return extendedData.LockID;
    }

    public final int getItemSet() {
        return extendedData.ItemSet;
    }

    public final int getMap() {
        return extendedData.InstanceBound;
    }

    public final BagFamilyMask getBagFamily() {
        return BagFamilyMask.forValue(extendedData.BagFamily);
    }

    public final int getTotemCategory() {
        return extendedData.TotemCategoryID;
    }

    public final int getSocketBonus() {
        return extendedData.SocketMatchEnchantmentId;
    }

    public final int getGemProperties() {
        return extendedData.GemProperties;
    }

    public final float getQualityModifier() {
        return extendedData.QualityModifier;
    }

    public final int getDuration() {
        return extendedData.DurationInInventory;
    }

    public final int getItemLimitCategory() {
        return extendedData.limitCategory;
    }

    public final HolidayIds getHolidayID() {
        return HolidayIds.forValue(extendedData.RequiredHoliday);
    }

    public final float getDmgVariance() {
        return extendedData.DmgVariance;
    }

    public final byte getArtifactID() {
        return extendedData.ArtifactID;
    }

    public final byte getRequiredExpansion() {
        return (byte) extendedData.ExpansionID;
    }

    public final boolean isCurrencyToken() {
        return (getBagFamily().getValue() & BagFamilyMask.CurrencyTokens.getValue()) != 0;
    }

    public final int getMaxStackSize() {
        return (extendedData.Stackable == 2147483647 || extendedData.Stackable <= 0) ? (0x7FFFFFFF - 1) : extendedData.Stackable;
    }

    public final boolean isPotion() {
        return getClass() == itemClass.Consumable && getSubClass() == (int) ItemSubClassConsumable.Potion.getValue();
    }

    public final boolean isVellum() {
        return hasFlag(ItemFlags3.CanStoreEnchants);
    }

    public final boolean isConjuredConsumable() {
        return getClass() == itemClass.Consumable && hasFlag(ItemFlags.Conjured);
    }

    public final boolean isCraftingReagent() {
        return hasFlag(ItemFlags2.UsedInATradeskill);
    }

    public final boolean isWeapon() {
        return getClass() == itemClass.Weapon;
    }

    public final boolean isArmor() {
        return getClass() == itemClass.armor;
    }

    public final boolean isRangedWeapon() {
        return isWeapon() && (getSubClass() == (int) ItemSubClassWeapon.Bow.getValue() || getSubClass() == (int) ItemSubClassWeapon.Gun.getValue() || getSubClass() == (int) ItemSubClassWeapon.Crossbow.getValue());
    }

    public final String getName() {
        return getName(SharedConst.DefaultLocale);
    }

    public final String getName(Locale locale) {
        return extendedData.display.get(locale);
    }

    public final boolean hasFlag(ItemFlag flag) {
        return (extendedData.Flags[0] & flag.getValue()) != 0;
    }

    public final boolean hasFlag(ItemFlag2 flag) {
        return (extendedData.Flags[1] & flag.getValue()) != 0;
    }

    public final boolean hasFlag(ItemFlag3 flag) {
        return (extendedData.Flags[2] & flag.getValue()) != 0;
    }

    public final boolean hasFlag(ItemFlag4 flag) {
        return (extendedData.Flags[3] & flag.getValue()) != 0;
    }

    public final boolean hasFlag(ItemFlagsCustom customFlag) {
        return (getFlagsCu().getValue() & customFlag.getValue()) != 0;
    }

    public final boolean canChangeEquipStateInCombat() {
        switch (getInventoryType()) {
            case Relic:
            case Shield:
            case Holdable:
                return true;
            default:
                break;
        }

        switch (getClass()) {
            case Weapon:
            case Projectile:
                return true;
        }

        return false;
    }

    public final SkillType getSkill() {
        switch (getClass()) {
            case Weapon:
                if (getSubClass() >= ItemSubClassWeapon.max.getValue()) {
                    return 0;
                } else {
                    return ItemWeaponSkills[getSubClass()];
                }
            case Armor:
                if (getSubClass() >= ItemSubClassArmor.max.getValue()) {
                    return 0;
                } else {
                    return ItemArmorSkills[getSubClass()];
                }

            case Profession:

                if (ConfigMgr.GetDefaultValue("Professions.AllowClassicProfessionSlots", false)) {
                    if (getSubClass() >= ItemSubclassProfession.max.getValue()) {
                        return 0;
                    } else {
                        return _itemProfessionSkills[getSubClass()];
                    }
                } else if (getSubClass() >= ItemSubclassProfession.max.getValue()) {
                    return 0;
                } else {
                    return ItemProfessionSkills[getSubClass()];
                }

            default:
                return 0;
        }
    }

    public final int getArmor(int itemLevel) {
        var quality = getQuality() != itemQuality.Heirloom ? getQuality() : itemQuality.Rare;

        if (quality.getValue() > itemQuality.artifact.getValue()) {
            return 0;
        }

        // all items but shields
        if (getClass() != itemClass.armor || getSubClass() != (int) ItemSubClassArmor.Shield.getValue()) {
            var armorQuality = CliDB.ItemArmorQualityStorage.get(itemLevel);
            var armorTotal = CliDB.ItemArmorTotalStorage.get(itemLevel);

            if (armorQuality == null || armorTotal == null) {
                return 0;
            }

            var inventoryType = getInventoryType();

            if (inventoryType == inventoryType.Robe) {
                inventoryType = inventoryType.chest;
            }

            var location = CliDB.ArmorLocationStorage.get(inventoryType);

            if (location == null) {
                return 0;
            }

            if (getSubClass() < (int) ItemSubClassArmor.Cloth.getValue() || getSubClass() > (int) ItemSubClassArmor.Plate.getValue()) {
                return 0;
            }

            var total = 1.0f;
            var locationModifier = 1.0f;

            switch (ItemSubClassArmor.forValue(getSubClass())) {
                case Cloth:
                    total = armorTotal.Cloth;
                    locationModifier = location.Clothmodifier;

                    break;
                case Leather:
                    total = armorTotal.Leather;
                    locationModifier = location.Leathermodifier;

                    break;
                case Mail:
                    total = armorTotal.MAIL;
                    locationModifier = location.Chainmodifier;

                    break;
                case Plate:
                    total = armorTotal.Plate;
                    locationModifier = location.Platemodifier;

                    break;
                default:
                    break;
            }

            return (int) (armorQuality.QualityMod[quality.getValue()] * total * locationModifier + 0.5f);
        }

        // shields
        var shield = CliDB.ItemArmorShieldStorage.get(itemLevel);

        if (shield == null) {
            return 0;
        }

        return (int) (shield.Quality[quality.getValue()] + 0.5f);
    }

    public final float getDPS(int itemLevel) {
        var quality = getQuality() != itemQuality.Heirloom ? getQuality() : itemQuality.Rare;

        if (getClass() != itemClass.Weapon || quality.getValue() > itemQuality.artifact.getValue()) {
            return 0.0f;
        }

        var dps = 0.0f;

        switch (getInventoryType()) {
            case Ammo:
                dps = CliDB.ItemDamageAmmoStorage.get(itemLevel).Quality[quality.getValue()];

                break;
            case Weapon2Hand:
                if (hasFlag(ItemFlags2.CasterWeapon)) {
                    dps = CliDB.ItemDamageTwoHandCasterStorage.get(itemLevel).Quality[quality.getValue()];
                } else {
                    dps = CliDB.ItemDamageTwoHandStorage.get(itemLevel).Quality[quality.getValue()];
                }

                break;
            case Ranged:
            case Thrown:
            case RangedRight:
                switch (ItemSubClassWeapon.forValue(getSubClass())) {
                    case Wand:
                        dps = CliDB.ItemDamageOneHandCasterStorage.get(itemLevel).Quality[quality.getValue()];

                        break;
                    case Bow:
                    case Gun:
                    case Crossbow:
                        if (hasFlag(ItemFlags2.CasterWeapon)) {
                            dps = CliDB.ItemDamageTwoHandCasterStorage.get(itemLevel).Quality[quality.getValue()];
                        } else {
                            dps = CliDB.ItemDamageTwoHandStorage.get(itemLevel).Quality[quality.getValue()];
                        }

                        break;
                    default:
                        break;
                }

                break;
            case Weapon:
            case WeaponMainhand:
            case WeaponOffhand:
                if (hasFlag(ItemFlags2.CasterWeapon)) {
                    dps = CliDB.ItemDamageOneHandCasterStorage.get(itemLevel).Quality[quality.getValue()];
                } else {
                    dps = CliDB.ItemDamageOneHandStorage.get(itemLevel).Quality[quality.getValue()];
                }

                break;
            default:
                break;
        }

        return dps;
    }

    public final void getDamage(int itemLevel, tangible.OutObject<Float> minDamage, tangible.OutObject<Float> maxDamage) {
        minDamage.outArgValue = maxDamage.outArgValue = 0.0f;
        var dps = getDPS(itemLevel);

        if (dps > 0.0f) {
            var avgDamage = dps * getDelay() * 0.001f;
            minDamage.outArgValue = (getDmgVariance() * -0.5f + 1.0f) * avgDamage;
            maxDamage.outArgValue = (float) Math.floor(avgDamage * (getDmgVariance() * 0.5f + 1.0f) + 0.5f);
        }
    }

    public final boolean isUsableByLootSpecialization(Player player, boolean alwaysAllowBoundToAccount) {
        if (hasFlag(ItemFlags.IsBoundToAccount) && alwaysAllowBoundToAccount) {
            return true;
        }

        var spec = player.getLootSpecId();

        if (spec == 0) {
            spec = player.getPrimarySpecialization();
        }

        if (spec == 0) {
            spec = player.getDefaultSpecId();
        }

        var chrSpecialization = CliDB.ChrSpecializationStorage.get(spec);

        if (chrSpecialization == null) {
            return false;
        }

        var levelIndex = 0;

        if (player.getLevel() >= 110) {
            levelIndex = 2;
        } else if (player.getLevel() > 40) {
            levelIndex = 1;
        }

        return getSpecializations()[levelIndex].Get(calculateItemSpecBit(chrSpecialization));
    }

    public final int getStatModifierBonusStat(int index) {
        return extendedData.StatModifierBonusStat[index];
    }

    public final int getStatPercentEditor(int index) {
        return extendedData.StatPercentEditor[index];
    }

    public final float getStatPercentageOfSocket(int index) {
        return extendedData.StatPercentageOfSocket[index];
    }

    public final int getArea(int index) {
        return extendedData.ZoneBound[index];
    }

    public final SocketColor getSocketColor(int index) {
        return SocketColor.forValue(extendedData.SocketType[index]);
    }
}
