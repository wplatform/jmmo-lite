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


@Table(name = "item_spec")
@Db2DataBind(name = "ItemSpec.db2", layoutHash = 0xB17B7986, parentIndexField = 3, fields = {
        @Db2Field(name = "specializationID", type = Db2Type.SHORT),
        @Db2Field(name = "minLevel", type = Db2Type.BYTE),
        @Db2Field(name = "maxLevel", type = Db2Type.BYTE),
        @Db2Field(name = "itemType", type = Db2Type.BYTE),
        @Db2Field(name = "primaryStat", type = Db2Type.BYTE),
        @Db2Field(name = "secondaryStat", type = Db2Type.BYTE)
})
public class ItemSpec implements DbcEntity {
    @Id
    
    @Column("ID")
    private int id;

    @Column("SpecializationID")
    private short specializationID;

    @Column("MinLevel")
    private byte minLevel;

    @Column("MaxLevel")
    private byte maxLevel;

    @Column("ItemType")
    private byte itemType;

    @Column("PrimaryStat")
    private byte primaryStat;

    @Column("SecondaryStat")
    private byte secondaryStat;

    @Id
    
    @Column("VerifiedBuild")
    private int verifiedBuild;

}
