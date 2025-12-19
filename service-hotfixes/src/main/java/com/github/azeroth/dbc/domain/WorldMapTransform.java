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


@Table(name = "world_map_transforms")
@Db2DataBind(name = "WorldMapTransforms.db2", layoutHash = 0x99FB4B71, parentIndexField = 3, fields = {
        @Db2Field(name = {"regionMinX", "regionMinY", "regionMinZ", "regionMaxX", "regionMaxY", "regionMaxZ"}, type = Db2Type.FLOAT),
        @Db2Field(name = {"regionOffsetX", "regionOffsetY"}, type = Db2Type.FLOAT),
        @Db2Field(name = "regionScale", type = Db2Type.FLOAT),
        @Db2Field(name = "mapID", type = Db2Type.SHORT),
        @Db2Field(name = "areaID", type = Db2Type.SHORT),
        @Db2Field(name = "newMapID", type = Db2Type.SHORT),
        @Db2Field(name = "newDungeonMapID", type = Db2Type.SHORT),
        @Db2Field(name = "newAreaID", type = Db2Type.SHORT),
        @Db2Field(name = "flags", type = Db2Type.BYTE),
        @Db2Field(name = "priority", type = Db2Type.INT, signed = true)
})
public class WorldMapTransform implements DbcEntity {
    @Id
    
    @Column("ID")
    private int id;

    @Column("RegionMinX")
    private float regionMinX;

    @Column("RegionMinY")
    private float regionMinY;

    @Column("RegionMinZ")
    private float regionMinZ;

    @Column("RegionMaxX")
    private float regionMaxX;

    @Column("RegionMaxY")
    private float regionMaxY;

    @Column("RegionMaxZ")
    private float regionMaxZ;

    @Column("RegionOffsetX")
    private float regionOffsetX;

    @Column("RegionOffsetY")
    private float regionOffsetY;

    @Column("RegionScale")
    private float regionScale;

    @Column("MapID")
    private short mapID;

    @Column("AreaID")
    private short areaID;

    @Column("NewMapID")
    private short newMapID;

    @Column("NewDungeonMapID")
    private short newDungeonMapID;

    @Column("NewAreaID")
    private short newAreaID;

    @Column("Flags")
    private byte flags;

    @Column("Priority")
    private int priority;

    @Id
    
    @Column("VerifiedBuild")
    private int verifiedBuild;

}
