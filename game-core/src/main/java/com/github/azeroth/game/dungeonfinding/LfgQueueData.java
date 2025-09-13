package com.github.azeroth.game.dungeonfinding;


import java.util.ArrayList;
import java.util.HashMap;

// Stores player or group queue info
public class LfgQueueData {
    public long joinTime;
    public byte tanks;
    public byte healers;
    public byte dps;
    public ArrayList<Integer> dungeons;
    public HashMap<ObjectGuid, LfgRoles> roles;
    public String bestCompatible = "";

    public LfgQueueData() {
        joinTime = gameTime.GetGameTime();
        tanks = SharedConst.LFGTanksNeeded;
        healers = SharedConst.LFGHealersNeeded;
        dps = SharedConst.LFGDPSNeeded;
    }

    public LfgQueueData(long _joinTime, ArrayList<Integer> _dungeons, HashMap<ObjectGuid, LfgRoles> _roles) {
        joinTime = _joinTime;
        tanks = SharedConst.LFGTanksNeeded;
        healers = SharedConst.LFGHealersNeeded;
        dps = SharedConst.LFGDPSNeeded;
        dungeons = _dungeons;
        roles = _roles;
    }
}
