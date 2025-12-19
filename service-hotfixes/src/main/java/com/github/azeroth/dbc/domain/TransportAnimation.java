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


@Table(name = "transport_animation")
@Db2DataBind(name = "TransportAnimation.db2", layoutHash = 0x099987ED, parentIndexField = 3, fields = {
        @Db2Field(name = "timeIndex", type = Db2Type.INT),
        @Db2Field(name = {"posX", "posY", "posZ"}, type = Db2Type.FLOAT),
        @Db2Field(name = "sequenceID", type = Db2Type.BYTE),
        @Db2Field(name = "transportID", type = Db2Type.INT, signed = true)
})
public class TransportAnimation implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("TimeIndex")
    private int timeIndex;

    @Column("PosX")
    private float posX;

    @Column("PosY")
    private float posY;

    @Column("PosZ")
    private float posZ;

    @Column("SequenceID")
    private short sequenceID;

    @Column("TransportID")
    private int transportID;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
