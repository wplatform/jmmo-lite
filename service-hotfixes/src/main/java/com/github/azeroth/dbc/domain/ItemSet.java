package com.github.azeroth.dbc.domain;

import com.github.azeroth.cache.DbcEntity;
import com.github.azeroth.common.LocalizedString;
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


@Table(name = "item_set")
@Db2DataBind(name = "ItemSet.db2", layoutHash = 0x847FF58A, fields = {
        @Db2Field(name = "name", type = Db2Type.STRING),
        @Db2Field(name = {"itemID1", "itemID2", "itemID3", "itemID4", "itemID5", "itemID6", "itemID7", "itemID8", "itemID9", "itemID10", "itemID11", "itemID12", "itemID13", "itemID14", "itemID15", "itemID16", "itemID17"}, type = Db2Type.INT),
        @Db2Field(name = "requiredSkillRank", type = Db2Type.SHORT),
        @Db2Field(name = "requiredSkill", type = Db2Type.INT),
        @Db2Field(name = "setFlags", type = Db2Type.INT)
})
public class ItemSet implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("Name")
    private LocalizedString name;

    @Column("ItemID1")
    private int itemID1;

    @Column("ItemID2")
    private int itemID2;

    @Column("ItemID3")
    private int itemID3;

    @Column("ItemID4")
    private int itemID4;

    @Column("ItemID5")
    private int itemID5;

    @Column("ItemID6")
    private int itemID6;

    @Column("ItemID7")
    private int itemID7;

    @Column("ItemID8")
    private int itemID8;

    @Column("ItemID9")
    private int itemID9;

    @Column("ItemID10")
    private int itemID10;

    @Column("ItemID11")
    private int itemID11;

    @Column("ItemID12")
    private int itemID12;

    @Column("ItemID13")
    private int itemID13;

    @Column("ItemID14")
    private int itemID14;

    @Column("ItemID15")
    private int itemID15;

    @Column("ItemID16")
    private int itemID16;

    @Column("ItemID17")
    private int itemID17;

    @Column("RequiredSkillRank")
    private short requiredSkillRank;

    @Column("RequiredSkill")
    private int requiredSkill;

    @Column("SetFlags")
    private int setFlags;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
