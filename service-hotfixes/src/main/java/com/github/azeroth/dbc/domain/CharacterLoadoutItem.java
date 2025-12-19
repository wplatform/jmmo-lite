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
@ToString(onlyExplicitlyIncluded = true)


@Table(name = "character_loadout_item")
@Db2DataBind(name = "CharacterLoadoutItem.db2", layoutHash = 0x24843CD8, parentIndexField = 0, fields = {
        @Db2Field(name = "itemID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "characterLoadoutID", type = Db2Type.SHORT)
})
public class CharacterLoadoutItem implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;


    @Column("CharacterLoadoutID")
    private int characterLoadoutID;


    @Column("ItemID")
    private Long itemID;

}