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


@Table(name = "spell_item_enchantment_condition")
@Db2DataBind(name = "SpellItemEnchantmentCondition.db2", layoutHash = 0xB9C16961, fields = {
        @Db2Field(name = {"ltOperand1", "ltOperand2", "ltOperand3", "ltOperand4", "ltOperand5"}, type = Db2Type.INT),
        @Db2Field(name = {"ltOperandType1", "ltOperandType2", "ltOperandType3", "ltOperandType4", "ltOperandType5"}, type = Db2Type.BYTE),
        @Db2Field(name = {"operator1", "operator2", "operator3", "operator4", "operator5"}, type = Db2Type.BYTE),
        @Db2Field(name = {"rtOperandType1", "rtOperandType2", "rtOperandType3", "rtOperandType4", "rtOperandType5"}, type = Db2Type.BYTE),
        @Db2Field(name = {"rtOperand1", "rtOperand2", "rtOperand3", "rtOperand4", "rtOperand5"}, type = Db2Type.BYTE),
        @Db2Field(name = {"logic1", "logic2", "logic3", "logic4", "logic5"}, type = Db2Type.BYTE)
})
public class SpellItemEnchantmentCondition implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("LtOperand1")
    private int ltOperand1;

    @Column("LtOperand2")
    private int ltOperand2;

    @Column("LtOperand3")
    private int ltOperand3;

    @Column("LtOperand4")
    private int ltOperand4;

    @Column("LtOperand5")
    private int ltOperand5;

    @Column("LtOperandType1")
    private byte ltOperandType1;

    @Column("LtOperandType2")
    private byte ltOperandType2;

    @Column("LtOperandType3")
    private byte ltOperandType3;

    @Column("LtOperandType4")
    private byte ltOperandType4;

    @Column("LtOperandType5")
    private byte ltOperandType5;

    @Column("Operator1")
    private byte operator1;

    @Column("Operator2")
    private byte operator2;

    @Column("Operator3")
    private byte operator3;

    @Column("Operator4")
    private byte operator4;

    @Column("Operator5")
    private byte operator5;

    @Column("RtOperandType1")
    private byte rtOperandType1;

    @Column("RtOperandType2")
    private byte rtOperandType2;

    @Column("RtOperandType3")
    private byte rtOperandType3;

    @Column("RtOperandType4")
    private byte rtOperandType4;

    @Column("RtOperandType5")
    private byte rtOperandType5;

    @Column("RtOperand1")
    private byte rtOperand1;

    @Column("RtOperand2")
    private byte rtOperand2;

    @Column("RtOperand3")
    private byte rtOperand3;

    @Column("RtOperand4")
    private byte rtOperand4;

    @Column("RtOperand5")
    private byte rtOperand5;

    @Column("Logic1")
    private byte logic1;

    @Column("Logic2")
    private byte logic2;

    @Column("Logic3")
    private byte logic3;

    @Column("Logic4")
    private byte logic4;

    @Column("Logic5")
    private byte logic5;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
