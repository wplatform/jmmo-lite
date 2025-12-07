package com.github.azeroth.game.movement;


import com.badlogic.gdx.math.Vector3;
import com.github.azeroth.game.domain.object.Position;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.entity.vehicle.TransportObject;

// Transforms coordinates from global to transport offsets
public class TransportPathTransform {

    private final TransportObject transport;


    public TransportPathTransform(Unit owner, boolean transformForTransport) {
        this.transport = transformForTransport ? owner.getDirectTransport() : null;
    }

    public final Vector3 calc(Vector3 input) {

        if (transport != null) {
            var pos = new Position(input.x, input.y, input.z);
            transport.calculatePassengerOffset(pos);
            return new Vector3(pos.getX(), pos.getY(), pos.getZ());
        }
        return input;
    }

    public final float calc(float input) {
        if (transport != null) {
            input -= transport.getTransportOrientation();
        }
        return input;
    }
}
