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


@Table(name = "character_loadout")
@Db2DataBind(name = "CharacterLoadout.db2", layoutHash = 0xCA30C801, indexField = 1, fields = {
        @Db2Field(name = "id", type = Db2Type.INT, signed = true),
        @Db2Field(name = "raceMask", type = Db2Type.LONG),
        @Db2Field(name = "chrClassID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "purpose", type = Db2Type.INT, signed = true),
})
public class CharacterLoadout implements DbcEntity {

    @Id

    @Column("ID")
    private int id;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;


    @Column("RaceMask")
    private Long raceMask;


    @Column("ChrClassID")
    private byte chrClassID;


    @Column("Purpose")
    private int purpose;


    @Column("ItemContext")
    private byte itemContext;

}