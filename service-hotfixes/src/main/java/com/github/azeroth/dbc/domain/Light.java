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


@Table(name = "light")
@Db2DataBind(name = "Light.db2", layoutHash = 0x25025A13, fields = {
        @Db2Field(name = {"gameCoordsX", "gameCoordsY", "gameCoordsZ"}, type = Db2Type.FLOAT),
        @Db2Field(name = "gameFalloffStart", type = Db2Type.FLOAT),
        @Db2Field(name = "gameFalloffEnd", type = Db2Type.FLOAT),
        @Db2Field(name = "continentID", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = {"lightParamsID1", "lightParamsID2", "lightParamsID3", "lightParamsID4", "lightParamsID5", "lightParamsID6", "lightParamsID7", "lightParamsID8"}, type = Db2Type.SHORT)
})
public class Light implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("GameCoordsX")
    private float gameCoordsX;

    @Column("GameCoordsY")
    private float gameCoordsY;

    @Column("GameCoordsZ")
    private float gameCoordsZ;

    @Column("GameFalloffStart")
    private float gameFalloffStart;

    @Column("GameFalloffEnd")
    private float gameFalloffEnd;

    @Column("ContinentID")
    private short continentID;

    @Column("LightParamsID1")
    private short lightParamsID1;

    @Column("LightParamsID2")
    private short lightParamsID2;

    @Column("LightParamsID3")
    private short lightParamsID3;

    @Column("LightParamsID4")
    private short lightParamsID4;

    @Column("LightParamsID5")
    private short lightParamsID5;

    @Column("LightParamsID6")
    private short lightParamsID6;

    @Column("LightParamsID7")
    private short lightParamsID7;

    @Column("LightParamsID8")
    private short lightParamsID8;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
