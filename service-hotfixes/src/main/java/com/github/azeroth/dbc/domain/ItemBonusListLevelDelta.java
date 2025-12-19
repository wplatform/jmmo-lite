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


@Table(name = "item_bonus_list_level_delta")
@Db2DataBind(name = "ItemBonusListLevelDelta.db2", layoutHash = 0xDFBF5AC9, indexField = 1, fields = {
        @Db2Field(name = "itemLevelDelta", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "id", type = Db2Type.INT)
})
public class ItemBonusListLevelDelta implements DbcEntity {
    @Column("ItemLevelDelta")
    private short itemLevelDelta;

    @Id

    @Column("ID")
    private int id;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
