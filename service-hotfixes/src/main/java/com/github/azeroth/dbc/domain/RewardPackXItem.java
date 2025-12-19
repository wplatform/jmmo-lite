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


@Table(name = "reward_pack_x_item")
@Db2DataBind(name = "RewardPackXItem.db2", layoutHash = 0x74F6B9BD, parentIndexField = 2, fields = {
        @Db2Field(name = "itemID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "itemQuantity", type = Db2Type.INT, signed = true),
        @Db2Field(name = "rewardPackID", type = Db2Type.INT, signed = true)
})
public class RewardPackXItem implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("ItemID")
    private int itemID;

    @Column("ItemQuantity")
    private int itemQuantity;

    @Column("RewardPackID")
    private int rewardPackID;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
