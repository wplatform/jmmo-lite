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


@Table(name = "pvp_item")
@Db2DataBind(name = "PVPItem.db2", layoutHash = 0xBD449801, fields = {
        @Db2Field(name = "itemID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "itemLevelDelta", type = Db2Type.BYTE)
})
public class PvpItem implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("ItemID")
    private int itemID;

    @Column("ItemLevelDelta")
    private byte itemLevelDelta;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
