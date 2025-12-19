package com.github.azeroth.dbc.domain;

import com.github.azeroth.cache.DbcEntity;
import com.github.azeroth.common.EnumFlag;
import com.github.azeroth.common.LocalizedString;
import com.github.azeroth.dbc.db2.Db2Field;
import com.github.azeroth.dbc.db2.Db2DataBind;
import com.github.azeroth.dbc.db2.Db2Type;
import com.github.azeroth.dbc.defines.AreaFlag;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString


@Table(name = "area_table")
@Db2DataBind(name = "AreaTable.db2", layoutHash = 0x0CA01129, fields = {
        @Db2Field(name = "zoneName", type = Db2Type.STRING_NOT_LOCALIZED),
        @Db2Field(name = "areaName", type = Db2Type.STRING),
        @Db2Field(name = {"flags1", "flags2"}, type = Db2Type.INT, signed = true),
        @Db2Field(name = "ambientMultiplier", type = Db2Type.FLOAT),
        @Db2Field(name = "continentID", type = Db2Type.SHORT),
        @Db2Field(name = "parentAreaID", type = Db2Type.SHORT),
        @Db2Field(name = "areaBit", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "ambienceID", type = Db2Type.SHORT),
        @Db2Field(name = "zoneMusic", type = Db2Type.SHORT),
        @Db2Field(name = "introSound", type = Db2Type.SHORT),
        @Db2Field(name = {"liquidTypeID1", "liquidTypeID2", "liquidTypeID3", "liquidTypeID4"}, type = Db2Type.SHORT),
        @Db2Field(name = "uwZoneMusic", type = Db2Type.SHORT),
        @Db2Field(name = "uwAmbience", type = Db2Type.SHORT),
        @Db2Field(name = "pvpCombatWorldStateID", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "soundProviderPref", type = Db2Type.BYTE),
        @Db2Field(name = "soundProviderPrefUnderwater", type = Db2Type.BYTE),
        @Db2Field(name = "explorationLevel", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "factionGroupMask", type = Db2Type.BYTE),
        @Db2Field(name = "mountFlags", type = Db2Type.BYTE),
        @Db2Field(name = "wildBattlePetLevelMin", type = Db2Type.BYTE),
        @Db2Field(name = "wildBattlePetLevelMax", type = Db2Type.BYTE),
        @Db2Field(name = "windSettingsID", type = Db2Type.BYTE),
        @Db2Field(name = "uwIntroSound", type = Db2Type.INT)
})
public class AreaTable implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("ZoneName")
    private String zoneName;

    @Column("AreaName")
    private LocalizedString areaName;

    @Column("Flags1")
    private int flags1;

    @Column("Flags2")
    private int flags2;

    @Column("AmbientMultiplier")
    private float ambientMultiplier;

    @Column("ContinentID")
    private short continentID;

    @Column("ParentAreaID")
    private short parentAreaID;

    @Column("AreaBit")
    private short areaBit;

    @Column("AmbienceID")
    private short ambienceID;

    @Column("ZoneMusic")
    private short zoneMusic;

    @Column("IntroSound")
    private short introSound;

    @Column("LiquidTypeID1")
    private short liquidTypeID1;

    @Column("LiquidTypeID2")
    private short liquidTypeID2;

    @Column("LiquidTypeID3")
    private short liquidTypeID3;

    @Column("LiquidTypeID4")
    private short liquidTypeID4;

    @Column("UwZoneMusic")
    private short uwZoneMusic;

    @Column("UwAmbience")
    private short uwAmbience;

    @Column("PvpCombatWorldStateID")
    private short pvpCombatWorldStateID;

    @Column("SoundProviderPref")
    private byte soundProviderPref;

    @Column("SoundProviderPrefUnderwater")
    private byte soundProviderPrefUnderwater;

    @Column("ExplorationLevel")
    private byte explorationLevel;

    @Column("FactionGroupMask")
    private byte factionGroupMask;

    @Column("MountFlags")
    private byte mountFlags;

    @Column("WildBattlePetLevelMin")
    private byte wildBattlePetLevelMin;

    @Column("WildBattlePetLevelMax")
    private byte wildBattlePetLevelMax;

    @Column("WindSettingsID")
    private byte windSettingsID;

    @Column("UwIntroSound")
    private int uwIntroSound;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;


    public short getLiquidTypeID(int index) {
        Short[] shorts = {liquidTypeID1, liquidTypeID2, liquidTypeID3, liquidTypeID4};
        return shorts[index];
    }

    public EnumFlag<AreaFlag> getFlags() {
        return EnumFlag.of(AreaFlag.class, flags1);
    }


    private boolean isSanctuary()
    {
        return getFlags().hasFlag(AreaFlag.NoPvP);
    }

}
