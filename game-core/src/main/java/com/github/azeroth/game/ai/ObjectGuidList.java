package com.github.azeroth.game.ai;

import com.github.azeroth.game.entity.object.WorldObject;

import java.util.ArrayList;


class ObjectGuidList {
    private final ArrayList<ObjectGuid> guidList = new ArrayList<>();
    private final ArrayList<WorldObject> objectList = new ArrayList<>();

    public ObjectGuidList(ArrayList<WorldObject> objectList) {
        objectList = objectList;

        for (var obj : objectList) {
            guidList.add(obj.getGUID());
        }
    }

    public final ArrayList<WorldObject> getObjectList(WorldObject obj) {
        updateObjects(obj);

        return objectList;
    }

    public final void addGuid(ObjectGuid guid) {
        guidList.add(guid);
    }

    //sanitize vector using _guidVector
    private void updateObjects(WorldObject obj) {
        objectList.clear();

        for (var guid : guidList) {
            var newObj = global.getObjAccessor().GetWorldObject(obj, guid);

            if (newObj != null) {
                objectList.add(newObj);
            }
        }
    }
}
