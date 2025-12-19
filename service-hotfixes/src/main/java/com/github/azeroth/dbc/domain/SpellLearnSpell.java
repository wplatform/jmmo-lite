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


@Table(name = "spell_learn_spell")
@Db2DataBind(name = "SpellLearnSpell.db2", layoutHash = 0x153EBA26, fields = {
        @Db2Field(name = "spellID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "learnSpellID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "overridesSpellID", type = Db2Type.INT, signed = true)
})
public class SpellLearnSpell implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("SpellID")
    private int spellID;

    @Column("LearnSpellID")
    private int learnSpellID;

    @Column("OverridesSpellID")
    private int overridesSpellID;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
