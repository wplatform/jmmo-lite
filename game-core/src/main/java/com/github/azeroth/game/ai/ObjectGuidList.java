package com.github.azeroth.game.ai;



import java.util.*;






public class ObjectGuidList {
    private final ArrayList<ObjectGuid> guidList = new ArrayList<ObjectGuid>();
    private final ArrayList<WorldObject> objectList = new ArrayList<WorldObject>();

    public ObjectGuidList(ArrayList<WorldObject> objectList) {
        this.objectList = objectList;

        for (var obj : this.objectList) {
            guidList.add(obj.getGUID().clone());
        }
    }

    public final ArrayList<WorldObject> getObjectList(WorldObject obj) {
        updateObjects(obj);

        return objectList;
    }

    public final void addGuid(ObjectGuid guid) {
        guidList.add(guid.clone());
    }

    //sanitize vector using _guidVector
    private void updateObjects(WorldObject obj) {
        objectList.clear();

        for (var guid : guidList) {
            var newObj = Global.getObjAccessor().getWorldObject(obj, guid.clone());

            if (newObj != null) {
                objectList.add(newObj);
            }
        }
    }
}