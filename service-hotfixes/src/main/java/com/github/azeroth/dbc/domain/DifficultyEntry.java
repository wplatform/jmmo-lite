package com.github.azeroth.dbc.domain;

import com.github.azeroth.cache.DbcEntity;
import com.github.azeroth.common.EnumFlag;
import com.github.azeroth.common.LocalizedString;
import com.github.azeroth.dbc.db2.Db2Field;
import com.github.azeroth.dbc.db2.Db2DataBind;
import com.github.azeroth.dbc.db2.Db2Type;
import com.github.azeroth.dbc.defines.DifficultyFlag;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString


@Table(name = "difficulty")
@Db2DataBind(name = "Difficulty.db2", layoutHash = 0x92302BB8, fields = {
        @Db2Field(name = "name", type = Db2Type.STRING),
        @Db2Field(name = "groupSizeHealthCurveID", type = Db2Type.SHORT),
        @Db2Field(name = "groupSizeDmgCurveID", type = Db2Type.SHORT),
        @Db2Field(name = "groupSizeSpellPointsCurveID", type = Db2Type.SHORT),
        @Db2Field(name = "fallbackDifficultyID", type = Db2Type.BYTE),
        @Db2Field(name = "instanceType", type = Db2Type.BYTE),
        @Db2Field(name = "minPlayers", type = Db2Type.BYTE),
        @Db2Field(name = "maxPlayers", type = Db2Type.BYTE),
        @Db2Field(name = "oldEnumValue", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "flags", type = Db2Type.BYTE),
        @Db2Field(name = "toggleDifficultyID", type = Db2Type.BYTE),
        @Db2Field(name = "itemContext", type = Db2Type.BYTE),
        @Db2Field(name = "orderIndex", type = Db2Type.BYTE)
})
public class DifficultyEntry implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("Name")
    private LocalizedString name;

    @Column("GroupSizeHealthCurveID")
    private short groupSizeHealthCurveID;

    @Column("GroupSizeDmgCurveID")
    private short groupSizeDmgCurveID;

    @Column("GroupSizeSpellPointsCurveID")
    private short groupSizeSpellPointsCurveID;

    @Column("FallbackDifficultyID")
    private byte fallbackDifficultyID;

    @Column("InstanceType")
    private byte instanceType;

    @Column("MinPlayers")
    private byte minPlayers;

    @Column("MaxPlayers")
    private byte maxPlayers;

    @Column("OldEnumValue")
    private byte oldEnumValue;

    @Column("Flags")
    private short flags;

    @Column("ToggleDifficultyID")
    private byte toggleDifficultyID;

    @Column("ItemContext")
    private byte itemContext;

    @Column("OrderIndex")
    private byte orderIndex;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;


    public EnumFlag<DifficultyFlag> flags() {
        return EnumFlag.of(DifficultyFlag.class, flags);
    }

}
