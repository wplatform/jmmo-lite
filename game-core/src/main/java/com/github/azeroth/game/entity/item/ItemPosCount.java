package com.github.azeroth.game.entity.item;

import java.util.ArrayList;


public class ItemPosCount {
    public short pos;
    public int count;

    public ItemPosCount(short pos, int count) {
        pos = pos;
        count = count;
    }

    public final boolean isContainedIn(ArrayList<ItemPosCount> vec) {
        for (var posCount : vec) {
            if (posCount.pos == pos) {
                return true;
            }
        }

        return false;
    }
}
