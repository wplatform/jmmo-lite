package com.github.azeroth.game.map;

import com.github.azeroth.game.entity.gobject.GameObject;

class GameObjectInRangeCheck implements ICheck<GameObject> {
    private final float _x, _y, _z, range;
    private final int entry;


    public GameObjectInRangeCheck(float x, float y, float z, float range) {
        this(x, y, z, range, 0);
    }

    public GameObjectInRangeCheck(float x, float y, float z, float range, int entry) {
        _x = x;
        _y = y;
        _z = z;
        range = range;
        entry = entry;
    }

    public final boolean invoke(GameObject go) {
        if (entry == 0 || (go.getTemplate() != null && go.getTemplate().entry == entry)) {
            return go.isInRange(_x, _y, _z, range);
        } else {
            return false;
        }
    }
}
