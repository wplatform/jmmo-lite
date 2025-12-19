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


@Table(name = "spell_effect")
@Db2DataBind(name = "SpellEffect.db2", layoutHash = 0x3244098B, indexField = 0, parentIndexField = 29, fields = {
        @Db2Field(name = "id", type = Db2Type.INT),
        @Db2Field(name = "effect", type = Db2Type.INT),
        @Db2Field(name = "effectBasePoints", type = Db2Type.INT, signed = true),
        @Db2Field(name = "effectIndex", type = Db2Type.INT, signed = true),
        @Db2Field(name = "effectAura", type = Db2Type.INT, signed = true),
        @Db2Field(name = "difficultyID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "effectAmplitude", type = Db2Type.FLOAT),
        @Db2Field(name = "effectAuraPeriod", type = Db2Type.INT, signed = true),
        @Db2Field(name = "effectBonusCoefficient", type = Db2Type.FLOAT),
        @Db2Field(name = "effectChainAmplitude", type = Db2Type.FLOAT),
        @Db2Field(name = "effectChainTargets", type = Db2Type.INT, signed = true),
        @Db2Field(name = "effectDieSides", type = Db2Type.INT, signed = true),
        @Db2Field(name = "effectItemType", type = Db2Type.INT, signed = true),
        @Db2Field(name = "effectMechanic", type = Db2Type.INT, signed = true),
        @Db2Field(name = "effectPointsPerResource", type = Db2Type.FLOAT),
        @Db2Field(name = "effectRealPointsPerLevel", type = Db2Type.FLOAT),
        @Db2Field(name = "effectTriggerSpell", type = Db2Type.INT, signed = true),
        @Db2Field(name = "effectPosFacing", type = Db2Type.FLOAT),
        @Db2Field(name = "effectAttributes", type = Db2Type.INT, signed = true),
        @Db2Field(name = "bonusCoefficientFromAP", type = Db2Type.FLOAT),
        @Db2Field(name = "pvpMultiplier", type = Db2Type.FLOAT),
        @Db2Field(name = "coefficient", type = Db2Type.FLOAT),
        @Db2Field(name = "variance", type = Db2Type.FLOAT),
        @Db2Field(name = "resourceCoefficient", type = Db2Type.FLOAT),
        @Db2Field(name = "groupSizeBasePointsCoefficient", type = Db2Type.FLOAT),
        @Db2Field(name = {"effectSpellClassMask1", "effectSpellClassMask2", "effectSpellClassMask3", "effectSpellClassMask4"}, type = Db2Type.INT, signed = true),
        @Db2Field(name = {"effectMiscValue1", "effectMiscValue2"}, type = Db2Type.INT, signed = true),
        @Db2Field(name = {"effectRadiusIndex1", "effectRadiusIndex2"}, type = Db2Type.INT),
        @Db2Field(name = {"implicitTarget1", "implicitTarget2"}, type = Db2Type.INT),
        @Db2Field(name = "spellID", type = Db2Type.INT, signed = true)
})
public class SpellEffect implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("Effect")
    private int effect;

    @Column("EffectBasePoints")
    private int effectBasePoints;

    @Column("EffectIndex")
    private int effectIndex;

    @Column("EffectAura")
    private int effectAura;

    @Column("DifficultyID")
    private int difficultyID;

    @Column("EffectAmplitude")
    private float effectAmplitude;

    @Column("EffectAuraPeriod")
    private int effectAuraPeriod;

    @Column("EffectBonusCoefficient")
    private float effectBonusCoefficient;

    @Column("EffectChainAmplitude")
    private float effectChainAmplitude;

    @Column("EffectChainTargets")
    private int effectChainTargets;

    @Column("EffectDieSides")
    private int effectDieSides;

    @Column("EffectItemType")
    private int effectItemType;

    @Column("EffectMechanic")
    private int effectMechanic;

    @Column("EffectPointsPerResource")
    private float effectPointsPerResource;

    @Column("EffectRealPointsPerLevel")
    private float effectRealPointsPerLevel;

    @Column("EffectTriggerSpell")
    private int effectTriggerSpell;

    @Column("EffectPosFacing")
    private float effectPosFacing;

    @Column("EffectAttributes")
    private int effectAttributes;

    @Column("BonusCoefficientFromAP")
    private float bonusCoefficientFromAP;

    @Column("PvpMultiplier")
    private float pvpMultiplier;

    @Column("Coefficient")
    private float coefficient;

    @Column("Variance")
    private float variance;

    @Column("ResourceCoefficient")
    private float resourceCoefficient;

    @Column("GroupSizeBasePointsCoefficient")
    private float groupSizeBasePointsCoefficient;

    @Column("EffectSpellClassMask1")
    private int effectSpellClassMask1;

    @Column("EffectSpellClassMask2")
    private int effectSpellClassMask2;

    @Column("EffectSpellClassMask3")
    private int effectSpellClassMask3;

    @Column("EffectSpellClassMask4")
    private int effectSpellClassMask4;

    @Column("EffectMiscValue1")
    private int effectMiscValue1;

    @Column("EffectMiscValue2")
    private int effectMiscValue2;

    @Column("EffectRadiusIndex1")
    private int effectRadiusIndex1;

    @Column("EffectRadiusIndex2")
    private int effectRadiusIndex2;

    @Column("ImplicitTarget1")
    private int implicitTarget1;

    @Column("ImplicitTarget2")
    private int implicitTarget2;

    @Column("SpellID")
    private int spellID;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
