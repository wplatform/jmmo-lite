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


@Table(name = "item_modified_appearance")
@Db2DataBind(name = "ItemModifiedAppearance.db2", layoutHash = 0xE64FD18B, indexField = 1, parentIndexField = 0, fields = {
        @Db2Field(name = "itemID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "id", type = Db2Type.INT),
        @Db2Field(name = "itemAppearanceModifierID", type = Db2Type.BYTE),
        @Db2Field(name = "itemAppearanceID", type = Db2Type.SHORT),
        @Db2Field(name = "orderIndex", type = Db2Type.BYTE),
        @Db2Field(name = "transmogSourceTypeEnum", type = Db2Type.BYTE, signed = true)
})
public class ItemModifiedAppearance implements DbcEntity {
    @Column("ItemID")
    private int itemID;

    @Id

    @Column("ID")
    private int id;

    @Column("ItemAppearanceModifierID")
    private byte itemAppearanceModifierID;

    @Column("ItemAppearanceID")
    private int itemAppearanceID;

    @Column("OrderIndex")
    private byte orderIndex;

    @Column("TransmogSourceTypeEnum")
    private byte transmogSourceTypeEnum;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
