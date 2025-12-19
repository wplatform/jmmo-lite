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


@Table(name = "garr_site_level_plot_inst")
@Db2DataBind(name = "GarrSiteLevelPlotInst.db2", layoutHash = 0xC4E74201, parentIndexField = 1, fields = {
        @Db2Field(name = {"uiMarkerPosX", "uiMarkerPosY"}, type = Db2Type.FLOAT),
        @Db2Field(name = "garrSiteLevelID", type = Db2Type.SHORT),
        @Db2Field(name = "garrPlotInstanceID", type = Db2Type.BYTE),
        @Db2Field(name = "uiMarkerSize", type = Db2Type.BYTE)
})
public class GarrSiteLevelPlotInst implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("UiMarkerPosX")
    private float uiMarkerPosX;

    @Column("UiMarkerPosY")
    private float uiMarkerPosY;

    @Column("GarrSiteLevelID")
    private short garrSiteLevelID;

    @Column("GarrPlotInstanceID")
    private byte garrPlotInstanceID;

    @Column("UiMarkerSize")
    private byte uiMarkerSize;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
