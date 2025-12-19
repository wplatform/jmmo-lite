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


@Table(name = "item_bonus_tree_node")
@Db2DataBind(name = "ItemBonusTreeNode.db2", layoutHash = 0x84FE93B7, parentIndexField = 4, fields = {
        @Db2Field(name = "childItemBonusTreeID", type = Db2Type.SHORT),
        @Db2Field(name = "childItemBonusListID", type = Db2Type.SHORT),
        @Db2Field(name = "childItemLevelSelectorID", type = Db2Type.SHORT),
        @Db2Field(name = "itemContext", type = Db2Type.BYTE),
        @Db2Field(name = "parentItemBonusTreeID", type = Db2Type.SHORT)
})
public class ItemBonusTreeNode implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("ChildItemBonusTreeID")
    private short childItemBonusTreeID;

    @Column("ChildItemBonusListID")
    private short childItemBonusListID;

    @Column("ChildItemLevelSelectorID")
    private short childItemLevelSelectorID;

    @Column("ItemContext")
    private byte itemContext;

    @Column("ParentItemBonusTreeID")
    private short parentItemBonusTreeID;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
