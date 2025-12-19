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


@Table(name = "pvp_difficulty")
@Db2DataBind(name = "PVPDifficulty.db2", layoutHash = 0x970B5E15, parentIndexField = 3, fields = {
        @Db2Field(name = "rangeIndex", type = Db2Type.BYTE),
        @Db2Field(name = "minLevel", type = Db2Type.BYTE),
        @Db2Field(name = "maxLevel", type = Db2Type.BYTE),
        @Db2Field(name = "mapID", type = Db2Type.SHORT)
})
public class PvpDifficulty implements DbcEntity {
    @Id
    
    @Column("ID")
    private int id;

    @Column("RangeIndex")
    private byte rangeIndex;

    @Column("MinLevel")
    private short minLevel;

    @Column("MaxLevel")
    private short maxLevel;

    @Column("MapID")
    private short mapID;

    @Id
    
    @Column("VerifiedBuild")
    private int verifiedBuild;

}
