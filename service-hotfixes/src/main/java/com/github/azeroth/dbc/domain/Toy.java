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


@Table(name = "toy")
@Db2DataBind(name = "Toy.db2", layoutHash = 0x5409C5EA, indexField = 4, fields = {
        @Db2Field(name = "sourceText", type = Db2Type.STRING),
        @Db2Field(name = "itemID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "flags", type = Db2Type.BYTE),
        @Db2Field(name = "sourceTypeEnum", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "id", type = Db2Type.INT)
})
public class Toy implements DbcEntity {
    @Column("SourceText")
    private LocalizedString sourceText;

    @Column("ItemID")
    private int itemID;

    @Column("Flags")
    private byte flags;

    @Column("SourceTypeEnum")
    private byte sourceTypeEnum;

    @Id

    @Column("ID")
    private int id;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
