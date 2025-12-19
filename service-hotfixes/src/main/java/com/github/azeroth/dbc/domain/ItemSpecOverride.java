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


@Table(name = "item_spec_override")
@Db2DataBind(name = "ItemSpecOverride.db2", layoutHash = 0xE499CD2A, parentIndexField = 1, fields = {
        @Db2Field(name = "specID", type = Db2Type.SHORT),
        @Db2Field(name = "itemID", type = Db2Type.INT, signed = true)
})
public class ItemSpecOverride implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("SpecID")
    private short specID;

    @Column("ItemID")
    private int itemID;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
