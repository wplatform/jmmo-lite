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


@Table(name = "quest_package_item")
@Db2DataBind(name = "QuestPackageItem.db2", layoutHash = 0xCF9401CF, fields = {
        @Db2Field(name = "itemID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "packageID", type = Db2Type.SHORT),
        @Db2Field(name = "displayType", type = Db2Type.BYTE),
        @Db2Field(name = "itemQuantity", type = Db2Type.INT)
})
public class QuestPackageItem implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("ItemID")
    private int itemID;

    @Column("PackageID")
    private short packageID;

    @Column("DisplayType")
    private byte displayType;

    @Column("ItemQuantity")
    private int itemQuantity;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
