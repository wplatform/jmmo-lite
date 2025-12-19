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


@Table(name = "unit_condition")
@Db2DataBind(name = "UnitCondition.db2", layoutHash = 0x62802D9C, fields = {
        @Db2Field(name = {"value1", "value2", "value3", "value4", "value5", "value6", "value7", "value8"}, type = Db2Type.INT, signed = true),
        @Db2Field(name = "flags", type = Db2Type.BYTE),
        @Db2Field(name = {"variable1", "variable2", "variable3", "variable4", "variable5", "variable6", "variable7", "variable8"}, type = Db2Type.BYTE),
        @Db2Field(name = {"op1", "op2", "op3", "op4", "op5", "op6", "op7", "op8"}, type = Db2Type.BYTE, signed = true)
})
public class UnitCondition implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("Value1")
    private int value1;

    @Column("Value2")
    private int value2;

    @Column("Value3")
    private int value3;

    @Column("Value4")
    private int value4;

    @Column("Value5")
    private int value5;

    @Column("Value6")
    private int value6;

    @Column("Value7")
    private int value7;

    @Column("Value8")
    private int value8;

    @Column("Flags")
    private byte flags;

    @Column("Variable1")
    private byte variable1;

    @Column("Variable2")
    private byte variable2;

    @Column("Variable3")
    private byte variable3;

    @Column("Variable4")
    private byte variable4;

    @Column("Variable5")
    private byte variable5;

    @Column("Variable6")
    private byte variable6;

    @Column("Variable7")
    private byte variable7;

    @Column("Variable8")
    private byte variable8;

    @Column("Op1")
    private byte op1;

    @Column("Op2")
    private byte op2;

    @Column("Op3")
    private byte op3;

    @Column("Op4")
    private byte op4;

    @Column("Op5")
    private byte op5;

    @Column("Op6")
    private byte op6;

    @Column("Op7")
    private byte op7;

    @Column("Op8")
    private byte op8;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
