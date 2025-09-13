package com.github.azeroth.game.map;

import com.github.azeroth.game.entity.object.WorldObject;

public class UnitAuraCheck<T extends WorldObject> implements ICheck<T> {
    private final boolean present;
    private final int spellId;
    private final ObjectGuid casterGUID;


    public UnitAuraCheck(boolean present, int spellId) {
        this(present, spellId, null);
    }

    public UnitAuraCheck(boolean present, int spellId, ObjectGuid casterGUID) {
        present = present;
        spellId = spellId;
        casterGUID = casterGUID;
    }

    public final boolean invoke(T obj) {
        return obj.toUnit() && obj.toUnit().hasAura(spellId, casterGUID) == present;
    }


//	public static implicit operator Predicate<T>(UnitAuraCheck<T> unit)
//		{
//			return unit.Invoke;
//		}
}
