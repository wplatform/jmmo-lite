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


@Table(name = "cfg_regions")
@Db2DataBind(name = "Cfg_Regions.db2", layoutHash = 0x9F4272BF, fields = {
        @Db2Field(name = "tag", type = Db2Type.STRING_NOT_LOCALIZED),
        @Db2Field(name = "raidorigin", type = Db2Type.INT),
        @Db2Field(name = "challengeOrigin", type = Db2Type.INT),
        @Db2Field(name = "regionID", type = Db2Type.SHORT),
        @Db2Field(name = "regionGroupMask", type = Db2Type.BYTE),
        @Db2Field(name = "timeEventRegionGroupID", type = Db2Type.BYTE)
})
public class CfgRegion implements DbcEntity {
    @Id
    @Column("ID")
    private int id;

    @Column("Tag")
    private String tag;

    @Column("Raidorigin")
    private int raidorigin;

    @Column("ChallengeOrigin")
    private int challengeOrigin;

    @Column("RegionID")
    private short regionID;

    @Column("RegionGroupMask")
    private byte regionGroupMask;

    @Column("TimeEventRegionGroupID")
    private byte timeEventRegionGroupID;

    @Id
    @Column("VerifiedBuild")
    private int verifiedBuild;

}
