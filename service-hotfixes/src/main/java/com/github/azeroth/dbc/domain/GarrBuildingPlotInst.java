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


@Table(name = "garr_building_plot_inst")
@Db2DataBind(name = "GarrBuildingPlotInst.db2", layoutHash = 0xF45B6227, indexField = 4, parentIndexField = 3, fields = {
        @Db2Field(name = {"mapOffsetX", "mapOffsetY"}, type = Db2Type.FLOAT),
        @Db2Field(name = "uiTextureAtlasMemberID", type = Db2Type.SHORT),
        @Db2Field(name = "garrSiteLevelPlotInstID", type = Db2Type.SHORT),
        @Db2Field(name = "garrBuildingID", type = Db2Type.BYTE),
        @Db2Field(name = "id", type = Db2Type.INT)
})
public class GarrBuildingPlotInst implements DbcEntity {
    @Column("MapOffsetX")
    private float mapOffsetX;

    @Column("MapOffsetY")
    private float mapOffsetY;

    @Column("UiTextureAtlasMemberID")
    private short uiTextureAtlasMemberID;

    @Column("GarrSiteLevelPlotInstID")
    private short garrSiteLevelPlotInstID;

    @Column("GarrBuildingID")
    private short garrBuildingID;

    @Id

    @Column("ID")
    private int id;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
