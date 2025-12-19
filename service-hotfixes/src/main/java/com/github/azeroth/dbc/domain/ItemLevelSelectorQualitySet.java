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


@Table(name = "item_level_selector_quality_set")
@Db2DataBind(name = "ItemLevelSelectorQualitySet.db2", layoutHash = 0x20055BA8, fields = {
        @Db2Field(name = "ilvlRare", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "ilvlEpic", type = Db2Type.SHORT, signed = true)
})
public class ItemLevelSelectorQualitySet implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("IlvlRare")
    private short ilvlRare;

    @Column("IlvlEpic")
    private short ilvlEpic;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
