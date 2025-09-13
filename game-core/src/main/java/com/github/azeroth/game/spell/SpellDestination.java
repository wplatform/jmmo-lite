package com.github.azeroth.game.spell;

import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.domain.object.Position;
import com.github.azeroth.game.domain.object.WorldLocation;
import com.github.azeroth.game.entity.object.WorldObject;

public class SpellDestination {
    public WorldLocation position;
    public ObjectGuid transportGuid = ObjectGuid.EMPTY;
    public Position transportOffset;

    public SpellDestination() {
        position = new WorldLocation();
        transportGuid = ObjectGuid.EMPTY;
        transportOffset = new Position();
    }


    public SpellDestination(float x, float y, float z, float orientation) {
        this(x, y, z, orientation, (int) 0xFFFFFFFF);
    }

    public SpellDestination(float x, float y, float z) {
        this(x, y, z, 0.0f, (int) 0xFFFFFFFF);
    }

    public SpellDestination(float x, float y, float z, float orientation, int mapId) {
        this();
        position.relocate(x, y, z, orientation);
        transportGuid = ObjectGuid.EMPTY;
        position.setMapId(mapId);
    }

    public SpellDestination(Position pos) {
        this();
        position.relocate(pos);
        transportGuid = ObjectGuid.EMPTY;
    }

    public SpellDestination(WorldLocation loc) {
        this();
        position.worldRelocate(loc);
        transportGuid.clear();
        transportOffset.relocate(0, 0, 0, 0);
    }

    public SpellDestination(WorldObject wObj) {
        this();
        transportGuid = wObj.getTransGUID();
        transportOffset.relocate(wObj.getTransOffsetX(), wObj.getTransOffsetY(), wObj.getTransOffsetZ(), wObj.getTransOffsetO());
        position.relocate(wObj.getLocation());
    }

    public final void relocate(Position pos) {
        if (!transportGuid.isEmpty()) {
            Position offsetPosition = position.offsetPosition(pos);
            transportOffset.relocateOffset(offsetPosition);
        }

        position.relocate(pos);
    }

    public final void relocateOffset(Position offset) {
        if (!transportGuid.isEmpty()) {
            transportOffset.relocateOffset(offset);
        }

        position.relocateOffset(offset);
    }
}
