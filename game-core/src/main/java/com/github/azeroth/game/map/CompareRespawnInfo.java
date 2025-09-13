package com.github.azeroth.game.map;

import com.github.azeroth.game.domain.spawn.RespawnInfo;

import java.util.Comparator;


final class CompareRespawnInfo implements Comparator<RespawnInfo> {
    public int compare(RespawnInfo a, RespawnInfo b) {
        if (a == b) {
            return 0;
        }

        if (a.getRespawnTime() != b.getRespawnTime()) {
            return (new Long(a.getRespawnTime())).compareTo(b.getRespawnTime());
        }

        if (a.getSpawnId() != b.getSpawnId()) {
            return (new Long(a.getSpawnId())).compareTo(b.getSpawnId());
        }

        return a.getObjectType().CompareTo(b.getObjectType());
    }
}
