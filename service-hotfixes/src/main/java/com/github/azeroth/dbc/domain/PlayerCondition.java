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
    private byte flags;

    @Column("MinLevel")
    private short minLevel;

    @Column("MaxLevel")
    private short maxLevel;

    @Column("ClassMask")
    private int classMask;

    @Column("Gender")
    private byte gender;

    @Column("NativeGender")
    private byte nativeGender;

    @Column("SkillLogic")
    private int skillLogic;

    @Column("LanguageID")
    private short languageID;

    @Column("MinLanguage")
    private byte minLanguage;

    @Column("MaxLanguage")
    private int maxLanguage;

    @Column("MaxFactionID")
    private short maxFactionID;

    @Column("MaxReputation")
    private byte maxReputation;

    @Column("ReputationLogic")
    private int reputationLogic;

    @Column("CurrentPvpFaction")
    private byte currentPvpFaction;

    @Column("MinPVPRank")
    private byte minPVPRank;

    @Column("MaxPVPRank")
    private byte maxPVPRank;

    @Column("PvpMedal")
    private byte pvpMedal;

    @Column("PrevQuestLogic")
    private int prevQuestLogic;

    @Column("CurrQuestLogic")
    private int currQuestLogic;

    @Column("CurrentCompletedQuestLogic")
    private int currentCompletedQuestLogic;

    @Column("SpellLogic")
    private int spellLogic;

    @Column("ItemLogic")
    private int itemLogic;

    @Column("ItemFlags")
    private byte itemFlags;

    @Column("AuraSpellLogic")
    private int auraSpellLogic;

    @Column("WorldStateExpressionID")
    private short worldStateExpressionID;

    @Column("WeatherID")
    private byte weatherID;

    @Column("PartyStatus")
    private byte partyStatus;

    @Column("LifetimeMaxPVPRank")
    private byte lifetimeMaxPVPRank;

    @Column("AchievementLogic")
    private int achievementLogic;

    @Column("LfgLogic")
    private int lfgLogic;

    @Column("AreaLogic")
    private int areaLogic;

    @Column("CurrencyLogic")
    private int currencyLogic;

    @Column("QuestKillID")
    private int questKillID;

    @Column("QuestKillLogic")
    private int questKillLogic;

    @Column("MinExpansionLevel")
    private byte minExpansionLevel;

    @Column("MaxExpansionLevel")
    private byte maxExpansionLevel;

    @Column("MinExpansionTier")
    private byte minExpansionTier;

    @Column("MaxExpansionTier")
    private byte maxExpansionTier;

    @Column("MinGuildLevel")
    private byte minGuildLevel;

    @Column("MaxGuildLevel")
    private byte maxGuildLevel;

    @Column("PhaseUseFlags")
    private byte phaseUseFlags;

    @Column("PhaseID")
    private short phaseID;

    @Column("PhaseGroupID")
    private int phaseGroupID;

    @Column("MinAvgItemLevel")
    private int minAvgItemLevel;

    @Column("MaxAvgItemLevel")
    private int maxAvgItemLevel;

    @Column("MinAvgEquippedItemLevel")
    private short minAvgEquippedItemLevel;

    @Column("MaxAvgEquippedItemLevel")
    private short maxAvgEquippedItemLevel;

    @Column("ChrSpecializationIndex")
    private byte chrSpecializationIndex;

    @Column("ChrSpecializationRole")
    private byte chrSpecializationRole;

    @Column("PowerType")
    private byte powerType;

    @Column("PowerTypeComp")
    private byte powerTypeComp;

    @Column("PowerTypeValue")
    private byte powerTypeValue;

    @Column("ModifierTreeID")
    private int ModifierTreeID;

    @Column("WeaponSubclassMask")
    private int weaponSubclassMask;

    @Column("SkillID1")
    private short skillID1;

    @Column("SkillID2")
    private short skillID2;

    @Column("SkillID3")
    private short skillID3;

    @Column("SkillID4")
    private short skillID4;

    @Column("MinSkill1")
    private short minSkill1;

    @Column("MinSkill2")
    private short minSkill2;

    @Column("MinSkill3")
    private short minSkill3;

    @Column("MinSkill4")
    private short minSkill4;

    @Column("MaxSkill1")
    private short maxSkill1;

    @Column("MaxSkill2")
    private short maxSkill2;

    @Column("MaxSkill3")
    private short maxSkill3;

    @Column("MaxSkill4")
    private short maxSkill4;

    @Column("MinFactionID1")
    private int minFactionID1;

    @Column("MinFactionID2")
    private int minFactionID2;

    @Column("MinFactionID3")
    private int minFactionID3;

    @Column("MinReputation1")
    private byte minReputation1;

    @Column("MinReputation2")
    private byte minReputation2;

    @Column("MinReputation3")
    private byte minReputation3;

    @Column("PrevQuestID1")
    private int prevQuestID1;

    @Column("PrevQuestID2")
    private int prevQuestID2;

    @Column("PrevQuestID3")
    private int prevQuestID3;

    @Column("PrevQuestID4")
    private int prevQuestID4;

    @Column("CurrQuestID1")
    private int currQuestID1;

    @Column("CurrQuestID2")
    private int currQuestID2;

    @Column("CurrQuestID3")
    private int currQuestID3;

    @Column("CurrQuestID4")
    private int currQuestID4;

    @Column("CurrentCompletedQuestID1")
    private int currentCompletedQuestID1;

    @Column("CurrentCompletedQuestID2")
    private int currentCompletedQuestID2;

    @Column("CurrentCompletedQuestID3")
    private int currentCompletedQuestID3;

    @Column("CurrentCompletedQuestID4")
    private int currentCompletedQuestID4;

    @Column("SpellID1")
    private int spellID1;

    @Column("SpellID2")
    private int spellID2;

    @Column("SpellID3")
    private int spellID3;

    @Column("SpellID4")
    private int spellID4;

    @Column("ItemID1")
    private int itemID1;

    @Column("ItemID2")
    private int itemID2;

    @Column("ItemID3")
    private int itemID3;

    @Column("ItemID4")
    private int itemID4;

    @Column("ItemCount1")
    private int itemCount1;

    @Column("ItemCount2")
    private int itemCount2;

    @Column("ItemCount3")
    private int itemCount3;

    @Column("ItemCount4")
    private int itemCount4;

    @Column("Explored1")
    private short explored1;

    @Column("Explored2")
    private short explored2;

    @Column("Time1")
    private int time1;

    @Column("Time2")
    private int time2;

    @Column("AuraSpellID1")
    private int auraSpellID1;

    @Column("AuraSpellID2")
    private int auraSpellID2;

    @Column("AuraSpellID3")
    private int auraSpellID3;

    @Column("AuraSpellID4")
    private int auraSpellID4;

    @Column("AuraStacks1")
    private short auraStacks1;

    @Column("AuraStacks2")
    private short auraStacks2;

    @Column("AuraStacks3")
    private short auraStacks3;

    @Column("AuraStacks4")
    private short auraStacks4;

    @Column("Achievement1")
    private short achievement1;

    @Column("Achievement2")
    private short achievement2;

    @Column("Achievement3")
    private short achievement3;

    @Column("Achievement4")
    private short achievement4;

    @Column("LfgStatus1")
    private byte lfgStatus1;

    @Column("LfgStatus2")
    private byte lfgStatus2;

    @Column("LfgStatus3")
    private byte lfgStatus3;

    @Column("LfgStatus4")
    private byte lfgStatus4;

    @Column("LfgCompare1")
    private byte lfgCompare1;

    @Column("LfgCompare2")
    private byte lfgCompare2;

    @Column("LfgCompare3")
    private byte lfgCompare3;

    @Column("LfgCompare4")
    private byte lfgCompare4;

    @Column("LfgValue1")
    private int lfgValue1;

    @Column("LfgValue2")
    private int lfgValue2;

    @Column("LfgValue3")
    private int lfgValue3;

    @Column("LfgValue4")
    private int lfgValue4;

    @Column("AreaID1")
    private short areaID1;

    @Column("AreaID2")
    private short areaID2;

    @Column("AreaID3")
    private short areaID3;

    @Column("AreaID4")
    private short areaID4;

    @Column("CurrencyID1")
    private int currencyID1;

    @Column("CurrencyID2")
    private int currencyID2;

    @Column("CurrencyID3")
    private int currencyID3;

    @Column("CurrencyID4")
    private int currencyID4;

    @Column("CurrencyCount1")
    private int currencyCount1;

    @Column("CurrencyCount2")
    private int currencyCount2;

    @Column("CurrencyCount3")
    private int currencyCount3;

    @Column("CurrencyCount4")
    private int currencyCount4;

    @Column("QuestKillMonster1")
    private int questKillMonster1;

    @Column("QuestKillMonster2")
    private int questKillMonster2;

    @Column("QuestKillMonster3")
    private int questKillMonster3;

    @Column("QuestKillMonster4")
    private int questKillMonster4;

    @Column("QuestKillMonster5")
    private int questKillMonster5;

    @Column("QuestKillMonster6")
    private int questKillMonster6;

    @Column("MovementFlags1")
    private int movementFlags1;

    @Column("MovementFlags2")
    private int movementFlags2;

    @Id
    @Column("VerifiedBuild")
    private int verifiedBuild;


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
