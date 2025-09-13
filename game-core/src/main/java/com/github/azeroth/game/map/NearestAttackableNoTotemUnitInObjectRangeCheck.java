package com.github.azeroth.game.map;


import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.unit.Unit;

public class NearestAttackableNoTotemUnitInObjectRangeCheck implements ICheck<unit> {
    private final WorldObject obj;
    private float range;

    public NearestAttackableNoTotemUnitInObjectRangeCheck(WorldObject obj, float range) {
        obj = obj;
        range = range;
    }

    public final boolean invoke(Unit u) {
        if (!u.isAlive()) {
            return false;
        }

        if (u.getCreatureType() == creatureType.NonCombatPet) {
            return false;
        }

        if (u.isTypeId(TypeId.UNIT) && u.isTotem()) {
            return false;
        }

        if (!u.isTargetableForAttack(false)) {
            return false;
        }

        if (!obj.isWithinDist(u, range) || obj.isValidAttackTarget(u)) {
            return false;
        }

        range = obj.getDistance(u);

        return true;
    }
}
