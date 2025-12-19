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


@Table(name = "garr_site_level")
@Db2DataBind(name = "GarrSiteLevel.db2", layoutHash = 0xD3979C38, fields = {
        @Db2Field(name = {"townHallUiPosX", "townHallUiPosY"}, type = Db2Type.FLOAT),
        @Db2Field(name = "mapID", type = Db2Type.SHORT),
        @Db2Field(name = "uiTextureKitID", type = Db2Type.SHORT),
        @Db2Field(name = "upgradeMovieID", type = Db2Type.SHORT),
        @Db2Field(name = "upgradeCost", type = Db2Type.SHORT),
        @Db2Field(name = "upgradeGoldCost", type = Db2Type.SHORT),
        @Db2Field(name = "garrLevel", type = Db2Type.BYTE),
        @Db2Field(name = "garrSiteID", type = Db2Type.BYTE),
        @Db2Field(name = "maxBuildingLevel", type = Db2Type.BYTE)
})
public class GarrSiteLevel implements DbcEntity {
    @Id
    
    @Column("ID")
    private int id;

    @Column("TownHallUiPosX")
    private float townHallUiPosX;

    @Column("TownHallUiPosY")
    private float townHallUiPosY;

    @Column("MapID")
    private short mapID;

    @Column("UiTextureKitID")
    private short uiTextureKitID;

    @Column("UpgradeMovieID")
    private short upgradeMovieID;

    @Column("UpgradeCost")
    private short upgradeCost;

    @Column("UpgradeGoldCost")
    private short upgradeGoldCost;

    @Column("GarrLevel")
    private byte garrLevel;

    @Column("GarrSiteID")
    private short garrSiteID;

    @Column("MaxBuildingLevel")
    private byte maxBuildingLevel;

    @Id
    
    @Column("VerifiedBuild")
    private int verifiedBuild;

}
