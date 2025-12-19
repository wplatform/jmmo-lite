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


@Table(name = "spell_label")
@Db2DataBind(name = "SpellLabel.db2", layoutHash = 0x68E44736, parentIndexField = 1, fields = {
        @Db2Field(name = "labelID", type = Db2Type.INT),
        @Db2Field(name = "spellID", type = Db2Type.INT, signed = true)
})
public class SpellLabel implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("LabelID")
    private int labelID;

    @Column("SpellID")
    private int spellID;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
