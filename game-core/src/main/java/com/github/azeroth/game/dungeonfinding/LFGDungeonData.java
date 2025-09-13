package com.github.azeroth.game.dungeonfinding;


public class LFGDungeonData {
    public int id;
    public String name;
    public int map;
    public LfgType type = LfgType.values()[0];
    public int expansion;
    public int group;
    public int contentTuningId;
    public Difficulty difficulty = Difficulty.values()[0];
    public boolean seasonal;
    public float x, y, z, o;
    public short requiredItemLevel;

    public LFGDungeonData(LFGDungeonsRecord dbc) {
        id = dbc.id;
        name = dbc.name.charAt(global.getWorldMgr().getDefaultDbcLocale());
        map = (int) dbc.mapID;
        type = dbc.typeID;
        expansion = dbc.expansionLevel;
        group = dbc.groupID;
        contentTuningId = dbc.contentTuningID;
        difficulty = dbc.difficultyID;
        seasonal = dbc.Flags[0].hasFlag(LfgFlags.Seasonal);
    }

    // Helpers
    public final int entry() {
        return (int) (id + (type.getValue() << 24));
    }
}
