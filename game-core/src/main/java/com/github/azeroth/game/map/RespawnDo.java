package com.github.azeroth.game.map;


import com.github.azeroth.game.entity.object.WorldObject;

public class RespawnDo implements IDoWork<WorldObject> {
    public final void invoke(WorldObject obj) {
        switch (obj.getObjectTypeId()) {
            case Unit:
                obj.toCreature().respawn();

                break;
            case GameObject:
                obj.toGameObject().respawn();

                break;
        }
    }
}
