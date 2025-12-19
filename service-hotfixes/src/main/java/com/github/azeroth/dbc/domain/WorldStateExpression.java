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


@Table(name = "world_state_expression")
@Db2DataBind(name = "WorldStateExpression.db2", layoutHash = 0xA69C9812, fields = {
        @Db2Field(name = "expression", type = Db2Type.STRING_NOT_LOCALIZED)
})
public class WorldStateExpression implements DbcEntity {
    @Id
    
    @Column("ID")
    private int id;

    @Column("Expression")
    private String expression;

    @Id
    
    @Column("VerifiedBuild")
    private int verifiedBuild;

}
