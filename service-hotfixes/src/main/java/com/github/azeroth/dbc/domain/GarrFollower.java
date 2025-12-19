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


@Table(name = "garr_follower")
@Db2DataBind(name = "GarrFollower.db2", layoutHash = 0xAAB75E04, indexField = 31, fields = {
        @Db2Field(name = "hordeSourceText", type = Db2Type.STRING),
        @Db2Field(name = "allianceSourceText", type = Db2Type.STRING),
        @Db2Field(name = "titleName", type = Db2Type.STRING),
        @Db2Field(name = "hordeCreatureID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "allianceCreatureID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "hordeIconFileDataID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "allianceIconFileDataID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "hordeSlottingBroadcastTextID", type = Db2Type.INT),
        @Db2Field(name = "allySlottingBroadcastTextID", type = Db2Type.INT),
        @Db2Field(name = "hordeGarrFollItemSetID", type = Db2Type.SHORT),
        @Db2Field(name = "allianceGarrFollItemSetID", type = Db2Type.SHORT),
        @Db2Field(name = "itemLevelWeapon", type = Db2Type.SHORT),
        @Db2Field(name = "itemLevelArmor", type = Db2Type.SHORT),
        @Db2Field(name = "hordeUITextureKitID", type = Db2Type.SHORT),
        @Db2Field(name = "allianceUITextureKitID", type = Db2Type.SHORT),
        @Db2Field(name = "garrFollowerTypeID", type = Db2Type.BYTE),
        @Db2Field(name = "hordeGarrFollRaceID", type = Db2Type.BYTE),
        @Db2Field(name = "allianceGarrFollRaceID", type = Db2Type.BYTE),
        @Db2Field(name = "quality", type = Db2Type.BYTE),
        @Db2Field(name = "hordeGarrClassSpecID", type = Db2Type.BYTE),
        @Db2Field(name = "allianceGarrClassSpecID", type = Db2Type.BYTE),
        @Db2Field(name = "followerLevel", type = Db2Type.BYTE),
        @Db2Field(name = "gender", type = Db2Type.BYTE),
        @Db2Field(name = "flags", type = Db2Type.BYTE),
        @Db2Field(name = "hordeSourceTypeEnum", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "allianceSourceTypeEnum", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "garrTypeID", type = Db2Type.BYTE),
        @Db2Field(name = "vitality", type = Db2Type.BYTE),
        @Db2Field(name = "chrClassID", type = Db2Type.BYTE),
        @Db2Field(name = "hordeFlavorGarrStringID", type = Db2Type.BYTE),
        @Db2Field(name = "allianceFlavorGarrStringID", type = Db2Type.BYTE),
        @Db2Field(name = "id", type = Db2Type.INT)
})
public class GarrFollower implements DbcEntity {
    @Column("HordeSourceText")
    private LocalizedString hordeSourceText;

    @Column("AllianceSourceText")
    private LocalizedString allianceSourceText;

    @Column("TitleName")
    private LocalizedString titleName;

    @Column("HordeCreatureID")
    private int hordeCreatureID;

    @Column("AllianceCreatureID")
    private int allianceCreatureID;

    @Column("HordeIconFileDataID")
    private int hordeIconFileDataID;

    @Column("AllianceIconFileDataID")
    private int allianceIconFileDataID;

    @Column("HordeSlottingBroadcastTextID")
    private int hordeSlottingBroadcastTextID;

    @Column("AllySlottingBroadcastTextID")
    private int allySlottingBroadcastTextID;

    @Column("HordeGarrFollItemSetID")
    private short hordeGarrFollItemSetID;

    @Column("AllianceGarrFollItemSetID")
    private short allianceGarrFollItemSetID;

    @Column("ItemLevelWeapon")
    private short itemLevelWeapon;

    @Column("ItemLevelArmor")
    private short itemLevelArmor;

    @Column("HordeUITextureKitID")
    private short hordeUITextureKitID;

    @Column("AllianceUITextureKitID")
    private short allianceUITextureKitID;

    @Column("GarrFollowerTypeID")
    private byte garrFollowerTypeID;

    @Column("HordeGarrFollRaceID")
    private byte hordeGarrFollRaceID;

    @Column("AllianceGarrFollRaceID")
    private byte allianceGarrFollRaceID;

    @Column("Quality")
    private byte quality;

    @Column("HordeGarrClassSpecID")
    private short hordeGarrClassSpecID;

    @Column("AllianceGarrClassSpecID")
    private short allianceGarrClassSpecID;

    @Column("FollowerLevel")
    private byte followerLevel;

    @Column("Gender")
    private byte gender;

    @Column("Flags")
    private byte flags;

    @Column("HordeSourceTypeEnum")
    private byte hordeSourceTypeEnum;

    @Column("AllianceSourceTypeEnum")
    private byte allianceSourceTypeEnum;

    @Column("GarrTypeID")
    private byte garrTypeID;

    @Column("Vitality")
    private byte vitality;

    @Column("ChrClassID")
    private byte chrClassID;

    @Column("HordeFlavorGarrStringID")
    private byte hordeFlavorGarrStringID;

    @Column("AllianceFlavorGarrStringID")
    private byte allianceFlavorGarrStringID;

    @Id

    @Column("ID")
    private int id;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
