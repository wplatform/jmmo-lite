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


@Table(name = "taxi_path")
@Db2DataBind(name = "TaxiPath.db2", layoutHash = 0xF44E2BF5, indexField = 2, parentIndexField = 0, fields = {
        @Db2Field(name = "fromTaxiNode", type = Db2Type.SHORT),
        @Db2Field(name = "toTaxiNode", type = Db2Type.SHORT),
        @Db2Field(name = "id", type = Db2Type.INT),
        @Db2Field(name = "cost", type = Db2Type.INT)
})
public class TaxiPath implements DbcEntity {
    @Column("FromTaxiNode")
    private short fromTaxiNode;

    @Column("ToTaxiNode")
    private short toTaxiNode;

    @Id

    @Column("ID")
    private int id;

    @Column("Cost")
    private int cost;

    @Id

    @Column("VerifiedBuild")
    private Integer verifiedBuild;

}
