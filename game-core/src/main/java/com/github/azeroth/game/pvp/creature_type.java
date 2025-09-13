package com.github.azeroth.game.pvp;


class creature_type {
    private final Position pos;

    public int entry;

    public int map;


    public creature_type(int entry, int map, float _x, float _y, float _z, float _o) {
        entry = entry;
        map = map;
        pos = new Position(_x, _y, _z, _o);
    }
}
