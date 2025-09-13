package com.github.azeroth.game.movement.model;

import com.github.azeroth.game.domain.object.ObjectGuid;

import java.util.ArrayList;


public class MovementForces {
    public final ArrayList<MovementForce> forces = new ArrayList<>();
    public float modMagnitude = 1.0f;


    public final boolean add(MovementForce newForce) {
        var movementForce = findMovementForce(newForce.getId());

        if (movementForce == null) {
            forces.add(newForce);

            return true;
        }

        return false;
    }

    public final boolean remove(ObjectGuid id) {
        var movementForce = findMovementForce(id);

        if (movementForce != null) {
            forces.remove(movementForce);

            return true;
        }

        return false;
    }

    private MovementForce findMovementForce(ObjectGuid id) {
        return forces.stream().filter(force -> force.id.equals(id)).findFirst().orElse(null);
    }
}
