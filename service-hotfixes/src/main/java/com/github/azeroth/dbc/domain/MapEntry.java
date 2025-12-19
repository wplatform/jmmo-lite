package com.github.azeroth.dbc.domain;

import com.github.azeroth.cache.DbcEntity;
import com.github.azeroth.common.EnumFlag;
import com.github.azeroth.common.LocalizedString;
import com.github.azeroth.dbc.db2.Db2DataBind;
import com.github.azeroth.dbc.db2.Db2Field;
import com.github.azeroth.dbc.db2.Db2Type;
import com.github.azeroth.dbc.defines.MapFlag;
import com.github.azeroth.dbc.defines.MapFlag2;
import com.github.azeroth.dbc.defines.MapTypes;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString


@Table(name = "map")
@Db2DataBind(name = "Map.db2", layoutHash = 0xF568DF12, fields = {
        @Db2Field(name = "directory", type = Db2Type.STRING_NOT_LOCALIZED),
        @Db2Field(name = "mapName", type = Db2Type.STRING),
        @Db2Field(name = {"mapDescription0", "mapDescription1"}, type = Db2Type.STRING),
        @Db2Field(name = "pvpShortDescription", type = Db2Type.STRING),
        @Db2Field(name = "pvpLongDescription", type = Db2Type.STRING),
        @Db2Field(name = {"flags1", "flags2"}, type = Db2Type.INT, signed = true),
        @Db2Field(name = "minimapIconScale", type = Db2Type.FLOAT),
        @Db2Field(name = "corpseX", type = Db2Type.FLOAT),
        @Db2Field(name = "corpseY", type = Db2Type.FLOAT),
        @Db2Field(name = "areaTableID", type = Db2Type.SHORT),
        @Db2Field(name = "loadingScreenID", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "corpseMapID", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "timeOfDayOverride", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "parentMapID", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "cosmeticParentMapID", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "windSettingsID", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "instanceType", type = Db2Type.BYTE),
        @Db2Field(name = "mapType", type = Db2Type.BYTE),
        @Db2Field(name = "expansionID", type = Db2Type.BYTE),
        @Db2Field(name = "maxPlayers", type = Db2Type.BYTE),
        @Db2Field(name = "timeOffset", type = Db2Type.BYTE)
})
public class MapEntry implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("Directory")
    private String directory;

    @Column("MapName")
    private LocalizedString mapName;

    @Column("MapDescription0")
    private LocalizedString mapDescription0;

    @Column("MapDescription1")
    private LocalizedString mapDescription1;

    @Column("PvpShortDescription")
    private LocalizedString pvpShortDescription;

    @Column("PvpLongDescription")
    private LocalizedString pvpLongDescription;

    @Column("Flags1")
    private int flags1;

    @Column("Flags2")
    private int flags2;

    @Column("MinimapIconScale")
    private float minimapIconScale;

    @Column("CorpseX")
    private float corpseX;

    @Column("CorpseY")
    private float corpseY;

    @Column("AreaTableID")
    private short areaTableID;

    @Column("LoadingScreenID")
    private short loadingScreenID;

    @Column("CorpseMapID")
    private short corpseMapID;

    @Column("TimeOfDayOverride")
    private short timeOfDayOverride;

    @Column("ParentMapID")
    private short parentMapID;

    @Column("CosmeticParentMapID")
    private short cosmeticParentMapID;

    @Column("WindSettingsID")
    private short windSettingsID;

    @Column("instanceType")
    private byte instanceType;

    @Column("MapType")
    private byte mapType;

    @Column("ExpansionID")
    private byte expansionID;

    @Column("MaxPlayers")
    private byte maxPlayers;

    @Column("TimeOffset")
    private byte timeOffset;

    @Id
    @Column("VerifiedBuild")
    private int verifiedBuild;

    public byte expansion() { return expansionID; }

    public boolean isDungeon() {
        return (instanceType == MapTypes.MAP_INSTANCE.ordinal()
                || instanceType == MapTypes.MAP_RAID.ordinal()
                || instanceType == MapTypes.MAP_SCENARIO.ordinal()) && !isGarrison();
    }

    public boolean isNonRaidDungeon() {
        return instanceType == MapTypes.MAP_INSTANCE.ordinal();
    }

    public boolean isInstanceable() {
        return instanceType == MapTypes.MAP_INSTANCE.ordinal()
                || instanceType == MapTypes.MAP_RAID.ordinal()
                || instanceType == MapTypes.MAP_BATTLEGROUND.ordinal()
                || instanceType == MapTypes.MAP_ARENA.ordinal()
                || instanceType == MapTypes.MAP_SCENARIO.ordinal();
    }

    public boolean isRaid() {
        return instanceType == MapTypes.MAP_RAID.ordinal();
    }

    public boolean isBattleground() {
        return instanceType == MapTypes.MAP_BATTLEGROUND.ordinal();
    }

    public boolean isBattleArena() {
        return instanceType == MapTypes.MAP_ARENA.ordinal();
    }

    public boolean isBattlegroundOrArena() {
        return instanceType == MapTypes.MAP_BATTLEGROUND.ordinal() || instanceType == MapTypes.MAP_ARENA.ordinal();
    }

    public boolean isScenario() {
        return instanceType == MapTypes.MAP_SCENARIO.ordinal();
    }

    public boolean isWorldMap() {
        return instanceType == MapTypes.MAP_COMMON.ordinal();
    }

    public boolean getEntrancePos(int mapid, float x, float y) {
        if (corpseMapID < 0)
            return false;

        mapid = corpseMapID;
        x = corpseX;
        y = corpseY;
        return true;
    }

    public boolean isContinent() {
        return switch (id) {
            case 0, 1, 530, 571, 870, 1116, 1220, 1642, 1643, 2222, 2444 -> true;
            default -> false;
        };
    }

    public boolean isDynamicDifficultyMap() {
        return getFlags().hasFlag(MapFlag.DynamicDifficulty);
    }

    public boolean isFlexLocking() {
        return getFlags().hasFlag(MapFlag.FlexibleRaidLocking);
    }

    public boolean isGarrison() {
        return getFlags().hasFlag(MapFlag.Garrison);
    }

    public boolean isSplitByFaction() {
        return id == 609 || // Acherus (DeathKnight Start)
                id == 1265 ||   // Assault on the Dark Portal (WoD Intro)
                id == 1481 ||   // Mardum (DH Start)
                id == 2175 ||   // Exiles Reach - NPE
                id == 2570;     // Forbidden Reach (Dracthyr/Evoker Start)
    }

    public EnumFlag<MapFlag> getFlags() {
        return EnumFlag.of(MapFlag.class, flags1);
    }

    public EnumFlag<MapFlag2> getFlags2() {
        return EnumFlag.of(MapFlag2.class, flags2);
    }

}
