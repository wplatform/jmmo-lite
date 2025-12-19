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


@Table(name = "item_price_base")
@Db2DataBind(name = "ItemPriceBase.db2", layoutHash = 0x4BD234D7, fields = {
        @Db2Field(name = "armor", type = Db2Type.FLOAT),
        @Db2Field(name = "weapon", type = Db2Type.FLOAT),
        @Db2Field(name = "itemLevel", type = Db2Type.SHORT)
})
public class ItemPriceBase implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("Armor")
    private float armor;

    @Column("Weapon")
    private float weapon;

    @Column("ItemLevel")
    private short itemLevel;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
