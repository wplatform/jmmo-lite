package com.github.azeroth.game.entity.vehicle;

import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.domain.object.Position;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.map.Map;


public interface ITransport {
    static void updatePassengerPosition(ITransport transport, Map map, WorldObject passenger, Position pos, boolean setHomePosition) {
        // transport teleported but passenger not yet (can happen for players)
        if (passenger.getMap() != map) {
            return;
        }

        // Do not use Unit::UpdatePosition here, we don't want to remove auras
        // as if regular movement occurred
        switch (passenger.getObjectTypeId()) {
            case UNIT: {
                var creature = passenger.toCreature();
                map.creatureRelocation(creature, pos, false);

                if (setHomePosition) {
                    pos = creature.getTransportHomePosition();
                    transport.calculatePassengerPosition(pos);
                    creature.setHomePosition(pos);
                }

                break;
            }
            case PLAYER:
                //relocate only passengers in world and skip any player that might be still logging in/teleporting
                if (passenger.isInWorld() && !passenger.toPlayer().isBeingTeleported()) {
                    map.playerRelocation(passenger.toPlayer(), pos);
                    passenger.toPlayer().setFallInformation(0, passenger.getLocation().getZ());
                }

                break;
            case GAME_OBJECT:
                map.gameObjectRelocation(passenger.toGameObject(), pos, false);
                passenger.toGameObject().relocateStationaryPosition(pos);

                break;
            case DYNAMIC_OBJECT:
                map.dynamicObjectRelocation(passenger.toDynObject(), pos);

                break;
            case AREA_TRIGGER:
                map.areaTriggerRelocation(passenger.toAreaTrigger(), pos);

                break;
            default:
                break;
        }

        var unit = passenger.toUnit();

        if (unit != null) {
            var vehicle = unit.getVehicleKit();

            if (vehicle != null) {
                vehicle.relocatePassengers();
            }
        }
    }

    static void calculatePassengerPosition(Position pos, float transX, float transY, float transZ, float transO) {
        float inx = pos.getX(), iny = pos.getY(), inz = pos.getZ();
        pos.setO(Position.normalizeOrientation(transO + pos.getO()));

        pos.setX(transX + inx * (float) Math.cos(transO) - iny * (float) Math.sin(transO));
        pos.setY(transY + iny * (float) Math.cos(transO) + inx * (float) Math.sin(transO));
        pos.setZ(transZ + inz);
    }

    static void calculatePassengerOffset(Position pos, float transX, float transY, float transZ, float transO) {
        pos.setO(Position.normalizeOrientation(pos.getO() - transO));

        pos.setZ(pos.getZ() - transZ);
        pos.setY(pos.getY() - transY); // y = searchedY * std::cos(o) + searchedX * std::sin(o)
        pos.setX(pos.getX() - transX); // x = searchedX * std::cos(o) + searchedY * std::sin(o + pi)
        float inx = pos.getX(), iny = pos.getY();
        pos.setY((iny - inx * (float) Math.tan(transO)) / ((float) Math.cos(transO) + (float) Math.sin(transO) * (float) Math.tan(transO)));
        pos.setX((inx + iny * (float) Math.tan(transO)) / ((float) Math.cos(transO) + (float) Math.sin(transO) * (float) Math.tan(transO)));
    }

    ObjectGuid getTransportGUID();

    // This method transforms supplied transport offsets into global coordinates
    void calculatePassengerPosition(Position pos);

    // This method transforms supplied global coordinates into local offsets
    void calculatePassengerOffset(Position pos);

    float getTransportOrientation();

    void addPassenger(WorldObject passenger);

    ITransport removePassenger(WorldObject passenger);

    int GetMapIdForSpawning();
}
