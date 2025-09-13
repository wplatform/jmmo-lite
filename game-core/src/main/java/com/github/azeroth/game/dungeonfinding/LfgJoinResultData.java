package com.github.azeroth.game.dungeonfinding;


import java.util.ArrayList;
import java.util.HashMap;

public class LfgJoinResultData {
    public LfgJoinResult result = LfgJoinResult.values()[0];
    public LfgRoleCheckState state = LfgRoleCheckState.values()[0];
    public HashMap<ObjectGuid, HashMap<Integer, LfgLockInfoData>> lockmap = new HashMap<ObjectGuid, HashMap<Integer, LfgLockInfoData>>();
    public ArrayList<String> playersMissingRequirement = new ArrayList<>();


    public LfgJoinResultData(LfgJoinResult result) {
        this(result, LfgRoleCheckState.Default);
    }

    public LfgJoinResultData() {
        this(LfgJoinResult.Ok, LfgRoleCheckState.Default);
    }

    public LfgJoinResultData(LfgJoinResult result, LfgRoleCheckState state) {
        result = result;
        state = state;
    }
}
