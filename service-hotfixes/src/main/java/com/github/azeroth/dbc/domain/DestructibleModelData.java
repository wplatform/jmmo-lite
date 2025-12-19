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


@Table(name = "destructible_model_data")
@Db2DataBind(name = "DestructibleModelData.db2", layoutHash = 0x1092C9AF, fields = {
        @Db2Field(name = "state0Wmo", type = Db2Type.SHORT),
        @Db2Field(name = "state1Wmo", type = Db2Type.SHORT),
        @Db2Field(name = "state2Wmo", type = Db2Type.SHORT),
        @Db2Field(name = "state3Wmo", type = Db2Type.SHORT),
        @Db2Field(name = "healEffectSpeed", type = Db2Type.SHORT),
        @Db2Field(name = "state0ImpactEffectDoodadSet", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "state0AmbientDoodadSet", type = Db2Type.BYTE),
        @Db2Field(name = "state0NameSet", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "state1DestructionDoodadSet", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "state1ImpactEffectDoodadSet", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "state1AmbientDoodadSet", type = Db2Type.BYTE),
        @Db2Field(name = "state1NameSet", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "state2DestructionDoodadSet", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "state2ImpactEffectDoodadSet", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "state2AmbientDoodadSet", type = Db2Type.BYTE),
        @Db2Field(name = "state2NameSet", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "state3InitDoodadSet", type = Db2Type.BYTE),
        @Db2Field(name = "state3AmbientDoodadSet", type = Db2Type.BYTE),
        @Db2Field(name = "state3NameSet", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "ejectDirection", type = Db2Type.BYTE),
        @Db2Field(name = "doNotHighlight", type = Db2Type.BYTE),
        @Db2Field(name = "healEffect", type = Db2Type.BYTE)
})
public class DestructibleModelData implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("State0Wmo")
    private int state0Wmo;

    @Column("State1Wmo")
    private int state1Wmo;

    @Column("State2Wmo")
    private int state2Wmo;

    @Column("State3Wmo")
    private int state3Wmo;

    @Column("HealEffectSpeed")
    private short healEffectSpeed;

    @Column("State0ImpactEffectDoodadSet")
    private byte state0ImpactEffectDoodadSet;

    @Column("State0AmbientDoodadSet")
    private byte state0AmbientDoodadSet;

    @Column("State0NameSet")
    private byte state0NameSet;

    @Column("State1DestructionDoodadSet")
    private byte state1DestructionDoodadSet;

    @Column("State1ImpactEffectDoodadSet")
    private byte state1ImpactEffectDoodadSet;

    @Column("State1AmbientDoodadSet")
    private byte state1AmbientDoodadSet;

    @Column("State1NameSet")
    private byte state1NameSet;

    @Column("State2DestructionDoodadSet")
    private byte state2DestructionDoodadSet;

    @Column("State2ImpactEffectDoodadSet")
    private byte state2ImpactEffectDoodadSet;

    @Column("State2AmbientDoodadSet")
    private byte state2AmbientDoodadSet;

    @Column("State2NameSet")
    private byte state2NameSet;

    @Column("State3InitDoodadSet")
    private byte state3InitDoodadSet;

    @Column("State3AmbientDoodadSet")
    private byte state3AmbientDoodadSet;

    @Column("State3NameSet")
    private byte state3NameSet;

    @Column("EjectDirection")
    private byte ejectDirection;

    @Column("DoNotHighlight")
    private byte doNotHighlight;

    @Column("HealEffect")
    private byte healEffect;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
