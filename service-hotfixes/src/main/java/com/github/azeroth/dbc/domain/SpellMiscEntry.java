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


@Table(name = "spell_misc")
@Db2DataBind(name = "SpellMisc.db2", layoutHash = 0xCDC114D5, parentIndexField = 10, fields = {
        @Db2Field(name = "castingTimeIndex", type = Db2Type.SHORT),
        @Db2Field(name = "durationIndex", type = Db2Type.SHORT),
        @Db2Field(name = "rangeIndex", type = Db2Type.SHORT),
        @Db2Field(name = "schoolMask", type = Db2Type.BYTE),
        @Db2Field(name = "spellIconFileDataID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "speed", type = Db2Type.FLOAT),
        @Db2Field(name = "activeIconFileDataID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "launchDelay", type = Db2Type.FLOAT),
        @Db2Field(name = "difficultyID", type = Db2Type.BYTE),
        @Db2Field(name = {"attributes1", "attributes2", "attributes3", "attributes4", "attributes5", "attributes6", "attributes7", "attributes8", "attributes9", "attributes10", "attributes11", "attributes12", "attributes13", "attributes14"}, type = Db2Type.INT, signed = true),
        @Db2Field(name = "spellID", type = Db2Type.INT, signed = true)
})
public class SpellMiscEntry implements DbcEntity {
    @Id
    
    @Column("ID")
    private int id;

    @Column("CastingTimeIndex")
    private short castingTimeIndex;

    @Column("DurationIndex")
    private short durationIndex;

    @Column("RangeIndex")
    private short rangeIndex;

    @Column("SchoolMask")
    private byte schoolMask;

    @Column("SpellIconFileDataID")
    private int spellIconFileDataID;

    @Column("Speed")
    private float speed;

    @Column("ActiveIconFileDataID")
    private int activeIconFileDataID;

    @Column("LaunchDelay")
    private float launchDelay;

    @Column("DifficultyID")
    private byte difficultyID;

    @Column("Attributes1")
    private int attributes1;

    @Column("Attributes2")
    private int attributes2;

    @Column("Attributes3")
    private int attributes3;

    @Column("Attributes4")
    private int attributes4;

    @Column("Attributes5")
    private int attributes5;

    @Column("Attributes6")
    private int attributes6;

    @Column("Attributes7")
    private int attributes7;

    @Column("Attributes8")
    private int attributes8;

    @Column("Attributes9")
    private int attributes9;

    @Column("Attributes10")
    private int attributes10;

    @Column("Attributes11")
    private int attributes11;

    @Column("Attributes12")
    private int attributes12;

    @Column("Attributes13")
    private int attributes13;

    @Column("Attributes14")
    private int attributes14;

    @Column("SpellID")
    private int spellID;

    @Id
    @Column("VerifiedBuild")
    private int verifiedBuild;
    
    public int getAttributes(int index) {
        return switch (index) {
            case 0 -> attributes1;
            case 1 -> attributes2;
            case 2 -> attributes3;
            case 3 -> attributes4;
            case 4 -> attributes5;
            case 5 -> attributes6;
            case 6 -> attributes7;
            case 7 -> attributes8;
            case 8 -> attributes9;
            case 9 -> attributes10;
            case 10 -> attributes11;
            case 11 -> attributes12;
            case 12 -> attributes13;
            case 13 -> attributes14;
            default -> throw new IllegalArgumentException("index(" + index + ") must be between 0 and 13");
        };
    }

}
