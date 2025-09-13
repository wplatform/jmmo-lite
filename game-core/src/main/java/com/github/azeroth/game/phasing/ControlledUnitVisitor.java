package com.github.azeroth.game.phasing;

import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.unit.Unit;

import java.util.HashSet;

class ControlledUnitVisitor {
    private final HashSet<WorldObject> visited = new HashSet<WorldObject>();

    public ControlledUnitVisitor(WorldObject owner) {
        visited.add(owner);
    }

    public final void visitControlledOf(Unit unit, tangible.Action1Param<unit> func) {
        for (var controlled : unit.getControlled()) {
            // Player inside nested vehicle should not phase the root vehicle and its accessories (only direct root vehicle control does)
            if (!controlled.isPlayer() && controlled.getVehicle1() == null) {
                if (visited.add(controlled)) {
                    func.invoke(controlled);
                }
            }
        }

        for (var summonGuid : unit.getSummonSlot()) {
            if (!summonGuid.isEmpty()) {
                var summon = ObjectAccessor.getCreature(unit, summonGuid);

                if (summon != null) {
                    if (visited.add(summon)) {
                        func.invoke(summon);
                    }
                }
            }
        }

        var vehicle = unit.getVehicleKit();

        if (vehicle != null) {
            for (var seatPair : vehicle.Seats.entrySet()) {
                var passenger = global.getObjAccessor().GetUnit(unit, seatPair.getValue().passenger.guid);

                if (passenger != null && passenger != unit) {
                    if (visited.add(passenger)) {
                        func.invoke(passenger);
                    }
                }
            }
        }
    }
}
