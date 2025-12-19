package com.github.azeroth.dbc.domain;

import com.github.azeroth.common.LocalizedString;
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


@Table(name = "garr_building")
@Db2DataBind(name = "GarrBuilding.db2", layoutHash = 0x200F9858, fields = {
        @Db2Field(name = "allianceName", type = Db2Type.STRING),
        @Db2Field(name = "hordeName", type = Db2Type.STRING),
        @Db2Field(name = "description", type = Db2Type.STRING),
        @Db2Field(name = "tooltip", type = Db2Type.STRING),
        @Db2Field(name = "hordeGameObjectID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "allianceGameObjectID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "iconFileDataID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "currencyTypeID", type = Db2Type.SHORT),
        @Db2Field(name = "hordeUiTextureKitID", type = Db2Type.SHORT),
        @Db2Field(name = "allianceUiTextureKitID", type = Db2Type.SHORT),
        @Db2Field(name = "allianceSceneScriptPackageID", type = Db2Type.SHORT),
        @Db2Field(name = "hordeSceneScriptPackageID", type = Db2Type.SHORT),
        @Db2Field(name = "garrAbilityID", type = Db2Type.SHORT),
        @Db2Field(name = "bonusGarrAbilityID", type = Db2Type.SHORT),
        @Db2Field(name = "goldCost", type = Db2Type.SHORT),
        @Db2Field(name = "garrSiteID", type = Db2Type.BYTE),
        @Db2Field(name = "buildingType", type = Db2Type.BYTE),
        @Db2Field(name = "upgradeLevel", type = Db2Type.BYTE),
        @Db2Field(name = "flags", type = Db2Type.BYTE),
        @Db2Field(name = "shipmentCapacity", type = Db2Type.BYTE),
        @Db2Field(name = "garrTypeID", type = Db2Type.BYTE),
        @Db2Field(name = "buildSeconds", type = Db2Type.INT, signed = true),
        @Db2Field(name = "currencyQty", type = Db2Type.INT, signed = true),
        @Db2Field(name = "maxAssignments", type = Db2Type.INT, signed = true)
})
public class GarrBuilding implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("AllianceName")
    private LocalizedString allianceName;

    @Column("HordeName")
    private LocalizedString hordeName;

    @Column("Description")
    private LocalizedString description;

    @Column("Tooltip")
    private LocalizedString tooltip;

    @Column("HordeGameObjectID")
    private int hordeGameObjectID;

    @Column("AllianceGameObjectID")
    private int allianceGameObjectID;

    @Column("IconFileDataID")
    private int iconFileDataID;

    @Column("CurrencyTypeID")
    private short currencyTypeID;

    @Column("HordeUiTextureKitID")
    private short hordeUiTextureKitID;

    @Column("AllianceUiTextureKitID")
    private short allianceUiTextureKitID;

    @Column("AllianceSceneScriptPackageID")
    private short allianceSceneScriptPackageID;

    @Column("HordeSceneScriptPackageID")
    private short hordeSceneScriptPackageID;

    @Column("GarrAbilityID")
    private short garrAbilityID;

    @Column("BonusGarrAbilityID")
    private short bonusGarrAbilityID;

    @Column("GoldCost")
    private short goldCost;

    @Column("GarrSiteID")
    private byte garrSiteID;

    @Column("BuildingType")
    private byte buildingType;

    @Column("UpgradeLevel")
    private byte upgradeLevel;

    @Column("Flags")
    private byte flags;

    @Column("ShipmentCapacity")
    private byte shipmentCapacity;

    @Column("GarrTypeID")
    private byte garrTypeID;

    @Column("BuildSeconds")
    private int buildSeconds;

    @Column("CurrencyQty")
    private int currencyQty;

    @Column("MaxAssignments")
    private int maxAssignments;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
