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


@Table(name = "ruleset_item_upgrade")
@Db2DataBind(name = "RulesetItemUpgrade.db2", layoutHash = 0xFB641AE0, fields = {
        @Db2Field(name = "itemID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "itemUpgradeID", type = Db2Type.SHORT)
})
public class RuleSetItemUpgrade implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("ItemID")
    private int itemID;

    @Column("ItemUpgradeID")
    private short itemUpgradeID;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
