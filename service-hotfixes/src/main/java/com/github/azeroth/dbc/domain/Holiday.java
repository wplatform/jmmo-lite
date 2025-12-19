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


@Table(name = "holidays")
@Db2DataBind(name = "Holidays.db2", layoutHash = 0x7C3E60FC, indexField = 0, fields = {
        @Db2Field(name = "id", type = Db2Type.INT),
        @Db2Field(name = {"date1", "date2", "date3", "date4", "date5", "date6", "date7", "date8", "date9", "date10", "date11", "date12", "date13", "date14", "date15", "date16"}, type = Db2Type.INT),
        @Db2Field(name = {"duration1", "duration2", "duration3", "duration4", "duration5", "duration6", "duration7", "duration8", "duration9", "duration10"}, type = Db2Type.SHORT),
        @Db2Field(name = "region", type = Db2Type.SHORT),
        @Db2Field(name = "looping", type = Db2Type.BYTE),
        @Db2Field(name = {"calendarFlags1", "calendarFlags2", "calendarFlags3", "calendarFlags4", "calendarFlags5", "calendarFlags6", "calendarFlags7", "calendarFlags8", "calendarFlags9", "calendarFlags10"}, type = Db2Type.BYTE),
        @Db2Field(name = "priority", type = Db2Type.BYTE),
        @Db2Field(name = "calendarFilterType", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "flags", type = Db2Type.BYTE),
        @Db2Field(name = "holidayNameID", type = Db2Type.INT),
        @Db2Field(name = "holidayDescriptionID", type = Db2Type.INT),
        @Db2Field(name = {"textureFileDataID1", "textureFileDataID2", "textureFileDataID3"}, type = Db2Type.INT, signed = true)
})
public class Holiday implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("Date1")
    private int date1;

    @Column("Date2")
    private int date2;

    @Column("Date3")
    private int date3;

    @Column("Date4")
    private int date4;

    @Column("Date5")
    private int date5;

    @Column("Date6")
    private int date6;

    @Column("Date7")
    private int date7;

    @Column("Date8")
    private int date8;

    @Column("Date9")
    private int date9;

    @Column("Date10")
    private int date10;

    @Column("Date11")
    private int date11;

    @Column("Date12")
    private int date12;

    @Column("Date13")
    private int date13;

    @Column("Date14")
    private int date14;

    @Column("Date15")
    private int date15;

    @Column("Date16")
    private int date16;

    @Column("Duration1")
    private short duration1;

    @Column("Duration2")
    private short duration2;

    @Column("Duration3")
    private short duration3;

    @Column("Duration4")
    private short duration4;

    @Column("Duration5")
    private short duration5;

    @Column("Duration6")
    private short duration6;

    @Column("Duration7")
    private short duration7;

    @Column("Duration8")
    private short duration8;

    @Column("Duration9")
    private short duration9;

    @Column("Duration10")
    private short duration10;

    @Column("Region")
    private short region;

    @Column("Looping")
    private byte looping;

    @Column("CalendarFlags1")
    private byte calendarFlags1;

    @Column("CalendarFlags2")
    private byte calendarFlags2;

    @Column("CalendarFlags3")
    private byte calendarFlags3;

    @Column("CalendarFlags4")
    private byte calendarFlags4;

    @Column("CalendarFlags5")
    private byte calendarFlags5;

    @Column("CalendarFlags6")
    private byte calendarFlags6;

    @Column("CalendarFlags7")
    private byte calendarFlags7;

    @Column("CalendarFlags8")
    private byte calendarFlags8;

    @Column("CalendarFlags9")
    private byte calendarFlags9;

    @Column("CalendarFlags10")
    private byte calendarFlags10;

    @Column("Priority")
    private byte priority;

    @Column("CalendarFilterType")
    private byte calendarFilterType;

    @Column("Flags")
    private byte flags;

    @Column("HolidayNameID")
    private int holidayNameID;

    @Column("HolidayDescriptionID")
    private int holidayDescriptionID;

    @Column("TextureFileDataID1")
    private int textureFileDataID1;

    @Column("TextureFileDataID2")
    private int textureFileDataID2;

    @Column("TextureFileDataID3")
    private int textureFileDataID3;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
