package com.github.azeroth.dbc.domain;

import com.github.azeroth.cache.DbcEntity;
import com.github.azeroth.common.EnumFlag;
import com.github.azeroth.dbc.db2.Db2Field;
import com.github.azeroth.dbc.db2.Db2DataBind;
import com.github.azeroth.dbc.db2.Db2Type;
import com.github.azeroth.dbc.defines.SummonPropertyFlag;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString


@Table(name = "summon_properties")
@Db2DataBind(name = "SummonProperties.db2", layoutHash = 0xFB8338FC, fields = {
        @Db2Field(name = "flags", type = Db2Type.INT, signed = true),
        @Db2Field(name = "control", type = Db2Type.INT, signed = true),
        @Db2Field(name = "faction", type = Db2Type.INT, signed = true),
        @Db2Field(name = "title", type = Db2Type.INT, signed = true),
        @Db2Field(name = "slot", type = Db2Type.INT, signed = true)
})
public class SummonProperty implements DbcEntity {
    @Id
    @Column("ID")
    private int id;

    @Column("Flags")
    private int flags;

    @Column("Control")
    private int control;

    @Column("Faction")
    private int faction;

    @Column("Title")
    private int title;

    @Column("Slot")
    private int slot;

    @Id
    @Column("VerifiedBuild")
    private int verifiedBuild;

    public EnumFlag<SummonPropertyFlag> getFlags() {
        return EnumFlag.of(SummonPropertyFlag.class, flags);
    }

}
