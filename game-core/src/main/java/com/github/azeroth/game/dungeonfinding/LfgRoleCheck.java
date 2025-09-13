package com.github.azeroth.game.dungeonfinding;


import java.util.ArrayList;
import java.util.HashMap;

public class LfgRoleCheck {
    public long cancelTime;
    public HashMap<ObjectGuid, LfgRoles> roles = new HashMap<ObjectGuid, LfgRoles>();
    public LfgRoleCheckState state = LfgRoleCheckState.values()[0];

    public ArrayList<Integer> dungeons = new ArrayList<>();

    public int rDungeonId;
    public ObjectGuid leader = ObjectGuid.EMPTY;
}
