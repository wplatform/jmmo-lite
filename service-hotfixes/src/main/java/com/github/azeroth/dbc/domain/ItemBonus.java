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


@Table(name = "item_bonus")
@Db2DataBind(name = "ItemBonus.db2", layoutHash = 0xE12FB1A0, parentIndexField = 1, fields = {
        @Db2Field(name = {"value1", "value2", "value3"}, type = Db2Type.INT, signed = true),
        @Db2Field(name = "parentItemBonusListID", type = Db2Type.SHORT),
        @Db2Field(name = "type", type = Db2Type.BYTE),
        @Db2Field(name = "orderIndex", type = Db2Type.BYTE)
})
public class ItemBonus implements DbcEntity {
    @Id
    
    @Column("ID")
    private int id;

    @Column("Value1")
    private int value1;

    @Column("Value2")
    private int value2;

    @Column("Value3")
    private int value3;

    @Column("ParentItemBonusListID")
    private short parentItemBonusListID;

    @Column("Type")
    private byte type;

    @Column("OrderIndex")
    private byte orderIndex;

    @Id
    
    @Column("VerifiedBuild")
    private int verifiedBuild;

}
