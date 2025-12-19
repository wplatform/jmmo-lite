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


@Table(name = "item_set_spell")
@Db2DataBind(name = "ItemSetSpell.db2", layoutHash = 0xF65D0AF8, parentIndexField = 3, fields = {
        @Db2Field(name = "spellID", type = Db2Type.INT),
        @Db2Field(name = "chrSpecID", type = Db2Type.SHORT),
        @Db2Field(name = "threshold", type = Db2Type.BYTE),
        @Db2Field(name = "itemSetID", type = Db2Type.SHORT)
})
public class ItemSetSpell implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("SpellID")
    private int spellID;

    @Column("ChrSpecID")
    private short chrSpecID;

    @Column("Threshold")
    private byte threshold;

    @Column("ItemSetID")
    private short itemSetID;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
