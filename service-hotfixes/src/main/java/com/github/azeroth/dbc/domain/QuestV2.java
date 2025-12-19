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


@Table(name = "quest_v2")
@Db2DataBind(name = "QuestV2.db2", layoutHash = 0x70495C9B, fields = {
        @Db2Field(name = "uniqueBitFlag", type = Db2Type.SHORT)
})
public class QuestV2 implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("UniqueBitFlag")
    private short uniqueBitFlag;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
