package com.github.azeroth.game.map;

import com.github.azeroth.game.entity.object.WorldObject;

public class ObjectGUIDCheck implements ICheck<WorldObject> {
    private final ObjectGuid gUID;

    public ObjectGUIDCheck(ObjectGuid GUID) {
        gUID = GUID;
    }

    public final boolean invoke(WorldObject obj) {
        return Objects.equals(obj.getGUID(), gUID);
    }


//	public static implicit operator Predicate<WorldObject>(ObjectGUIDCheck check)
//		{
//			return check.Invoke;
//		}
}
