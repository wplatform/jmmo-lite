package com.github.azeroth.game.pvp;


public class go_type {
    public int entry;
    public int map;
    public Position pos;
    public Quaternion rot;

    public go_type(int entry, int map, float _x, float _y, float _z, float _o, float _rot0, float _rot1, float _rot2, float _rot3) {
        entry = entry;
        map = map;
        pos = new Position(_x, _y, _z, _o);
        rot = new Quaternion(_rot0, _rot1, _rot2, _rot3);
    }
}
