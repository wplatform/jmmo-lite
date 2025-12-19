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


@Table(name = "garr_ability")
@Db2DataBind(name = "GarrAbility.db2", layoutHash = 0x5DF95DBD, indexField = 7, fields = {
        @Db2Field(name = "name", type = Db2Type.STRING),
        @Db2Field(name = "description", type = Db2Type.STRING),
        @Db2Field(name = "iconFileDataID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "flags", type = Db2Type.SHORT),
        @Db2Field(name = "factionChangeGarrAbilityID", type = Db2Type.SHORT),
        @Db2Field(name = "garrAbilityCategoryID", type = Db2Type.BYTE),
        @Db2Field(name = "garrFollowerTypeID", type = Db2Type.BYTE),
        @Db2Field(name = "id", type = Db2Type.INT)
})
public class GarrAbility implements DbcEntity {
    @Column("Name")
    private LocalizedString name;

    @Column("Description")
    private LocalizedString description;

    @Column("IconFileDataID")
    private int iconFileDataID;

    @Column("Flags")
    private short flags;

    @Column("FactionChangeGarrAbilityID")
    private short factionChangeGarrAbilityID;

    @Column("GarrAbilityCategoryID")
    private byte garrAbilityCategoryID;

    @Column("GarrFollowerTypeID")
    private byte garrFollowerTypeID;

    @Id
    
    @Column("ID")
    private int id;

    @Id
    
    @Column("VerifiedBuild")
    private int verifiedBuild;

}
