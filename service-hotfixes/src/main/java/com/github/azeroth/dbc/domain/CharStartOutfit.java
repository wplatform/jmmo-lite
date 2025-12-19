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


@Table(name = "char_start_outfit")
@Db2DataBind(name = "CharStartOutfit.db2", layoutHash = 0x0EEBEE24, parentIndexField = 6, fields = {
        @Db2Field(name = {"itemID1", "itemID2", "itemID3", "itemID4", "itemID5", "itemID6", "itemID7", "itemID8", "itemID9", "itemID10", "itemID11", "itemID12", "itemID13", "itemID14", "itemID15", "itemID16", "itemID17", "itemID18", "itemID19", "itemID20", "itemID21", "itemID22", "itemID23", "itemID24"}, type = Db2Type.INT, signed = true),
        @Db2Field(name = "petDisplayID", type = Db2Type.INT),
        @Db2Field(name = "classID", type = Db2Type.BYTE),
        @Db2Field(name = "sexID", type = Db2Type.BYTE),
        @Db2Field(name = "outfitID", type = Db2Type.BYTE),
        @Db2Field(name = "petFamilyID", type = Db2Type.BYTE),
        @Db2Field(name = "raceID", type = Db2Type.BYTE)
})
public class CharStartOutfit implements DbcEntity {
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

    @Column("ItemID18")
    private int itemID18;

    @Column("ItemID19")
    private int itemID19;

    @Column("ItemID20")
    private int itemID20;

    @Column("ItemID21")
    private int itemID21;

    @Column("ItemID22")
    private int itemID22;

    @Column("ItemID23")
    private int itemID23;

    @Column("ItemID24")
    private int itemID24;

    @Column("PetDisplayID")
    private int petDisplayID;

    @Column("ClassID")
    private byte classID;

    @Column("SexID")
    private byte sexID;

    @Column("OutfitID")
    private byte outfitID;

    @Column("PetFamilyID")
    private short petFamilyID;

    @Column("RaceID")
    private byte raceID;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

    public int[] getItems() {
        return new int[]{
                itemID1, itemID2, itemID3, itemID4, itemID5, itemID6, itemID7, itemID8, itemID9, itemID10,
                itemID11, itemID12, itemID13, itemID14, itemID15, itemID16, itemID17, itemID18, itemID19, itemID20,
                itemID21, itemID22, itemID23, itemID24
        };
    }

}
