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


@Table(name = "bank_bag_slot_prices")
@Db2DataBind(name = "BankBagSlotPrices.db2", layoutHash = 0xEA0AC2AA, fields = {
        @Db2Field(name = "cost", type = Db2Type.INT)
})
public class BankBagSlotPrice implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("Cost")
    private int cost;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
