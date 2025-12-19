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


@Table(name = "criteria_tree")
@Db2DataBind(name = "CriteriaTree.db2", layoutHash = 0x0A1B99C2, fields = {
        @Db2Field(name = "description", type = Db2Type.STRING),
        @Db2Field(name = "amount", type = Db2Type.INT, signed = true),
        @Db2Field(name = "flags", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "operator", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "criteriaID", type = Db2Type.INT),
        @Db2Field(name = "parent", type = Db2Type.INT),
        @Db2Field(name = "orderIndex", type = Db2Type.INT, signed = true)
})
public class CriteriaTree implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("Description")
    private LocalizedString description;

    @Column("Amount")
    private int amount;

    @Column("Flags")
    private short flags;

    @Column("Operator")
    private byte operator;

    @Column("CriteriaID")
    private int criteriaID;

    @Column("Parent")
    private int parent;

    @Column("OrderIndex")
    private int orderIndex;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
