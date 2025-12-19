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


@Table(name = "spell_item_enchantment")
@Db2DataBind(name = "SpellItemEnchantment.db2", layoutHash = 0x80DEA734, fields = {
        @Db2Field(name = "name", type = Db2Type.STRING),
        @Db2Field(name = {"effectArg1", "effectArg2", "effectArg3"}, type = Db2Type.INT),
        @Db2Field(name = {"effectScalingPoints1", "effectScalingPoints2", "effectScalingPoints3"}, type = Db2Type.FLOAT),
        @Db2Field(name = "transmogCost", type = Db2Type.INT),
        @Db2Field(name = "iconFileDataID", type = Db2Type.INT),
        @Db2Field(name = {"effectPointsMin1", "effectPointsMin2", "effectPointsMin3"}, type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "itemVisual", type = Db2Type.SHORT),
        @Db2Field(name = "flags", type = Db2Type.SHORT),
        @Db2Field(name = "requiredSkillID", type = Db2Type.SHORT),
        @Db2Field(name = "requiredSkillRank", type = Db2Type.SHORT),
        @Db2Field(name = "itemLevel", type = Db2Type.SHORT),
        @Db2Field(name = "charges", type = Db2Type.BYTE),
        @Db2Field(name = {"effect1", "effect2", "effect3"}, type = Db2Type.BYTE),
        @Db2Field(name = "conditionID", type = Db2Type.BYTE),
        @Db2Field(name = "minLevel", type = Db2Type.BYTE),
        @Db2Field(name = "maxLevel", type = Db2Type.BYTE),
        @Db2Field(name = "scalingClass", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "scalingClassRestricted", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "transmogPlayerConditionID", type = Db2Type.INT)
})
public class SpellItemEnchantment implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("Name")
    private LocalizedString name;

    @Column("EffectArg1")
    private int effectArg1;

    @Column("EffectArg2")
    private int effectArg2;

    @Column("EffectArg3")
    private int effectArg3;

    @Column("EffectScalingPoints1")
    private float effectScalingPoints1;

    @Column("EffectScalingPoints2")
    private float effectScalingPoints2;

    @Column("EffectScalingPoints3")
    private float effectScalingPoints3;

    @Column("TransmogCost")
    private int transmogCost;

    @Column("IconFileDataID")
    private int iconFileDataID;

    @Column("EffectPointsMin1")
    private short effectPointsMin1;

    @Column("EffectPointsMin2")
    private short effectPointsMin2;

    @Column("EffectPointsMin3")
    private short effectPointsMin3;

    @Column("ItemVisual")
    private short itemVisual;

    @Column("Flags")
    private short flags;

    @Column("RequiredSkillID")
    private short requiredSkillID;

    @Column("RequiredSkillRank")
    private short requiredSkillRank;

    @Column("ItemLevel")
    private short itemLevel;

    @Column("Charges")
    private byte charges;

    @Column("Effect1")
    private byte effect1;

    @Column("Effect2")
    private byte effect2;

    @Column("Effect3")
    private byte effect3;

    @Column("ConditionID")
    private byte conditionID;

    @Column("MinLevel")
    private byte minLevel;

    @Column("MaxLevel")
    private byte maxLevel;

    @Column("ScalingClass")
    private byte scalingClass;

    @Column("ScalingClassRestricted")
    private byte scalingClassRestricted;

    @Column("TransmogPlayerConditionID")
    private int transmogPlayerConditionID;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
