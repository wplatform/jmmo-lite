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


@Table(name = "spell_visual_effect_name")
@Db2DataBind(name = "SpellVisualEffectName.db2", layoutHash = 0xB930A934, fields = {
        @Db2Field(name = "effectRadius", type = Db2Type.FLOAT),
        @Db2Field(name = "baseMissileSpeed", type = Db2Type.FLOAT),
        @Db2Field(name = "scale", type = Db2Type.FLOAT),
        @Db2Field(name = "minAllowedScale", type = Db2Type.FLOAT),
        @Db2Field(name = "maxAllowedScale", type = Db2Type.FLOAT),
        @Db2Field(name = "alpha", type = Db2Type.FLOAT),
        @Db2Field(name = "flags", type = Db2Type.INT),
        @Db2Field(name = "genericID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "textureFileDataID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "type", type = Db2Type.BYTE),
        @Db2Field(name = "modelFileDataID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "ribbonQualityID", type = Db2Type.INT),
        @Db2Field(name = "dissolveEffectID", type = Db2Type.INT, signed = true)
})
public class SpellVisualEffectName implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("EffectRadius")
    private float effectRadius;

    @Column("BaseMissileSpeed")
    private float baseMissileSpeed;

    @Column("Scale")
    private float scale;

    @Column("MinAllowedScale")
    private float minAllowedScale;

    @Column("MaxAllowedScale")
    private float maxAllowedScale;

    @Column("Alpha")
    private float alpha;

    @Column("Flags")
    private int flags;

    @Column("GenericID")
    private int genericID;

    @Column("TextureFileDataID")
    private int textureFileDataID;

    @Column("Type")
    private byte type;

    @Column("ModelFileDataID")
    private int modelFileDataID;

    @Column("RibbonQualityID")
    private int ribbonQualityID;

    @Column("DissolveEffectID")
    private int dissolveEffectID;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
