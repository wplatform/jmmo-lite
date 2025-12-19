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


@Table(name = "scenario")
@Db2DataBind(name = "Scenario.db2", layoutHash = 0xD052232A, fields = {
        @Db2Field(name = "name", type = Db2Type.STRING),
        @Db2Field(name = "areaTableID", type = Db2Type.SHORT),
        @Db2Field(name = "flags", type = Db2Type.BYTE),
        @Db2Field(name = "type", type = Db2Type.BYTE)
})
public class Scenario implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("Name")
    private LocalizedString name;

    @Column("AreaTableID")
    private short areaTableID;

    @Column("Flags")
    private byte flags;

    @Column("Type")
    private byte type;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
