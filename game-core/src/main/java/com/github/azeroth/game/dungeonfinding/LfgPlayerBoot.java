package com.github.azeroth.game.dungeonfinding;


import java.util.HashMap;

public class LfgPlayerBoot {
    public long cancelTime;
    public boolean inProgress;
    public HashMap<ObjectGuid, LfgAnswer> votes = new HashMap<ObjectGuid, LfgAnswer>();
    public ObjectGuid victim = ObjectGuid.EMPTY;
    public String reason;
}
