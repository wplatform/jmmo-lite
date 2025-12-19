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


@Table(name = "item_extended_cost")
@Db2DataBind(name = "ItemExtendedCost.db2", layoutHash = 0xC31F4DEF, fields = {
        @Db2Field(name = {"itemID1", "itemID2", "itemID3", "itemID4", "itemID5"}, type = Db2Type.INT, signed = true),
        @Db2Field(name = {"currencyCount1", "currencyCount2", "currencyCount3", "currencyCount4", "currencyCount5"}, type = Db2Type.INT),
        @Db2Field(name = {"itemCount1", "itemCount2", "itemCount3", "itemCount4", "itemCount5"}, type = Db2Type.SHORT),
        @Db2Field(name = "requiredArenaRating", type = Db2Type.SHORT),
        @Db2Field(name = {"currencyID1", "currencyID2", "currencyID3", "currencyID4", "currencyID5"}, type = Db2Type.SHORT),
        @Db2Field(name = "arenaBracket", type = Db2Type.BYTE),
        @Db2Field(name = "minFactionID", type = Db2Type.BYTE),
        @Db2Field(name = "minReputation", type = Db2Type.BYTE),
        @Db2Field(name = "flags", type = Db2Type.BYTE),
        @Db2Field(name = "requiredAchievement", type = Db2Type.BYTE)
})
public class ItemExtendedCost implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

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

    @Column("CurrencyCount1")
    private int currencyCount1;

    @Column("CurrencyCount2")
    private int currencyCount2;

    @Column("CurrencyCount3")
    private int currencyCount3;

    @Column("CurrencyCount4")
    private int currencyCount4;

    @Column("CurrencyCount5")
    private int currencyCount5;

    @Column("ItemCount1")
    private short itemCount1;

    @Column("ItemCount2")
    private short itemCount2;

    @Column("ItemCount3")
    private short itemCount3;

    @Column("ItemCount4")
    private short itemCount4;

    @Column("ItemCount5")
    private short itemCount5;

    @Column("RequiredArenaRating")
    private short requiredArenaRating;

    @Column("CurrencyID1")
    private short currencyID1;

    @Column("CurrencyID2")
    private short currencyID2;

    @Column("CurrencyID3")
    private short currencyID3;

    @Column("CurrencyID4")
    private short currencyID4;

    @Column("CurrencyID5")
    private short currencyID5;

    @Column("ArenaBracket")
    private byte arenaBracket;

    @Column("MinFactionID")
    private byte minFactionID;

    @Column("MinReputation")
    private byte minReputation;

    @Column("Flags")
    private byte flags;

    @Column("RequiredAchievement")
    private byte requiredAchievement;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
