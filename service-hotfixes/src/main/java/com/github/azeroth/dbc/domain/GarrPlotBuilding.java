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


@Table(name = "garr_plot_building")
@Db2DataBind(name = "GarrPlotBuilding.db2", layoutHash = 0x3F77A6FA, fields = {
        @Db2Field(name = "garrPlotID", type = Db2Type.BYTE),
        @Db2Field(name = "garrBuildingID", type = Db2Type.BYTE)
})
public class GarrPlotBuilding implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("GarrPlotID")
    private byte garrPlotID;

    @Column("GarrBuildingID")
    private short garrBuildingID;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
