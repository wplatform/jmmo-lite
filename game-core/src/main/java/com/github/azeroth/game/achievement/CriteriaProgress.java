package com.github.azeroth.game.achievement;

import com.github.azeroth.game.domain.object.ObjectGuid;

public class CriteriaProgress {
    public long counter;
    public long date; // latest update time.
    public ObjectGuid playerGUID = ObjectGuid.EMPTY; // GUID of the player that completed this criteria (guild achievements)
    public boolean changed;
}
