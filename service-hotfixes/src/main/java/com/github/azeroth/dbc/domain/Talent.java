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


@Table(name = "talent")
@Db2DataBind(name = "Talent.db2", layoutHash = 0xE8850B48, fields = {
        @Db2Field(name = "description", type = Db2Type.STRING),
        @Db2Field(name = "spellID", type = Db2Type.INT),
        @Db2Field(name = "overridesSpellID", type = Db2Type.INT),
        @Db2Field(name = "specID", type = Db2Type.SHORT),
        @Db2Field(name = "tierID", type = Db2Type.BYTE),
        @Db2Field(name = "columnIndex", type = Db2Type.BYTE),
        @Db2Field(name = "flags", type = Db2Type.BYTE),
        @Db2Field(name = {"categoryMask1", "categoryMask2"}, type = Db2Type.BYTE),
        @Db2Field(name = "classID", type = Db2Type.BYTE)
})
public class Talent implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("Description")
    private LocalizedString description;

    @Column("SpellID")
    private int spellID;

    @Column("OverridesSpellID")
    private int overridesSpellID;

    @Column("SpecID")
    private short specID;

    @Column("TierID")
    private byte tierID;

    @Column("ColumnIndex")
    private byte columnIndex;

    @Column("Flags")
    private byte flags;

    @Column("CategoryMask1")
    private byte categoryMask1;

    @Column("CategoryMask2")
    private byte categoryMask2;

    @Column("ClassID")
    private byte classID;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
