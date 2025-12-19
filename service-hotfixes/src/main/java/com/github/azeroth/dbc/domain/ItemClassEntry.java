package com.github.azeroth.dbc.domain;

import com.github.azeroth.cache.DbcEntity;
import com.github.azeroth.common.LocalizedString;
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


@Table(name = "item_class")
@Db2DataBind(name = "ItemClass.db2", layoutHash = 0xA1E4663C, fields = {
        @Db2Field(name = "className", type = Db2Type.STRING),
        @Db2Field(name = "priceModifier", type = Db2Type.FLOAT),
        @Db2Field(name = "classID", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "flags", type = Db2Type.BYTE)
})
public class ItemClassEntry implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("ClassName")
    private LocalizedString className;

    @Column("PriceModifier")
    private float priceModifier;

    @Column("ClassID")
    private byte classID;

    @Column("Flags")
    private byte flags;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
