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


@Table(name = "liquid_type")
@Db2DataBind(name = "LiquidType.db2", layoutHash = 0x3313BBF3, fields = {
        @Db2Field(name = "name", type = Db2Type.STRING_NOT_LOCALIZED),
        @Db2Field(name = {"texture1", "texture2", "texture3", "texture4", "texture5", "texture6"}, type = Db2Type.STRING_NOT_LOCALIZED),
        @Db2Field(name = "spellID", type = Db2Type.INT),
        @Db2Field(name = "maxDarkenDepth", type = Db2Type.FLOAT),
        @Db2Field(name = "fogDarkenIntensity", type = Db2Type.FLOAT),
        @Db2Field(name = "ambDarkenIntensity", type = Db2Type.FLOAT),
        @Db2Field(name = "dirDarkenIntensity", type = Db2Type.FLOAT),
        @Db2Field(name = "particleScale", type = Db2Type.FLOAT),
        @Db2Field(name = {"color1", "color2"}, type = Db2Type.INT, signed = true),
        @Db2Field(name = {"float1", "float2", "float3", "float4", "float5", "float6", "float7", "float8", "float9", "float10", "float11", "float12", "float13", "float14", "float15", "float16", "float17", "float18"}, type = Db2Type.FLOAT),
        @Db2Field(name = {"int1", "int2", "int3", "int4"}, type = Db2Type.INT),
        @Db2Field(name = "flags", type = Db2Type.SHORT),
        @Db2Field(name = "lightID", type = Db2Type.SHORT),
        @Db2Field(name = "soundBank", type = Db2Type.BYTE),
        @Db2Field(name = "particleMovement", type = Db2Type.BYTE),
        @Db2Field(name = "particleTexSlots", type = Db2Type.BYTE),
        @Db2Field(name = "materialID", type = Db2Type.BYTE),
        @Db2Field(name = {"frameCountTexture1", "frameCountTexture2", "frameCountTexture3", "frameCountTexture4", "frameCountTexture5", "frameCountTexture6"}, type = Db2Type.BYTE),
        @Db2Field(name = "soundID", type = Db2Type.INT)
})
public class LiquidType implements DbcEntity {
    @Id
    
    @Column("ID")
    private int id;

    @Column("Name")
    private String name;

    @Column("Texture1")
    private String texture1;

    @Column("Texture2")
    private String texture2;

    @Column("Texture3")
    private String texture3;

    @Column("Texture4")
    private String texture4;

    @Column("Texture5")
    private String texture5;

    @Column("Texture6")
    private String texture6;

    @Column("SpellID")
    private int spellID;

    @Column("MaxDarkenDepth")
    private float maxDarkenDepth;

    @Column("FogDarkenIntensity")
    private float fogDarkenIntensity;

    @Column("AmbDarkenIntensity")
    private float ambDarkenIntensity;

    @Column("DirDarkenIntensity")
    private float dirDarkenIntensity;

    @Column("ParticleScale")
    private float particleScale;

    @Column("Color1")
    private int color1;

    @Column("Color2")
    private int color2;

    @Column("Float1")
    private float float1;

    @Column("Float2")
    private float float2;

    @Column("Float3")
    private float float3;

    @Column("Float4")
    private float float4;

    @Column("Float5")
    private float float5;

    @Column("Float6")
    private float float6;

    @Column("Float7")
    private float float7;

    @Column("Float8")
    private float float8;

    @Column("Float9")
    private float float9;

    @Column("Float10")
    private float float10;

    @Column("Float11")
    private float float11;

    @Column("Float12")
    private float float12;

    @Column("Float13")
    private float float13;

    @Column("Float14")
    private float float14;

    @Column("Float15")
    private float float15;

    @Column("Float16")
    private float float16;

    @Column("Float17")
    private float float17;

    @Column("Float18")
    private float float18;

    @Column("Int1")
    private int int1;

    @Column("Int2")
    private int int2;

    @Column("Int3")
    private int int3;

    @Column("Int4")
    private int int4;

    @Column("Flags")
    private short flags;

    @Column("LightID")
    private short lightID;

    @Column("SoundBank")
    private byte soundBank;

    @Column("ParticleMovement")
    private byte particleMovement;

    @Column("ParticleTexSlots")
    private byte particleTexSlots;

    @Column("MaterialID")
    private byte materialID;

    @Column("FrameCountTexture1")
    private byte frameCountTexture1;

    @Column("FrameCountTexture2")
    private byte frameCountTexture2;

    @Column("FrameCountTexture3")
    private byte frameCountTexture3;

    @Column("FrameCountTexture4")
    private byte frameCountTexture4;

    @Column("FrameCountTexture5")
    private byte frameCountTexture5;

    @Column("FrameCountTexture6")
    private byte frameCountTexture6;

    @Column("SoundID")
    private int soundID;

    @Id
    
    @Column("VerifiedBuild")
    private int verifiedBuild;

}
