package com.github.azeroth.game.map;

import com.github.azeroth.game.entity.object.WorldObject;

public class HeightDifferenceCheck implements ICheck<WorldObject> {
    private final WorldObject baseObject;
    private final float difference;
    private final boolean reverse;

    public HeightDifferenceCheck(WorldObject go, float diff, boolean reverse) {
        baseObject = go;
        difference = diff;
        reverse = reverse;
    }

    public final boolean invoke(WorldObject unit) {
        return (unit.getLocation().getZ() - baseObject.getLocation().getZ() > difference) != reverse;
    }
}
