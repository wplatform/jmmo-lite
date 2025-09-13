package com.github.azeroth.game.map.grid;


import com.github.azeroth.common.Assert;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.domain.object.enums.NotifyFlag;
import com.github.azeroth.game.domain.object.enums.TypeId;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.map.grid.visitor.GridObjectVisitor;
import com.github.azeroth.game.map.grid.visitor.GridVisitor;
import com.github.azeroth.game.map.grid.visitor.GridVisitorResult;
import com.github.azeroth.game.map.grid.visitor.WorldObjectVisitor;

import java.util.concurrent.ConcurrentHashMap;

public class NCell {

    //AllGridObjectTypes: GameObject, Creature(except pets), DynamicObject, Corpse(Bones), AreaTrigger, SceneObject, Conversation
    private final ConcurrentHashMap<ObjectGuid, WorldObject> allGridObjects = new ConcurrentHashMap<>();

    //AllWorldObjectTypes: Player, Creature(pets), Corpse(resurrectable), DynamicObject(farsight target)
    private final ConcurrentHashMap<ObjectGuid, WorldObject> allWorldObjects = new ConcurrentHashMap<>();


    public void addGridObject(WorldObject object) {
        switch (object.getObjectTypeId()) {
            case GAME_OBJECT, CORPSE, UNIT, DYNAMIC_OBJECT, AREA_TRIGGER, SCENE_OBJECT, CONVERSATION ->
                    allGridObjects.put(object.getGUID(), object);
            default -> throw new IllegalArgumentException("Unsupported type for addGridObject: " + object.getObjectTypeId());
        }
    }


    public void addWorldObject(WorldObject object) {
        switch (object.getObjectTypeId()) {
            case PLAYER,CORPSE,UNIT,DYNAMIC_OBJECT -> allWorldObjects.put(object.getGUID(), object);
            default -> throw new IllegalArgumentException("Unsupported type for addWorldObject: " + object.getObjectTypeId());
        }
    }

    public void removeWorldObject(WorldObject object) {
        allWorldObjects.remove(object.getGUID());
    }


    public void removeGridObject(WorldObject object) {
        allGridObjects.remove(object.getGUID());
    }


    public void visit(GridVisitor visitor) {

        if(visitor instanceof WorldObjectVisitor worldObjectVisitor) {
            for (var entry : allWorldObjects.entrySet()) {
                WorldObject source = entry.getValue();
                GridVisitorResult result = switch (source.getObjectTypeId()) {
                    case TypeId.PLAYER -> worldObjectVisitor.visit(source.toGameObject());
                    case TypeId.UNIT -> worldObjectVisitor.visit(source.toCreature());
                    case TypeId.CORPSE -> worldObjectVisitor.visit(source.toCorpse());
                    case TypeId.DYNAMIC_OBJECT -> worldObjectVisitor.visit(source.toDynObject());
                    default -> throw new IllegalStateException("Unsupported type for visit: " + source.getObjectTypeId());
                };
                if(result == GridVisitorResult.TERMINATE) {
                    return;
                }
            }
        } else if (visitor instanceof GridObjectVisitor gridObjectVisitor) {
            for (var entry : allWorldObjects.entrySet()) {
                WorldObject source = entry.getValue();
                GridVisitorResult result = switch (source.getObjectTypeId()) {
                    case TypeId.GAME_OBJECT -> gridObjectVisitor.visit(source.toGameObject());
                    case TypeId.UNIT -> gridObjectVisitor.visit(source.toCreature());
                    case TypeId.DYNAMIC_OBJECT -> gridObjectVisitor.visit(source.toDynObject());
                    case TypeId.AREA_TRIGGER -> gridObjectVisitor.visit(source.toAreaTrigger());
                    case TypeId.SCENE_OBJECT -> gridObjectVisitor.visit(source.toSceneObject());
                    case TypeId.CONVERSATION -> gridObjectVisitor.visit(source.toConversation());
                    case TypeId.CORPSE -> gridObjectVisitor.visit(source.toCorpse());
                    default -> throw new IllegalStateException("Unsupported type for visit: " + source.getObjectTypeId());
                };
                if(result == GridVisitorResult.TERMINATE) {
                    return;
                }
            }
        } else {
            for (var entry : allWorldObjects.entrySet()) {
                if(visitor.visit(entry.getValue()) == GridVisitorResult.TERMINATE) {
                    return;
                }
            }
            for (var entry : allGridObjects.entrySet()) {
                if(visitor.visit(entry.getValue()) == GridVisitorResult.TERMINATE) {
                    return;
                }
            }
        }
    }

    public int getWorldObjectCountInCell() {
        return allGridObjects.size();
    }

}
