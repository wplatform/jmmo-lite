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


@Table(name = "lfg_dungeons")
@Db2DataBind(name = "LFGDungeons.db2", layoutHash = 0xF02081A0, fields = {
        @Db2Field(name = "name", type = Db2Type.STRING),
        @Db2Field(name = "description", type = Db2Type.STRING),
        @Db2Field(name = "flags", type = Db2Type.INT, signed = true),
        @Db2Field(name = "minGear", type = Db2Type.FLOAT),
        @Db2Field(name = "maxLevel", type = Db2Type.SHORT),
        @Db2Field(name = "targetLevelMax", type = Db2Type.SHORT),
        @Db2Field(name = "mapID", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "randomID", type = Db2Type.SHORT),
        @Db2Field(name = "scenarioID", type = Db2Type.SHORT),
        @Db2Field(name = "finalEncounterID", type = Db2Type.SHORT),
        @Db2Field(name = "bonusReputationAmount", type = Db2Type.SHORT),
        @Db2Field(name = "mentorItemLevel", type = Db2Type.SHORT),
        @Db2Field(name = "requiredPlayerConditionId", type = Db2Type.SHORT),
        @Db2Field(name = "minLevel", type = Db2Type.BYTE),
        @Db2Field(name = "targetLevel", type = Db2Type.BYTE),
        @Db2Field(name = "targetLevelMin", type = Db2Type.BYTE),
        @Db2Field(name = "difficultyID", type = Db2Type.BYTE),
        @Db2Field(name = "typeID", type = Db2Type.BYTE),
        @Db2Field(name = "faction", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "expansionLevel", type = Db2Type.BYTE),
        @Db2Field(name = "orderIndex", type = Db2Type.BYTE),
        @Db2Field(name = "groupID", type = Db2Type.BYTE),
        @Db2Field(name = "countTank", type = Db2Type.BYTE),
        @Db2Field(name = "countHealer", type = Db2Type.BYTE),
        @Db2Field(name = "countDamage", type = Db2Type.BYTE),
        @Db2Field(name = "minCountTank", type = Db2Type.BYTE),
        @Db2Field(name = "minCountHealer", type = Db2Type.BYTE),
        @Db2Field(name = "minCountDamage", type = Db2Type.BYTE),
        @Db2Field(name = "subtype", type = Db2Type.BYTE),
        @Db2Field(name = "mentorCharLevel", type = Db2Type.BYTE),
        @Db2Field(name = "iconTextureFileID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "rewardsBgTextureFileID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "popupBgTextureFileID", type = Db2Type.INT, signed = true)
})
public class LfgDungeon implements DbcEntity {
    @Id
    
    @Column("ID")
    private int id;

    @Column("Name")
    private LocalizedString name;

    @Column("Description")
    private LocalizedString description;

    @Column("Flags")
    private int flags;

    @Column("MinGear")
    private float minGear;

    @Column("MaxLevel")
    private short maxLevel;

    @Column("TargetLevelMax")
    private short targetLevelMax;

    @Column("MapID")
    private short mapID;

    @Column("RandomID")
    private short randomID;

    @Column("ScenarioID")
    private short scenarioID;

    @Column("FinalEncounterID")
    private short finalEncounterID;

    @Column("BonusReputationAmount")
    private short bonusReputationAmount;

    @Column("MentorItemLevel")
    private short mentorItemLevel;

    @Column("RequiredPlayerConditionId")
    private int requiredPlayerConditionId;

    @Column("MinLevel")
    private byte minLevel;

    @Column("TargetLevel")
    private byte targetLevel;

    @Column("TargetLevelMin")
    private byte targetLevelMin;

    @Column("DifficultyID")
    private byte difficultyID;

    @Column("TypeID")
    private byte typeID;

    @Column("Faction")
    private byte faction;

    @Column("ExpansionLevel")
    private byte expansionLevel;

    @Column("OrderIndex")
    private byte orderIndex;

    @Column("GroupID")
    private byte groupID;

    @Column("CountTank")
    private byte countTank;

    @Column("CountHealer")
    private byte countHealer;

    @Column("CountDamage")
    private byte countDamage;

    @Column("MinCountTank")
    private byte minCountTank;

    @Column("MinCountHealer")
    private byte minCountHealer;

    @Column("MinCountDamage")
    private byte minCountDamage;

    @Column("Subtype")
    private byte subtype;

    @Column("MentorCharLevel")
    private byte mentorCharLevel;

    @Column("IconTextureFileID")
    private int iconTextureFileID;

    @Column("RewardsBgTextureFileID")
    private int rewardsBgTextureFileID;

    @Column("PopupBgTextureFileID")
    private int popupBgTextureFileID;

    @Id
    
    @Column("VerifiedBuild")
    private int verifiedBuild;

}
