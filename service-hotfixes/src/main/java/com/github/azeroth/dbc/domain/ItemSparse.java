package com.github.azeroth.dbc.domain;

import com.github.azeroth.cache.DbcEntity;
import com.github.azeroth.common.LocalizedString;
import com.github.azeroth.dbc.db2.Db2Field;
import com.github.azeroth.dbc.db2.Db2DataBind;
import com.github.azeroth.dbc.db2.Db2Type;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString


@Table(name = "item_sparse")
@Db2DataBind(name = "ItemSparse.db2", layoutHash = 0x4007DE16, fields = {
        @Db2Field(name = "allowableRace", type = Db2Type.LONG, signed = true),
        @Db2Field(name = "display", type = Db2Type.STRING),
        @Db2Field(name = "display1", type = Db2Type.STRING),
        @Db2Field(name = "display2", type = Db2Type.STRING),
        @Db2Field(name = "display3", type = Db2Type.STRING),
        @Db2Field(name = "description", type = Db2Type.STRING),
        @Db2Field(name = {"flags1", "flags2", "flags3", "flags4"}, type = Db2Type.INT, signed = true),
        @Db2Field(name = "priceRandomValue", type = Db2Type.FLOAT),
        @Db2Field(name = "priceVariance", type = Db2Type.FLOAT),
        @Db2Field(name = "vendorStackCount", type = Db2Type.INT),
        @Db2Field(name = "buyPrice", type = Db2Type.INT),
        @Db2Field(name = "sellPrice", type = Db2Type.INT),
        @Db2Field(name = "requiredAbility", type = Db2Type.INT),
        @Db2Field(name = "maxCount", type = Db2Type.INT, signed = true),
        @Db2Field(name = "stackable", type = Db2Type.INT, signed = true),
        @Db2Field(name = {"statPercentEditor1", "statPercentEditor2", "statPercentEditor3", "statPercentEditor4", "statPercentEditor5", "statPercentEditor6", "statPercentEditor7", "statPercentEditor8", "statPercentEditor9", "statPercentEditor10"}, type = Db2Type.INT, signed = true),
        @Db2Field(name = {"statPercentageOfSocket1", "statPercentageOfSocket2", "statPercentageOfSocket3", "statPercentageOfSocket4", "statPercentageOfSocket5", "statPercentageOfSocket6", "statPercentageOfSocket7", "statPercentageOfSocket8", "statPercentageOfSocket9", "statPercentageOfSocket10"}, type = Db2Type.FLOAT),
        @Db2Field(name = "itemRange", type = Db2Type.FLOAT),
        @Db2Field(name = "bagFamily", type = Db2Type.INT),
        @Db2Field(name = "qualityModifier", type = Db2Type.FLOAT),
        @Db2Field(name = "durationInInventory", type = Db2Type.INT),
        @Db2Field(name = "dmgVariance", type = Db2Type.FLOAT),
        @Db2Field(name = "allowableClass", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "itemLevel", type = Db2Type.SHORT),
        @Db2Field(name = "requiredSkill", type = Db2Type.SHORT),
        @Db2Field(name = "requiredSkillRank", type = Db2Type.SHORT),
        @Db2Field(name = "minFactionID", type = Db2Type.SHORT),
        @Db2Field(name = {"itemStatValue1", "itemStatValue2", "itemStatValue3", "itemStatValue4", "itemStatValue5", "itemStatValue6", "itemStatValue7", "itemStatValue8", "itemStatValue9", "itemStatValue10"}, type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "scalingStatDistributionID", type = Db2Type.SHORT),
        @Db2Field(name = "itemDelay", type = Db2Type.SHORT),
        @Db2Field(name = "pageID", type = Db2Type.SHORT),
        @Db2Field(name = "startQuestID", type = Db2Type.SHORT),
        @Db2Field(name = "lockID", type = Db2Type.SHORT),
        @Db2Field(name = "randomSelect", type = Db2Type.SHORT),
        @Db2Field(name = "itemRandomSuffixGroupID", type = Db2Type.SHORT),
        @Db2Field(name = "itemSet", type = Db2Type.SHORT),
        @Db2Field(name = "zoneBound", type = Db2Type.SHORT),
        @Db2Field(name = "instanceBound", type = Db2Type.SHORT),
        @Db2Field(name = "totemCategoryID", type = Db2Type.SHORT),
        @Db2Field(name = "socketMatchEnchantmentId", type = Db2Type.SHORT),
        @Db2Field(name = "gemProperties", type = Db2Type.SHORT),
        @Db2Field(name = "limitCategory", type = Db2Type.SHORT),
        @Db2Field(name = "requiredHoliday", type = Db2Type.SHORT),
        @Db2Field(name = "requiredTransmogHoliday", type = Db2Type.SHORT),
        @Db2Field(name = "itemNameDescriptionID", type = Db2Type.SHORT),
        @Db2Field(name = "overallQualityID", type = Db2Type.BYTE),
        @Db2Field(name = "inventoryType", type = Db2Type.BYTE),
        @Db2Field(name = "requiredLevel", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "requiredPVPRank", type = Db2Type.BYTE),
        @Db2Field(name = "requiredPVPMedal", type = Db2Type.BYTE),
        @Db2Field(name = "minReputation", type = Db2Type.BYTE),
        @Db2Field(name = "containerSlots", type = Db2Type.BYTE),
        @Db2Field(name = {"statModifierBonusStat1", "statModifierBonusStat2", "statModifierBonusStat3", "statModifierBonusStat4", "statModifierBonusStat5", "statModifierBonusStat6", "statModifierBonusStat7", "statModifierBonusStat8", "statModifierBonusStat9", "statModifierBonusStat10"}, type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "damageDamageType", type = Db2Type.BYTE),
        @Db2Field(name = "bonding", type = Db2Type.BYTE),
        @Db2Field(name = "languageID", type = Db2Type.BYTE),
        @Db2Field(name = "pageMaterialID", type = Db2Type.BYTE),
        @Db2Field(name = "material", type = Db2Type.BYTE),
        @Db2Field(name = "sheatheType", type = Db2Type.BYTE),
        @Db2Field(name = {"socketType1", "socketType2", "socketType3"}, type = Db2Type.BYTE),
        @Db2Field(name = "spellWeightCategory", type = Db2Type.BYTE),
        @Db2Field(name = "spellWeight", type = Db2Type.BYTE),
        @Db2Field(name = "artifactID", type = Db2Type.BYTE),
        @Db2Field(name = "expansionID", type = Db2Type.BYTE)
})
public class ItemSparse implements DbcEntity {
    @Id
    
    @Column("ID")
    private int id;

    @Column("AllowableRace")
    private Long allowableRace;

    @Column("Display")
    private LocalizedString display;

    @Column("Display1")
    private LocalizedString display1;

    @Column("Display2")
    private LocalizedString display2;

    @Column("Display3")
    private LocalizedString display3;

    @Column("Description")
    private LocalizedString description;

    @Column("Flags1")
    private Integer flags1;

    @Column("Flags2")
    private Integer flags2;

    @Column("Flags3")
    private Integer flags3;

    @Column("Flags4")
    private Integer flags4;

    @Column("PriceRandomValue")
    private Float priceRandomValue;

    @Column("PriceVariance")
    private Float priceVariance;

    @Column("VendorStackCount")
    private Integer vendorStackCount;

    @Column("BuyPrice")
    private Integer buyPrice;

    @Column("SellPrice")
    private Integer sellPrice;

    @Column("RequiredAbility")
    private Integer requiredAbility;

    @Column("MaxCount")
    private Integer maxCount;

    @Column("Stackable")
    private Integer stackable;

    @Column("StatPercentEditor1")
    private Integer statPercentEditor1;

    @Column("StatPercentEditor2")
    private Integer statPercentEditor2;

    @Column("StatPercentEditor3")
    private Integer statPercentEditor3;

    @Column("StatPercentEditor4")
    private Integer statPercentEditor4;

    @Column("StatPercentEditor5")
    private Integer statPercentEditor5;

    @Column("StatPercentEditor6")
    private Integer statPercentEditor6;

    @Column("StatPercentEditor7")
    private Integer statPercentEditor7;

    @Column("StatPercentEditor8")
    private Integer statPercentEditor8;

    @Column("StatPercentEditor9")
    private Integer statPercentEditor9;

    @Column("StatPercentEditor10")
    private Integer statPercentEditor10;

    @Column("StatPercentageOfSocket1")
    private Float statPercentageOfSocket1;

    @Column("StatPercentageOfSocket2")
    private Float statPercentageOfSocket2;

    @Column("StatPercentageOfSocket3")
    private Float statPercentageOfSocket3;

    @Column("StatPercentageOfSocket4")
    private Float statPercentageOfSocket4;

    @Column("StatPercentageOfSocket5")
    private Float statPercentageOfSocket5;

    @Column("StatPercentageOfSocket6")
    private Float statPercentageOfSocket6;

    @Column("StatPercentageOfSocket7")
    private Float statPercentageOfSocket7;

    @Column("StatPercentageOfSocket8")
    private Float statPercentageOfSocket8;

    @Column("StatPercentageOfSocket9")
    private Float statPercentageOfSocket9;

    @Column("StatPercentageOfSocket10")
    private Float statPercentageOfSocket10;

    @Column("ItemRange")
    private Float itemRange;

    @Column("BagFamily")
    private Integer bagFamily;

    @Column("QualityModifier")
    private Float qualityModifier;

    @Column("DurationInInventory")
    private Integer durationInInventory;

    @Column("DmgVariance")
    private Float dmgVariance;

    @Column("AllowableClass")
    private Short allowableClass;

    @Column("ItemLevel")
    private Short itemLevel;

    @Column("RequiredSkill")
    private Short requiredSkill;

    @Column("RequiredSkillRank")
    private Short requiredSkillRank;

    @Column("MinFactionID")
    private Short minFactionID;

    @Column("ItemStatValue1")
    private Short itemStatValue1;

    @Column("ItemStatValue2")
    private Short itemStatValue2;

    @Column("ItemStatValue3")
    private Short itemStatValue3;

    @Column("ItemStatValue4")
    private Short itemStatValue4;

    @Column("ItemStatValue5")
    private Short itemStatValue5;

    @Column("ItemStatValue6")
    private Short itemStatValue6;

    @Column("ItemStatValue7")
    private Short itemStatValue7;

    @Column("ItemStatValue8")
    private Short itemStatValue8;

    @Column("ItemStatValue9")
    private Short itemStatValue9;

    @Column("ItemStatValue10")
    private Short itemStatValue10;

    @Column("ScalingStatDistributionID")
    private Short scalingStatDistributionID;

    @Column("ItemDelay")
    private Short itemDelay;

    @Column("PageID")
    private Short pageID;

    @Column("StartQuestID")
    private Integer startQuestID;

    @Column("LockID")
    private Short lockID;

    @Column("RandomSelect")
    private Short randomSelect;

    @Column("ItemRandomSuffixGroupID")
    private Short itemRandomSuffixGroupID;

    @Column("ItemSet")
    private Short itemSet;

    @Column("ZoneBound")
    private Short zoneBound;

    @Column("InstanceBound")
    private Short instanceBound;

    @Column("TotemCategoryID")
    private Short totemCategoryID;

    @Column("SocketMatchEnchantmentId")
    private Short socketMatchEnchantmentId;

    @Column("GemProperties")
    private Short gemProperties;

    @Column("LimitCategory")
    private Short limitCategory;

    @Column("RequiredHoliday")
    private Short requiredHoliday;

    @Column("RequiredTransmogHoliday")
    private Short requiredTransmogHoliday;

    @Column("ItemNameDescriptionID")
    private Short itemNameDescriptionID;

    @Column("OverallQualityID")
    private Byte overallQualityID;

    @Column("InventoryType")
    private Byte inventoryType;

    @Column("RequiredLevel")
    private Byte requiredLevel;

    @Column("RequiredPVPRank")
    private Byte requiredPVPRank;

    @Column("RequiredPVPMedal")
    private Byte requiredPVPMedal;

    @Column("MinReputation")
    private Byte minReputation;

    @Column("ContainerSlots")
    private Byte containerSlots;

    @Column("StatModifierBonusStat1")
    private Byte statModifierBonusStat1;

    @Column("StatModifierBonusStat2")
    private Byte statModifierBonusStat2;

    @Column("StatModifierBonusStat3")
    private Byte statModifierBonusStat3;

    @Column("StatModifierBonusStat4")
    private Byte statModifierBonusStat4;

    @Column("StatModifierBonusStat5")
    private Byte statModifierBonusStat5;

    @Column("StatModifierBonusStat6")
    private Byte statModifierBonusStat6;

    @Column("StatModifierBonusStat7")
    private Byte statModifierBonusStat7;

    @Column("StatModifierBonusStat8")
    private Byte statModifierBonusStat8;

    @Column("StatModifierBonusStat9")
    private Byte statModifierBonusStat9;

    @Column("StatModifierBonusStat10")
    private Byte statModifierBonusStat10;

    @Column("DamageDamageType")
    private Byte damageDamageType;

    @Column("Bonding")
    private Byte bonding;

    @Column("LanguageID")
    private Byte languageID;

    @Column("PageMaterialID")
    private Byte pageMaterialID;

    @Column("Material")
    private Byte material;

    @Column("SheatheType")
    private Byte sheatheType;

    @Column("SocketType1")
    private Byte socketType1;

    @Column("SocketType2")
    private Byte socketType2;

    @Column("SocketType3")
    private Byte socketType3;

    @Column("SpellWeightCategory")
    private Byte spellWeightCategory;

    @Column("SpellWeight")
    private Byte spellWeight;

    @Column("ArtifactID")
    private Byte artifactID;

    @Column("ExpansionID")
    private Byte expansionID;

    @Id
    @Column("VerifiedBuild")
    private Integer verifiedBuild;

    public byte getStatModifierBonusStat(int index) {
        return switch (index) {
            case 0 -> statModifierBonusStat1;
            case 1 -> statModifierBonusStat2;
            case 2 -> statModifierBonusStat3;
            case 3 -> statModifierBonusStat4;
            case 4 -> statModifierBonusStat5;
            case 5 -> statModifierBonusStat6;
            case 6 -> statModifierBonusStat7;
            case 7 -> statModifierBonusStat8;
            case 8 -> statModifierBonusStat9;
            case 9 -> statModifierBonusStat10;
            default -> throw new ArrayIndexOutOfBoundsException("Out of bounds: " + index);
        };
    }

    public short getItemStatValue(int index) {
        return switch (index) {
            case 0 -> itemStatValue1;
            case 1 -> itemStatValue2;
            case 2 -> itemStatValue3;
            case 3 -> itemStatValue4;
            case 4 -> itemStatValue5;
            case 5 -> itemStatValue6;
            case 6 -> itemStatValue7;
            case 7 -> itemStatValue8;
            case 8 -> itemStatValue9;
            case 9 -> itemStatValue10;
            default -> throw new ArrayIndexOutOfBoundsException("Out of bounds: " + index);
        };
    }

    public int getStatPercentEditor(int index) {
        return switch (index) {
            case 0 -> statPercentEditor1;
            case 1 -> statPercentEditor2;
            case 2 -> statPercentEditor3;
            case 3 -> statPercentEditor4;
            case 4 -> statPercentEditor5;
            case 5 -> statPercentEditor6;
            case 6 -> statPercentEditor7;
            case 7 -> statPercentEditor8;
            case 8 -> statPercentEditor9;
            case 9 -> statPercentEditor10;
            default -> throw new ArrayIndexOutOfBoundsException("Out of bounds: " + index);
        };
    }

    public float getStatPercentageOfSocket(int index) {
        return switch (index) {
            case 0 -> statPercentageOfSocket1;
            case 1 -> statPercentageOfSocket2;
            case 2 -> statPercentageOfSocket3;
            case 3 -> statPercentageOfSocket4;
            case 4 -> statPercentageOfSocket5;
            case 5 -> statPercentageOfSocket6;
            case 6 -> statPercentageOfSocket7;
            case 7 -> statPercentageOfSocket8;
            case 8 -> statPercentageOfSocket9;
            case 9 -> statPercentageOfSocket10;
            default -> throw new ArrayIndexOutOfBoundsException("Out of bounds: " + index);
        };
    }
}
