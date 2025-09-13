package com.github.azeroth.game.map;

import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.unit.Unit;

public class GetAllAlliesOfTargetCreaturesWithinRange implements ICheck<Creature> {
    private final Unit pObject;
    private final float fRange;


    public GetAllAlliesOfTargetCreaturesWithinRange(Unit obj) {
        this(obj, 0f);
    }

    public GetAllAlliesOfTargetCreaturesWithinRange(Unit obj, float maxRange) {
        pObject = obj;
        fRange = maxRange;
    }

    public final boolean invoke(Creature creature) {
        if (creature.isHostileTo(pObject)) {
            return false;
        }

        if (fRange != 0f) {
            if (fRange > 0.0f && !pObject.isWithinDist(creature, fRange, false)) {
                return false;
            }

            if (fRange < 0.0f && pObject.isWithinDist(creature, fRange, false)) {
                return false;
            }
        }

        return true;
    }
}
