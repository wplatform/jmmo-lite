package com.github.azeroth.game.entity.unit;

import com.github.azeroth.game.entity.object.WorldObject;

import java.util.Comparator;


public class ObjectDistanceOrderPred implements Comparator<WorldObject> {
    private final WorldObject refObj;
    private final boolean ascending;


    public ObjectDistanceOrderPred(WorldObject pRefObj) {
        this(pRefObj, true);
    }

    public ObjectDistanceOrderPred(WorldObject pRefObj, boolean ascending) {
        refObj = pRefObj;
        ascending = ascending;
    }

    public final int compare(WorldObject pLeft, WorldObject pRight) {
        return (_ascending ? refObj.getDistanceOrder(pLeft, pRight) : !refObj.getDistanceOrder(pLeft, pRight)) ? 1 : 0;
    }
}
