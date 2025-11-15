package com.github.azeroth.dbc.defines;

public enum FactionMask {
    PLAYER(1),                              // any player
    ALLIANCE(2),                            // player or creature from alliance team
    HORDE(4),                               // player or creature from horde team
    MONSTER(8);                             // aggressive creature from monster team

    // if none flags set then non-aggressive creature
    FactionMask(int i) {
        this.value = (byte) i;
    }

    public final byte value;
}
