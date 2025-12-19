package com.github.azeroth.dbc.domain;

import com.github.azeroth.cache.DbcEntity;
import com.github.azeroth.common.LocalizedString;
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


@Table(name = "garr_plot")
@Db2DataBind(name = "GarrPlot.db2", layoutHash = 0xE12049E0, fields = {
        @Db2Field(name = "name", type = Db2Type.STRING),
        @Db2Field(name = "allianceConstructObjID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "hordeConstructObjID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "uiCategoryID", type = Db2Type.BYTE),
        @Db2Field(name = "plotType", type = Db2Type.BYTE),
        @Db2Field(name = "flags", type = Db2Type.BYTE),
        @Db2Field(name = {"upgradeRequirement1", "upgradeRequirement2"}, type = Db2Type.INT)
})
public class GarrPlot implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("Name")
    private LocalizedString name;

    @Column("AllianceConstructObjID")
    private int allianceConstructObjID;

    @Column("HordeConstructObjID")
    private int hordeConstructObjID;

    @Column("UiCategoryID")
    private byte uiCategoryID;

    @Column("PlotType")
    private byte plotType;

    @Column("Flags")
    private byte flags;

    @Column("UpgradeRequirement1")
    private int upgradeRequirement1;

    @Column("UpgradeRequirement2")
    private int upgradeRequirement2;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
