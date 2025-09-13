package com.github.azeroth.game.dungeonfinding;


import java.util.ArrayList;

public class LfgUpdateData {
    public LfgUpdateType updateType = LfgUpdateType.values()[0];
    public LfgState state = LfgState.values()[0];
    public ArrayList<Integer> dungeons = new ArrayList<>();


    public LfgUpdateData() {
        this(LfgUpdateType.Default);
    }

    public LfgUpdateData(LfgUpdateType type) {
        updateType = type;
        state = LfgState.NONE;
    }

    public LfgUpdateData(LfgUpdateType type, ArrayList<Integer> _dungeons) {
        updateType = type;
        state = LfgState.NONE;
        dungeons = _dungeons;
    }

    public LfgUpdateData(LfgUpdateType type, LfgState state, ArrayList<Integer> _dungeons) {
        updateType = type;
        state = state;
        dungeons = _dungeons;
    }
}
