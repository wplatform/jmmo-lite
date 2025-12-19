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


@Table(name = "spell_radius")
@Db2DataBind(name = "SpellRadius.db2", layoutHash = 0xC12E5C90, fields = {
        @Db2Field(name = "radius", type = Db2Type.FLOAT),
        @Db2Field(name = "radiusPerLevel", type = Db2Type.FLOAT),
        @Db2Field(name = "radiusMin", type = Db2Type.FLOAT),
        @Db2Field(name = "radiusMax", type = Db2Type.FLOAT)
})
public class SpellRadius implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("Radius")
    private float radius;

    @Column("RadiusPerLevel")
    private float radiusPerLevel;

    @Column("RadiusMin")
    private float radiusMin;

    @Column("RadiusMax")
    private float radiusMax;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
