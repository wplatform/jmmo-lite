package game.ai;

import game.*;

// Copyright (c) Forged WoW LLC <https://github.com/ForgedWoW/ForgedCore>
// Licensed under GPL-3.0 license. See <https://github.com/ForgedWoW/ForgedCore/blob/master/LICENSE> for full information.



public enum EscortState {
    None(0x00), //nothing in progress
    Escorting(0x01), //escort are in progress
    Returning(0x02), //escort is returning after being in combat
    Paused(0x04); //will not proceed with waypoints before state is removed

    public static final int SIZE = java.lang.Integer.SIZE;

    private int intValue;
    private static java.util.HashMap<Integer, EscortState> mappings;
    private static java.util.HashMap<Integer, EscortState> getMappings() {
        if (mappings == null) {
            synchronized (EscortState.class) {
                if (mappings == null) {
                    mappings = new java.util.HashMap<Integer, EscortState>();
                }
            }
        }
        return mappings;
    }

    private EscortState(int value) {
        intValue = value;
        getMappings().put(value, this);
    }

    public int getValue() {
        return intValue;
    }

    public static EscortState forValue(int value) {
        return getMappings().get(value);
    }
}