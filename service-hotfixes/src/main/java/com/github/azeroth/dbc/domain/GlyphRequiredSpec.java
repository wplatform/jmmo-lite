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


@Table(name = "glyph_required_spec")
@Db2DataBind(name = "GlyphRequiredSpec.db2", layoutHash = 0xDD6481CE, parentIndexField = 1, fields = {
        @Db2Field(name = "chrSpecializationID", type = Db2Type.SHORT),
        @Db2Field(name = "glyphPropertiesID", type = Db2Type.SHORT)
})
public class GlyphRequiredSpec implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("ChrSpecializationID")
    private short chrSpecializationID;

    @Column("GlyphPropertiesID")
    private short glyphPropertiesID;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
