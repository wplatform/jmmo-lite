package com.github.azeroth.game.phasing;

import com.github.azeroth.game.entity.object.WorldObject;

import java.util.ArrayList;
import java.util.Objects;

class PersonalPhaseSpawns {
    public static Duration DELETE_TIME_DEFAULT = durationofMinutes(1);

    public ArrayList<WorldObject> objects = new ArrayList<>();
    public ArrayList<SHORT> grids = new ArrayList<>();
    public Duration durationRemaining = null;

    public final boolean isEmpty() {
        return Objects.isEmpty() && grids.isEmpty();
    }
}
