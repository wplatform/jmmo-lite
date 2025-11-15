package com.github.azeroth.dbc.domain;

import com.github.azeroth.cache.DbcEntity;
import com.github.azeroth.common.LocalizedString;
import com.github.azeroth.dbc.db2.Db2Field;
import com.github.azeroth.dbc.db2.Db2DataBind;
import com.github.azeroth.dbc.db2.Db2Type;
import com.github.azeroth.defines.PlayerClassMask;
import com.github.azeroth.defines.RaceMask;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString


@Table(name = "player_condition")
@Db2DataBind(name = "PlayerCondition.db2", layoutHash = 0x5B3DA113, indexField = 2, fields = {
        @Db2Field(name = "raceMask", type = Db2Type.LONG, signed = true),
        @Db2Field(name = "failureDescription", type = Db2Type.STRING),
        @Db2Field(name = "id", type = Db2Type.INT),
        @Db2Field(name = "flags", type = Db2Type.BYTE),
        @Db2Field(name = "minLevel", type = Db2Type.SHORT),
        @Db2Field(name = "maxLevel", type = Db2Type.SHORT),
        @Db2Field(name = "classMask", type = Db2Type.INT, signed = true),
        @Db2Field(name = "gender", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "nativeGender", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "skillLogic", type = Db2Type.INT),
        @Db2Field(name = "languageID", type = Db2Type.BYTE),
        @Db2Field(name = "minLanguage", type = Db2Type.BYTE),
        @Db2Field(name = "maxLanguage", type = Db2Type.INT),
        @Db2Field(name = "maxFactionID", type = Db2Type.SHORT),
        @Db2Field(name = "maxReputation", type = Db2Type.BYTE),
        @Db2Field(name = "reputationLogic", type = Db2Type.INT),
        @Db2Field(name = "currentPvpFaction", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "minPVPRank", type = Db2Type.BYTE),
        @Db2Field(name = "maxPVPRank", type = Db2Type.BYTE),
        @Db2Field(name = "pvpMedal", type = Db2Type.BYTE),
        @Db2Field(name = "prevQuestLogic", type = Db2Type.INT),
        @Db2Field(name = "currQuestLogic", type = Db2Type.INT),
        @Db2Field(name = "currentCompletedQuestLogic", type = Db2Type.INT),
        @Db2Field(name = "spellLogic", type = Db2Type.INT),
        @Db2Field(name = "itemLogic", type = Db2Type.INT),
        @Db2Field(name = "itemFlags", type = Db2Type.BYTE),
        @Db2Field(name = "auraSpellLogic", type = Db2Type.INT),
        @Db2Field(name = "worldStateExpressionID", type = Db2Type.SHORT),
        @Db2Field(name = "weatherID", type = Db2Type.BYTE),
        @Db2Field(name = "partyStatus", type = Db2Type.BYTE),
        @Db2Field(name = "lifetimeMaxPVPRank", type = Db2Type.BYTE),
        @Db2Field(name = "achievementLogic", type = Db2Type.INT),
        @Db2Field(name = "lfgLogic", type = Db2Type.INT),
        @Db2Field(name = "areaLogic", type = Db2Type.INT),
        @Db2Field(name = "currencyLogic", type = Db2Type.INT),
        @Db2Field(name = "questKillID", type = Db2Type.SHORT),
        @Db2Field(name = "questKillLogic", type = Db2Type.INT),
        @Db2Field(name = "minExpansionLevel", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "maxExpansionLevel", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "minExpansionTier", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "maxExpansionTier", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "minGuildLevel", type = Db2Type.BYTE),
        @Db2Field(name = "maxGuildLevel", type = Db2Type.BYTE),
        @Db2Field(name = "phaseUseFlags", type = Db2Type.BYTE),
        @Db2Field(name = "phaseID", type = Db2Type.SHORT),
        @Db2Field(name = "phaseGroupID", type = Db2Type.INT),
        @Db2Field(name = "minAvgItemLevel", type = Db2Type.INT, signed = true),
        @Db2Field(name = "maxAvgItemLevel", type = Db2Type.INT, signed = true),
        @Db2Field(name = "minAvgEquippedItemLevel", type = Db2Type.SHORT),
        @Db2Field(name = "maxAvgEquippedItemLevel", type = Db2Type.SHORT),
        @Db2Field(name = "chrSpecializationIndex", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "chrSpecializationRole", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "powerType", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "powerTypeComp", type = Db2Type.BYTE),
        @Db2Field(name = "powerTypeValue", type = Db2Type.BYTE),
        @Db2Field(name = "ModifierTreeID", type = Db2Type.INT),
        @Db2Field(name = "weaponSubclassMask", type = Db2Type.INT, signed = true),
        @Db2Field(name = {"skillID1", "skillID2", "skillID3", "skillID4"}, type = Db2Type.SHORT),
        @Db2Field(name = {"minSkill1", "minSkill2", "minSkill3", "minSkill4"}, type = Db2Type.SHORT),
        @Db2Field(name = {"maxSkill1", "maxSkill2", "maxSkill3", "maxSkill4"}, type = Db2Type.SHORT),
        @Db2Field(name = {"minFactionID1", "minFactionID2", "minFactionID3"}, type = Db2Type.INT),
        @Db2Field(name = {"minReputation1", "minReputation2", "minReputation3"}, type = Db2Type.BYTE),
        @Db2Field(name = {"prevQuestID1", "prevQuestID2", "prevQuestID3", "prevQuestID4"}, type = Db2Type.SHORT),
        @Db2Field(name = {"currQuestID1", "currQuestID2", "currQuestID3", "currQuestID4"}, type = Db2Type.SHORT),
        @Db2Field(name = {"currentCompletedQuestID1", "currentCompletedQuestID2", "currentCompletedQuestID3", "currentCompletedQuestID4"}, type = Db2Type.SHORT),
        @Db2Field(name = {"spellID1", "spellID2", "spellID3", "spellID4"}, type = Db2Type.INT, signed = true),
        @Db2Field(name = {"itemID1", "itemID2", "itemID3", "itemID4"}, type = Db2Type.INT, signed = true),
        @Db2Field(name = {"itemCount1", "itemCount2", "itemCount3", "itemCount4"}, type = Db2Type.INT),
        @Db2Field(name = {"explored1", "explored2"}, type = Db2Type.SHORT),
        @Db2Field(name = {"time1", "time2"}, type = Db2Type.INT),
        @Db2Field(name = {"auraSpellID1", "auraSpellID2", "auraSpellID3", "auraSpellID4"}, type = Db2Type.INT, signed = true),
        @Db2Field(name = {"auraStacks1", "auraStacks2", "auraStacks3", "auraStacks4"}, type = Db2Type.BYTE),
        @Db2Field(name = {"achievement1", "achievement2", "achievement3", "achievement4"}, type = Db2Type.SHORT),
        @Db2Field(name = {"lfgStatus1", "lfgStatus2", "lfgStatus3", "lfgStatus4"}, type = Db2Type.BYTE),
        @Db2Field(name = {"lfgCompare1", "lfgCompare2", "lfgCompare3", "lfgCompare4"}, type = Db2Type.BYTE),
        @Db2Field(name = {"lfgValue1", "lfgValue2", "lfgValue3", "lfgValue4"}, type = Db2Type.INT),
        @Db2Field(name = {"areaID1", "areaID2", "areaID3", "areaID4"}, type = Db2Type.SHORT),
        @Db2Field(name = {"currencyID1", "currencyID2", "currencyID3", "currencyID4"}, type = Db2Type.INT),
        @Db2Field(name = {"currencyCount1", "currencyCount2", "currencyCount3", "currencyCount4"}, type = Db2Type.INT),
        @Db2Field(name = {"questKillMonster1", "questKillMonster2", "questKillMonster3", "questKillMonster4", "questKillMonster5", "questKillMonster6"}, type = Db2Type.INT),
        @Db2Field(name = {"movementFlags1", "movementFlags2"}, type = Db2Type.INT, signed = true)
})
public class PlayerCondition implements DbcEntity {
    @Column("RaceMask")
    private Long raceMask;

    @Column("FailureDescription")
    private LocalizedString failureDescription;

    @Id
    @Column("ID")
    private int id;

    @Column("Flags")
    private Byte flags;

    @Column("MinLevel")
    private Short minLevel;

    @Column("MaxLevel")
    private Short maxLevel;

    @Column("ClassMask")
    private Integer classMask;

    @Column("Gender")
    private Byte gender;

    @Column("NativeGender")
    private Byte nativeGender;

    @Column("SkillLogic")
    private Integer skillLogic;

    @Column("LanguageID")
    private Short languageID;

    @Column("MinLanguage")
    private Byte minLanguage;

    @Column("MaxLanguage")
    private Integer maxLanguage;

    @Column("MaxFactionID")
    private Short maxFactionID;

    @Column("MaxReputation")
    private Byte maxReputation;

    @Column("ReputationLogic")
    private Integer reputationLogic;

    @Column("CurrentPvpFaction")
    private Byte currentPvpFaction;

    @Column("MinPVPRank")
    private Byte minPVPRank;

    @Column("MaxPVPRank")
    private Byte maxPVPRank;

    @Column("PvpMedal")
    private Byte pvpMedal;

    @Column("PrevQuestLogic")
    private Integer prevQuestLogic;

    @Column("CurrQuestLogic")
    private Integer currQuestLogic;

    @Column("CurrentCompletedQuestLogic")
    private Integer currentCompletedQuestLogic;

    @Column("SpellLogic")
    private Integer spellLogic;

    @Column("ItemLogic")
    private Integer itemLogic;

    @Column("ItemFlags")
    private Byte itemFlags;

    @Column("AuraSpellLogic")
    private Integer auraSpellLogic;

    @Column("WorldStateExpressionID")
    private Short worldStateExpressionID;

    @Column("WeatherID")
    private Byte weatherID;

    @Column("PartyStatus")
    private Byte partyStatus;

    @Column("LifetimeMaxPVPRank")
    private Byte lifetimeMaxPVPRank;

    @Column("AchievementLogic")
    private Integer achievementLogic;

    @Column("LfgLogic")
    private Integer lfgLogic;

    @Column("AreaLogic")
    private Integer areaLogic;

    @Column("CurrencyLogic")
    private Integer currencyLogic;

    @Column("QuestKillID")
    private Integer questKillID;

    @Column("QuestKillLogic")
    private Integer questKillLogic;

    @Column("MinExpansionLevel")
    private Byte minExpansionLevel;

    @Column("MaxExpansionLevel")
    private Byte maxExpansionLevel;

    @Column("MinExpansionTier")
    private Byte minExpansionTier;

    @Column("MaxExpansionTier")
    private Byte maxExpansionTier;

    @Column("MinGuildLevel")
    private Byte minGuildLevel;

    @Column("MaxGuildLevel")
    private Byte maxGuildLevel;

    @Column("PhaseUseFlags")
    private Byte phaseUseFlags;

    @Column("PhaseID")
    private Short phaseID;

    @Column("PhaseGroupID")
    private Integer phaseGroupID;

    @Column("MinAvgItemLevel")
    private Integer minAvgItemLevel;

    @Column("MaxAvgItemLevel")
    private Integer maxAvgItemLevel;

    @Column("MinAvgEquippedItemLevel")
    private Short minAvgEquippedItemLevel;

    @Column("MaxAvgEquippedItemLevel")
    private Short maxAvgEquippedItemLevel;

    @Column("ChrSpecializationIndex")
    private Byte chrSpecializationIndex;

    @Column("ChrSpecializationRole")
    private Byte chrSpecializationRole;

    @Column("PowerType")
    private Byte powerType;

    @Column("PowerTypeComp")
    private Byte powerTypeComp;

    @Column("PowerTypeValue")
    private Byte powerTypeValue;

    @Column("ModifierTreeID")
    private Integer ModifierTreeID;

    @Column("WeaponSubclassMask")
    private Integer weaponSubclassMask;

    @Column("SkillID1")
    private Short skillID1;

    @Column("SkillID2")
    private Short skillID2;

    @Column("SkillID3")
    private Short skillID3;

    @Column("SkillID4")
    private Short skillID4;

    @Column("MinSkill1")
    private Short minSkill1;

    @Column("MinSkill2")
    private Short minSkill2;

    @Column("MinSkill3")
    private Short minSkill3;

    @Column("MinSkill4")
    private Short minSkill4;

    @Column("MaxSkill1")
    private Short maxSkill1;

    @Column("MaxSkill2")
    private Short maxSkill2;

    @Column("MaxSkill3")
    private Short maxSkill3;

    @Column("MaxSkill4")
    private Short maxSkill4;

    @Column("MinFactionID1")
    private Integer minFactionID1;

    @Column("MinFactionID2")
    private Integer minFactionID2;

    @Column("MinFactionID3")
    private Integer minFactionID3;

    @Column("MinReputation1")
    private Byte minReputation1;

    @Column("MinReputation2")
    private Byte minReputation2;

    @Column("MinReputation3")
    private Byte minReputation3;

    @Column("PrevQuestID1")
    private Integer prevQuestID1;

    @Column("PrevQuestID2")
    private Integer prevQuestID2;

    @Column("PrevQuestID3")
    private Integer prevQuestID3;

    @Column("PrevQuestID4")
    private Integer prevQuestID4;

    @Column("CurrQuestID1")
    private Integer currQuestID1;

    @Column("CurrQuestID2")
    private Integer currQuestID2;

    @Column("CurrQuestID3")
    private Integer currQuestID3;

    @Column("CurrQuestID4")
    private Integer currQuestID4;

    @Column("CurrentCompletedQuestID1")
    private Integer currentCompletedQuestID1;

    @Column("CurrentCompletedQuestID2")
    private Integer currentCompletedQuestID2;

    @Column("CurrentCompletedQuestID3")
    private Integer currentCompletedQuestID3;

    @Column("CurrentCompletedQuestID4")
    private Integer currentCompletedQuestID4;

    @Column("SpellID1")
    private Integer spellID1;

    @Column("SpellID2")
    private Integer spellID2;

    @Column("SpellID3")
    private Integer spellID3;

    @Column("SpellID4")
    private Integer spellID4;

    @Column("ItemID1")
    private Integer itemID1;

    @Column("ItemID2")
    private Integer itemID2;

    @Column("ItemID3")
    private Integer itemID3;

    @Column("ItemID4")
    private Integer itemID4;

    @Column("ItemCount1")
    private Integer itemCount1;

    @Column("ItemCount2")
    private Integer itemCount2;

    @Column("ItemCount3")
    private Integer itemCount3;

    @Column("ItemCount4")
    private Integer itemCount4;

    @Column("Explored1")
    private Short explored1;

    @Column("Explored2")
    private Short explored2;

    @Column("Time1")
    private Integer time1;

    @Column("Time2")
    private Integer time2;

    @Column("AuraSpellID1")
    private Integer auraSpellID1;

    @Column("AuraSpellID2")
    private Integer auraSpellID2;

    @Column("AuraSpellID3")
    private Integer auraSpellID3;

    @Column("AuraSpellID4")
    private Integer auraSpellID4;

    @Column("AuraStacks1")
    private Short auraStacks1;

    @Column("AuraStacks2")
    private Short auraStacks2;

    @Column("AuraStacks3")
    private Short auraStacks3;

    @Column("AuraStacks4")
    private Short auraStacks4;

    @Column("Achievement1")
    private Short achievement1;

    @Column("Achievement2")
    private Short achievement2;

    @Column("Achievement3")
    private Short achievement3;

    @Column("Achievement4")
    private Short achievement4;

    @Column("LfgStatus1")
    private Byte lfgStatus1;

    @Column("LfgStatus2")
    private Byte lfgStatus2;

    @Column("LfgStatus3")
    private Byte lfgStatus3;

    @Column("LfgStatus4")
    private Byte lfgStatus4;

    @Column("LfgCompare1")
    private Byte lfgCompare1;

    @Column("LfgCompare2")
    private Byte lfgCompare2;

    @Column("LfgCompare3")
    private Byte lfgCompare3;

    @Column("LfgCompare4")
    private Byte lfgCompare4;

    @Column("LfgValue1")
    private Integer lfgValue1;

    @Column("LfgValue2")
    private Integer lfgValue2;

    @Column("LfgValue3")
    private Integer lfgValue3;

    @Column("LfgValue4")
    private Integer lfgValue4;

    @Column("AreaID1")
    private Short areaID1;

    @Column("AreaID2")
    private Short areaID2;

    @Column("AreaID3")
    private Short areaID3;

    @Column("AreaID4")
    private Short areaID4;

    @Column("CurrencyID1")
    private Integer currencyID1;

    @Column("CurrencyID2")
    private Integer currencyID2;

    @Column("CurrencyID3")
    private Integer currencyID3;

    @Column("CurrencyID4")
    private Integer currencyID4;

    @Column("CurrencyCount1")
    private Integer currencyCount1;

    @Column("CurrencyCount2")
    private Integer currencyCount2;

    @Column("CurrencyCount3")
    private Integer currencyCount3;

    @Column("CurrencyCount4")
    private Integer currencyCount4;

    @Column("QuestKillMonster1")
    private Integer questKillMonster1;

    @Column("QuestKillMonster2")
    private Integer questKillMonster2;

    @Column("QuestKillMonster3")
    private Integer questKillMonster3;

    @Column("QuestKillMonster4")
    private Integer questKillMonster4;

    @Column("QuestKillMonster5")
    private Integer questKillMonster5;

    @Column("QuestKillMonster6")
    private Integer questKillMonster6;

    @Column("MovementFlags1")
    private Integer movementFlags1;

    @Column("MovementFlags2")
    private Integer movementFlags2;

    @Id
    @Column("VerifiedBuild")
    private Integer verifiedBuild;


    public RaceMask getRaceMask() {
        return RaceMask.of(raceMask);
    }

    public short[] getSkillID() {
        return new short[]{skillID1, skillID2, skillID3, skillID4};
    }

    public int[] getItemID() {
        return new int[]{itemID1, itemID2, itemID3, itemID4};
    }

    public int[] getItemCount() {
        return new int[]{itemCount1, itemCount2, itemCount3, itemCount4};
    }

    public int[] getCurrencyID() {
        return new int[]{currencyID1, currencyID2, currencyID3, currencyID4};
    }

    public int[] getCurrencyCount() {
        return new int[]{currencyCount1, currencyCount2, currencyCount3, currencyCount4};
    }

    public int[] getQuestKillMonster() {
        return new int[]{questKillMonster1, questKillMonster2, questKillMonster3, questKillMonster4, questKillMonster5, questKillMonster6};
    }

    public int[] getMovementFlags() {
        return new int[]{movementFlags1, movementFlags2};
    }

    public int[] getExplored() {
        return new int[]{explored1, explored2};
    }

    public int[] getTime() {
        return new int[]{time1, time2};
    }

    public int[] getAuraSpellID() {
        return new int[]{auraSpellID1, auraSpellID2, auraSpellID3, auraSpellID4};
    }

    public int[] getAuraStacks() {
        return new int[]{auraStacks1, auraStacks2, auraStacks3, auraStacks4};
    }

    public int[] getAchievement() {
        return new int[]{achievement1, achievement2, achievement3, achievement4};
    }

    public int[] getLfgStatus() {
        return new int[]{lfgStatus1, lfgStatus2, lfgStatus3, lfgStatus4};
    }

    public int[] getLfgCompare() {
        return new int[]{lfgCompare1, lfgCompare2, lfgCompare3, lfgCompare4};
    }

    public int[] getLfgValue() {
        return new int[]{lfgValue1, lfgValue2, lfgValue3, lfgValue4};
    }

    public int[] getAreaID() {
        return new int[]{areaID1, areaID2, areaID3, areaID4};
    }

    public short[] getMinSkill() {
        return new short[]{minSkill1, minSkill2, minSkill3, minSkill4};
    }

    public short[] getMaxSkill() {
        return new short[]{maxSkill1, maxSkill2, maxSkill3, maxSkill4};
    }

    public int[] getMinFactionID() {
        return new int[]{minFactionID1, minFactionID2, minFactionID3};
    }

    public byte[] getMinReputation() {
        return new byte[]{minReputation1, minReputation2, minReputation3};
    }

    public int[] getPrevQuestID() {
        return new int[]{prevQuestID1, prevQuestID2, prevQuestID3, prevQuestID4};
    }
}
