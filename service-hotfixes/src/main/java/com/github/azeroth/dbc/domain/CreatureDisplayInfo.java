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


@Table(name = "creature_display_info")
@Db2DataBind(name = "CreatureDisplayInfo.db2", layoutHash = 0x406268DF, indexField = 0, fields = {
        @Db2Field(name = "id", type = Db2Type.INT),
        @Db2Field(name = "creatureModelScale", type = Db2Type.FLOAT),
        @Db2Field(name = "modelID", type = Db2Type.SHORT),
        @Db2Field(name = "nPCSoundID", type = Db2Type.SHORT),
        @Db2Field(name = "sizeClass", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "flags", type = Db2Type.BYTE),
        @Db2Field(name = "gender", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "extendedDisplayInfoID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "portraitTextureFileDataID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "creatureModelAlpha", type = Db2Type.BYTE),
        @Db2Field(name = "soundID", type = Db2Type.SHORT),
        @Db2Field(name = "playerOverrideScale", type = Db2Type.FLOAT),
        @Db2Field(name = "portraitCreatureDisplayInfoID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "bloodID", type = Db2Type.BYTE),
        @Db2Field(name = "particleColorID", type = Db2Type.SHORT),
        @Db2Field(name = "creatureGeosetData", type = Db2Type.INT),
        @Db2Field(name = "objectEffectPackageID", type = Db2Type.SHORT),
        @Db2Field(name = "animReplacementSetID", type = Db2Type.SHORT),
        @Db2Field(name = "unarmedWeaponType", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "stateSpellVisualKitID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "petInstanceScale", type = Db2Type.FLOAT),
        @Db2Field(name = "mountPoofSpellVisualKitID", type = Db2Type.INT, signed = true),
        @Db2Field(name = {"textureVariationFileDataID1", "textureVariationFileDataID2", "textureVariationFileDataID3"}, type = Db2Type.INT, signed = true)
})
public class CreatureDisplayInfo implements DbcEntity {
    @Id
    
    @Column("ID")
    private int id;

    @Column("CreatureModelScale")
    private float creatureModelScale;

    @Column("ModelID")
    private short modelID;

    @Column("NPCSoundID")
    private short nPCSoundID;

    @Column("SizeClass")
    private byte sizeClass;

    @Column("Flags")
    private byte flags;

    @Column("Gender")
    private byte gender;

    @Column("ExtendedDisplayInfoID")
    private int extendedDisplayInfoID;

    @Column("PortraitTextureFileDataID")
    private int portraitTextureFileDataID;

    @Column("CreatureModelAlpha")
    private short creatureModelAlpha;

    @Column("SoundID")
    private short soundID;

    @Column("PlayerOverrideScale")
    private float playerOverrideScale;

    @Column("PortraitCreatureDisplayInfoID")
    private int portraitCreatureDisplayInfoID;

    @Column("BloodID")
    private byte bloodID;

    @Column("ParticleColorID")
    private short particleColorID;

    @Column("CreatureGeosetData")
    private int creatureGeosetData;

    @Column("ObjectEffectPackageID")
    private short objectEffectPackageID;

    @Column("AnimReplacementSetID")
    private short animReplacementSetID;

    @Column("UnarmedWeaponType")
    private byte unarmedWeaponType;

    @Column("StateSpellVisualKitID")
    private int stateSpellVisualKitID;

    @Column("PetInstanceScale")
    private float petInstanceScale;

    @Column("MountPoofSpellVisualKitID")
    private int mountPoofSpellVisualKitID;

    @Column("TextureVariationFileDataID1")
    private int textureVariationFileDataID1;

    @Column("TextureVariationFileDataID2")
    private int textureVariationFileDataID2;

    @Column("TextureVariationFileDataID3")
    private int textureVariationFileDataID3;

    @Id
    
    @Column("VerifiedBuild")
    private int verifiedBuild;

}
