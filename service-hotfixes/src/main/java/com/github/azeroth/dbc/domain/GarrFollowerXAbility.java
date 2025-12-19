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


@Table(name = "garr_follower_x_ability")
@Db2DataBind(name = "GarrFollowerXAbility.db2", layoutHash = 0x996447F1, parentIndexField = 2, fields = {
        @Db2Field(name = "garrAbilityID", type = Db2Type.SHORT),
        @Db2Field(name = "factionIndex", type = Db2Type.BYTE),
        @Db2Field(name = "garrFollowerID", type = Db2Type.SHORT)
})
public class GarrFollowerXAbility implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("GarrAbilityID")
    private short garrAbilityID;

    @Column("FactionIndex")
    private byte factionIndex;

    @Column("GarrFollowerID")
    private short garrFollowerID;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
