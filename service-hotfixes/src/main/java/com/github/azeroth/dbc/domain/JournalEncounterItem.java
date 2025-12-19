package com.github.azeroth.dbc.domain;

import com.github.azeroth.cache.DbcEntity;
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


@Table(name = "journal_encounter_item")
@Db2DataBind(name = "JournalEncounterItem.db2", layoutHash = 0x39230FF9, indexField = 5, parentIndexField = 1, fields = {
        @Db2Field(name = "itemID", type = Db2Type.INT),
        @Db2Field(name = "journalEncounterID", type = Db2Type.SHORT),
        @Db2Field(name = "difficultyMask", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "factionMask", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "flags", type = Db2Type.BYTE),
        @Db2Field(name = "id", type = Db2Type.INT)
})
public class JournalEncounterItem implements DbcEntity {
    @Column("ItemID")
    private int itemID;

    @Column("JournalEncounterID")
    private short journalEncounterID;

    @Column("DifficultyMask")
    private byte difficultyMask;

    @Column("FactionMask")
    private byte factionMask;

    @Column("Flags")
    private byte flags;

    @Id

    @Column("ID")
    private int id;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
