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
@ToString(onlyExplicitlyIncluded = true)


@Table(name = "item_modified_appearance_extra")
@Db2DataBind(name = "ItemModifiedAppearanceExtra.db2", layoutHash = 0x77212236, fields = {
        @Db2Field(name = "iconFileDataID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "unequippedIconFileDataID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "sheatheType", type = Db2Type.BYTE),
        @Db2Field(name = "displayWeaponSubclassID", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "displayInventoryType", type = Db2Type.BYTE, signed = true),
})
public class ItemModifiedAppearanceExtra implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;


    @Column("IconFileDataID")
    private int iconFileDataID;


    @Column("UnequippedIconFileDataID")
    private int unequippedIconFileDataID;


    @Column("SheatheType")
    private short sheatheType;


    @Column("DisplayWeaponSubclassID")
    private byte displayWeaponSubclassID;


    @Column("DisplayInventoryType")
    private byte displayInventoryType;

}