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


@Table(name = "item_limit_category_condition")
@Db2DataBind(name = "ItemLimitCategoryCondition.db2", layoutHash = 0xDE8EAD49, parentIndexField = 2, fields = {
        @Db2Field(name = "addQuantity", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "playerConditionID", type = Db2Type.INT),
        @Db2Field(name = "parentItemLimitCategoryID", type = Db2Type.INT, signed = true)
})
public class ItemLimitCategoryCondition implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("AddQuantity")
    private byte addQuantity;

    @Column("PlayerConditionID")
    private int playerConditionID;

    @Column("ParentItemLimitCategoryID")
    private int parentItemLimitCategoryID;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
