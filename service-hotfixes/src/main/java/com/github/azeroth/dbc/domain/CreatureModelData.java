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


@Table(name = "creature_model_data")
@Db2DataBind(name = "CreatureModelData.db2", layoutHash = 0x983BD312, fields = {
        @Db2Field(name = "modelScale", type = Db2Type.FLOAT),
        @Db2Field(name = "footprintTextureLength", type = Db2Type.FLOAT),
        @Db2Field(name = "footprintTextureWidth", type = Db2Type.FLOAT),
        @Db2Field(name = "footprintParticleScale", type = Db2Type.FLOAT),
        @Db2Field(name = "collisionWidth", type = Db2Type.FLOAT),
        @Db2Field(name = "collisionHeight", type = Db2Type.FLOAT),
        @Db2Field(name = "mountHeight", type = Db2Type.FLOAT),
        @Db2Field(name = {"geoBox1", "geoBox2", "geoBox3", "geoBox4", "geoBox5", "geoBox6"}, type = Db2Type.FLOAT),
        @Db2Field(name = "worldEffectScale", type = Db2Type.FLOAT),
        @Db2Field(name = "attachedEffectScale", type = Db2Type.FLOAT),
        @Db2Field(name = "missileCollisionRadius", type = Db2Type.FLOAT),
        @Db2Field(name = "missileCollisionPush", type = Db2Type.FLOAT),
        @Db2Field(name = "missileCollisionRaise", type = Db2Type.FLOAT),
        @Db2Field(name = "overrideLootEffectScale", type = Db2Type.FLOAT),
        @Db2Field(name = "overrideNameScale", type = Db2Type.FLOAT),
        @Db2Field(name = "overrideSelectionRadius", type = Db2Type.FLOAT),
        @Db2Field(name = "tamedPetBaseScale", type = Db2Type.FLOAT),
        @Db2Field(name = "hoverHeight", type = Db2Type.FLOAT),
        @Db2Field(name = "flags", type = Db2Type.INT),
        @Db2Field(name = "fileDataID", type = Db2Type.INT),
        @Db2Field(name = "sizeClass", type = Db2Type.INT),
        @Db2Field(name = "bloodID", type = Db2Type.INT),
        @Db2Field(name = "footprintTextureID", type = Db2Type.INT),
        @Db2Field(name = "foleyMaterialID", type = Db2Type.INT),
        @Db2Field(name = "footstepCameraEffectID", type = Db2Type.INT),
        @Db2Field(name = "deathThudCameraEffectID", type = Db2Type.INT),
        @Db2Field(name = "soundID", type = Db2Type.INT),
        @Db2Field(name = "creatureGeosetDataID", type = Db2Type.INT)
})
public class CreatureModelData implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("ModelScale")
    private float modelScale;

    @Column("FootprintTextureLength")
    private float footprintTextureLength;

    @Column("FootprintTextureWidth")
    private float footprintTextureWidth;

    @Column("FootprintParticleScale")
    private float footprintParticleScale;

    @Column("CollisionWidth")
    private float collisionWidth;

    @Column("CollisionHeight")
    private float collisionHeight;

    @Column("MountHeight")
    private float mountHeight;

    @Column("GeoBox1")
    private float geoBox1;

    @Column("GeoBox2")
    private float geoBox2;

    @Column("GeoBox3")
    private float geoBox3;

    @Column("GeoBox4")
    private float geoBox4;

    @Column("GeoBox5")
    private float geoBox5;

    @Column("GeoBox6")
    private float geoBox6;

    @Column("WorldEffectScale")
    private float worldEffectScale;

    @Column("AttachedEffectScale")
    private float attachedEffectScale;

    @Column("MissileCollisionRadius")
    private float missileCollisionRadius;

    @Column("MissileCollisionPush")
    private float missileCollisionPush;

    @Column("MissileCollisionRaise")
    private float missileCollisionRaise;

    @Column("OverrideLootEffectScale")
    private float overrideLootEffectScale;

    @Column("OverrideNameScale")
    private float overrideNameScale;

    @Column("OverrideSelectionRadius")
    private float overrideSelectionRadius;

    @Column("TamedPetBaseScale")
    private float tamedPetBaseScale;

    @Column("HoverHeight")
    private float hoverHeight;

    @Column("Flags")
    private int flags;

    @Column("FileDataID")
    private int fileDataID;

    @Column("SizeClass")
    private int sizeClass;

    @Column("BloodID")
    private int bloodID;

    @Column("FootprintTextureID")
    private int footprintTextureID;

    @Column("FoleyMaterialID")
    private int foleyMaterialID;

    @Column("FootstepCameraEffectID")
    private int footstepCameraEffectID;

    @Column("DeathThudCameraEffectID")
    private int deathThudCameraEffectID;

    @Column("SoundID")
    private int soundID;

    @Column("CreatureGeosetDataID")
    private int creatureGeosetDataID;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
