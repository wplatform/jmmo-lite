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


@Table(name = "spell_range")
@Db2DataBind(name = "SpellRange.db2", layoutHash = 0xDE2E3F8E, fields = {
        @Db2Field(name = "displayName", type = Db2Type.STRING),
        @Db2Field(name = "displayNameShort", type = Db2Type.STRING),
        @Db2Field(name = {"rangeMin1", "rangeMin2"}, type = Db2Type.FLOAT),
        @Db2Field(name = {"rangeMax1", "rangeMax2"}, type = Db2Type.FLOAT),
        @Db2Field(name = "flags", type = Db2Type.BYTE)
})
public class SpellRange implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("DisplayName")
    private LocalizedString displayName;

    @Column("DisplayNameShort")
    private LocalizedString displayNameShort;

    @Column("RangeMin1")
    private float rangeMin1;

    @Column("RangeMin2")
    private float rangeMin2;

    @Column("RangeMax1")
    private float rangeMax1;

    @Column("RangeMax2")
    private float rangeMax2;

    @Column("Flags")
    private byte flags;

    @Id
    @Column("VerifiedBuild")
    private int verifiedBuild;

    public float[] getRangeMin() {
        return new float[]{rangeMin1, rangeMin2};
    }

    public float[] getRangeMax() {
        return new float[]{rangeMax1, rangeMax2};
    }

}
