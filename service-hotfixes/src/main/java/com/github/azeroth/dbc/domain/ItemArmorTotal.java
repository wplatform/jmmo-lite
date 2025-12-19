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


@Table(name = "item_armor_total")
@Db2DataBind(name = "ItemArmorTotal.db2", layoutHash = 0x45C396DD, fields = {
        @Db2Field(name = "cloth", type = Db2Type.FLOAT),
        @Db2Field(name = "leather", type = Db2Type.FLOAT),
        @Db2Field(name = "mail", type = Db2Type.FLOAT),
        @Db2Field(name = "plate", type = Db2Type.FLOAT),
        @Db2Field(name = "itemLevel", type = Db2Type.SHORT, signed = true)
})
public class ItemArmorTotal implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("Cloth")
    private float cloth;

    @Column("Leather")
    private float leather;

    @Column("Mail")
    private float mail;

    @Column("Plate")
    private float plate;

    @Column("ItemLevel")
    private short itemLevel;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
