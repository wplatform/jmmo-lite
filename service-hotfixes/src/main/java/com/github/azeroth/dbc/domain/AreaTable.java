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
    private Integer flags1;

    @Column("Flags2")
    private Integer flags2;

    @Column("AmbientMultiplier")
    private Float ambientMultiplier;

    @Column("ContinentID")
    private Short continentID;

    @Column("ParentAreaID")
    private Short parentAreaID;

    @Column("AreaBit")
    private Short areaBit;

    @Column("AmbienceID")
    private Short ambienceID;

    @Column("ZoneMusic")
    private Short zoneMusic;

    @Column("IntroSound")
    private Short introSound;

    @Column("LiquidTypeID1")
    private Short liquidTypeID1;

    @Column("LiquidTypeID2")
    private Short liquidTypeID2;

    @Column("LiquidTypeID3")
    private Short liquidTypeID3;

    @Column("LiquidTypeID4")
    private Short liquidTypeID4;

    @Column("UwZoneMusic")
    private Short uwZoneMusic;

    @Column("UwAmbience")
    private Short uwAmbience;

    @Column("PvpCombatWorldStateID")
    private Short pvpCombatWorldStateID;

    @Column("SoundProviderPref")
    private Byte soundProviderPref;

    @Column("SoundProviderPrefUnderwater")
    private Byte soundProviderPrefUnderwater;

    @Column("ExplorationLevel")
    private Byte explorationLevel;

    @Column("FactionGroupMask")
    private Byte factionGroupMask;

    @Column("MountFlags")
    private Byte mountFlags;

    @Column("WildBattlePetLevelMin")
    private Byte wildBattlePetLevelMin;

    @Column("WildBattlePetLevelMax")
    private Byte wildBattlePetLevelMax;

    @Column("WindSettingsID")
    private Byte windSettingsID;

    @Column("UwIntroSound")
    private Integer uwIntroSound;

    @Id

    @Column("VerifiedBuild")
    private Integer verifiedBuild;


    public Short getLiquidTypeID(int index) {
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
