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


@Table(name = "specialization_spells")
@Db2DataBind(name = "SpecializationSpells.db2", layoutHash = 0xAE3436F3, indexField = 5, parentIndexField = 3, fields = {
        @Db2Field(name = "description", type = Db2Type.STRING),
        @Db2Field(name = "spellID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "overridesSpellID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "specID", type = Db2Type.SHORT),
        @Db2Field(name = "displayOrder", type = Db2Type.BYTE),
        @Db2Field(name = "id", type = Db2Type.INT)
})
public class SpecializationSpell implements DbcEntity {
    @Column("Description")
    private LocalizedString description;

    @Column("SpellID")
    private int spellID;

    @Column("OverridesSpellID")
    private int overridesSpellID;

    @Column("SpecID")
    private short specID;

    @Column("DisplayOrder")
    private byte displayOrder;

    @Id

    @Column("ID")
    private int id;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
