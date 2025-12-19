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


@Table(name = "garr_class_spec")
@Db2DataBind(name = "GarrClassSpec.db2", layoutHash = 0x194CD478, indexField = 7, fields = {
        @Db2Field(name = "classSpec", type = Db2Type.STRING),
        @Db2Field(name = "classSpecMale", type = Db2Type.STRING),
        @Db2Field(name = "classSpecFemale", type = Db2Type.STRING),
        @Db2Field(name = "uiTextureAtlasMemberID", type = Db2Type.SHORT),
        @Db2Field(name = "garrFollItemSetID", type = Db2Type.SHORT),
        @Db2Field(name = "followerClassLimit", type = Db2Type.BYTE),
        @Db2Field(name = "flags", type = Db2Type.BYTE),
        @Db2Field(name = "id", type = Db2Type.INT)
})
public class GarrClassSpec implements DbcEntity {
    @Column("ClassSpec")
    private LocalizedString classSpec;

    @Column("ClassSpecMale")
    private LocalizedString classSpecMale;

    @Column("ClassSpecFemale")
    private LocalizedString classSpecFemale;

    @Column("UiTextureAtlasMemberID")
    private short uiTextureAtlasMemberID;

    @Column("GarrFollItemSetID")
    private short garrFollItemSetID;

    @Column("FollowerClassLimit")
    private byte followerClassLimit;

    @Column("Flags")
    private byte flags;

    @Id

    @Column("ID")
    private int id;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
