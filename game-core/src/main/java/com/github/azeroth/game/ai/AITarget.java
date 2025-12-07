package game.ai;

import game.*;

// Copyright (c) Forged WoW LLC <https://github.com/ForgedWoW/ForgedCore>
// Licensed under GPL-3.0 license. See <https://github.com/ForgedWoW/ForgedCore/blob/master/LICENSE> for full information.



public enum AITarget {
    Self,
    Victim,
    Enemy,
    Ally,
    Buff,
    Debuff;

    public static final int SIZE = java.lang.Integer.SIZE;

    public int getValue() {
        return this.ordinal();
    }

    public static AITarget forValue(int value) {
        return values()[value];
    }
}