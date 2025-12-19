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


@Table(name = "item_level_selector")
@Db2DataBind(name = "ItemLevelSelector.db2", layoutHash = 0x8143060E, fields = {
        @Db2Field(name = "minItemLevel", type = Db2Type.SHORT),
        @Db2Field(name = "itemLevelSelectorQualitySetID", type = Db2Type.SHORT)
})
public class ItemLevelSelector implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("MinItemLevel")
    private short minItemLevel;

    @Column("ItemLevelSelectorQualitySetID")
    private short itemLevelSelectorQualitySetID;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
