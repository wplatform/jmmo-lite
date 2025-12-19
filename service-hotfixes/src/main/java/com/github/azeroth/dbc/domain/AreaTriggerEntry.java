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


@Table(name = "area_trigger")
@Db2DataBind(name = "AreaTrigger.db2", layoutHash = 0x378573E8, indexField = 14, parentIndexField = 6, fields = {
        @Db2Field(name = {"posX", "posY", "posZ"}, type = Db2Type.FLOAT),
        @Db2Field(name = "radius", type = Db2Type.FLOAT),
        @Db2Field(name = "boxLength", type = Db2Type.FLOAT),
        @Db2Field(name = "boxWidth", type = Db2Type.FLOAT),
        @Db2Field(name = "boxHeight", type = Db2Type.FLOAT),
        @Db2Field(name = "boxYaw", type = Db2Type.FLOAT),
        @Db2Field(name = "continentID", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "phaseID", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "phaseGroupID", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "shapeID", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "areaTriggerActionSetID", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "phaseUseFlags", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "shapeType", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "flags", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "id", type = Db2Type.INT)
})
public class AreaTriggerEntry implements DbcEntity {
    @Column("PosX")
    private float posX;

    @Column("PosY")
    private float posY;

    @Column("PosZ")
    private float posZ;

    @Column("Radius")
    private float radius;

    @Column("BoxLength")
    private float boxLength;

    @Column("BoxWidth")
    private float boxWidth;

    @Column("BoxHeight")
    private float boxHeight;

    @Column("BoxYaw")
    private float boxYaw;

    @Column("ContinentID")
    private short continentID;

    @Column("PhaseID")
    private short phaseID;

    @Column("PhaseGroupID")
    private short phaseGroupID;

    @Column("ShapeID")
    private short shapeID;

    @Column("AreaTriggerActionSetID")
    private short areaTriggerActionSetID;

    @Column("PhaseUseFlags")
    private byte phaseUseFlags;

    @Column("ShapeType")
    private byte shapeType;

    @Column("Flags")
    private byte flags;

    @Id

    @Column("ID")
    private int id;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
