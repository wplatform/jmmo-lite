package com.github.azeroth.game.map;


import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.unit.Unit;

public class AnyFriendlyUnitInObjectRangeCheck implements ICheck<unit> {
    private final WorldObject obj;
    private final Unit funit;
    private final float range;
    private final boolean playerOnly;
    private final boolean incOwnRadius;
    private final boolean incTargetRadius;


    public AnyFriendlyUnitInObjectRangeCheck(WorldObject obj, Unit funit, float range, boolean playerOnly, boolean incOwnRadius) {
        this(obj, funit, range, playerOnly, incOwnRadius, true);
    }

    public AnyFriendlyUnitInObjectRangeCheck(WorldObject obj, Unit funit, float range, boolean playerOnly) {
        this(obj, funit, range, playerOnly, true, true);
    }

    public AnyFriendlyUnitInObjectRangeCheck(WorldObject obj, Unit funit, float range) {
        this(obj, funit, range, false, true, true);
    }

    public AnyFriendlyUnitInObjectRangeCheck(WorldObject obj, Unit funit, float range, boolean playerOnly, boolean incOwnRadius, boolean incTargetRadius) {
        obj = obj;
        funit = funit;
        range = range;
        playerOnly = playerOnly;
        incOwnRadius = incOwnRadius;
        incTargetRadius = incTargetRadius;
    }

    public final boolean invoke(Unit u) {
        if (!u.isAlive()) {
            return false;
        }

        var searchRadius = range;

        if (incOwnRadius) {
            searchRadius += obj.getCombatReach();
        }

        if (incTargetRadius) {
            searchRadius += u.getCombatReach();
        }

        if (!u.isInMap(obj) || !u.inSamePhase(obj) || !u.getLocation().isWithinDoubleVerticalCylinder(obj.getLocation(), searchRadius, searchRadius)) {
            return false;
        }

        if (!funit.isFriendlyTo(u)) {
            return false;
        }

        return !playerOnly || u.getObjectTypeId() == TypeId.PLAYER;
    }
}
