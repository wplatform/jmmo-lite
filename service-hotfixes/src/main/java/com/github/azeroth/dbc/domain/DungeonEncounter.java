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


@Table(name = "dungeon_encounter")
@Db2DataBind(name = "DungeonEncounter.db2", layoutHash = 0xB04A2596, indexField = 6, parentIndexField = 2, fields = {
        @Db2Field(name = "name", type = Db2Type.STRING),
        @Db2Field(name = "creatureDisplayID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "mapID", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "difficultyID", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "bit", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "flags", type = Db2Type.BYTE),
        @Db2Field(name = "id", type = Db2Type.INT),
        @Db2Field(name = "orderIndex", type = Db2Type.INT, signed = true),
        @Db2Field(name = "spellIconFileID", type = Db2Type.INT, signed = true)
})
public class DungeonEncounter implements DbcEntity {
    @Column("Name")
    private LocalizedString name;

    @Column("CreatureDisplayID")
    private int creatureDisplayID;

    @Column("MapID")
    private short mapID;

    @Column("DifficultyID")
    private byte difficultyID;

    @Column("Bit")
    private byte bit;

    @Column("Flags")
    private byte flags;

    @Id
    
    @Column("ID")
    private int id;

    @Column("OrderIndex")
    private int orderIndex;

    @Column("SpellIconFileID")
    private int spellIconFileID;

    @Id
    
    @Column("VerifiedBuild")
    private int verifiedBuild;

}
