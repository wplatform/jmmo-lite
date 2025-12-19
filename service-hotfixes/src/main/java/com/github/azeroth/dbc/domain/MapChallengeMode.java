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
@ToString(onlyExplicitlyIncluded = true)


@Table(name = "map_challenge_mode")
@Db2DataBind(name = "MapChallengeMode.db2", layoutHash = 0xC5261662, indexField = 1, parentIndexField = 2, fields = {
        @Db2Field(name = "name", type = Db2Type.STRING),
        @Db2Field(name = "id", type = Db2Type.INT, signed = true),
        @Db2Field(name = "mapID", type = Db2Type.SHORT),
        @Db2Field(name = {"criteriaCount1", "criteriaCount2", "criteriaCount3"}, type = Db2Type.SHORT),
        @Db2Field(name = "flags", type = Db2Type.BYTE)
})
public class MapChallengeMode implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

    
    @Column("Name")
    private LocalizedString name;


    @Column("MapID")
    private int mapID;


    @Column("Flags")
    private short flags;


    @Column("CriteriaCount1")
    private short criteriaCount1;


    @Column("CriteriaCount2")
    private short criteriaCount2;


    @Column("CriteriaCount3")
    private short criteriaCount3;

}