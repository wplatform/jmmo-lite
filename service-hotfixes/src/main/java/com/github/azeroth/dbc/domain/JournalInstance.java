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


@Table(name = "journal_instance")
@Db2DataBind(name = "JournalInstance.db2", layoutHash = 0x1691CC3D, indexField = 10, fields = {
        @Db2Field(name = "name", type = Db2Type.STRING),
        @Db2Field(name = "description", type = Db2Type.STRING),
        @Db2Field(name = "buttonFileDataID", type = Db2Type.INT),
        @Db2Field(name = "buttonSmallFileDataID", type = Db2Type.INT),
        @Db2Field(name = "backgroundFileDataID", type = Db2Type.INT),
        @Db2Field(name = "loreFileDataID", type = Db2Type.INT),
        @Db2Field(name = "mapID", type = Db2Type.SHORT),
        @Db2Field(name = "areaID", type = Db2Type.SHORT),
        @Db2Field(name = "orderIndex", type = Db2Type.BYTE),
        @Db2Field(name = "flags", type = Db2Type.BYTE),
        @Db2Field(name = "id", type = Db2Type.INT)
})
public class JournalInstance implements DbcEntity {
    @Column("Name")
    private LocalizedString name;

    @Column("Description")
    private LocalizedString description;

    @Column("ButtonFileDataID")
    private int buttonFileDataID;

    @Column("ButtonSmallFileDataID")
    private int buttonSmallFileDataID;

    @Column("BackgroundFileDataID")
    private int backgroundFileDataID;

    @Column("LoreFileDataID")
    private int loreFileDataID;

    @Column("MapID")
    private short mapID;

    @Column("AreaID")
    private short areaID;

    @Column("OrderIndex")
    private byte orderIndex;

    @Column("Flags")
    private byte flags;

    @Id

    @Column("ID")
    private int id;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
