package com.github.azeroth.game.spell;


import com.github.azeroth.game.domain.object.Position;
import com.github.azeroth.game.entity.object.WorldObject;

import java.util.ArrayList;

public class WorldObjectSpellAreaTargetCheck extends WorldObjectSpellTargetCheck {
    private final float range;
    private final Position position;

    public WorldObjectSpellAreaTargetCheck(float range, Position position, WorldObject caster, WorldObject referer, SpellInfo spellInfo, SpellTargetCheckTypes selectionType, ArrayList<Condition> condList, SpellTargetObjectTypes objectType) {
        super(caster, referer, spellInfo, selectionType, condList, objectType);
        range = range;
        position = position;
    }

    @Override
    public boolean invoke(WorldObject target) {
        if (target.toGameObject()) {
            // isInRange including the dimension of the GO
            var isInRange = target.toGameObject().isInRange(position.getX(), position.getY(), position.getZ(), range);

            if (!isInRange) {
                return false;
            }
        } else {
            var isInsideCylinder = target.isWithinDist2D(position, range) && Math.abs(target.getLocation().getZ() - position.getZ()) <= range;

            if (!isInsideCylinder) {
                return false;
            }
        }

        return super.invoke(target);
    }
}
