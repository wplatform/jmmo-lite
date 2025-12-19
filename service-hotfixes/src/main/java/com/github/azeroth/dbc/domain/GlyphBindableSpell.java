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


@Table(name = "glyphbindablespell")
@Db2DataBind(name = "GlyphBindableSpell.db2", layoutHash = 0xEA228DFA, parentIndexField = 1, fields = {
        @Db2Field(name = "spellID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "glyphPropertiesID", type = Db2Type.SHORT, signed = true)
})
public class GlyphBindableSpell implements DbcEntity {
    @Id
    
    @Column("ID")
    private int id;

    @Column("SpellID")
    private int spellID;

    @Column("GlyphPropertiesID")
    private short glyphPropertiesID;

    @Id
    
    @Column("VerifiedBuild")
    private int verifiedBuild;

}
