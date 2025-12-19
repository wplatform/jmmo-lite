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
@ToString(onlyExplicitlyIncluded = true)


@Table(name = "journal_encounter_section")
@Db2DataBind(name = "JournalEncounterSection.db2", layoutHash = 0x13E56B12, fields = {
        @Db2Field(name = "title", type = Db2Type.STRING),
        @Db2Field(name = "bodyText", type = Db2Type.STRING),
        @Db2Field(name = "iconCreatureDisplayInfoID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "spellID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "iconFileDataID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "journalEncounterID", type = Db2Type.SHORT),
        @Db2Field(name = "nextSiblingSectionID", type = Db2Type.SHORT),
        @Db2Field(name = "firstChildSectionID", type = Db2Type.SHORT),
        @Db2Field(name = "parentSectionID", type = Db2Type.SHORT),
        @Db2Field(name = "flags", type = Db2Type.SHORT),
        @Db2Field(name = "iconFlags", type = Db2Type.SHORT),
        @Db2Field(name = "orderIndex", type = Db2Type.BYTE),
        @Db2Field(name = "type", type = Db2Type.BYTE),
        @Db2Field(name = "difficultyMask", type = Db2Type.BYTE),
        @Db2Field(name = "uiModelSceneID", type = Db2Type.INT),
})
public class JournalEncounterSection implements DbcEntity {
    @Id
    
    @Column("ID")
    private int id;

    @Id
    
    @Column("VerifiedBuild")
    private int verifiedBuild;


    @Column("Title")
    private LocalizedString title;


    @Column("BodyText")
    private LocalizedString bodyText;

    
    @Column("JournalEncounterID")
    private int journalEncounterID;

    
    @Column("OrderIndex")
    private short orderIndex;

    
    @Column("ParentSectionID")
    private int parentSectionID;

    
    @Column("FirstChildSectionID")
    private int firstChildSectionID;

    
    @Column("NextSiblingSectionID")
    private int nextSiblingSectionID;

    
    @Column("Type")
    private byte type;

    
    @Column("IconCreatureDisplayInfoID")
    private Long iconCreatureDisplayInfoID;

    
    @Column("UiModelSceneID")
    private int uiModelSceneID;

    
    @Column("SpellID")
    private int spellID;

    
    @Column("IconFileDataID")
    private int iconFileDataID;

    
    @Column("Flags")
    private int flags;

    
    @Column("IconFlags")
    private int iconFlags;

    
    @Column("DifficultyMask")
    private byte difficultyMask;

}