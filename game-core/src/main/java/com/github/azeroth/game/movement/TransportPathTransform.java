package com.github.azeroth.game.movement;


import com.github.azeroth.game.entity.unit.Unit;

// Transforms coordinates from global to transport offsets
public class TransportPathTransform {
    private final Unit owner;
    private final boolean transformForTransport;

    public TransportPathTransform(Unit owner, boolean transformForTransport) {
        owner = owner;
        transformForTransport = transformForTransport;
    }

    public final Vector3 calc(Vector3 input) {
        var pos = new Position(input);

        if (transformForTransport) {
            var transport = owner.getDirectTransport();

            if (transport != null) {
                transport.calculatePassengerOffset(pos);
            }
        }

        return pos;
    }
}
