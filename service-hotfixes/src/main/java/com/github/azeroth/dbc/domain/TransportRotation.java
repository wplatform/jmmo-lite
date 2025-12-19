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


@Table(name = "transport_rotation")
@Db2DataBind(name = "TransportRotation.db2", layoutHash = 0x72035AA9, parentIndexField = 2, fields = {
        @Db2Field(name = "timeIndex", type = Db2Type.INT),
        @Db2Field(name = {"rot1", "rot2", "rot3", "rot4"}, type = Db2Type.FLOAT),
        @Db2Field(name = "gameObjectsID", type = Db2Type.INT, signed = true)
})
public class TransportRotation implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("TimeIndex")
    private int timeIndex;

    @Column("Rot1")
    private float rot1;

    @Column("Rot2")
    private float rot2;

    @Column("Rot3")
    private float rot3;

    @Column("Rot4")
    private float rot4;

    @Column("GameObjectsID")
    private int gameObjectsID;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
