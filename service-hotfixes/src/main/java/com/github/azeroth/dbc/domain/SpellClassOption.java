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


@Table(name = "spell_class_options")
@Db2DataBind(name = "SpellClassOptions.db2", layoutHash = 0x80FBD67A, fields = {
        @Db2Field(name = "spellID", type = Db2Type.INT, signed = true),
        @Db2Field(name = {"spellClassMask1", "spellClassMask2", "spellClassMask3", "spellClassMask4"}, type = Db2Type.INT, signed = true),
        @Db2Field(name = "spellClassSet", type = Db2Type.BYTE),
        @Db2Field(name = "modalNextSpell", type = Db2Type.INT)
})
public class SpellClassOption implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("SpellID")
    private int spellID;

    @Column("SpellClassMask1")
    private int spellClassMask1;

    @Column("SpellClassMask2")
    private int spellClassMask2;

    @Column("SpellClassMask3")
    private int spellClassMask3;

    @Column("SpellClassMask4")
    private int spellClassMask4;

    @Column("SpellClassSet")
    private byte spellClassSet;

    @Column("ModalNextSpell")
    private int modalNextSpell;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
