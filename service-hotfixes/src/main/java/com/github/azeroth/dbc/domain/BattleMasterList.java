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


@Table(name = "battlemaster_list")
@Db2DataBind(name = "BattlemasterList.db2", layoutHash = 0xD8AAA088, fields = {
        @Db2Field(name = "name", type = Db2Type.STRING),
        @Db2Field(name = "gameType", type = Db2Type.STRING),
        @Db2Field(name = "shortDescription", type = Db2Type.STRING),
        @Db2Field(name = "longDescription", type = Db2Type.STRING),
        @Db2Field(name = "iconFileDataID", type = Db2Type.INT, signed = true),
        @Db2Field(name = {"mapID1", "mapID2", "mapID3", "mapID4", "mapID5", "mapID6", "mapID7", "mapID8", "mapID9", "mapID10", "mapID11", "mapID12", "mapID13", "mapID14", "mapID15", "mapID16"}, type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "holidayWorldState", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "requiredPlayerConditionID", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "instanceType", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "groupsAllowed", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "maxGroupSize", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "minLevel", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "maxLevel", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "ratedPlayers", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "minPlayers", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "maxPlayers", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "flags", type = Db2Type.BYTE, signed = true)
})
public class BattleMasterList implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("Name")
    private LocalizedString name;

    @Column("GameType")
    private LocalizedString gameType;

    @Column("ShortDescription")
    private LocalizedString shortDescription;

    @Column("LongDescription")
    private LocalizedString longDescription;

    @Column("IconFileDataID")
    private int iconFileDataID;

    @Column("MapID1")
    private short mapID1;

    @Column("MapID2")
    private short mapID2;

    @Column("MapID3")
    private short mapID3;

    @Column("MapID4")
    private short mapID4;

    @Column("MapID5")
    private short mapID5;

    @Column("MapID6")
    private short mapID6;

    @Column("MapID7")
    private short mapID7;

    @Column("MapID8")
    private short mapID8;

    @Column("MapID9")
    private short mapID9;

    @Column("MapID10")
    private short mapID10;

    @Column("MapID11")
    private short mapID11;

    @Column("MapID12")
    private short mapID12;

    @Column("MapID13")
    private short mapID13;

    @Column("MapID14")
    private short mapID14;

    @Column("MapID15")
    private short mapID15;

    @Column("MapID16")
    private short mapID16;

    @Column("HolidayWorldState")
    private short holidayWorldState;

    @Column("RequiredPlayerConditionID")
    private short requiredPlayerConditionID;

    @Column("InstanceType")
    private byte instanceType;

    @Column("GroupsAllowed")
    private byte groupsAllowed;

    @Column("MaxGroupSize")
    private byte maxGroupSize;

    @Column("MinLevel")
    private byte minLevel;

    @Column("MaxLevel")
    private byte maxLevel;

    @Column("RatedPlayers")
    private byte ratedPlayers;

    @Column("MinPlayers")
    private byte minPlayers;

    @Column("MaxPlayers")
    private byte maxPlayers;

    @Column("Flags")
    private byte flags;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
