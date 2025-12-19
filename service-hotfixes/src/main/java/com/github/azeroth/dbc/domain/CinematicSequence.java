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


@Table(name = "cinematic_sequence")
@Db2DataBind(name = "CinematicSequences.db2", layoutHash = 0x470FDA8C, fields = {
        @Db2Field(name = "soundID", type = Db2Type.INT),
        @Db2Field(name = {"camera1", "camera2", "camera3", "camera4", "camera5", "camera6", "camera7", "camera8"}, type = Db2Type.SHORT)
})
public class CinematicSequence implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("SoundID")
    private int soundID;

    @Column("Camera1")
    private short camera1;

    @Column("Camera2")
    private short camera2;

    @Column("Camera3")
    private short camera3;

    @Column("Camera4")
    private short camera4;

    @Column("Camera5")
    private short camera5;

    @Column("Camera6")
    private short camera6;

    @Column("Camera7")
    private short camera7;

    @Column("Camera8")
    private short camera8;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
