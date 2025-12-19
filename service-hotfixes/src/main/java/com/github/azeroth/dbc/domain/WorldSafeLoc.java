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


@Table(name = "world_safe_loc")
@Db2DataBind(name = "WorldSafeLocs.db2", layoutHash = 0x605EA8A6, parentIndexField = 3, fields = {
        @Db2Field(name = "areaName", type = Db2Type.STRING),
        @Db2Field(name = {"locX", "locY", "locZ"}, type = Db2Type.FLOAT),
        @Db2Field(name = "facing", type = Db2Type.FLOAT),
        @Db2Field(name = "mapID", type = Db2Type.SHORT)
})
public class WorldSafeLoc implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("AreaName")
    private LocalizedString areaName;

    @Column("LocX")
    private float locX;

    @Column("LocY")
    private float locY;

    @Column("LocZ")
    private float locZ;

    @Column("Facing")
    private float facing;

    @Column("MapID")
    private short mapID;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;


}
