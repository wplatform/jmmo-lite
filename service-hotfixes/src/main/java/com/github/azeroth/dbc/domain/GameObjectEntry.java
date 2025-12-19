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


@Table(name = "gameobjects")
@Db2DataBind(name = "GameObjects.db2", layoutHash = 0x597E8643, indexField = 11, parentIndexField = 5, fields = {
        @Db2Field(name = "name", type = Db2Type.STRING),
        @Db2Field(name = {"posX", "posY", "posZ"}, type = Db2Type.FLOAT),
        @Db2Field(name = {"rot1", "rot2", "rot3", "rot4"}, type = Db2Type.FLOAT),
        @Db2Field(name = "scale", type = Db2Type.FLOAT),
        @Db2Field(name = {"propValue1", "propValue2", "propValue3", "propValue4", "propValue5", "propValue6", "propValue7", "propValue8"}, type = Db2Type.INT, signed = true),
        @Db2Field(name = "ownerID", type = Db2Type.SHORT),
        @Db2Field(name = "displayID", type = Db2Type.SHORT),
        @Db2Field(name = "phaseID", type = Db2Type.SHORT),
        @Db2Field(name = "phaseGroupID", type = Db2Type.SHORT),
        @Db2Field(name = "phaseUseFlags", type = Db2Type.BYTE),
        @Db2Field(name = "typeID", type = Db2Type.BYTE),
        @Db2Field(name = "id", type = Db2Type.INT)
})
public class GameObjectEntry implements DbcEntity {
    @Column("Name")
    private LocalizedString name;

    @Column("PosX")
    private float posX;

    @Column("PosY")
    private float posY;

    @Column("PosZ")
    private float posZ;

    @Column("Rot1")
    private float rot1;

    @Column("Rot2")
    private float rot2;

    @Column("Rot3")
    private float rot3;

    @Column("Rot4")
    private float rot4;

    @Column("Scale")
    private float scale;

    @Column("PropValue1")
    private int propValue1;

    @Column("PropValue2")
    private int propValue2;

    @Column("PropValue3")
    private int propValue3;

    @Column("PropValue4")
    private int propValue4;

    @Column("PropValue5")
    private int propValue5;

    @Column("PropValue6")
    private int propValue6;

    @Column("PropValue7")
    private int propValue7;

    @Column("PropValue8")
    private int propValue8;

    @Column("OwnerID")
    private short ownerID;

    @Column("DisplayID")
    private int displayID;

    @Column("PhaseID")
    private short phaseID;

    @Column("PhaseGroupID")
    private short phaseGroupID;

    @Column("PhaseUseFlags")
    private byte phaseUseFlags;

    @Column("TypeID")
    private byte typeID;

    @Id

    @Column("ID")
    private int id;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;


    public int[] getPropValues() {
        return new int[]{propValue1, propValue2, propValue3, propValue4, propValue5, propValue6, propValue7, propValue8};
    }

}
