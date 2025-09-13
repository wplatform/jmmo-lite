package com.github.azeroth.game;


import java.util.ArrayList;

public class PhaseInfoStruct {
    public int id;
    public ArrayList<Integer> areas = new ArrayList<>();

    public PhaseInfoStruct(int id) {
        this.id = id;
    }

    public final boolean isAllowedInArea(int areaId) {
        return areas.Any(areaToCheck -> global.getDB2Mgr().IsInArea(areaId, areaToCheck));
    }
}
