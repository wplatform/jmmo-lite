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


@Table(name = "area_group_member")
@Db2DataBind(name = "AreaGroupMember.db2", layoutHash = 0x50AA43EE, parentIndexField = 1, fields = {
        @Db2Field(name = "areaID", type = Db2Type.SHORT),
        @Db2Field(name = "areaGroupID", type = Db2Type.SHORT)
})
public class AreaGroupMember implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("AreaID")
    private short areaID;

    @Column("AreaGroupID")
    private short areaGroupID;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
