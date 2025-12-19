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


@Table(name = "spell_cast_times")
@Db2DataBind(name = "SpellCastTimes.db2", layoutHash = 0x4129C6A4, fields = {
        @Db2Field(name = "base", type = Db2Type.INT, signed = true),
        @Db2Field(name = "minimum", type = Db2Type.INT, signed = true),
        @Db2Field(name = "perLevel", type = Db2Type.SHORT, signed = true)
})
public class SpellCastTime implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("Base")
    private int base;

    @Column("Minimum")
    private int minimum;

    @Column("PerLevel")
    private short perLevel;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
