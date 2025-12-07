package game.ai;

import game.*;

// Copyright (c) Forged WoW LLC <https://github.com/ForgedWoW/ForgedCore>
// Licensed under GPL-3.0 license. See <https://github.com/ForgedWoW/ForgedCore/blob/master/LICENSE> for full information.



public enum FollowState {
    None(0x00),
    Inprogress(0x01), //must always have this state for any follow
    Paused(0x02), //disables following
    Complete(0x04), //follow is completed and may end
    PreEvent(0x08), //not implemented (allow pre event to run, before follow is initiated)
    PostEvent(0x10); //can be set at complete and allow post event to run

    public static final int SIZE = java.lang.Integer.SIZE;

    private int intValue;
    private static java.util.HashMap<Integer, FollowState> mappings;
    private static java.util.HashMap<Integer, FollowState> getMappings() {
        if (mappings == null) {
            synchronized (FollowState.class) {
                if (mappings == null) {
                    mappings = new java.util.HashMap<Integer, FollowState>();
                }
            }
        }
        return mappings;
    }

    private FollowState(int value) {
        intValue = value;
        getMappings().put(value, this);
    }

    public int getValue() {
        return intValue;
    }

    public static FollowState forValue(int value) {
        return getMappings().get(value);
    }
}