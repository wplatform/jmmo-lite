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


@Table(name = "spell_scaling")
@Db2DataBind(name = "SpellScaling.db2", layoutHash = 0xF67A5719, fields = {
        @Db2Field(name = "spellID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "scalesFromItemLevel", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "klass", type = Db2Type.INT, signed = true),
        @Db2Field(name = "minScalingLevel", type = Db2Type.INT),
        @Db2Field(name = "maxScalingLevel", type = Db2Type.INT)
})
public class SpellScaling implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("SpellID")
    private int spellID;

    @Column("ScalesFromItemLevel")
    private short scalesFromItemLevel;

    @Column("Class")
    private int klass;

    @Column("MinScalingLevel")
    private int minScalingLevel;

    @Column("MaxScalingLevel")
    private int maxScalingLevel;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
