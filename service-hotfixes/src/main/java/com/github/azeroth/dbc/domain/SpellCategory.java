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


@Table(name = "spell_category")
@Db2DataBind(name = "SpellCategory.db2", layoutHash = 0xEA60E384, fields = {
        @Db2Field(name = "name", type = Db2Type.STRING),
        @Db2Field(name = "chargeRecoveryTime", type = Db2Type.INT, signed = true),
        @Db2Field(name = "flags", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "usesPerWeek", type = Db2Type.BYTE),
        @Db2Field(name = "maxCharges", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "typeMask", type = Db2Type.INT, signed = true)
})
public class SpellCategory implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("Name")
    private LocalizedString name;

    @Column("ChargeRecoveryTime")
    private int chargeRecoveryTime;

    @Column("Flags")
    private byte flags;

    @Column("UsesPerWeek")
    private byte usesPerWeek;

    @Column("MaxCharges")
    private byte maxCharges;

    @Column("TypeMask")
    private int typeMask;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
