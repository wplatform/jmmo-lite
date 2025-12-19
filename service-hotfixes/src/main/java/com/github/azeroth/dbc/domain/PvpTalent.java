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


@Table(name = "pvp_talent")
@Db2DataBind(name = "PvpTalent.db2", layoutHash = 0x6EB51740, fields = {
        @Db2Field(name = "description", type = Db2Type.STRING),
        @Db2Field(name = "spellID", type = Db2Type.INT),
        @Db2Field(name = "overridesSpellID", type = Db2Type.INT),
        @Db2Field(name = "actionBarSpellID", type = Db2Type.INT),
        @Db2Field(name = "tierID", type = Db2Type.INT),
        @Db2Field(name = "columnIndex", type = Db2Type.INT),
        @Db2Field(name = "flags", type = Db2Type.INT, signed = true),
        @Db2Field(name = "classID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "specID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "role", type = Db2Type.INT, signed = true)
})
public class PvpTalent implements DbcEntity {
    @Id
    
    @Column("ID")
    private int id;

    @Column("Description")
    private LocalizedString description;

    @Column("SpellID")
    private int spellID;

    @Column("OverridesSpellID")
    private int overridesSpellID;

    @Column("ActionBarSpellID")
    private int actionBarSpellID;

    @Column("TierID")
    private int tierID;

    @Column("ColumnIndex")
    private int columnIndex;

    @Column("Flags")
    private int flags;

    @Column("ClassID")
    private int classID;

    @Column("SpecID")
    private int specID;

    @Column("Role")
    private int role;

    @Id
    
    @Column("VerifiedBuild")
    private int verifiedBuild;

}
