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


@Table(name = "chr_classes")
@Db2DataBind(name = "ChrClasses.db2", layoutHash = 0x6F7AB8E7, indexField = 19, fields = {
        @Db2Field(name = "petNameToken", type = Db2Type.STRING_NOT_LOCALIZED),
        @Db2Field(name = "name", type = Db2Type.STRING),
        @Db2Field(name = "nameFemale", type = Db2Type.STRING),
        @Db2Field(name = "nameMale", type = Db2Type.STRING),
        @Db2Field(name = "filename", type = Db2Type.STRING_NOT_LOCALIZED),
        @Db2Field(name = "createScreenFileDataID", type = Db2Type.INT),
        @Db2Field(name = "selectScreenFileDataID", type = Db2Type.INT),
        @Db2Field(name = "lowResScreenFileDataID", type = Db2Type.INT),
        @Db2Field(name = "iconFileDataID", type = Db2Type.INT),
        @Db2Field(name = "startingLevel", type = Db2Type.INT, signed = true),
        @Db2Field(name = "flags", type = Db2Type.SHORT),
        @Db2Field(name = "cinematicSequenceID", type = Db2Type.SHORT),
        @Db2Field(name = "defaultSpec", type = Db2Type.SHORT),
        @Db2Field(name = "displayPower", type = Db2Type.BYTE),
        @Db2Field(name = "spellClassSet", type = Db2Type.BYTE),
        @Db2Field(name = "attackPowerPerStrength", type = Db2Type.BYTE),
        @Db2Field(name = "attackPowerPerAgility", type = Db2Type.BYTE),
        @Db2Field(name = "rangedAttackPowerPerAgility", type = Db2Type.BYTE),
        @Db2Field(name = "primaryStatPriority", type = Db2Type.BYTE),
        @Db2Field(name = "id", type = Db2Type.INT)
})
public class ChrClass implements DbcEntity {
    @Column("PetNameToken")
    private String petNameToken;

    @Column("Name")
    private LocalizedString name;

    @Column("NameFemale")
    private LocalizedString nameFemale;

    @Column("NameMale")
    private LocalizedString nameMale;

    @Column("Filename")
    private String filename;

    @Column("CreateScreenFileDataID")
    private int createScreenFileDataID;

    @Column("SelectScreenFileDataID")
    private int selectScreenFileDataID;

    @Column("LowResScreenFileDataID")
    private int lowResScreenFileDataID;

    @Column("IconFileDataID")
    private int iconFileDataID;

    @Column("StartingLevel")
    private int startingLevel;

    @Column("Flags")
    private short flags;

    @Column("CinematicSequenceID")
    private short cinematicSequenceID;

    @Column("DefaultSpec")
    private short defaultSpec;

    @Column("DisplayPower")
    private byte displayPower;

    @Column("SpellClassSet")
    private byte spellClassSet;

    @Column("AttackPowerPerStrength")
    private byte attackPowerPerStrength;

    @Column("AttackPowerPerAgility")
    private byte attackPowerPerAgility;

    @Column("RangedAttackPowerPerAgility")
    private byte rangedAttackPowerPerAgility;

    @Column("PrimaryStatPriority")
    private byte primaryStatPriority;

    @Id
    
    @Column("ID")
    private int id;

    @Id
    
    @Column("VerifiedBuild")
    private int verifiedBuild;

}
