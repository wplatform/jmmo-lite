package com.github.azeroth.dbc.domain;

import com.github.azeroth.cache.DbcEntity;
import com.github.azeroth.common.LocalizedString;
import com.github.azeroth.dbc.db2.Db2Field;
import com.github.azeroth.dbc.db2.Db2DataBind;
import com.github.azeroth.dbc.db2.Db2Type;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@ToString


@Table(name = "chr_races")
@Db2DataBind(name = "ChrRaces.db2", layoutHash = 0x51C511F9, indexField = 30, fields = {
        @Db2Field(name = "clientPrefix", type = Db2Type.STRING_NOT_LOCALIZED),
        @Db2Field(name = "clientFileString", type = Db2Type.STRING_NOT_LOCALIZED),
        @Db2Field(name = "name", type = Db2Type.STRING),
        @Db2Field(name = "nameFemale", type = Db2Type.STRING),
        @Db2Field(name = "nameLowercase", type = Db2Type.STRING),
        @Db2Field(name = "nameFemaleLowercase", type = Db2Type.STRING),
        @Db2Field(name = "flags", type = Db2Type.INT, signed = true),
        @Db2Field(name = "maleDisplayId", type = Db2Type.INT),
        @Db2Field(name = "femaleDisplayId", type = Db2Type.INT),
        @Db2Field(name = "createScreenFileDataID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "selectScreenFileDataID", type = Db2Type.INT, signed = true),
        @Db2Field(name = {"maleCustomizeOffset1", "maleCustomizeOffset2", "maleCustomizeOffset3"}, type = Db2Type.FLOAT),
        @Db2Field(name = {"femaleCustomizeOffset1", "femaleCustomizeOffset2", "femaleCustomizeOffset3"}, type = Db2Type.FLOAT),
        @Db2Field(name = "lowResScreenFileDataID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "startingLevel", type = Db2Type.INT, signed = true),
        @Db2Field(name = "uiDisplayOrder", type = Db2Type.INT, signed = true),
        @Db2Field(name = "factionID", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "resSicknessSpellID", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "splashSoundID", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "cinematicSequenceID", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "baseLanguage", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "creatureType", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "alliance", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "raceRelated", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "unalteredVisualRaceID", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "charComponentTextureLayoutID", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "defaultClassID", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "neutralRaceID", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "displayRaceID", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "charComponentTexLayoutHiResID", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "id", type = Db2Type.INT),
        @Db2Field(name = "highResMaleDisplayId", type = Db2Type.INT),
        @Db2Field(name = "highResFemaleDisplayId", type = Db2Type.INT),
        @Db2Field(name = "heritageArmorAchievementID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "maleSkeletonFileDataID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "femaleSkeletonFileDataID", type = Db2Type.INT, signed = true),
        @Db2Field(name = {"alteredFormStartVisualKitID1", "alteredFormStartVisualKitID2", "alteredFormStartVisualKitID3"}, type = Db2Type.INT),
        @Db2Field(name = {"alteredFormFinishVisualKitID1", "alteredFormFinishVisualKitID2", "alteredFormFinishVisualKitID3"}, type = Db2Type.INT)
})
public class ChrRace implements DbcEntity {
    @Column("ClientPrefix")
    private String clientPrefix;

    @Column("ClientFileString")
    private String clientFileString;

    @Column("Name")
    private LocalizedString name;

    @Column("NameFemale")
    private LocalizedString nameFemale;

    @Column("NameLowercase")
    private LocalizedString nameLowercase;

    @Column("NameFemaleLowercase")
    private LocalizedString nameFemaleLowercase;

    @Column("Flags")
    private int flags;

    @Column("MaleDisplayId")
    private int maleDisplayId;

    @Column("FemaleDisplayId")
    private int femaleDisplayId;

    @Column("CreateScreenFileDataID")
    private int createScreenFileDataID;

    @Column("SelectScreenFileDataID")
    private int selectScreenFileDataID;

    @Column("MaleCustomizeOffset1")
    private float maleCustomizeOffset1;

    @Column("MaleCustomizeOffset2")
    private float maleCustomizeOffset2;

    @Column("MaleCustomizeOffset3")
    private float maleCustomizeOffset3;

    @Column("FemaleCustomizeOffset1")
    private float femaleCustomizeOffset1;

    @Column("FemaleCustomizeOffset2")
    private float femaleCustomizeOffset2;

    @Column("FemaleCustomizeOffset3")
    private float femaleCustomizeOffset3;

    @Column("LowResScreenFileDataID")
    private int lowResScreenFileDataID;

    @Column("StartingLevel")
    private int startingLevel;

    @Column("UiDisplayOrder")
    private int uiDisplayOrder;

    @Column("FactionID")
    private short factionID;

    @Column("ResSicknessSpellID")
    private short resSicknessSpellID;

    @Column("SplashSoundID")
    private short splashSoundID;

    @Column("CinematicSequenceID")
    private short cinematicSequenceID;

    @Column("BaseLanguage")
    private byte baseLanguage;

    @Column("CreatureType")
    private byte creatureType;

    @Column("Alliance")
    private byte alliance;

    @Column("RaceRelated")
    private byte raceRelated;

    @Column("UnalteredVisualRaceID")
    private byte unalteredVisualRaceID;

    @Column("CharComponentTextureLayoutID")
    private byte charComponentTextureLayoutID;

    @Column("DefaultClassID")
    private byte defaultClassID;

    @Column("NeutralRaceID")
    private byte neutralRaceID;

    @Column("DisplayRaceID")
    private byte displayRaceID;

    @Column("CharComponentTexLayoutHiResID")
    private byte charComponentTexLayoutHiResID;

    @Id

    @Column("ID")
    private int id;

    @Column("HighResMaleDisplayId")
    private int highResMaleDisplayId;

    @Column("HighResFemaleDisplayId")
    private int highResFemaleDisplayId;

    @Column("HeritageArmorAchievementID")
    private int heritageArmorAchievementID;

    @Column("MaleSkeletonFileDataID")
    private int maleSkeletonFileDataID;

    @Column("FemaleSkeletonFileDataID")
    private int femaleSkeletonFileDataID;

    @Column("AlteredFormStartVisualKitID1")
    private int alteredFormStartVisualKitID1;

    @Column("AlteredFormStartVisualKitID2")
    private int alteredFormStartVisualKitID2;

    @Column("AlteredFormStartVisualKitID3")
    private int alteredFormStartVisualKitID3;

    @Column("AlteredFormFinishVisualKitID1")
    private int alteredFormFinishVisualKitID1;

    @Column("AlteredFormFinishVisualKitID2")
    private int alteredFormFinishVisualKitID2;

    @Column("AlteredFormFinishVisualKitID3")
    private int alteredFormFinishVisualKitID3;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
