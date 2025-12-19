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


@Table(name = "rand_prop_points")
@Db2DataBind(name = "RandPropPoints.db2", layoutHash = 0x4E2C0BCC, fields = {
        @Db2Field(name = {"epic1", "epic2", "epic3", "epic4", "epic5"}, type = Db2Type.INT),
        @Db2Field(name = {"superior1", "superior2", "superior3", "superior4", "superior5"}, type = Db2Type.INT),
        @Db2Field(name = {"good1", "good2", "good3", "good4", "good5"}, type = Db2Type.INT)
})
public class RandPropPoint implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("Epic1")
    private int epic1;

    @Column("Epic2")
    private int epic2;

    @Column("Epic3")
    private int epic3;

    @Column("Epic4")
    private int epic4;

    @Column("Epic5")
    private int epic5;

    @Column("Superior1")
    private int superior1;

    @Column("Superior2")
    private int superior2;

    @Column("Superior3")
    private int superior3;

    @Column("Superior4")
    private int superior4;

    @Column("Superior5")
    private int superior5;

    @Column("Good1")
    private int good1;

    @Column("Good2")
    private int good2;

    @Column("Good3")
    private int good3;

    @Column("Good4")
    private int good4;

    @Column("Good5")
    private int good5;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
