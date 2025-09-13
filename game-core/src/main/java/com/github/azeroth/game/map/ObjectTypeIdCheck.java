package com.github.azeroth.game.map;


import com.github.azeroth.game.entity.object.WorldObject;

public class ObjectTypeIdCheck implements ICheck<WorldObject> {
    private final TypeId typeId;
    private final boolean equals;

    public ObjectTypeIdCheck(TypeId typeId, boolean equals) {
        typeId = typeId;
        equals = equals;
    }

    public final boolean invoke(WorldObject obj) {
        return (obj.getObjectTypeId() == typeId) == equals;
    }
}
