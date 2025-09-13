package com.github.azeroth.game.achievement;

import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.player.Player;

import java.util.ArrayList;


public class CriteriaDataSet {
    private final ArrayList<CriteriaData> storage = new ArrayList<>();
    private int criteriaId;

    public final void add(CriteriaData data) {
        storage.add(data);
    }


    public final boolean meets(Player source, WorldObject target, int miscValue) {
        return meets(source, target, miscValue, 0);
    }

    public final boolean meets(Player source, WorldObject target) {
        return meets(source, target, 0, 0);
    }

    public final boolean meets(Player source, WorldObject target, int miscValue, int miscValue2) {
        for (var data : storage) {
            if (!data.meets(criteriaId, source, target, miscValue, miscValue2)) {
                return false;
            }
        }

        return true;
    }

    public final void setCriteriaId(int id) {
        criteriaId = id;
    }
}
